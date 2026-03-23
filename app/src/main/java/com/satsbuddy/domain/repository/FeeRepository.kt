package com.satsbuddy.domain.repository

import com.satsbuddy.domain.model.RecommendedFees

interface FeeRepository {
    suspend fun fetchRecommendedFees(): RecommendedFees
}
