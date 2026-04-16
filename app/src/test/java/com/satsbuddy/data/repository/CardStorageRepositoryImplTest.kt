package com.satsbuddy.data.repository

import com.satsbuddy.data.local.EncryptedCardStorage
import com.satsbuddy.domain.model.SatsCardInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardStorageRepositoryImplTest {

    private val storage = mockk<EncryptedCardStorage>(relaxed = true)
    private val repository = CardStorageRepositoryImpl(storage)

    @Test
    fun `loadCards delegates to storage`() = runTest {
        val cards = listOf(
            SatsCardInfo(pubkey = "pk1", version = "1.0"),
            SatsCardInfo(pubkey = "pk2", version = "2.0")
        )
        coEvery { storage.loadCards() } returns cards

        val result = repository.loadCards()

        assertEquals(2, result.size)
        assertEquals("pk1", result[0].pubkey)
        assertEquals("pk2", result[1].pubkey)
    }

    @Test
    fun `loadCards returns empty list when storage is empty`() = runTest {
        coEvery { storage.loadCards() } returns emptyList()

        val result = repository.loadCards()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `saveCards delegates to storage`() = runTest {
        val cards = listOf(SatsCardInfo(pubkey = "pk1", version = "1.0"))

        repository.saveCards(cards)

        coVerify { storage.saveCards(cards) }
    }

    @Test
    fun `saveCards with empty list delegates to storage`() = runTest {
        repository.saveCards(emptyList())

        coVerify { storage.saveCards(emptyList()) }
    }
}
