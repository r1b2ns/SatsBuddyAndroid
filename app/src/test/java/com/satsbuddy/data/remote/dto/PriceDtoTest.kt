package com.satsbuddy.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class PriceDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `toDomain maps all currency fields`() {
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

        val domain = dto.toDomain()

        assertEquals(1700000000, domain.time)
        assertEquals(43000.0, domain.usd, 0.01)
        assertEquals(39000.0, domain.eur, 0.01)
        assertEquals(34000.0, domain.gbp, 0.01)
        assertEquals(58000.0, domain.cad, 0.01)
        assertEquals(38000.0, domain.chf, 0.01)
        assertEquals(66000.0, domain.aud, 0.01)
        assertEquals(6400000.0, domain.jpy, 0.01)
    }

    @Test
    fun `default values are zero`() {
        val dto = PriceDto()

        assertEquals(0, dto.time)
        assertEquals(0.0, dto.usd, 0.01)
    }

    @Test
    fun `deserialization from JSON with uppercase currency keys`() {
        val raw = """
            {
                "time": 1710000000,
                "USD": 65000.99,
                "EUR": 60000.50,
                "GBP": 51000.25,
                "CAD": 88000.00,
                "CHF": 57000.75,
                "AUD": 99000.10,
                "JPY": 9750000.00
            }
        """.trimIndent()

        val dto = json.decodeFromString<PriceDto>(raw)

        assertEquals(1710000000, dto.time)
        assertEquals(65000.99, dto.usd, 0.01)
        assertEquals(60000.50, dto.eur, 0.01)
        assertEquals(51000.25, dto.gbp, 0.01)
        assertEquals(88000.00, dto.cad, 0.01)
        assertEquals(57000.75, dto.chf, 0.01)
        assertEquals(99000.10, dto.aud, 0.01)
        assertEquals(9750000.00, dto.jpy, 0.01)
    }
}
