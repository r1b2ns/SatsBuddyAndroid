package com.satsbuddy.presentation.slothistory

import androidx.lifecycle.SavedStateHandle
import com.satsbuddy.domain.model.SlotTransaction
import com.satsbuddy.domain.usecase.GetBalanceUseCase
import com.satsbuddy.domain.usecase.GetTransactionsUseCase
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
    private val getTransactions = mockk<GetTransactionsUseCase>()
    private val getBalance = mockk<GetBalanceUseCase>()

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
        slotNumber: Int = 0
    ): SlotHistoryViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf("cardIdentifier" to cardIdentifier, "slotNumber" to slotNumber)
        )
        return SlotHistoryViewModel(getTransactions, getBalance, savedStateHandle)
    }

    @Test
    fun `savedStateHandle params are read correctly`() {
        val vm = createViewModel("my_card", 3)
        assertEquals("my_card", vm.cardIdentifier)
        assertEquals(3, vm.slotNumber)
    }

    @Test
    fun `loadHistory fetches balance and transactions`() = runTest {
        coEvery { getBalance("bc1qslot") } returns Result.success(25_000L)
        coEvery { getTransactions("bc1qslot") } returns Result.success(
            listOf(
                SlotTransaction("tx1", 25_000, 500, 1700000000, true, SlotTransaction.Direction.INCOMING)
            )
        )

        val vm = createViewModel()
        vm.loadHistory("bc1qslot")

        assertEquals(25_000L, vm.uiState.value.slotBalance)
        assertFalse(vm.uiState.value.isSweepDisabled)
        assertEquals(1, vm.uiState.value.transactions.size)
        assertEquals("tx1", vm.uiState.value.transactions[0].txid)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `loadHistory disables sweep when balance is zero`() = runTest {
        coEvery { getBalance("bc1qempty") } returns Result.success(0L)
        coEvery { getTransactions("bc1qempty") } returns Result.success(emptyList())

        val vm = createViewModel()
        vm.loadHistory("bc1qempty")

        assertEquals(0L, vm.uiState.value.slotBalance)
        assertTrue(vm.uiState.value.isSweepDisabled)
    }

    @Test
    fun `loadHistory sets error when transactions fail`() = runTest {
        coEvery { getBalance("bc1qtest") } returns Result.success(10_000L)
        coEvery { getTransactions("bc1qtest") } returns Result.failure(RuntimeException("Timeout"))

        val vm = createViewModel()
        vm.loadHistory("bc1qtest")

        assertEquals("Timeout", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadHistory updates balance even if transactions fail`() = runTest {
        coEvery { getBalance("bc1qtest") } returns Result.success(5_000L)
        coEvery { getTransactions("bc1qtest") } returns Result.failure(RuntimeException("Error"))

        val vm = createViewModel()
        vm.loadHistory("bc1qtest")

        assertEquals(5_000L, vm.uiState.value.slotBalance)
        assertFalse(vm.uiState.value.isSweepDisabled)
    }
}
