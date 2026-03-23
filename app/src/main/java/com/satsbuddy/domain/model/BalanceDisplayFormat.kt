package com.satsbuddy.domain.model

import java.text.NumberFormat
import java.util.Locale

enum class BalanceDisplayFormat(val key: String) {
    BITCOIN("btc"), SATS("sats"), FIAT("fiat"), BIP177("bip177");

    fun formatted(satAmount: Long, price: Price? = null, locale: Locale = Locale.getDefault()): String = when (this) {
        SATS -> NumberFormat.getNumberInstance(locale).format(satAmount)
        BITCOIN -> "%.8f".format(satAmount.toDouble() / 100_000_000)
        FIAT -> {
            val (code, rate) = price?.preferredRate(locale) ?: ("USD" to 0.0)
            val fiatAmount = (satAmount.toDouble() / 100_000_000) * rate
            val format = NumberFormat.getCurrencyInstance(locale)
            try { format.currency = java.util.Currency.getInstance(code) } catch (_: Exception) {}
            format.format(fiatAmount)
        }
        BIP177 -> {
            val format = NumberFormat.getNumberInstance(locale)
            format.maximumFractionDigits = 8; format.minimumFractionDigits = 0
            "\u20BF ${format.format(satAmount.toDouble() / 100_000_000)}"
        }
    }

    fun displaySuffix(): String = when (this) { SATS -> " sats"; BITCOIN -> " BTC"; BIP177 -> ""; FIAT -> "" }
    fun next(): BalanceDisplayFormat { val v = entries; return v[(ordinal + 1) % v.size] }
    companion object { fun fromKey(key: String) = entries.firstOrNull { it.key == key } ?: SATS }
}
