package com.satsbuddy.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Currency
import java.util.Locale

@Serializable
data class Price(
    val time: Int = 0,
    @SerialName("USD") val usd: Double = 0.0,
    @SerialName("EUR") val eur: Double = 0.0,
    @SerialName("GBP") val gbp: Double = 0.0,
    @SerialName("CAD") val cad: Double = 0.0,
    @SerialName("CHF") val chf: Double = 0.0,
    @SerialName("AUD") val aud: Double = 0.0,
    @SerialName("JPY") val jpy: Double = 0.0
) {
    companion object {
        val supportedCurrencyCodes = setOf("USD", "EUR", "GBP", "CAD", "CHF", "AUD", "JPY")
    }

    fun valueFor(currencyCode: String): Double? = when (currencyCode.uppercase()) {
        "USD" -> usd; "EUR" -> eur; "GBP" -> gbp; "CAD" -> cad
        "CHF" -> chf; "AUD" -> aud; "JPY" -> jpy; else -> null
    }

    fun preferredCurrencyCode(locale: Locale = Locale.getDefault()): String {
        val code = try { Currency.getInstance(locale)?.currencyCode?.uppercase() } catch (_: Exception) { null }
        return if (code != null && supportedCurrencyCodes.contains(code)) code else "USD"
    }

    fun preferredRate(locale: Locale = Locale.getDefault()): Pair<String, Double> {
        val code = preferredCurrencyCode(locale)
        return code to (valueFor(code) ?: usd)
    }
}
