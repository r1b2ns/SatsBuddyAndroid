package com.satsbuddy.data.repository

import com.satsbuddy.data.remote.MempoolApi
import com.satsbuddy.domain.model.Price
import com.satsbuddy.domain.repository.PriceRepository
import javax.inject.Inject

class PriceRepositoryImpl @Inject constructor(
    private val mempoolApi: MempoolApi
) : PriceRepository {

    override suspend fun fetchPrice(): Price {
        return mempoolApi.getPrices().toDomain()
    }
}
