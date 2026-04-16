package com.satsbuddy.data.remote.dto

import com.satsbuddy.domain.model.SlotTransaction
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val testAddress = "bc1qtest"

    // region toDomain

    @Test
    fun `toDomain maps incoming transaction correctly`() {
        val dto = TransactionDto(
            txid = "tx123",
            fee = 500,
            vout = listOf(
                TxOutput(scriptpubkeyAddress = testAddress, value = 30_000),
                TxOutput(scriptpubkeyAddress = "bc1qother", value = 10_000)
            ),
            status = TxStatus(confirmed = true, blockTime = 1700000000)
        )

        val domain = dto.toDomain(testAddress)

        assertEquals("tx123", domain.txid)
        assertEquals(30_000L, domain.amount)
        assertEquals(500L, domain.fee)
        assertEquals(1700000000L, domain.timestamp)
        assertTrue(domain.confirmed)
        assertEquals(SlotTransaction.Direction.INCOMING, domain.direction)
    }

    @Test
    fun `toDomain maps outgoing transaction correctly`() {
        val dto = TransactionDto(
            txid = "tx456",
            fee = 1000,
            vin = listOf(
                TxInput(
                    prevout = TxOutput(scriptpubkeyAddress = testAddress, value = 50_000)
                )
            ),
            vout = listOf(
                TxOutput(scriptpubkeyAddress = "bc1qrecipient", value = 40_000),
                TxOutput(scriptpubkeyAddress = testAddress, value = 9_000)  // change
            ),
            status = TxStatus(confirmed = true, blockTime = 1700000000)
        )

        val domain = dto.toDomain(testAddress)

        // netAmount = received(9000) - sent(50000) = -41000, amount = abs(-41000)
        assertEquals("tx456", domain.txid)
        assertEquals(41_000L, domain.amount)
        assertEquals(SlotTransaction.Direction.OUTGOING, domain.direction)
    }

    @Test
    fun `toDomain handles unconfirmed transaction`() {
        val dto = TransactionDto(
            txid = "txunconf",
            vout = listOf(TxOutput(scriptpubkeyAddress = testAddress, value = 5_000)),
            status = TxStatus(confirmed = false)
        )

        val domain = dto.toDomain(testAddress)

        assertFalse(domain.confirmed)
        assertNull(domain.timestamp)
    }

    @Test
    fun `toDomain with zero net amount is INCOMING`() {
        val dto = TransactionDto(
            txid = "txzero",
            vin = listOf(
                TxInput(prevout = TxOutput(scriptpubkeyAddress = testAddress, value = 10_000))
            ),
            vout = listOf(
                TxOutput(scriptpubkeyAddress = testAddress, value = 10_000)
            ),
            status = TxStatus(confirmed = true)
        )

        val domain = dto.toDomain(testAddress)

        assertEquals(0L, domain.amount)
        assertEquals(SlotTransaction.Direction.INCOMING, domain.direction)
    }

    @Test
    fun `toDomain handles null prevout in vin`() {
        val dto = TransactionDto(
            txid = "txnullprevout",
            vin = listOf(TxInput(prevout = null)),
            vout = listOf(TxOutput(scriptpubkeyAddress = testAddress, value = 20_000)),
            status = TxStatus(confirmed = true)
        )

        val domain = dto.toDomain(testAddress)

        assertEquals(20_000L, domain.amount)
        assertEquals(SlotTransaction.Direction.INCOMING, domain.direction)
    }

    @Test
    fun `toDomain with address not in any output`() {
        val dto = TransactionDto(
            txid = "txnoaddr",
            vin = listOf(
                TxInput(prevout = TxOutput(scriptpubkeyAddress = testAddress, value = 100_000))
            ),
            vout = listOf(
                TxOutput(scriptpubkeyAddress = "bc1qother1", value = 60_000),
                TxOutput(scriptpubkeyAddress = "bc1qother2", value = 39_000)
            ),
            status = TxStatus(confirmed = true)
        )

        val domain = dto.toDomain(testAddress)

        // received = 0, sent = 100000, net = -100000
        assertEquals(100_000L, domain.amount)
        assertEquals(SlotTransaction.Direction.OUTGOING, domain.direction)
    }

    // endregion

    // region serialization

    @Test
    fun `deserialization from JSON`() {
        val raw = """
            {
                "txid": "abc123",
                "version": 2,
                "locktime": 0,
                "vin": [
                    {
                        "txid": "prev_tx",
                        "vout": 1,
                        "prevout": {
                            "scriptpubkey": "0014abc",
                            "scriptpubkey_address": "bc1qsender",
                            "value": 75000
                        },
                        "sequence": 4294967295
                    }
                ],
                "vout": [
                    {
                        "scriptpubkey": "0014def",
                        "scriptpubkey_address": "bc1qtest",
                        "value": 50000
                    }
                ],
                "size": 200,
                "weight": 500,
                "fee": 2000,
                "status": {
                    "confirmed": true,
                    "block_height": 850000,
                    "block_hash": "000000000000000000abcdef",
                    "block_time": 1710000000
                }
            }
        """.trimIndent()

        val dto = json.decodeFromString<TransactionDto>(raw)

        assertEquals("abc123", dto.txid)
        assertEquals(2, dto.version)
        assertEquals(1, dto.vin.size)
        assertEquals("prev_tx", dto.vin[0].txid)
        assertEquals(1, dto.vin[0].vout)
        assertEquals("bc1qsender", dto.vin[0].prevout?.scriptpubkeyAddress)
        assertEquals(75_000L, dto.vin[0].prevout?.value)
        assertEquals(1, dto.vout.size)
        assertEquals("bc1qtest", dto.vout[0].scriptpubkeyAddress)
        assertEquals(50_000L, dto.vout[0].value)
        assertEquals(2000L, dto.fee)
        assertTrue(dto.status.confirmed)
        assertEquals(850000, dto.status.blockHeight)
        assertEquals(1710000000L, dto.status.blockTime)
    }

    @Test
    fun `TxStatus deserialization with unconfirmed status`() {
        val raw = """{"confirmed": false}"""

        val status = json.decodeFromString<TxStatus>(raw)

        assertFalse(status.confirmed)
        assertNull(status.blockHeight)
        assertNull(status.blockHash)
        assertNull(status.blockTime)
    }

    // endregion
}
