package com.satsbuddy.data.repository

import android.nfc.Tag
import com.satsbuddy.data.nfc.CkTapCardDataSource
import com.satsbuddy.domain.model.AppError
import com.satsbuddy.domain.model.SatsCardInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CardRepositoryImplTest {

    private val dataSource = mockk<CkTapCardDataSource>()
    private val repository = CardRepositoryImpl(dataSource)
    private val tag = mockk<Tag>(relaxed = true)

    @Test
    fun `readCardInfo delegates to data source`() = runTest {
        val cardInfo = SatsCardInfo(pubkey = "pk1", version = "1.0")
        coEvery { dataSource.readCard(tag) } returns cardInfo

        val result = repository.readCardInfo(tag)

        assertEquals(cardInfo, result)
        coVerify { dataSource.readCard(tag) }
    }

    @Test
    fun `readCardInfo propagates exception`() = runTest {
        coEvery { dataSource.readCard(tag) } throws AppError.WrongCard

        try {
            repository.readCardInfo(tag)
            assert(false) { "Expected AppError.WrongCard" }
        } catch (e: AppError.WrongCard) {
            // expected
        }
    }

    @Test
    fun `setupNextSlot delegates to data source with all params`() = runTest {
        val cardInfo = SatsCardInfo(pubkey = "pk2", version = "1.0", activeSlot = 1)
        coEvery { dataSource.setupNextSlot(tag, "123456", "id1") } returns cardInfo

        val result = repository.setupNextSlot(tag, "123456", "id1")

        assertEquals(cardInfo, result)
        coVerify { dataSource.setupNextSlot(tag, "123456", "id1") }
    }

    @Test
    fun `setupNextSlot propagates IncorrectCvc`() = runTest {
        coEvery { dataSource.setupNextSlot(tag, "wrong", "id1") } throws AppError.IncorrectCvc()

        try {
            repository.setupNextSlot(tag, "wrong", "id1")
            assert(false) { "Expected AppError.IncorrectCvc" }
        } catch (e: AppError.IncorrectCvc) {
            // expected
        }
    }
}
