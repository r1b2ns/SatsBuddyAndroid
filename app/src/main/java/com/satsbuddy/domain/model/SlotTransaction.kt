package com.satsbuddy.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SlotTransaction(
    val txid: String,
    val amount: Long,
    val fee: Long? = null,
    val timestamp: Long? = null,
    val confirmed: Boolean,
    val direction: Direction
) {
    @Serializable
    enum class Direction { INCOMING, OUTGOING }
}
