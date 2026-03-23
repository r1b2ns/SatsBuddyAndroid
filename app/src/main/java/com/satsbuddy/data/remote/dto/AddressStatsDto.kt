package com.satsbuddy.data.remote.dto

import com.satsbuddy.domain.model.SlotTransaction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressStatsDto(
    val address: String = "",
    @SerialName("chain_stats") val chainStats: ChainStats = ChainStats(),
    @SerialName("mempool_stats") val mempoolStats: MempoolStats = MempoolStats()
) {
    val confirmedBalance: Long
        get() = chainStats.fundedTxoSum - chainStats.spentTxoSum

    val unconfirmedBalance: Long
        get() = mempoolStats.fundedTxoSum - mempoolStats.spentTxoSum

    val totalBalance: Long
        get() = confirmedBalance + unconfirmedBalance
}

@Serializable
data class ChainStats(
    @SerialName("funded_txo_count") val fundedTxoCount: Int = 0,
    @SerialName("funded_txo_sum") val fundedTxoSum: Long = 0,
    @SerialName("spent_txo_count") val spentTxoCount: Int = 0,
    @SerialName("spent_txo_sum") val spentTxoSum: Long = 0,
    @SerialName("tx_count") val txCount: Int = 0
)

@Serializable
data class MempoolStats(
    @SerialName("funded_txo_count") val fundedTxoCount: Int = 0,
    @SerialName("funded_txo_sum") val fundedTxoSum: Long = 0,
    @SerialName("spent_txo_count") val spentTxoCount: Int = 0,
    @SerialName("spent_txo_sum") val spentTxoSum: Long = 0,
    @SerialName("tx_count") val txCount: Int = 0
)
