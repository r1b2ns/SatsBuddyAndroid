package com.satsbuddy.data.repository

import com.satsbuddy.data.remote.MempoolApi
import com.satsbuddy.data.remote.dto.FeeEstimatesDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FeeRepositoryImplTest {

    private val api = mockk<MempoolApi>()
    private val repository = FeeRepositoryImpl(api)

    @Test
    fun `fetchRecommendedFees returns domain model from api`() = runTest {
        val dto = FeeEstimatesDto(
            fastestFee = 50,
            halfHourFee = 30,
            hourFee = 15,
            economyFee = 8,
            minimumFee = 4
        )
        coEvery { api.getRecommendedFees() } returns dto

        val result = repository.fetchRecommendedFees()

        assertEquals(50, result.fastestFee)
        assertEquals(30, result.halfHourFee)
        assertEquals(15, result.hourFee)
        assertEquals(8, result.economyFee)
        assertEquals(4, result.minimumFee)
        coVerify { api.getRecommendedFees() }
    }

    @Test
    fun `fetchRecommendedFees propagates exception`() = runTest {
        coEvery { api.getRecommendedFees() } throws RuntimeException("Timeout")

        try {
            repository.fetchRecommendedFees()
            assert(false) { "Expected exception" }
        } catch (e: RuntimeException) {
            assertEquals("Timeout", e.message)
        }
    }
}
