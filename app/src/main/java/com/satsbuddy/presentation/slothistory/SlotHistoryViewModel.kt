package com.satsbuddy.presentation.slothistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satsbuddy.domain.usecase.GetBalanceUseCase
import com.satsbuddy.domain.usecase.GetTransactionsUseCase
import com.satsbuddy.domain.usecase.LoadCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SlotHistoryViewModel @Inject constructor(
    private val loadCards: LoadCardsUseCase,
    private val getTransactions: GetTransactionsUseCase,
    private val getBalance: GetBalanceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cardIdentifier: String = savedStateHandle["cardIdentifier"] ?: ""
    val slotNumber: Int = savedStateHandle["slotNumber"] ?: 0

    private val _uiState = MutableStateFlow(SlotHistoryUiState())
    val uiState: StateFlow<SlotHistoryUiState> = _uiState.asStateFlow()

    init {
        resolveSlotAndLoad()
    }

    private fun resolveSlotAndLoad() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            loadCards()
                .onSuccess { cards ->
                    val card = cards.firstOrNull { it.cardIdentifier == cardIdentifier }
                    val slot = card?.slots?.firstOrNull { it.slotNumber == slotNumber }
                    _uiState.update {
                        it.copy(
                            address = slot?.address,
                            isUsed = slot?.isUsed == true,
                            isActive = slot?.isActive == true
                        )
                    }
                    val address = slot?.address
                    if (address != null) {
                        loadHistory(address)
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
        }
    }

    fun loadHistory(address: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            getBalance(address)
                .onSuccess { balance ->
                    _uiState.update {
                        it.copy(
                            slotBalance = balance,
                            isSweepDisabled = balance == 0L
                        )
                    }
                }

            getTransactions(address)
                .onSuccess { txs ->
                    _uiState.update { it.copy(transactions = txs, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }
}
