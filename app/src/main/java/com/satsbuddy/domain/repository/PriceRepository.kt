package com.satsbuddy.domain.repository

import com.satsbuddy.domain.model.Price

interface PriceRepository {
    suspend fun fetchPrice(): Price
}
