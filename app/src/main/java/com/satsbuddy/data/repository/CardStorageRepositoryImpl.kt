package com.satsbuddy.data.repository

import com.satsbuddy.data.local.EncryptedCardStorage
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.CardStorageRepository
import javax.inject.Inject

class CardStorageRepositoryImpl @Inject constructor(
    private val storage: EncryptedCardStorage
) : CardStorageRepository {

    override suspend fun loadCards(): List<SatsCardInfo> = storage.loadCards()

    override suspend fun saveCards(cards: List<SatsCardInfo>) = storage.saveCards(cards)
}
