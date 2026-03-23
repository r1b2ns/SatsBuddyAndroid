package com.satsbuddy.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SatsCardInfo(
    val version: String = "",
    val birth: Long? = null,
    val address: String? = null,
    val pubkey: String = "",
    val cardIdent: String? = null,
    val cardNonce: String? = null,
    val activeSlot: Int? = null,
    val totalSlots: Int? = null,
    val isActive: Boolean = true,
    val dateScanned: Long = System.currentTimeMillis(),
    val label: String? = null,
    val slots: List<SlotInfo> = emptyList()
) {
    val displayName: String
        get() {
            val trimmed = label?.trim()
            if (!trimmed.isNullOrEmpty()) return trimmed
            if (pubkey.isNotEmpty()) return pubkey
            if (!address.isNullOrEmpty()) return address
            return "SATSCARD"
        }

    val cardIdentifier: String
        get() = cardIdent?.takeIf { it.isNotEmpty() } ?: pubkey

    val displayActiveSlotNumber: Int?
        get() = activeSlot?.let { it + 1 }

    @Transient
    val activeSlotInfo: SlotInfo? = slots.firstOrNull { it.isActive }
}
