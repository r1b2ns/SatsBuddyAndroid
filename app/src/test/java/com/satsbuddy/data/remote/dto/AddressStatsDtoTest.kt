package com.satsbuddy.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AddressStatsDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `confirmedBalance is funded minus spent on chain`() {
        val dto = AddressStatsDto(
            chainStats = ChainStats(fundedTxoSum = 500_000, spentTxoSum = 200_000)
        )
        assertEquals(300_000L, dto.confirmedBalance)
    }

    @Test
    fun `unconfirmedBalance is funded minus spent in mempool`() {
        val dto = AddressStatsDto(
            mempoolStats = MempoolStats(fundedTxoSum = 50_000, spentTxoSum = 10_000)
        )
        assertEquals(40_000L, dto.unconfirmedBalance)
    }

    @Test
    fun `totalBalance sums confirmed and unconfirmed`() {
        val dto = AddressStatsDto(
            chainStats = ChainStats(fundedTxoSum = 100_000, spentTxoSum = 30_000),
            mempoolStats = MempoolStats(fundedTxoSum = 20_000, spentTxoSum = 5_000)
        )
        assertEquals(70_000L, dto.confirmedBalance)
        assertEquals(15_000L, dto.unconfirmedBalance)
        assertEquals(85_000L, dto.totalBalance)
    }

    @Test
    fun `default values produce zero balances`() {
        val dto = AddressStatsDto()
        assertEquals(0L, dto.confirmedBalance)
        assertEquals(0L, dto.unconfirmedBalance)
        assertEquals(0L, dto.totalBalance)
    }

    @Test
    fun `negative unconfirmed balance when spent exceeds funded in mempool`() {
        val dto = AddressStatsDto(
            mempoolStats = MempoolStats(fundedTxoSum = 0, spentTxoSum = 30_000)
        )
        assertEquals(-30_000L, dto.unconfirmedBalance)
    }

    @Test
    fun `deserialization from JSON with snake_case keys`() {
        val raw = """
            {
                "address": "bc1qxyz",
                "chain_stats": {
                    "funded_txo_count": 10,
                    "funded_txo_sum": 1000000,
                    "spent_txo_count": 5,
                    "spent_txo_sum": 400000,
                    "tx_count": 15
                },
                "mempool_stats": {
                    "funded_txo_count": 2,
                    "funded_txo_sum": 50000,
                    "spent_txo_count": 1,
                    "spent_txo_sum": 10000,
                    "tx_count": 3
                }
            }
        """.trimIndent()

        val dto = json.decodeFromString<AddressStatsDto>(raw)

        assertEquals("bc1qxyz", dto.address)
        assertEquals(10, dto.chainStats.fundedTxoCount)
        assertEquals(1_000_000L, dto.chainStats.fundedTxoSum)
        assertEquals(5, dto.chainStats.spentTxoCount)
        assertEquals(400_000L, dto.chainStats.spentTxoSum)
        assertEquals(15, dto.chainStats.txCount)
        assertEquals(600_000L, dto.confirmedBalance)
        assertEquals(40_000L, dto.unconfirmedBalance)
        assertEquals(640_000L, dto.totalBalance)
    }
}
