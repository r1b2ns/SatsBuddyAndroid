package com.satsbuddy.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class BalanceDisplayFormatTest {

    private val usLocale = Locale.US

    // region SATS format

    @Test
    fun `SATS formats with thousand separators`() {
        val result = BalanceDisplayFormat.SATS.formatted(1_234_567, locale = usLocale)
        assertEquals("1,234,567", result)
    }

    @Test
    fun `SATS formats zero`() {
        val result = BalanceDisplayFormat.SATS.formatted(0, locale = usLocale)
        assertEquals("0", result)
    }

    @Test
    fun `SATS formats single sat`() {
        val result = BalanceDisplayFormat.SATS.formatted(1, locale = usLocale)
        assertEquals("1", result)
    }

    // endregion

    // region BITCOIN format

    @Test
    fun `BITCOIN formats 1 BTC correctly`() {
        val result = BalanceDisplayFormat.BITCOIN.formatted(100_000_000)
        assertEquals("1.00000000", result)
    }

    @Test
    fun `BITCOIN formats fractional amount`() {
        val result = BalanceDisplayFormat.BITCOIN.formatted(50_000)
        assertEquals("0.00050000", result)
    }

    @Test
    fun `BITCOIN formats zero`() {
        val result = BalanceDisplayFormat.BITCOIN.formatted(0)
        assertEquals("0.00000000", result)
    }

    @Test
    fun `BITCOIN always shows 8 decimal places`() {
        val result = BalanceDisplayFormat.BITCOIN.formatted(10_000_000)
        assertEquals("0.10000000", result)
    }

    // endregion

    // region FIAT format

    @Test
    fun `FIAT formats with USD price`() {
        val price = Price(usd = 50000.0)
        val result = BalanceDisplayFormat.FIAT.formatted(100_000_000, price, usLocale)
        // 1 BTC * $50,000 = $50,000.00
        assertTrue(result.contains("50,000.00") || result.contains("50000"))
    }

    @Test
    fun `FIAT returns zero when price is null`() {
        val result = BalanceDisplayFormat.FIAT.formatted(100_000_000, null, usLocale)
        // rate=0.0 → fiat=0
        assertTrue(result.contains("0"))
    }

    @Test
    fun `FIAT formats fractional sat amount`() {
        val price = Price(usd = 100_000.0)
        val result = BalanceDisplayFormat.FIAT.formatted(50_000, price, usLocale)
        // 0.0005 BTC * $100,000 = $50.00
        assertTrue(result.contains("50.00"))
    }

    // endregion

    // region BIP177 format

    @Test
    fun `BIP177 formats with bitcoin sign prefix`() {
        val result = BalanceDisplayFormat.BIP177.formatted(100_000_000, locale = usLocale)
        assertTrue(result.startsWith("\u20BF"))
        assertTrue(result.contains("1"))
    }

    @Test
    fun `BIP177 formats zero`() {
        val result = BalanceDisplayFormat.BIP177.formatted(0, locale = usLocale)
        assertTrue(result.startsWith("\u20BF"))
        assertTrue(result.contains("0"))
    }

    @Test
    fun `BIP177 formats fractional without trailing zeros`() {
        val result = BalanceDisplayFormat.BIP177.formatted(10_000_000, locale = usLocale)
        assertTrue(result.startsWith("\u20BF"))
        assertTrue(result.contains("0.1"))
    }

    // endregion

    // region displaySuffix

    @Test
    fun `displaySuffix returns correct values`() {
        assertEquals(" sats", BalanceDisplayFormat.SATS.displaySuffix())
        assertEquals(" BTC", BalanceDisplayFormat.BITCOIN.displaySuffix())
        assertEquals("", BalanceDisplayFormat.BIP177.displaySuffix())
        assertEquals("", BalanceDisplayFormat.FIAT.displaySuffix())
    }

    // endregion

    // region next

    @Test
    fun `next cycles through all formats`() {
        assertEquals(BalanceDisplayFormat.SATS, BalanceDisplayFormat.BITCOIN.next())
        assertEquals(BalanceDisplayFormat.FIAT, BalanceDisplayFormat.SATS.next())
        assertEquals(BalanceDisplayFormat.BIP177, BalanceDisplayFormat.FIAT.next())
        assertEquals(BalanceDisplayFormat.BITCOIN, BalanceDisplayFormat.BIP177.next())
    }

    // endregion

    // region fromKey

    @Test
    fun `fromKey returns correct format`() {
        assertEquals(BalanceDisplayFormat.BITCOIN, BalanceDisplayFormat.fromKey("btc"))
        assertEquals(BalanceDisplayFormat.SATS, BalanceDisplayFormat.fromKey("sats"))
        assertEquals(BalanceDisplayFormat.FIAT, BalanceDisplayFormat.fromKey("fiat"))
        assertEquals(BalanceDisplayFormat.BIP177, BalanceDisplayFormat.fromKey("bip177"))
    }

    @Test
    fun `fromKey returns SATS for unknown key`() {
        assertEquals(BalanceDisplayFormat.SATS, BalanceDisplayFormat.fromKey("unknown"))
        assertEquals(BalanceDisplayFormat.SATS, BalanceDisplayFormat.fromKey(""))
    }

    // endregion

    // region key values

    @Test
    fun `key values are correct`() {
        assertEquals("btc", BalanceDisplayFormat.BITCOIN.key)
        assertEquals("sats", BalanceDisplayFormat.SATS.key)
        assertEquals("fiat", BalanceDisplayFormat.FIAT.key)
        assertEquals("bip177", BalanceDisplayFormat.BIP177.key)
    }

    // endregion
}
