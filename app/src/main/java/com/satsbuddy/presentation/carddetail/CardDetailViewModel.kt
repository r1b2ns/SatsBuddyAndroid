package com.satsbuddy.presentation.carddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satsbuddy.data.nfc.NfcSessionManager
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.usecase.GetBalanceUseCase
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val getBalance: GetBalanceUseCase,
    private val loadCards: LoadCardsUseCase,
    private val saveCards: SaveCardsUseCase,
    private val readCardInfo: ReadCardInfoUseCase,
    private val upsertCard: UpsertCardUseCase,
    private val nfcSessionManager: NfcSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val cardIdentifier: String = savedStateHandle["cardIdentifier"] ?: ""

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    private var cachedCards: List<SatsCardInfo> = emptyList()
    private var fetchToken: String = ""

    init {
        loadCard()
        observeNfcTags()
    }

    private fun loadCard() {
        viewModelScope.launch {
            loadCards().onSuccess { cards ->
                cachedCards = cards
                val card = cards.firstOrNull { it.cardIdentifier == cardIdentifier }
                if (card != null) {
                    _uiState.update {
                        it.copy(
                            displayName = card.displayName,
                            label = card.label,
                            slots = card.slots,
                            lastUpdated = card.dateScanned,
                            cardVersion = card.version
                        )
                    }
                    loadSlotDetails(card)
                }
            }
        }
    }

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

    fun updateLabel(newLabel: String) {
        val normalized = newLabel.trim().ifBlank { null }
        val updatedCards = cachedCards.map {
            if (it.cardIdentifier == cardIdentifier) it.copy(label = normalized) else it
        }
        cachedCards = updatedCards
        val updatedCard = updatedCards.firstOrNull { it.cardIdentifier == cardIdentifier }
        if (updatedCard != null) {
            _uiState.update {
                it.copy(
                    displayName = updatedCard.displayName,
                    label = updatedCard.label
                )
            }
        }
        viewModelScope.launch { saveCards(updatedCards) }
    }

    fun beginRefreshScan() {
        _uiState.update { it.copy(isScanning = true, errorMessage = null) }
    }

    fun cancelRefreshScan() {
        _uiState.update { it.copy(isScanning = false) }
    }

    private fun observeNfcTags() {
        viewModelScope.launch {
            nfcSessionManager.tagFlow.collect { tag ->
                if (!_uiState.value.isScanning) return@collect
                readCardInfo(tag)
                    .onSuccess { scanned ->
                        if (scanned.cardIdentifier != cardIdentifier) {
                            _uiState.update {
                                it.copy(
                                    isScanning = false,
                                    errorMessage = "Scanned card does not match this card."
                                )
                            }
                            return@onSuccess
                        }
                        val existing = cachedCards.firstOrNull { it.cardIdentifier == cardIdentifier }
                        val refreshed = scanned.copy(label = existing?.label ?: scanned.label)
                        val (updatedCards, _) = upsertCard(cachedCards, refreshed)
                        cachedCards = updatedCards
                        saveCards(updatedCards)
                        _uiState.update {
                            it.copy(
                                isScanning = false,
                                errorMessage = null,
                                displayName = refreshed.displayName,
                                label = refreshed.label,
                                slots = refreshed.slots,
                                lastUpdated = refreshed.dateScanned,
                                cardVersion = refreshed.version
                            )
                        }
                        loadSlotDetails(refreshed)
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isScanning = false,
                                errorMessage = error.message
                            )
                        }
                    }
            }
        }
    }
}
