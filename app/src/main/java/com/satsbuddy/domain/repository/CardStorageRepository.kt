package com.satsbuddy.domain.repository

import com.satsbuddy.domain.model.SatsCardInfo

interface CardStorageRepository {
    suspend fun loadCards(): List<SatsCardInfo>
    suspend fun saveCards(cards: List<SatsCardInfo>)
}
