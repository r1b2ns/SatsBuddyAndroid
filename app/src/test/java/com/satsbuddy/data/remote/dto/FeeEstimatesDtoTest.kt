package com.satsbuddy.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class FeeEstimatesDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = FeeEstimatesDto(
            fastestFee = 50,
            halfHourFee = 30,
            hourFee = 15,
            economyFee = 8,
            minimumFee = 4
        )

        val domain = dto.toDomain()

        assertEquals(50, domain.fastestFee)
        assertEquals(30, domain.halfHourFee)
        assertEquals(15, domain.hourFee)
        assertEquals(8, domain.economyFee)
        assertEquals(4, domain.minimumFee)
    }

    @Test
    fun `default values are reasonable`() {
        val dto = FeeEstimatesDto()

        assertEquals(10, dto.fastestFee)
        assertEquals(5, dto.halfHourFee)
        assertEquals(2, dto.hourFee)
        assertEquals(1, dto.economyFee)
        assertEquals(1, dto.minimumFee)
    }

    @Test
    fun `deserialization from JSON`() {
        val raw = """
            {
                "fastestFee": 100,
                "halfHourFee": 60,
                "hourFee": 30,
                "economyFee": 10,
                "minimumFee": 5
            }
        """.trimIndent()

        val dto = json.decodeFromString<FeeEstimatesDto>(raw)

        assertEquals(100, dto.fastestFee)
        assertEquals(60, dto.halfHourFee)
        assertEquals(30, dto.hourFee)
        assertEquals(10, dto.economyFee)
        assertEquals(5, dto.minimumFee)
    }
}
