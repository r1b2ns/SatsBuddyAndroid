package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.CardStorageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveCardsUseCaseTest {

    private val repository = mockk<CardStorageRepository>(relaxed = true)
    private val useCase = SaveCardsUseCase(repository)

    @Test
    fun `invoke returns success after saving`() = runTest {
        val cards = listOf(SatsCardInfo(pubkey = "pk1", version = "1.0"))

        val result = useCase(cards)

        assertTrue(result.isSuccess)
        coVerify { repository.saveCards(cards) }
    }

    @Test
    fun `invoke returns success with empty list`() = runTest {
        val result = useCase(emptyList())

        assertTrue(result.isSuccess)
        coVerify { repository.saveCards(emptyList()) }
    }

    @Test
    fun `invoke returns failure on exception`() = runTest {
        coEvery { repository.saveCards(any()) } throws RuntimeException("Write error")

        val result = useCase(listOf(SatsCardInfo(pubkey = "pk1", version = "1.0")))

        assertTrue(result.isFailure)
    }
}
