package com.satsbuddy.presentation.cardlist

import com.satsbuddy.domain.model.Price
import com.satsbuddy.domain.model.SatsCardInfo

data class CardListUiState(
    val cards: List<SatsCardInfo> = emptyList(),
    val isScanning: Boolean = false,
    val statusMessage: String = "",
    val price: Price? = null,
    val errorMessage: String? = null,
    val detailLoadingCardIdentifier: String? = null,
    val showSwipeToDeleteTip: Boolean = false,
    val cardPendingDeletion: SatsCardInfo? = null
)
