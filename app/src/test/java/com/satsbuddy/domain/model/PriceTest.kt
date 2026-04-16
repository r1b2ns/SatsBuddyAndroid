package com.satsbuddy.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class PriceTest {

    private val price = Price(
        time = 1700000000,
        usd = 43000.0,
        eur = 39000.0,
        gbp = 34000.0,
        cad = 58000.0,
        chf = 38000.0,
        aud = 66000.0,
        jpy = 6400000.0
    )

    // region valueFor

    @Test
    fun `valueFor returns correct value for each currency`() {
        assertEquals(43000.0, price.valueFor("USD")!!, 0.01)
        assertEquals(39000.0, price.valueFor("EUR")!!, 0.01)
        assertEquals(34000.0, price.valueFor("GBP")!!, 0.01)
        assertEquals(58000.0, price.valueFor("CAD")!!, 0.01)
        assertEquals(38000.0, price.valueFor("CHF")!!, 0.01)
        assertEquals(66000.0, price.valueFor("AUD")!!, 0.01)
        assertEquals(6400000.0, price.valueFor("JPY")!!, 0.01)
    }

    @Test
    fun `valueFor is case insensitive`() {
        assertEquals(43000.0, price.valueFor("usd")!!, 0.01)
        assertEquals(39000.0, price.valueFor("eur")!!, 0.01)
        assertEquals(34000.0, price.valueFor("Gbp")!!, 0.01)
    }

    @Test
    fun `valueFor returns null for unsupported currency`() {
        assertNull(price.valueFor("BRL"))
        assertNull(price.valueFor("CNY"))
        assertNull(price.valueFor(""))
    }

    // endregion

    // region preferredCurrencyCode

    @Test
    fun `preferredCurrencyCode returns USD for US locale`() {
        assertEquals("USD", price.preferredCurrencyCode(Locale.US))
    }

    @Test
    fun `preferredCurrencyCode returns GBP for UK locale`() {
        assertEquals("GBP", price.preferredCurrencyCode(Locale.UK))
    }

    @Test
    fun `preferredCurrencyCode returns JPY for Japan locale`() {
        assertEquals("JPY", price.preferredCurrencyCode(Locale.JAPAN))
    }

    @Test
    fun `preferredCurrencyCode returns EUR for Germany locale`() {
        assertEquals("EUR", price.preferredCurrencyCode(Locale.GERMANY))
    }

    @Test
    fun `preferredCurrencyCode returns CAD for Canada French locale`() {
        assertEquals("CAD", price.preferredCurrencyCode(Locale.CANADA_FRENCH))
    }

    @Test
    fun `preferredCurrencyCode falls back to USD for unsupported locale`() {
        // A locale without a known currency
        val unknownLocale = Locale("xx", "XX")
        assertEquals("USD", price.preferredCurrencyCode(unknownLocale))
    }

    // endregion

    // region preferredRate

    @Test
    fun `preferredRate returns USD pair for US locale`() {
        val (code, rate) = price.preferredRate(Locale.US)
        assertEquals("USD", code)
        assertEquals(43000.0, rate, 0.01)
    }

    @Test
    fun `preferredRate returns GBP pair for UK locale`() {
        val (code, rate) = price.preferredRate(Locale.UK)
        assertEquals("GBP", code)
        assertEquals(34000.0, rate, 0.01)
    }

    @Test
    fun `preferredRate returns JPY pair for Japan locale`() {
        val (code, rate) = price.preferredRate(Locale.JAPAN)
        assertEquals("JPY", code)
        assertEquals(6400000.0, rate, 0.01)
    }

    @Test
    fun `preferredRate falls back to USD for unsupported locale`() {
        val (code, rate) = price.preferredRate(Locale("xx", "XX"))
        assertEquals("USD", code)
        assertEquals(43000.0, rate, 0.01)
    }

    // endregion

    // region supportedCurrencyCodes

    @Test
    fun `supportedCurrencyCodes contains all 7 currencies`() {
        val codes = Price.supportedCurrencyCodes
        assertEquals(7, codes.size)
        assert(codes.containsAll(listOf("USD", "EUR", "GBP", "CAD", "CHF", "AUD", "JPY")))
    }

    // endregion

    // region defaults

    @Test
    fun `default Price has zero values`() {
        val defaultPrice = Price()
        assertEquals(0, defaultPrice.time)
        assertEquals(0.0, defaultPrice.usd, 0.01)
        assertEquals(0.0, defaultPrice.eur, 0.01)
    }

    // endregion
}
