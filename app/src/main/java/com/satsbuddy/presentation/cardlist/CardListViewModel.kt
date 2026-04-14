package com.satsbuddy.presentation.cardlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satsbuddy.data.nfc.NfcSessionManager
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.UserPreferencesRepository
import com.satsbuddy.domain.usecase.GetPriceUseCase
import com.satsbuddy.domain.usecase.LoadCardsUseCase
import com.satsbuddy.domain.usecase.ReadCardInfoUseCase
import com.satsbuddy.domain.usecase.SaveCardsUseCase
import com.satsbuddy.domain.usecase.UpsertCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardListViewModel @Inject constructor(
    private val readCardInfo: ReadCardInfoUseCase,
    private val loadCards: LoadCardsUseCase,
    private val saveCards: SaveCardsUseCase,
    private val upsertCard: UpsertCardUseCase,
    private val getPrice: GetPriceUseCase,
    private val nfcSessionManager: NfcSessionManager,
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardListUiState())
    val uiState: StateFlow<CardListUiState> = _uiState.asStateFlow()

    init {
        loadPersistedCards()
        refreshPrice()
        observeNfcTags()
        observeSwipeTip()
    }

    private fun observeSwipeTip() {
        viewModelScope.launch {
            userPreferences.swipeToDeleteTipDismissed.collect { dismissed ->
                _uiState.update { it.copy(showSwipeToDeleteTip = !dismissed) }
            }
        }
    }

    private fun loadPersistedCards() {
        viewModelScope.launch {
            loadCards().onSuccess { cards ->
                _uiState.update { it.copy(cards = cards) }
            }
        }
    }

    private fun observeNfcTags() {
        viewModelScope.launch {
            nfcSessionManager.tagFlow.collect { tag ->
                if (!_uiState.value.isScanning) return@collect
                _uiState.update { it.copy(statusMessage = "Reading card...") }
                readCardInfo(tag)
                    .onSuccess { card ->
                        val (updatedCards, _) = upsertCard(_uiState.value.cards, card)
                        _uiState.update {
                            it.copy(
                                cards = updatedCards,
                                isScanning = false,
                                statusMessage = "",
                                errorMessage = null
                            )
                        }
                        saveCards(updatedCards)
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isScanning = false,
                                errorMessage = error.message,
                                statusMessage = ""
                            )
                        }
                    }
            }
        }
    }

    fun beginScan() {
        _uiState.update { it.copy(isScanning = true, statusMessage = "Hold phone near SATSCARD", errorMessage = null) }
    }

    fun cancelScan() {
        _uiState.update { it.copy(isScanning = false, statusMessage = "") }
    }

    fun removeCard(card: SatsCardInfo) {
        val updated = _uiState.value.cards.filter { it.cardIdentifier != card.cardIdentifier }
        _uiState.update { it.copy(cards = updated, cardPendingDeletion = null) }
        viewModelScope.launch { saveCards(updated) }
    }

    fun requestCardDeletion(card: SatsCardInfo) {
        _uiState.update { it.copy(cardPendingDeletion = card) }
    }

    fun cancelCardDeletion() {
        _uiState.update { it.copy(cardPendingDeletion = null) }
    }

    fun confirmCardDeletion() {
        _uiState.value.cardPendingDeletion?.let { removeCard(it) }
    }

    fun dismissSwipeToDeleteTip() {
        _uiState.update { it.copy(showSwipeToDeleteTip = false) }
        viewModelScope.launch { userPreferences.setSwipeToDeleteTipDismissed(true) }
    }

    fun updateLabel(card: SatsCardInfo, newLabel: String) {
        val updated = _uiState.value.cards.map {
            if (it.cardIdentifier == card.cardIdentifier) it.copy(label = newLabel.ifBlank { null })
            else it
        }
        _uiState.update { it.copy(cards = updated) }
        viewModelScope.launch { saveCards(updated) }
    }

    fun refreshPrice() {
        viewModelScope.launch {
            getPrice().onSuccess { price ->
                _uiState.update { it.copy(price = price) }
            }
        }
    }
}
