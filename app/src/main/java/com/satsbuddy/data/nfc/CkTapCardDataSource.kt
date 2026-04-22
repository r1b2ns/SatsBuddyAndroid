package com.satsbuddy.data.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import org.bitcoindevkit.cktap.CardException
import org.bitcoindevkit.cktap.CkTapCard
import org.bitcoindevkit.cktap.CkTapException
import org.bitcoindevkit.cktap.DumpException
import org.bitcoindevkit.cktap.SatsCard
import org.bitcoindevkit.cktap.SatsCardStatus
import org.bitcoindevkit.cktap.SignPsbtException
import org.bitcoindevkit.cktap.StatusException
import org.bitcoindevkit.cktap.UnsealException
import org.bitcoindevkit.cktap.toCktap
import com.satsbuddy.data.bitcoin.BdkDataSource
import com.satsbuddy.domain.model.AppError
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.model.SlotInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for SATSCARD NFC operations, backed by the rust-cktap
 * UniFFI bindings exposed through the `:cktap-android` Gradle module.
 *
 * Every public operation opens a fresh [AndroidNfcTransport] bound to the
 * given NFC [Tag], drives the protocol via [toCktap], and releases both the
 * transport and the native card handle when it is done.
 */
@Singleton
class CkTapCardDataSource @Inject constructor(
    private val bdkDataSource: BdkDataSource
) {

    suspend fun readCard(tag: Tag): SatsCardInfo = withContext(Dispatchers.IO) {
        withSatsCard(tag) { card ->
            card.toSatsCardInfo()
        }
    }

    suspend fun setupNextSlot(tag: Tag, cvc: String, expectedId: String): SatsCardInfo =
        withContext(Dispatchers.IO) {
            withSatsCard(tag) { card ->
                val currentStatus = card.status()
                if (expectedId.isNotEmpty() && currentStatus.cardIdent != expectedId) {
                    throw AppError.WrongCard
                }
                if ((currentStatus.numSlots - currentStatus.activeSlot).toInt() <= 1) {
                    throw AppError.NoUnusedSlots
                }

                // Unseal the currently active slot; the card auto-advances to
                // the next slot, which may require `newSlot` to activate.
                try {
                    card.unseal(cvc)
                } catch (e: UnsealException) {
                    throw e.toAppError()
                }

                runCatching { card.newSlot(cvc) }

                card.toSatsCardInfo()
            }
        }

    suspend fun signPsbt(tag: Tag, slot: Int, psbt: String, cvc: String): String =
        withContext(Dispatchers.IO) {
            withSatsCard(tag) { card ->
                try {
                    card.signPsbt(slot.toUByte(), psbt, cvc)
                } catch (e: SignPsbtException) {
                    throw e.toAppError()
                }
            }
        }

    // region Internal helpers

    private suspend inline fun <R> withSatsCard(tag: Tag, block: (SatsCard) -> R): R {
        val isoDep = IsoDep.get(tag)
            ?: throw AppError.TransportError("Card does not support ISO-DEP")

        val transport = AndroidNfcTransport(isoDep)
        try {
            val ckTapCard = try {
                toCktap(transport)
            } catch (e: Exception) {
                throw e.toAppError()
            }

            try {
                val satsCard = when (ckTapCard) {
                    is CkTapCard.SatsCard -> ckTapCard.v1
                    is CkTapCard.TapSigner, is CkTapCard.SatsChip ->
                        throw AppError.WrongCard
                }
                return block(satsCard)
            } finally {
                runCatching { ckTapCard.destroy() }
            }
        } finally {
            transport.close()
        }
    }

    private suspend fun SatsCard.toSatsCardInfo(): SatsCardInfo {
        val status = status()
        val fullAddress = if (status.addr != null) {
            runCatching { address() }.getOrNull()
        } else {
            null
        }
        val slots = buildSlotList(this, status, fullAddress)
        return SatsCardInfo(
            version = status.ver,
            birth = status.birth.toLong(),
            address = fullAddress,
            pubkey = status.pubkey,
            cardIdent = status.cardIdent,
            activeSlot = status.activeSlot.toInt(),
            totalSlots = status.numSlots.toInt(),
            isActive = status.addr != null,
            slots = slots,
        )
    }

    private suspend fun buildSlotList(
        card: SatsCard,
        status: SatsCardStatus,
        activeFullAddress: String?,
    ): List<SlotInfo> {
        val total = status.numSlots.toInt()
        val active = status.activeSlot.toInt()
        return (0 until total).map { index ->
            val isActive = index == active
            val isUsed = index < active
            val pubkey: String?
            val descriptor: String?
            val address: String?
            when {
                isActive -> {
                    pubkey = status.pubkey
                    descriptor = null
                    address = activeFullAddress
                }
                isUsed -> {
                    val dump = runCatching { card.dump(index.toUByte(), null) }.getOrNull()
                    pubkey = dump?.pubkey
                    descriptor = dump?.pubkeyDescriptor
                    address = descriptor?.takeIf { it.isNotEmpty() }?.let { desc ->
                        runCatching { bdkDataSource.deriveAddress(desc) }.getOrNull()
                    }
                }
                else -> {
                    pubkey = null
                    descriptor = null
                    address = null
                }
            }
            SlotInfo(
                slotNumber = index,
                isActive = isActive,
                isUsed = isUsed,
                pubkey = pubkey,
                pubkeyDescriptor = descriptor,
                address = address,
            )
        }
    }

    // endregion
}

private fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is StatusException.CkTap -> err.toAppError()
    is UnsealException.CkTap -> err.toAppError()
    is SignPsbtException.CkTap -> err.toAppError()
    is CkTapException.Card -> err.toAppError()
    is CkTapException.Transport -> AppError.TransportError(msg)
    is CardException.BadAuth, is CardException.NeedsAuth -> AppError.IncorrectCvc()
    is CardException.RateLimited -> AppError.RateLimited()
    is CardException -> AppError.Generic(this::class.java.simpleName)
    is DumpException -> AppError.Generic(message ?: this::class.java.simpleName)
    else -> AppError.Generic(message ?: "Unknown cktap error")
}
