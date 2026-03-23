package com.satsbuddy.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SlotInfo(
    val slotNumber: Int,
    val isActive: Boolean,
    val isUsed: Boolean,
    val pubkey: String? = null,
    val pubkeyDescriptor: String? = null,
    val address: String? = null,
    val balance: Long? = null
) {
    val displaySlotNumber: Int get() = slotNumber + 1
}
