package com.satsbuddy.data.repository

import com.satsbuddy.data.remote.MempoolApi
import com.satsbuddy.data.remote.dto.TransactionDto
import com.satsbuddy.data.remote.dto.TxOutput
import com.satsbuddy.data.remote.dto.TxStatus
import com.satsbuddy.domain.model.SlotTransaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionRepositoryImplTest {

    private val api = mockk<MempoolApi>()
    private val repository = TransactionRepositoryImpl(api)

    @Test
    fun `getTransactions maps DTOs to domain models`() = runTest {
        val address = "bc1qtest"
        val dtos = listOf(
            TransactionDto(
                txid = "tx1",
                fee = 500,
                vout = listOf(TxOutput(scriptpubkeyAddress = address, value = 30_000)),
                status = TxStatus(confirmed = true, blockTime = 1700000000)
            ),
            TransactionDto(
                txid = "tx2",
                fee = 300,
                vout = listOf(TxOutput(scriptpubkeyAddress = address, value = 5_000)),
                status = TxStatus(confirmed = false)
            )
        )
        coEvery { api.getTransactions(address) } returns dtos

        val result = repository.getTransactions(address)

        assertEquals(2, result.size)

        assertEquals("tx1", result[0].txid)
        assertEquals(30_000L, result[0].amount)
        assertEquals(SlotTransaction.Direction.INCOMING, result[0].direction)
        assertTrue(result[0].confirmed)

        assertEquals("tx2", result[1].txid)
        assertEquals(5_000L, result[1].amount)
        assertEquals(SlotTransaction.Direction.INCOMING, result[1].direction)
        assertEquals(false, result[1].confirmed)

        coVerify { api.getTransactions(address) }
    }

    @Test
    fun `getTransactions returns empty list when no transactions`() = runTest {
        coEvery { api.getTransactions("bc1qempty") } returns emptyList()

        val result = repository.getTransactions("bc1qempty")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTransactions propagates exception`() = runTest {
        coEvery { api.getTransactions(any()) } throws RuntimeException("Network error")

        try {
            repository.getTransactions("bc1qfail")
            assert(false) { "Expected exception" }
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }
}
