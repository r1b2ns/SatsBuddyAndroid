package com.satsbuddy.domain.model

sealed class AppError(override val message: String) : Exception(message) {
    data class Generic(override val message: String) : AppError(message)
    data object NfcUnavailable : AppError("NFC is not available on this device")
    data class TransportError(override val message: String) : AppError(message)
    data class IncorrectCvc(val cooldownSeconds: Int? = null) : AppError(
        if (cooldownSeconds != null) "Incorrect CVC. Card cooling down for $cooldownSeconds seconds." else "Incorrect CVC."
    )
    data class RateLimited(val cooldownSeconds: Int? = null) : AppError("Too many attempts.${if (cooldownSeconds != null) " Wait $cooldownSeconds seconds." else ""}")
    data object WrongCard : AppError("Wrong SATSCARD detected.")
    data object NoUnusedSlots : AppError("No unused slots remaining.")
    data object InsufficientFunds : AppError("Insufficient funds.")
}
