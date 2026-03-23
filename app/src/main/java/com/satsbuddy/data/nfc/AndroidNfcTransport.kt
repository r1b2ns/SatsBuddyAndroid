package com.satsbuddy.data.nfc

import android.nfc.tech.IsoDep

/**
 * Bridges Android's IsoDep APDU transceive to the rust-cktap CkTransport interface.
 *
 * The actual CkTransport interface will be provided by the rust-cktap JNI bindings.
 * This class wraps IsoDep.transceive() so that CkTap operations work on Android.
 */
class AndroidNfcTransport(private val isoDep: IsoDep) {

    init {
        if (!isoDep.isConnected) {
            isoDep.connect()
            isoDep.timeout = 10_000 // 10 seconds
        }
    }

    fun transmitApdu(apdu: ByteArray): ByteArray {
        return isoDep.transceive(apdu)
    }

    fun close() {
        runCatching { isoDep.close() }
    }
}
