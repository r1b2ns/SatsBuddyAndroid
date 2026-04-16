package com.satsbuddy.domain.usecase

import android.nfc.Tag
import com.satsbuddy.domain.model.AppError
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.CardRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupNextSlotUseCaseTest {

    private val repository = mockk<CardRepository>()
    private val useCase = SetupNextSlotUseCase(repository)
    private val tag = mockk<Tag>(relaxed = true)

    @Test
    fun `invoke returns success with updated card info`() = runTest {
        val cardInfo = SatsCardInfo(pubkey = "pk_new", version = "1.0", activeSlot = 1)
        coEvery { repository.setupNextSlot(tag, "123456", "id1") } returns cardInfo

        val result = useCase(tag, "123456", "id1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.activeSlot)
    }

    @Test
    fun `invoke returns failure with IncorrectCvc`() = runTest {
        coEvery { repository.setupNextSlot(tag, "wrong", "id1") } throws AppError.IncorrectCvc()

        val result = useCase(tag, "wrong", "id1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.IncorrectCvc)
    }

    @Test
    fun `invoke returns failure with NoUnusedSlots`() = runTest {
        coEvery { repository.setupNextSlot(tag, "123456", "id1") } throws AppError.NoUnusedSlots

        val result = useCase(tag, "123456", "id1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NoUnusedSlots)
    }

    @Test
    fun `invoke returns failure with WrongCard`() = runTest {
        coEvery { repository.setupNextSlot(tag, "123456", "wrong_id") } throws AppError.WrongCard

        val result = useCase(tag, "123456", "wrong_id")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.WrongCard)
    }
}
