package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.CardStorageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadCardsUseCaseTest {

    private val repository = mockk<CardStorageRepository>()
    private val useCase = LoadCardsUseCase(repository)

    @Test
    fun `invoke returns success with cards`() = runTest {
        val cards = listOf(
            SatsCardInfo(pubkey = "pk1", version = "1.0"),
            SatsCardInfo(pubkey = "pk2", version = "2.0")
        )
        coEvery { repository.loadCards() } returns cards

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke returns success with empty list`() = runTest {
        coEvery { repository.loadCards() } returns emptyList()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `invoke returns failure on exception`() = runTest {
        coEvery { repository.loadCards() } throws RuntimeException("Storage error")

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
