package com.satsbuddy.data.nfc

import android.nfc.tech.IsoDep
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class AndroidNfcTransportTest {

    // region init

    @Test
    fun `init connects and sets timeout when isoDep is not connected`() {
        val isoDep = mockk<IsoDep>(relaxed = true) {
            every { isConnected } returns false
        }

        AndroidNfcTransport(isoDep)

        verify { isoDep.connect() }
        verify { isoDep.timeout = 10_000 }
    }

    @Test
    fun `init does not reconnect when isoDep is already connected`() {
        val isoDep = mockk<IsoDep>(relaxed = true) {
            every { isConnected } returns true
        }

        AndroidNfcTransport(isoDep)

        verify(exactly = 0) { isoDep.connect() }
        verify(exactly = 0) { isoDep.timeout = any() }
    }

    // endregion

    // region transmitApdu

    @Test
    fun `transmitApdu delegates to isoDep transceive`() = runTest {
        val commandApdu = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00)
        val responseApdu = byteArrayOf(0x90.toByte(), 0x00)

        val isoDep = mockk<IsoDep>(relaxed = true) {
            every { isConnected } returns true
            every { transceive(commandApdu) } returns responseApdu
        }

        val transport = AndroidNfcTransport(isoDep)
        val result = transport.transmitApdu(commandApdu)

        assertArrayEquals(responseApdu, result)
        verify { isoDep.transceive(commandApdu) }
    }

    @Test
    fun `transmitApdu with empty command`() = runTest {
        val emptyCommand = byteArrayOf()
        val response = byteArrayOf(0x6A.toByte(), 0x82.toByte())

        val isoDep = mockk<IsoDep>(relaxed = true) {
            every { isConnected } returns true
            every { transceive(emptyCommand) } returns response
        }

        val transport = AndroidNfcTransport(isoDep)
        val result = transport.transmitApdu(emptyCommand)

        assertArrayEquals(response, result)
    }

    // endregion

    // region close

    @Test
    fun `close calls isoDep close`() {
        val isoDep = mockk<IsoDep>(relaxed = true) {
            every { isConnected } returns true
        }

        val transport = AndroidNfcTransport(isoDep)
        transport.close()

        verify { isoDep.close() }
    }

    @Test
    fun `close does not throw when isoDep close throws`() {
        val isoDep = mockk<IsoDep>(relaxed = true) {
            every { isConnected } returns true
            every { close() } throws java.io.IOException("Tag was lost")
        }

        val transport = AndroidNfcTransport(isoDep)

        // Should not throw
        transport.close()
    }

    // endregion
}
