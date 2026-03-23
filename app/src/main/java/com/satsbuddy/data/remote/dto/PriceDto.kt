package com.satsbuddy.data.remote.dto

import com.satsbuddy.domain.model.Price
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirror the Price domain model since the API response shape matches directly.
// Price is already @Serializable so we can use it as the DTO, but this alias
// keeps the data layer decoupled and provides a mapping site.
@Serializable
data class PriceDto(
    val time: Int = 0,
    @SerialName("USD") val usd: Double = 0.0,
    @SerialName("EUR") val eur: Double = 0.0,
    @SerialName("GBP") val gbp: Double = 0.0,
    @SerialName("CAD") val cad: Double = 0.0,
    @SerialName("CHF") val chf: Double = 0.0,
    @SerialName("AUD") val aud: Double = 0.0,
    @SerialName("JPY") val jpy: Double = 0.0
) {
    fun toDomain(): Price = Price(
        time = time,
        usd = usd,
        eur = eur,
        gbp = gbp,
        cad = cad,
        chf = chf,
        aud = aud,
        jpy = jpy
    )
}
