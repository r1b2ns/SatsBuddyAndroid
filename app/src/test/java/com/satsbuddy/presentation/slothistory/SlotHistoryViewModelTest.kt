package com.satsbuddy.presentation.slothistory

import androidx.lifecycle.SavedStateHandle
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.model.SlotInfo
import com.satsbuddy.domain.model.SlotTransaction
import com.satsbuddy.domain.usecase.GetBalanceUseCase
import com.satsbuddy.domain.usecase.GetTransactionsUseCase
import com.satsbuddy.domain.usecase.LoadCardsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SlotHistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val loadCards = mockk<LoadCardsUseCase>()
    private val getTransactions = mockk<GetTransactionsUseCase>()
    private val getBalance = mockk<GetBalanceUseCase>()

    private val testCard = SatsCardInfo(
        pubkey = "pk1",
        cardIdent = "ident_1",
        address = "bc1qcard",
        slots = listOf(
            SlotInfo(slotNumber = 0, isActive = false, isUsed = true, address = "bc1qslot0"),
            SlotInfo(slotNumber = 1, isActive = true, isUsed = false, address = "bc1qslot1"),
            SlotInfo(slotNumber = 2, isActive = false, isUsed = false, address = null)
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        cardIdentifier: String = "ident_1",
        slotNumber: Int = 0,
        cards: List<SatsCardInfo> = listOf(testCard),
        balanceResult: Result<Long> = Result.success(25_000L),
        transactionsResult: Result<List<SlotTransaction>> = Result.success(emptyList())
    ): SlotHistoryViewModel {
        coEvery { loadCards() } returns Result.success(cards)
        coEvery { getBalance(any()) } returns balanceResult
        coEvery { getTransactions(any()) } returns transactionsResult
        val savedStateHandle = SavedStateHandle(
            mapOf("cardIdentifier" to cardIdentifier, "slotNumber" to slotNumber)
        )
        return SlotHistoryViewModel(loadCards, getTransactions, getBalance, savedStateHandle)
    }

    @Test
    fun `savedStateHandle params are read correctly`() = runTest {
        val vm = createViewModel("my_card", 3, cards = emptyList())
        assertEquals("my_card", vm.cardIdentifier)
        assertEquals(3, vm.slotNumber)
    }

    @Test
    fun `init resolves slot address, status and loads balance and transactions`() = runTest {
        val vm = createViewModel(
            slotNumber = 0,
            balanceResult = Result.success(501L),
            transactionsResult = Result.success(
                listOf(
                    SlotTransaction("tx1", 501, 500, 1712563260, true, SlotTransaction.Direction.INCOMING)
                )
            )
        )

        assertEquals("bc1qslot0", vm.uiState.value.address)
        assertTrue(vm.uiState.value.isUsed)
        assertFalse(vm.uiState.value.isActive)
        assertEquals(501L, vm.uiState.value.slotBalance)
        assertEquals(1, vm.uiState.value.transactions.size)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `active slot resolves with isActive true`() = runTest {
        val vm = createViewModel(slotNumber = 1)

        assertEquals("bc1qslot1", vm.uiState.value.address)
        assertTrue(vm.uiState.value.isActive)
        assertFalse(vm.uiState.value.isUsed)
    }

    @Test
    fun `slot without address leaves address null and does not load history`() = runTest {
        val vm = createViewModel(slotNumber = 2)

        assertNull(vm.uiState.value.address)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.slotBalance)
    }

    @Test
    fun `loadHistory disables sweep when balance is zero`() = runTest {
        val vm = createViewModel(balanceResult = Result.success(0L))
        assertEquals(0L, vm.uiState.value.slotBalance)
        assertTrue(vm.uiState.value.isSweepDisabled)
    }

    @Test
    fun `loadHistory sets error when transactions fail`() = runTest {
        val vm = createViewModel(
            balanceResult = Result.success(10_000L),
            transactionsResult = Result.failure(RuntimeException("Timeout"))
        )

        assertEquals("Timeout", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadHistory updates balance even if transactions fail`() = runTest {
        val vm = createViewModel(
            balanceResult = Result.success(5_000L),
            transactionsResult = Result.failure(RuntimeException("Error"))
        )

        assertEquals(5_000L, vm.uiState.value.slotBalance)
        assertFalse(vm.uiState.value.isSweepDisabled)
    }

    @Test
    fun `load failure sets error message`() = runTest {
        coEvery { loadCards() } returns Result.failure(RuntimeException("Storage error"))
        val savedStateHandle = SavedStateHandle(
            mapOf("cardIdentifier" to "ident_1", "slotNumber" to 0)
        )
        val vm = SlotHistoryViewModel(loadCards, getTransactions, getBalance, savedStateHandle)

        assertEquals("Storage error", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }
}
