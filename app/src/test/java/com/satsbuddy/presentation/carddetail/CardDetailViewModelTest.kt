package com.satsbuddy.presentation.carddetail

import androidx.lifecycle.SavedStateHandle
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.model.SlotInfo
import com.satsbuddy.domain.usecase.GetBalanceUseCase
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getBalance = mockk<GetBalanceUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(cardIdentifier: String = "ident_1"): CardDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("cardIdentifier" to cardIdentifier))
        return CardDetailViewModel(getBalance, savedStateHandle)
    }

    @Test
    fun `cardIdentifier is read from SavedStateHandle`() {
        val vm = createViewModel("my_card_id")
        assertEquals("my_card_id", vm.cardIdentifier)
    }

    @Test
    fun `cardIdentifier defaults to empty when not in SavedStateHandle`() {
        val vm = CardDetailViewModel(getBalance, SavedStateHandle())
        assertEquals("", vm.cardIdentifier)
    }

    @Test
    fun `loadSlotDetails fetches balance for active slot and updates slots`() = runTest {
        coEvery { getBalance("bc1qactive") } returns Result.success(50_000L)

        val vm = createViewModel()
        val card = SatsCardInfo(
            pubkey = "pk1",
            address = "bc1qactive",
            slots = listOf(
                SlotInfo(slotNumber = 0, isActive = false, isUsed = true, pubkey = "old_pk"),
                SlotInfo(slotNumber = 1, isActive = true, isUsed = false, address = "bc1qactive"),
                SlotInfo(slotNumber = 2, isActive = false, isUsed = false)
            )
        )

        vm.loadSlotDetails(card)

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(3, vm.uiState.value.slots.size)
        assertEquals(50_000L, vm.uiState.value.slots[1].balance)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `loadSlotDetails uses card address when no active slot has address`() = runTest {
        coEvery { getBalance("bc1qcard") } returns Result.success(30_000L)

        val vm = createViewModel()
        val card = SatsCardInfo(
            pubkey = "pk1",
            address = "bc1qcard",
            slots = listOf(
                SlotInfo(slotNumber = 0, isActive = true, isUsed = false, address = null)
            )
        )

        vm.loadSlotDetails(card)

        assertEquals(30_000L, vm.uiState.value.slots[0].balance)
    }

    @Test
    fun `loadSlotDetails sets error on balance failure`() = runTest {
        coEvery { getBalance(any()) } returns Result.failure(RuntimeException("Network error"))

        val vm = createViewModel()
        val card = SatsCardInfo(
            pubkey = "pk1",
            address = "bc1qtest",
            slots = listOf(
                SlotInfo(slotNumber = 0, isActive = true, isUsed = false, address = "bc1qtest")
            )
        )

        vm.loadSlotDetails(card)

        assertEquals("Network error", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadSlotDetails with no address does not fetch balance`() = runTest {
        val vm = createViewModel()
        val card = SatsCardInfo(
            pubkey = "pk1",
            address = null,
            slots = listOf(
                SlotInfo(slotNumber = 0, isActive = true, isUsed = false, address = null)
            )
        )

        vm.loadSlotDetails(card)

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.slots[0].balance)
    }
}
