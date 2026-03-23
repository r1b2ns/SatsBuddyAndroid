package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.CardStorageRepository
import javax.inject.Inject

class SaveCardsUseCase @Inject constructor(
    private val cardStorageRepository: CardStorageRepository
) {
    suspend operator fun invoke(cards: List<SatsCardInfo>): Result<Unit> = runCatching {
        cardStorageRepository.saveCards(cards)
    }
}
