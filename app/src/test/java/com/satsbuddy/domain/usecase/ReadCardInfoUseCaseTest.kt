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

class ReadCardInfoUseCaseTest {

    private val repository = mockk<CardRepository>()
    private val useCase = ReadCardInfoUseCase(repository)
    private val tag = mockk<Tag>(relaxed = true)

    @Test
    fun `invoke returns success with card info`() = runTest {
        val cardInfo = SatsCardInfo(pubkey = "pk1", version = "1.0", address = "bc1qtest")
        coEvery { repository.readCardInfo(tag) } returns cardInfo

        val result = useCase(tag)

        assertTrue(result.isSuccess)
        assertEquals("pk1", result.getOrNull()!!.pubkey)
    }

    @Test
    fun `invoke returns failure with WrongCard`() = runTest {
        coEvery { repository.readCardInfo(tag) } throws AppError.WrongCard

        val result = useCase(tag)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.WrongCard)
    }

    @Test
    fun `invoke returns failure with TransportError`() = runTest {
        coEvery { repository.readCardInfo(tag) } throws AppError.TransportError("NFC lost")

        val result = useCase(tag)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.TransportError)
    }
}
