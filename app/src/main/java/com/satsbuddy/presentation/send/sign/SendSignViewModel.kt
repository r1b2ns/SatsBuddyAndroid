package com.satsbuddy.presentation.send.sign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satsbuddy.data.nfc.NfcSessionManager
import com.satsbuddy.domain.usecase.BuildPsbtUseCase
import com.satsbuddy.domain.usecase.SignAndBroadcastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendSignViewModel @Inject constructor(
    private val buildPsbt: BuildPsbtUseCase,
    private val signAndBroadcast: SignAndBroadcastUseCase,
    private val nfcSessionManager: NfcSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendSignUiState())
    val uiState: StateFlow<SendSignUiState> = _uiState.asStateFlow()

    private var psbtBase64: String? = null

    fun preparePsbt(descriptor: String, destination: String, feeRate: Long) {
        _uiState.update {
            it.copy(
                state = SendSignState.PreparingPsbt,
                statusMessage = "Building transaction..."
            )
        }
        viewModelScope.launch {
            buildPsbt(descriptor, destination, feeRate)
                .onSuccess { psbt ->
                    psbtBase64 = psbt
                    _uiState.update {
                        it.copy(
                            state = SendSignState.Ready,
                            statusMessage = "Enter CVC and tap card to sign"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            state = SendSignState.Error(error.message ?: "Failed to build PSBT"),
                            statusMessage = error.message ?: "Error"
                        )
                    }
                }
        }
    }

    fun updateCvc(cvc: String) {
        _uiState.update { it.copy(cvc = cvc) }
    }

    fun startSign(slotNumber: Int) {
        val psbt = psbtBase64 ?: return
        val cvc = _uiState.value.cvc
        if (cvc.isBlank()) return

        _uiState.update {
            it.copy(state = SendSignState.Tapping, statusMessage = "Hold phone near SATSCARD...")
        }

        viewModelScope.launch {
            nfcSessionManager.tagFlow.collect { tag ->
                signAndBroadcast(tag, slotNumber, psbt, cvc)
                    .onSuccess { txid ->
                        _uiState.update {
                            it.copy(
                                state = SendSignState.Done,
                                signedTxid = txid,
                                statusMessage = "Transaction broadcast!"
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                state = SendSignState.Error(error.message ?: "Signing failed"),
                                statusMessage = error.message ?: "Error"
                            )
                        }
                    }
                return@collect
            }
        }
    }
}
