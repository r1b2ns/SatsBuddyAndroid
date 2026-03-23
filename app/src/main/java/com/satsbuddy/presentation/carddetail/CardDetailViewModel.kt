package com.satsbuddy.presentation.carddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.model.SlotInfo
import com.satsbuddy.domain.usecase.GetBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val getBalance: GetBalanceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cardIdentifier: String = savedStateHandle["cardIdentifier"] ?: ""

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    private var fetchToken: String = ""

    fun loadSlotDetails(card: SatsCardInfo) {
        val token = UUID.randomUUID().toString()
        fetchToken = token

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val slots = card.slots.toMutableList()
            val activeSlot = slots.firstOrNull { it.isActive }
            val address = activeSlot?.address ?: card.address

            if (address != null) {
                getBalance(address)
                    .onSuccess { balance ->
                        if (fetchToken != token) return@launch
                        val activeIndex = slots.indexOfFirst { it.isActive }
                        if (activeIndex >= 0) {
                            slots[activeIndex] = slots[activeIndex].copy(balance = balance)
                        }
                    }
                    .onFailure { error ->
                        if (fetchToken != token) return@launch
                        _uiState.update { it.copy(errorMessage = error.message) }
                    }
            }

            if (fetchToken == token) {
                _uiState.update { it.copy(slots = slots, isLoading = false) }
            }
        }
    }
}
