package com.satsbuddy.data.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.satsbuddy.domain.model.AppError
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.model.SlotInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for SATSCARD NFC operations via rust-cktap JNI bindings.
 *
 * The actual JNI integration requires the rust-cktap .aar library to be added
 * as a dependency. This implementation provides the full architecture and
 * placeholder calls that will be replaced with actual JNI invocations.
 *
 * JNI method signatures will follow the UniFFI-generated bindings pattern.
 */
@Singleton
class CkTapCardDataSource @Inject constructor() {

    companion object {
        // Load the native library when available
        // static { System.loadLibrary("cktap") }
    }

    suspend fun readCard(tag: Tag): SatsCardInfo = withContext(Dispatchers.IO) {
        val isoDep = IsoDep.get(tag)
            ?: throw AppError.TransportError("Card does not support ISO-DEP")

        val transport = AndroidNfcTransport(isoDep)
        try {
            // TODO: Replace with actual rust-cktap JNI call:
            // val card = CkTapCard.fromTransport(transport)
            // return card.status().toSatsCardInfo()
            readCardStub(transport)
        } finally {
            transport.close()
        }
    }

    suspend fun setupNextSlot(tag: Tag, cvc: String, expectedId: String): SatsCardInfo =
        withContext(Dispatchers.IO) {
            val isoDep = IsoDep.get(tag)
                ?: throw AppError.TransportError("Card does not support ISO-DEP")

            val transport = AndroidNfcTransport(isoDep)
            try {
                // TODO: Replace with actual rust-cktap JNI call:
                // val card = CkTapCard.fromTransport(transport)
                // card.unseal(cvc) or card.new_slot(cvc)
                // return card.status().toSatsCardInfo()
                throw AppError.Generic("rust-cktap JNI not yet integrated")
            } finally {
                transport.close()
            }
        }

    suspend fun signPsbt(tag: Tag, slot: Int, psbt: String, cvc: String): String =
        withContext(Dispatchers.IO) {
            val isoDep = IsoDep.get(tag)
                ?: throw AppError.TransportError("Card does not support ISO-DEP")

            val transport = AndroidNfcTransport(isoDep)
            try {
                // TODO: Replace with actual rust-cktap JNI call:
                // val card = CkTapCard.fromTransport(transport)
                // return card.sign_psbt(psbt, cvc, slot)
                throw AppError.Generic("rust-cktap JNI not yet integrated")
            } finally {
                transport.close()
            }
        }

    // Stub for development/testing without real hardware
    private fun readCardStub(transport: AndroidNfcTransport): SatsCardInfo {
        // Select SATSCARD AID: A000000458415453434152440001
        val selectApdu = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
            0x0E.toByte(),
            0xA0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte(),
            0x58.toByte(), 0x41.toByte(), 0x54.toByte(), 0x53.toByte(),
            0x43.toByte(), 0x41.toByte(), 0x52.toByte(), 0x44.toByte(),
            0x00.toByte(), 0x01.toByte()
        )
        val response = transport.transmitApdu(selectApdu)
        // Parse CBOR response — requires cbor library or manual parsing
        // For now return a minimal placeholder
        return SatsCardInfo(
            version = "0.9",
            pubkey = response.toHex(),
            isActive = true
        )
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
