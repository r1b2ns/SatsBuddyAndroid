package com.satsbuddy.presentation.slotlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satsbuddy.domain.usecase.LoadCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SlotListViewModel @Inject constructor(
    private val loadCards: LoadCardsUseCase,
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
                    _uiState.update {
                        it.copy(
                            displayName = card?.displayName.orEmpty(),
                            slots = card?.slots ?: emptyList(),
                            isLoading = false
                        )
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
}
