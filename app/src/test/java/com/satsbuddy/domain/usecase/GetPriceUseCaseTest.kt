package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.Price
import com.satsbuddy.domain.repository.PriceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPriceUseCaseTest {

    private val repository = mockk<PriceRepository>()
    private val useCase = GetPriceUseCase(repository)

    @Test
    fun `invoke returns success with price`() = runTest {
        val price = Price(time = 1700000000, usd = 43000.0, eur = 39000.0)
        coEvery { repository.fetchPrice() } returns price

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(43000.0, result.getOrNull()!!.usd, 0.01)
    }

    @Test
    fun `invoke returns failure on exception`() = runTest {
        coEvery { repository.fetchPrice() } throws RuntimeException("Server error")

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
