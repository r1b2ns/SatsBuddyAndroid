package com.satsbuddy.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecommendedFees(
    val fastestFee: Int, val halfHourFee: Int, val hourFee: Int, val economyFee: Int, val minimumFee: Int
)
