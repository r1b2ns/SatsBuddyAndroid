package com.satsbuddy.presentation.send.fee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satsbuddy.domain.usecase.GetFeesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeeViewModel @Inject constructor(
    private val getFees: GetFeesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeeUiState())
    val uiState: StateFlow<FeeUiState> = _uiState.asStateFlow()

    init {
        fetchFees()
    }

    private fun fetchFees() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getFees()
                .onSuccess { fees ->
                    _uiState.update {
                        it.copy(
                            recommendedFees = fees,
                            isLoading = false,
                            isManualFallback = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isManualFallback = true,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun selectIndex(index: Int) {
        _uiState.update { it.copy(selectedIndex = index.coerceIn(0, 3)) }
    }
}
