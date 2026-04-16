package com.satsbuddy.domain.usecase

import android.nfc.Tag
import com.satsbuddy.domain.model.AppError
import com.satsbuddy.domain.repository.PsbtRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignAndBroadcastUseCaseTest {

    private val repository = mockk<PsbtRepository>()
    private val useCase = SignAndBroadcastUseCase(repository)
    private val tag = mockk<Tag>(relaxed = true)

    @Test
    fun `invoke signs on card then broadcasts and returns txid`() = runTest {
        coEvery { repository.signOnCard(tag, 0, "raw_psbt", "123456") } returns "signed_psbt"
        coEvery { repository.broadcast("signed_psbt") } returns "txid_result"

        val result = useCase(tag, 0, "raw_psbt", "123456")

        assertTrue(result.isSuccess)
        assertEquals("txid_result", result.getOrNull())
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            repository.signOnCard(tag, 0, "raw_psbt", "123456")
            repository.broadcast("signed_psbt")
        }
    }

    @Test
    fun `invoke passes signed psbt from signOnCard to broadcast`() = runTest {
        coEvery { repository.signOnCard(tag, 2, "psbt_input", "000000") } returns "specific_signed"
        coEvery { repository.broadcast("specific_signed") } returns "txid_123"

        val result = useCase(tag, 2, "psbt_input", "000000")

        assertTrue(result.isSuccess)
        coVerify { repository.broadcast("specific_signed") }
    }

    @Test
    fun `invoke returns failure when signOnCard fails`() = runTest {
        coEvery { repository.signOnCard(tag, 0, "psbt", "wrong") } throws AppError.IncorrectCvc()

        val result = useCase(tag, 0, "psbt", "wrong")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.IncorrectCvc)
        coVerify(exactly = 0) { repository.broadcast(any()) }
    }

    @Test
    fun `invoke returns failure when broadcast fails after successful sign`() = runTest {
        coEvery { repository.signOnCard(tag, 0, "psbt", "123456") } returns "signed"
        coEvery { repository.broadcast("signed") } throws RuntimeException("Broadcast rejected")

        val result = useCase(tag, 0, "psbt", "123456")

        assertTrue(result.isFailure)
        assertEquals("Broadcast rejected", result.exceptionOrNull()?.message)
    }
}
