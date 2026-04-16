package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.SlotTransaction
import com.satsbuddy.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTransactionsUseCaseTest {

    private val repository = mockk<TransactionRepository>()
    private val useCase = GetTransactionsUseCase(repository)

    @Test
    fun `invoke returns success with transactions`() = runTest {
        val txs = listOf(
            SlotTransaction("tx1", 30_000, 500, 1700000000, true, SlotTransaction.Direction.INCOMING),
            SlotTransaction("tx2", 5_000, 300, null, false, SlotTransaction.Direction.OUTGOING)
        )
        coEvery { repository.getTransactions("bc1qtest") } returns txs

        val result = useCase("bc1qtest")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertEquals("tx1", result.getOrNull()!![0].txid)
    }

    @Test
    fun `invoke returns success with empty list`() = runTest {
        coEvery { repository.getTransactions("bc1qempty") } returns emptyList()

        val result = useCase("bc1qempty")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `invoke returns failure on exception`() = runTest {
        coEvery { repository.getTransactions(any()) } throws RuntimeException("Error")

        val result = useCase("bc1qfail")

        assertTrue(result.isFailure)
    }
}
