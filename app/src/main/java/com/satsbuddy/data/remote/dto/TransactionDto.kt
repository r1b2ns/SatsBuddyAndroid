package com.satsbuddy.data.remote.dto

import com.satsbuddy.domain.model.SlotTransaction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val txid: String = "",
    val version: Int = 0,
    val locktime: Long = 0,
    val vin: List<TxInput> = emptyList(),
    val vout: List<TxOutput> = emptyList(),
    val size: Int = 0,
    val weight: Int = 0,
    val fee: Long = 0,
    val status: TxStatus = TxStatus()
) {
    fun toDomain(address: String): SlotTransaction {
        val received = vout.filter { it.scriptpubkeyAddress == address }.sumOf { it.value }
        val sent = vin.filter { it.prevout?.scriptpubkeyAddress == address }.sumOf { it.prevout?.value ?: 0L }
        val netAmount = received - sent
        val direction = if (netAmount >= 0) SlotTransaction.Direction.INCOMING else SlotTransaction.Direction.OUTGOING
        return SlotTransaction(
            txid = txid,
            amount = Math.abs(netAmount),
            fee = fee,
            timestamp = status.blockTime,
            confirmed = status.confirmed,
            direction = direction
        )
    }
}

@Serializable
data class TxInput(
    val txid: String = "",
    val vout: Int = 0,
    val prevout: TxOutput? = null,
    val sequence: Long = 0
)

@Serializable
data class TxOutput(
    val scriptpubkey: String = "",
    @SerialName("scriptpubkey_address") val scriptpubkeyAddress: String? = null,
    val value: Long = 0
)

@Serializable
data class TxStatus(
    val confirmed: Boolean = false,
    @SerialName("block_height") val blockHeight: Int? = null,
    @SerialName("block_hash") val blockHash: String? = null,
    @SerialName("block_time") val blockTime: Long? = null
)
