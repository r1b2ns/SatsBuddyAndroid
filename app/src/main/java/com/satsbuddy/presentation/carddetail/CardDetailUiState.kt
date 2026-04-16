package com.satsbuddy.presentation.carddetail

import com.satsbuddy.domain.model.SlotInfo

data class CardDetailUiState(
    val slots: List<SlotInfo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val displayName: String = "",
    val label: String? = null,
    val lastUpdated: Long? = null,
    val cardVersion: String = ""
)
