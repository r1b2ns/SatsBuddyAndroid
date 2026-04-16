package com.satsbuddy.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.satsbuddy.data.remote.dto.AddressStatsDto
import com.satsbuddy.data.remote.dto.FeeEstimatesDto
import com.satsbuddy.data.remote.dto.PriceDto
import com.satsbuddy.data.remote.dto.TransactionDto
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class MempoolApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: MempoolApi

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MempoolApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // region getAddressStats

    @Test
    fun `getAddressStats parses response correctly`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(ADDRESS_STATS_JSON)
        )

        val result = api.getAddressStats("bc1qtest")

        assertEquals("bc1qtest", result.address)
        assertEquals(5, result.chainStats.fundedTxoCount)
        assertEquals(100_000L, result.chainStats.fundedTxoSum)
        assertEquals(3, result.chainStats.spentTxoCount)
        assertEquals(40_000L, result.chainStats.spentTxoSum)
        assertEquals(8, result.chainStats.txCount)
        assertEquals(1, result.mempoolStats.fundedTxoCount)
        assertEquals(10_000L, result.mempoolStats.fundedTxoSum)
        assertEquals(0, result.mempoolStats.spentTxoCount)
        assertEquals(0L, result.mempoolStats.spentTxoSum)
        assertEquals(1, result.mempoolStats.txCount)
    }

    @Test
    fun `getAddressStats sends correct request path`() = runTest {
        val address = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4"
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(ADDRESS_STATS_JSON))

        api.getAddressStats(address)

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/address/$address", request.path)
    }

    @Test
    fun `getAddressStats computed balances are correct`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(ADDRESS_STATS_JSON))

        val result = api.getAddressStats("bc1qtest")

        assertEquals(60_000L, result.confirmedBalance)   // 100000 - 40000
        assertEquals(10_000L, result.unconfirmedBalance)  // 10000 - 0
        assertEquals(70_000L, result.totalBalance)        // 60000 + 10000
    }

    @Test
    fun `getAddressStats handles empty address with defaults`() = runTest {
        val emptyJson = """
            {
                "address": "",
                "chain_stats": {},
                "mempool_stats": {}
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(emptyJson))

        val result = api.getAddressStats("")

        assertEquals("", result.address)
        assertEquals(0L, result.confirmedBalance)
        assertEquals(0L, result.unconfirmedBalance)
        assertEquals(0L, result.totalBalance)
    }

    // endregion

    // region getTransactions

    @Test
    fun `getTransactions parses response correctly`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TRANSACTIONS_JSON))

        val result = api.getTransactions("bc1qtest")

        assertEquals(2, result.size)

        val confirmedTx = result[0]
        assertEquals("txid_confirmed_123", confirmedTx.txid)
        assertEquals(2, confirmedTx.version)
        assertEquals(0L, confirmedTx.locktime)
        assertEquals(1, confirmedTx.vin.size)
        assertEquals(2, confirmedTx.vout.size)
        assertEquals(226, confirmedTx.size)
        assertEquals(574, confirmedTx.weight)
        assertEquals(1500L, confirmedTx.fee)
        assertTrue(confirmedTx.status.confirmed)
        assertEquals(800000, confirmedTx.status.blockHeight)
        assertEquals("00000000000000000002a7c4c1e48d76c5a37902165a270156b7a8d72f4d4200", confirmedTx.status.blockHash)
        assertEquals(1700000000L, confirmedTx.status.blockTime)

        val unconfirmedTx = result[1]
        assertEquals("txid_unconfirmed_456", unconfirmedTx.txid)
        assertFalse(unconfirmedTx.status.confirmed)
        assertNull(unconfirmedTx.status.blockHeight)
        assertNull(unconfirmedTx.status.blockHash)
        assertNull(unconfirmedTx.status.blockTime)
    }

    @Test
    fun `getTransactions sends correct request path`() = runTest {
        val address = "bc1qtest"
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TRANSACTIONS_JSON))

        api.getTransactions(address)

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/address/$address/txs", request.path)
    }

    @Test
    fun `getTransactions handles empty list`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val result = api.getTransactions("bc1qtest")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTransactions parses vin prevout correctly`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TRANSACTIONS_JSON))

        val result = api.getTransactions("bc1qtest")
        val vin = result[0].vin[0]

        assertEquals("prev_txid_abc", vin.txid)
        assertEquals(0, vin.vout)
        assertEquals(4294967295, vin.sequence)
        assertEquals("bc1qsender", vin.prevout?.scriptpubkeyAddress)
        assertEquals(50_000L, vin.prevout?.value)
    }

    // endregion

    // region getPrices

    @Test
    fun `getPrices parses response correctly`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(PRICES_JSON))

        val result = api.getPrices()

        assertEquals(1700000000, result.time)
        assertEquals(43250.50, result.usd, 0.01)
        assertEquals(39800.25, result.eur, 0.01)
        assertEquals(34100.75, result.gbp, 0.01)
        assertEquals(58900.00, result.cad, 0.01)
        assertEquals(38200.30, result.chf, 0.01)
        assertEquals(66500.10, result.aud, 0.01)
        assertEquals(6450000.0, result.jpy, 0.01)
    }

    @Test
    fun `getPrices sends correct request`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(PRICES_JSON))

        api.getPrices()

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/v1/prices", request.path)
    }

    // endregion

    // region getRecommendedFees

    @Test
    fun `getRecommendedFees parses response correctly`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(FEES_JSON))

        val result = api.getRecommendedFees()

        assertEquals(25, result.fastestFee)
        assertEquals(18, result.halfHourFee)
        assertEquals(12, result.hourFee)
        assertEquals(6, result.economyFee)
        assertEquals(3, result.minimumFee)
    }

    @Test
    fun `getRecommendedFees sends correct request`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(FEES_JSON))

        api.getRecommendedFees()

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/v1/fees/recommended", request.path)
    }

    // endregion

    // region broadcastTx

    @Test
    fun `broadcastTx sends hex as POST body`() = runTest {
        val txHex = "0200000001abcdef..."
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("\"txid_result_789\""))

        val result = api.broadcastTx(txHex)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/tx", request.path)
        assertTrue(request.body.readUtf8().contains(txHex))
    }

    // endregion

    // region ignoreUnknownKeys

    @Test
    fun `api ignores unknown keys in response`() = runTest {
        val jsonWithExtraFields = """
            {
                "fastestFee": 30,
                "halfHourFee": 20,
                "hourFee": 10,
                "economyFee": 5,
                "minimumFee": 2,
                "unknownField": "should be ignored",
                "anotherUnknown": 999
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonWithExtraFields))

        val result = api.getRecommendedFees()

        assertEquals(30, result.fastestFee)
    }

    // endregion

    companion object {
        private val ADDRESS_STATS_JSON = """
            {
                "address": "bc1qtest",
                "chain_stats": {
                    "funded_txo_count": 5,
                    "funded_txo_sum": 100000,
                    "spent_txo_count": 3,
                    "spent_txo_sum": 40000,
                    "tx_count": 8
                },
                "mempool_stats": {
                    "funded_txo_count": 1,
                    "funded_txo_sum": 10000,
                    "spent_txo_count": 0,
                    "spent_txo_sum": 0,
                    "tx_count": 1
                }
            }
        """.trimIndent()

        private val TRANSACTIONS_JSON = """
            [
                {
                    "txid": "txid_confirmed_123",
                    "version": 2,
                    "locktime": 0,
                    "vin": [
                        {
                            "txid": "prev_txid_abc",
                            "vout": 0,
                            "prevout": {
                                "scriptpubkey": "0014abcdef",
                                "scriptpubkey_address": "bc1qsender",
                                "value": 50000
                            },
                            "sequence": 4294967295
                        }
                    ],
                    "vout": [
                        {
                            "scriptpubkey": "001412345",
                            "scriptpubkey_address": "bc1qtest",
                            "value": 30000
                        },
                        {
                            "scriptpubkey": "001467890",
                            "scriptpubkey_address": "bc1qchange",
                            "value": 18500
                        }
                    ],
                    "size": 226,
                    "weight": 574,
                    "fee": 1500,
                    "status": {
                        "confirmed": true,
                        "block_height": 800000,
                        "block_hash": "00000000000000000002a7c4c1e48d76c5a37902165a270156b7a8d72f4d4200",
                        "block_time": 1700000000
                    }
                },
                {
                    "txid": "txid_unconfirmed_456",
                    "version": 2,
                    "locktime": 0,
                    "vin": [],
                    "vout": [
                        {
                            "scriptpubkey": "001412345",
                            "scriptpubkey_address": "bc1qtest",
                            "value": 5000
                        }
                    ],
                    "size": 150,
                    "weight": 400,
                    "fee": 500,
                    "status": {
                        "confirmed": false
                    }
                }
            ]
        """.trimIndent()

        private val PRICES_JSON = """
            {
                "time": 1700000000,
                "USD": 43250.50,
                "EUR": 39800.25,
                "GBP": 34100.75,
                "CAD": 58900.00,
                "CHF": 38200.30,
                "AUD": 66500.10,
                "JPY": 6450000.00
            }
        """.trimIndent()

        private val FEES_JSON = """
            {
                "fastestFee": 25,
                "halfHourFee": 18,
                "hourFee": 12,
                "economyFee": 6,
                "minimumFee": 3
            }
        """.trimIndent()
    }
}
