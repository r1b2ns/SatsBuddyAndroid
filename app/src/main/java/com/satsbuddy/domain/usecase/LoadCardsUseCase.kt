package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.CardStorageRepository
import javax.inject.Inject

class LoadCardsUseCase @Inject constructor(
    private val cardStorageRepository: CardStorageRepository
) {
    suspend operator fun invoke(): Result<List<SatsCardInfo>> = runCatching {
        cardStorageRepository.loadCards()
    }
}
