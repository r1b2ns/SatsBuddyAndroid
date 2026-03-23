package com.satsbuddy.presentation.send.sign

sealed class SendSignState {
    data object Idle : SendSignState()
    data object PreparingPsbt : SendSignState()
    data object Ready : SendSignState()
    data object Tapping : SendSignState()
    data object Done : SendSignState()
    data class Error(val message: String) : SendSignState()
}

data class SendSignUiState(
    val state: SendSignState = SendSignState.Idle,
    val cvc: String = "",
    val statusMessage: String = "",
    val signedTxid: String? = null
) {
    val isBusy: Boolean
        get() = state is SendSignState.PreparingPsbt || state is SendSignState.Tapping
    val canSign: Boolean
        get() = state is SendSignState.Ready && cvc.isNotBlank()
}
