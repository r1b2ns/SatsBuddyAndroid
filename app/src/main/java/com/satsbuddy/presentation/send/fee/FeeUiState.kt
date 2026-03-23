package com.satsbuddy.presentation.send.fee

import com.satsbuddy.domain.model.RecommendedFees

data class FeeUiState(
    val recommendedFees: RecommendedFees? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedIndex: Int = 2,
    val isManualFallback: Boolean = false
) {
    companion object {
        val manualFallbackFees = listOf(1, 2, 5, 10)
        val feeLabels = listOf("No Priority", "Low Priority", "Medium Priority", "High Priority")
    }

    val availableFees: List<Int>
        get() = if (isManualFallback) manualFallbackFees
        else recommendedFees?.let {
            listOf(it.economyFee, it.hourFee, it.halfHourFee, it.fastestFee)
        } ?: manualFallbackFees

    val selectedFee: Int get() = availableFees.getOrElse(selectedIndex) { 2 }
}
