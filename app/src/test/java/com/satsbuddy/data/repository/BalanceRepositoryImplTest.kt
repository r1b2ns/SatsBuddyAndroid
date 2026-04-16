package com.satsbuddy.data.repository

import com.satsbuddy.data.remote.MempoolApi
import com.satsbuddy.data.remote.dto.AddressStatsDto
import com.satsbuddy.data.remote.dto.ChainStats
import com.satsbuddy.data.remote.dto.MempoolStats
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BalanceRepositoryImplTest {

    private val api = mockk<MempoolApi>()
    private val repository = BalanceRepositoryImpl(api)

    @Test
    fun `getBalance returns totalBalance from address stats`() = runTest {
        val address = "bc1qtest"
        val stats = AddressStatsDto(
            address = address,
            chainStats = ChainStats(fundedTxoSum = 100_000, spentTxoSum = 30_000),
            mempoolStats = MempoolStats(fundedTxoSum = 10_000, spentTxoSum = 0)
        )
        coEvery { api.getAddressStats(address) } returns stats

        val result = repository.getBalance(address)

        assertEquals(80_000L, result) // (100000-30000) + (10000-0)
        coVerify { api.getAddressStats(address) }
    }

    @Test
    fun `getBalance returns zero for empty address stats`() = runTest {
        coEvery { api.getAddressStats("bc1qempty") } returns AddressStatsDto()

        val result = repository.getBalance("bc1qempty")

        assertEquals(0L, result)
    }

    @Test
    fun `getBalance propagates api exception`() = runTest {
        coEvery { api.getAddressStats(any()) } throws RuntimeException("Network error")

        try {
            repository.getBalance("bc1qfail")
            assert(false) { "Expected exception" }
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }
}
