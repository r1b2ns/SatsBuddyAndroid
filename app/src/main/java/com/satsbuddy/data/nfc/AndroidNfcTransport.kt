package com.satsbuddy.data.nfc

import android.nfc.tech.IsoDep
import org.bitcoindevkit.cktap.CkTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bridges Android's [IsoDep] APDU transceive channel to the rust-cktap
 * [CkTransport] interface consumed by the UniFFI bindings in
 * `org.bitcoindevkit.cktap`.
 *
 * The underlying [IsoDep] connection is opened lazily on first use and
 * must be explicitly closed via [close] when the NFC session ends.
 */
class AndroidNfcTransport(private val isoDep: IsoDep) : CkTransport {

    init {
        if (!isoDep.isConnected) {
            isoDep.connect()
            isoDep.timeout = 10_000 // 10 seconds
        }
    }

    /**
     * Transmit a raw APDU to the card.
     *
     * rust-cktap invokes this from a coroutine via UniFFI's async callback
     * machinery, so we dispatch to [Dispatchers.IO] to avoid blocking the
     * dispatcher that drives the Rust future.
     */
    override suspend fun transmitApdu(commandApdu: ByteArray): ByteArray =
        withContext(Dispatchers.IO) {
            isoDep.transceive(commandApdu)
        }

    fun close() {
        runCatching { isoDep.close() }
    }
}
