package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.repository.BalanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetBalanceUseCaseTest {

    private val repository = mockk<BalanceRepository>()
    private val useCase = GetBalanceUseCase(repository)

    @Test
    fun `invoke returns success with balance`() = runTest {
        coEvery { repository.getBalance("bc1qtest") } returns 50_000L

        val result = useCase("bc1qtest")

        assertTrue(result.isSuccess)
        assertEquals(50_000L, result.getOrNull())
    }

    @Test
    fun `invoke returns failure on exception`() = runTest {
        coEvery { repository.getBalance(any()) } throws RuntimeException("Network error")

        val result = useCase("bc1qfail")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
