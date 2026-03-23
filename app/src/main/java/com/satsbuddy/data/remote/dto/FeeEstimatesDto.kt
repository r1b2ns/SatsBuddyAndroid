package com.satsbuddy.data.remote.dto

import com.satsbuddy.domain.model.RecommendedFees
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeeEstimatesDto(
    @SerialName("fastestFee") val fastestFee: Int = 10,
    @SerialName("halfHourFee") val halfHourFee: Int = 5,
    @SerialName("hourFee") val hourFee: Int = 2,
    @SerialName("economyFee") val economyFee: Int = 1,
    @SerialName("minimumFee") val minimumFee: Int = 1
) {
    fun toDomain(): RecommendedFees = RecommendedFees(
        fastestFee = fastestFee,
        halfHourFee = halfHourFee,
        hourFee = hourFee,
        economyFee = economyFee,
        minimumFee = minimumFee
    )
}
