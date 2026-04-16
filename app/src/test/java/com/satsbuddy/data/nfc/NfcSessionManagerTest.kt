package com.satsbuddy.data.nfc

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NfcSessionManagerTest {

    private lateinit var context: Context
    private lateinit var nfcAdapter: NfcAdapter

    @Before
    fun setUp() {
        mockkStatic(NfcAdapter::class)
        context = mockk(relaxed = true)
        nfcAdapter = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createManager(adapterPresent: Boolean = true): NfcSessionManager {
        every { NfcAdapter.getDefaultAdapter(context) } returns if (adapterPresent) nfcAdapter else null
        return NfcSessionManager(context)
    }

    // region isNfcAvailable

    @Test
    fun `isNfcAvailable returns true when adapter exists`() {
        val manager = createManager(adapterPresent = true)
        assertTrue(manager.isNfcAvailable)
    }

    @Test
    fun `isNfcAvailable returns false when adapter is null`() {
        val manager = createManager(adapterPresent = false)
        assertFalse(manager.isNfcAvailable)
    }

    // endregion

    // region isNfcEnabled

    @Test
    fun `isNfcEnabled returns true when adapter is enabled`() {
        every { nfcAdapter.isEnabled } returns true
        val manager = createManager()
        assertTrue(manager.isNfcEnabled)
    }

    @Test
    fun `isNfcEnabled returns false when adapter is disabled`() {
        every { nfcAdapter.isEnabled } returns false
        val manager = createManager()
        assertFalse(manager.isNfcEnabled)
    }

    @Test
    fun `isNfcEnabled returns false when adapter is null`() {
        val manager = createManager(adapterPresent = false)
        assertFalse(manager.isNfcEnabled)
    }

    // endregion

    // region handleTag / tagFlow

    @Test
    fun `handleTag emits tag to tagFlow`() = runTest {
        val manager = createManager()
        val tag = mockk<Tag>(relaxed = true)

        val collected = mutableListOf<Tag>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            manager.tagFlow.collect { collected.add(it) }
        }

        manager.handleTag(tag)

        assertEquals(1, collected.size)
        assertEquals(tag, collected[0])
        job.cancel()
    }

    @Test
    fun `handleTag emits multiple tags in order`() = runTest {
        val manager = createManager()
        val tag1 = mockk<Tag>(relaxed = true)
        val tag2 = mockk<Tag>(relaxed = true)

        val collected = mutableListOf<Tag>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            manager.tagFlow.collect { collected.add(it) }
        }

        manager.handleTag(tag1)
        manager.handleTag(tag2)

        assertEquals(2, collected.size)
        assertEquals(tag1, collected[0])
        assertEquals(tag2, collected[1])
        job.cancel()
    }

    @Test
    fun `tagFlow does not emit without handleTag`() = runTest {
        val manager = createManager()

        val result = withTimeoutOrNull(100) {
            val collected = mutableListOf<Tag>()
            manager.tagFlow.collect { collected.add(it) }
            collected.firstOrNull()
        }

        assertNull(result)
    }

    // endregion

    // region enableReaderMode / disableReaderMode

    @Test
    fun `enableReaderMode calls adapter enableReaderMode with correct flags`() {
        val manager = createManager()
        val activity = mockk<android.app.Activity>(relaxed = true)

        manager.enableReaderMode(activity)

        val expectedFlags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        verify {
            nfcAdapter.enableReaderMode(
                activity,
                any(),
                expectedFlags,
                any()
            )
        }
    }

    @Test
    fun `disableReaderMode calls adapter disableReaderMode`() {
        val manager = createManager()
        val activity = mockk<android.app.Activity>(relaxed = true)

        manager.disableReaderMode(activity)

        verify { nfcAdapter.disableReaderMode(activity) }
    }

    @Test
    fun `enableReaderMode does not crash when adapter is null`() {
        val manager = createManager(adapterPresent = false)
        val activity = mockk<android.app.Activity>(relaxed = true)

        // Should not throw
        manager.enableReaderMode(activity)
    }

    @Test
    fun `disableReaderMode does not crash when adapter is null`() {
        val manager = createManager(adapterPresent = false)
        val activity = mockk<android.app.Activity>(relaxed = true)

        // Should not throw
        manager.disableReaderMode(activity)
    }

    // endregion

    // region enableReaderMode callback emits to tagFlow

    @Test
    fun `enableReaderMode callback emits tag to tagFlow`() = runTest {
        val manager = createManager()
        val activity = mockk<android.app.Activity>(relaxed = true)
        val tag = mockk<Tag>(relaxed = true)

        // Capture the callback passed to enableReaderMode
        var capturedCallback: NfcAdapter.ReaderCallback? = null
        every {
            nfcAdapter.enableReaderMode(activity, any(), any(), any())
        } answers {
            capturedCallback = secondArg()
        }

        manager.enableReaderMode(activity)
        assertNotNull(capturedCallback)

        val collected = mutableListOf<Tag>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            manager.tagFlow.collect { collected.add(it) }
        }

        capturedCallback!!.onTagDiscovered(tag)

        assertEquals(1, collected.size)
        assertEquals(tag, collected[0])
        job.cancel()
    }

    // endregion
}
