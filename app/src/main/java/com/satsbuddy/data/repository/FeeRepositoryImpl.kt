package com.satsbuddy.data.repository

import com.satsbuddy.data.remote.MempoolApi
import com.satsbuddy.domain.model.RecommendedFees
import com.satsbuddy.domain.repository.FeeRepository
import javax.inject.Inject

class FeeRepositoryImpl @Inject constructor(
    private val mempoolApi: MempoolApi
) : FeeRepository {

    override suspend fun fetchRecommendedFees(): RecommendedFees {
        return mempoolApi.getRecommendedFees().toDomain()
    }
}
