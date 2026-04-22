package com.satsbuddy.presentation.slotlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satsbuddy.domain.model.SlotInfo
import com.satsbuddy.domain.usecase.GetBalanceUseCase
import com.satsbuddy.domain.usecase.LoadCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SlotListViewModel @Inject constructor(
    private val loadCards: LoadCardsUseCase,
    private val getBalance: GetBalanceUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cardIdentifier: String = savedStateHandle["cardIdentifier"] ?: ""

    private val _uiState = MutableStateFlow(SlotListUiState())
    val uiState: StateFlow<SlotListUiState> = _uiState.asStateFlow()

    init {
        loadSlots()
    }

    private fun loadSlots() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            loadCards()
                .onSuccess { cards ->
                    val card = cards.firstOrNull { it.cardIdentifier == cardIdentifier }
                    val slots = card?.slots ?: emptyList()
                    _uiState.update {
                        it.copy(
                            displayName = card?.displayName.orEmpty(),
                            slots = slots,
                            isLoading = false
                        )
                    }
                    if (slots.isNotEmpty()) {
                        refreshBalances(slots)
                    }
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

    private suspend fun refreshBalances(slots: List<SlotInfo>) {
        val updates = slots
            .filter { (it.isActive || it.isUsed) && !it.address.isNullOrEmpty() }
            .map { slot ->
                viewModelScope.async {
                    slot.slotNumber to getBalance(slot.address!!).getOrNull()
                }
            }
            .awaitAll()
            .toMap()

        if (updates.isEmpty()) return

        _uiState.update { state ->
            state.copy(
                slots = state.slots.map { slot ->
                    val balance = updates[slot.slotNumber]
                    if (balance != null) slot.copy(balance = balance) else slot
                }
            )
        }
    }
}
