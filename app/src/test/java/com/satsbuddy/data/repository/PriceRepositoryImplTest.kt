package com.satsbuddy.data.repository

import com.satsbuddy.data.remote.MempoolApi
import com.satsbuddy.data.remote.dto.PriceDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PriceRepositoryImplTest {

    private val api = mockk<MempoolApi>()
    private val repository = PriceRepositoryImpl(api)

    @Test
    fun `fetchPrice returns domain model from api`() = runTest {
        val dto = PriceDto(
            time = 1700000000,
            usd = 43000.0,
            eur = 39000.0,
            gbp = 34000.0,
            cad = 58000.0,
            chf = 38000.0,
            aud = 66000.0,
            jpy = 6400000.0
        )
        coEvery { api.getPrices() } returns dto

        val result = repository.fetchPrice()

        assertEquals(1700000000, result.time)
        assertEquals(43000.0, result.usd, 0.01)
        assertEquals(39000.0, result.eur, 0.01)
        assertEquals(34000.0, result.gbp, 0.01)
        assertEquals(58000.0, result.cad, 0.01)
        assertEquals(38000.0, result.chf, 0.01)
        assertEquals(66000.0, result.aud, 0.01)
        assertEquals(6400000.0, result.jpy, 0.01)
        coVerify { api.getPrices() }
    }

    @Test
    fun `fetchPrice propagates exception`() = runTest {
        coEvery { api.getPrices() } throws RuntimeException("Server error")

        try {
            repository.fetchPrice()
            assert(false) { "Expected exception" }
        } catch (e: RuntimeException) {
            assertEquals("Server error", e.message)
        }
    }
}
