package com.satsbuddy.data.local

import androidx.datastore.core.DataStore
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.model.SlotInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EncryptedCardStorageTest {

    private lateinit var dataStore: DataStore<String>
    private lateinit var storage: EncryptedCardStorage

    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        dataStore = mockk(relaxed = true)
        storage = EncryptedCardStorage(dataStore)
    }

    // region loadCards

    @Test
    fun `loadCards returns parsed cards from dataStore`() = runTest {
        val cards = listOf(
            SatsCardInfo(pubkey = "pk1", version = "1.0", address = "bc1q1"),
            SatsCardInfo(pubkey = "pk2", version = "1.0", address = "bc1q2")
        )
        coEvery { dataStore.data } returns flowOf(json.encodeToString(cards))

        val result = storage.loadCards()

        assertEquals(2, result.size)
        assertEquals("pk1", result[0].pubkey)
        assertEquals("bc1q1", result[0].address)
        assertEquals("pk2", result[1].pubkey)
        assertEquals("bc1q2", result[1].address)
    }

    @Test
    fun `loadCards returns empty list for empty JSON array`() = runTest {
        coEvery { dataStore.data } returns flowOf("[]")

        val result = storage.loadCards()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `loadCards returns empty list for invalid JSON`() = runTest {
        coEvery { dataStore.data } returns flowOf("not valid json")

        val result = storage.loadCards()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `loadCards returns empty list for empty string`() = runTest {
        coEvery { dataStore.data } returns flowOf("")

        val result = storage.loadCards()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `loadCards preserves all card fields`() = runTest {
        val card = SatsCardInfo(
            pubkey = "pk_full",
            version = "2.0",
            birth = 1700000000L,
            address = "bc1qfulltest",
            cardIdent = "ident_123",
            cardNonce = "nonce_456",
            activeSlot = 3,
            totalSlots = 10,
            isActive = false,
            dateScanned = 1710000000L,
            label = "My Card",
            slots = listOf(
                SlotInfo(slotNumber = 0, isActive = false, isUsed = true, address = "bc1qslot0"),
                SlotInfo(slotNumber = 3, isActive = true, isUsed = true, pubkey = "slotpk3")
            )
        )
        coEvery { dataStore.data } returns flowOf(json.encodeToString(listOf(card)))

        val result = storage.loadCards()

        assertEquals(1, result.size)
        val loaded = result[0]
        assertEquals("pk_full", loaded.pubkey)
        assertEquals("2.0", loaded.version)
        assertEquals(1700000000L, loaded.birth)
        assertEquals("bc1qfulltest", loaded.address)
        assertEquals("ident_123", loaded.cardIdent)
        assertEquals("nonce_456", loaded.cardNonce)
        assertEquals(3, loaded.activeSlot)
        assertEquals(10, loaded.totalSlots)
        assertEquals(false, loaded.isActive)
        assertEquals(1710000000L, loaded.dateScanned)
        assertEquals("My Card", loaded.label)
        assertEquals(2, loaded.slots.size)
        assertEquals("bc1qslot0", loaded.slots[0].address)
        assertEquals("slotpk3", loaded.slots[1].pubkey)
    }

    @Test
    fun `loadCards ignores unknown keys in stored JSON`() = runTest {
        val rawJson = """[{"pubkey":"pk1","version":"1.0","unknownField":"ignored"}]"""
        coEvery { dataStore.data } returns flowOf(rawJson)

        val result = storage.loadCards()

        assertEquals(1, result.size)
        assertEquals("pk1", result[0].pubkey)
    }

    // endregion

    // region saveCards

    @Test
    fun `saveCards writes serialized JSON to dataStore`() = runTest {
        val cards = listOf(
            SatsCardInfo(pubkey = "pk_save", version = "1.0", address = "bc1qsave")
        )
        val transformSlot = slot<suspend (String) -> String>()
        coEvery { dataStore.updateData(capture(transformSlot)) } coAnswers {
            transformSlot.captured.invoke("")
        }

        storage.saveCards(cards)

        coVerify { dataStore.updateData(any()) }

        // Verify the transform produces valid JSON that can be deserialized back
        val serialized = transformSlot.captured.invoke("")
        val deserialized = json.decodeFromString<List<SatsCardInfo>>(serialized)
        assertEquals(1, deserialized.size)
        assertEquals("pk_save", deserialized[0].pubkey)
    }

    @Test
    fun `saveCards with empty list writes empty JSON array`() = runTest {
        val transformSlot = slot<suspend (String) -> String>()
        coEvery { dataStore.updateData(capture(transformSlot)) } coAnswers {
            transformSlot.captured.invoke("")
        }

        storage.saveCards(emptyList())

        val serialized = transformSlot.captured.invoke("")
        assertEquals("[]", serialized)
    }

    @Test
    fun `saveCards then loadCards round-trip`() = runTest {
        val cards = listOf(
            SatsCardInfo(pubkey = "pk_rt1", version = "1.0", label = "Card A"),
            SatsCardInfo(pubkey = "pk_rt2", version = "2.0", label = "Card B")
        )

        // Capture the JSON written by saveCards
        var storedJson = ""
        val transformSlot = slot<suspend (String) -> String>()
        coEvery { dataStore.updateData(capture(transformSlot)) } coAnswers {
            storedJson = transformSlot.captured.invoke("")
            storedJson
        }
        storage.saveCards(cards)

        // Feed the stored JSON back to loadCards
        coEvery { dataStore.data } returns flowOf(storedJson)
        val loaded = storage.loadCards()

        assertEquals(2, loaded.size)
        assertEquals("pk_rt1", loaded[0].pubkey)
        assertEquals("Card A", loaded[0].label)
        assertEquals("pk_rt2", loaded[1].pubkey)
        assertEquals("Card B", loaded[1].label)
    }

    // endregion
}
