package com.satsbuddy.data.repository

import android.nfc.Tag
import com.satsbuddy.data.bitcoin.BdkDataSource
import com.satsbuddy.data.nfc.CkTapCardDataSource
import com.satsbuddy.data.remote.MempoolApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PsbtRepositoryImplTest {

    private val bdkDataSource = mockk<BdkDataSource>()
    private val ckTapCardDataSource = mockk<CkTapCardDataSource>()
    private val api = mockk<MempoolApi>()
    private val repository = PsbtRepositoryImpl(bdkDataSource, ckTapCardDataSource, api)
    private val tag = mockk<Tag>(relaxed = true)

    @Test
    fun `buildSweepPsbt delegates to bdkDataSource`() = runTest {
        coEvery { bdkDataSource.buildSweepPsbt("desc", "bc1qdest", 10) } returns "psbt_hex"

        val result = repository.buildSweepPsbt("desc", "bc1qdest", 10)

        assertEquals("psbt_hex", result)
        coVerify { bdkDataSource.buildSweepPsbt("desc", "bc1qdest", 10) }
    }

    @Test
    fun `buildSweepPsbt propagates exception`() = runTest {
        coEvery { bdkDataSource.buildSweepPsbt(any(), any(), any()) } throws
                NotImplementedError("bdk-android not yet integrated")

        try {
            repository.buildSweepPsbt("desc", "bc1qdest", 10)
            assert(false) { "Expected NotImplementedError" }
        } catch (e: NotImplementedError) {
            // expected
        }
    }

    @Test
    fun `signOnCard delegates to ckTapCardDataSource`() = runTest {
        coEvery { ckTapCardDataSource.signPsbt(tag, 0, "psbt", "123456") } returns "signed_psbt"

        val result = repository.signOnCard(tag, 0, "psbt", "123456")

        assertEquals("signed_psbt", result)
        coVerify { ckTapCardDataSource.signPsbt(tag, 0, "psbt", "123456") }
    }

    @Test
    fun `broadcast delegates to mempoolApi`() = runTest {
        coEvery { api.broadcastTx("signed_hex") } returns "txid_abc"

        val result = repository.broadcast("signed_hex")

        assertEquals("txid_abc", result)
        coVerify { api.broadcastTx("signed_hex") }
    }

    @Test
    fun `broadcast propagates exception`() = runTest {
        coEvery { api.broadcastTx(any()) } throws RuntimeException("Broadcast failed")

        try {
            repository.broadcast("bad_hex")
            assert(false) { "Expected exception" }
        } catch (e: RuntimeException) {
            assertEquals("Broadcast failed", e.message)
        }
    }
}
