package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.SatsCardInfo
import javax.inject.Inject

class UpsertCardUseCase @Inject constructor() {
    /**
     * Inserts or updates [newCard] in [currentCards].
     * Returns the updated list and the index of the inserted/updated card.
     */
    operator fun invoke(
        currentCards: List<SatsCardInfo>,
        newCard: SatsCardInfo
    ): Pair<List<SatsCardInfo>, Int> {
        val existingIndex = currentCards.indexOfFirst { it.cardIdentifier == newCard.cardIdentifier }
        return if (existingIndex >= 0) {
            val updated = currentCards.toMutableList().also { it[existingIndex] = newCard }
            updated to existingIndex
        } else {
            val updated = listOf(newCard) + currentCards
            updated to 0
        }
    }
}
