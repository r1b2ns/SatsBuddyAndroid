package com.satsbuddy.presentation.slotlist

import com.satsbuddy.domain.model.SlotInfo

data class SlotListUiState(
    val displayName: String = "",
    val slots: List<SlotInfo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
