package com.satsbuddy.presentation.slothistory

import com.satsbuddy.domain.model.SlotTransaction

data class SlotHistoryUiState(
    val transactions: List<SlotTransaction> = emptyList(),
    val slotBalance: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSweepDisabled: Boolean = true
)
