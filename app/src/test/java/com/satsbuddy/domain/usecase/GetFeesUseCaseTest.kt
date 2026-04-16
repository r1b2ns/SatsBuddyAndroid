package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.RecommendedFees
import com.satsbuddy.domain.repository.FeeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFeesUseCaseTest {

    private val repository = mockk<FeeRepository>()
    private val useCase = GetFeesUseCase(repository)

    @Test
    fun `invoke returns success with fees`() = runTest {
        val fees = RecommendedFees(50, 30, 15, 8, 4)
        coEvery { repository.fetchRecommendedFees() } returns fees

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(fees, result.getOrNull())
    }

    @Test
    fun `invoke returns failure on exception`() = runTest {
        coEvery { repository.fetchRecommendedFees() } throws RuntimeException("Timeout")

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
