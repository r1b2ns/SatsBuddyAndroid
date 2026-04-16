package com.satsbuddy.presentation.carddetail

import androidx.lifecycle.SavedStateHandle
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.model.SlotInfo
import com.satsbuddy.domain.usecase.GetBalanceUseCase
import com.satsbuddy.domain.usecase.LoadCardsUseCase
import com.satsbuddy.domain.usecase.SaveCardsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
    private val loadCards = mockk<LoadCardsUseCase>()
    private val saveCards = mockk<SaveCardsUseCase>(relaxed = true)

    private val testCard = SatsCardInfo(
        pubkey = "pk1",
        cardIdent = "ident_1",
        address = "bc1qactive",
        label = "My Card",
        slots = listOf(
            SlotInfo(slotNumber = 0, isActive = false, isUsed = true, pubkey = "old_pk"),
            SlotInfo(slotNumber = 1, isActive = true, isUsed = false, address = "bc1qactive"),
            SlotInfo(slotNumber = 2, isActive = false, isUsed = false)
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
        cards: List<SatsCardInfo> = listOf(testCard),
        balanceResult: Result<Long> = Result.success(50_000L)
    ): CardDetailViewModel {
        coEvery { loadCards() } returns Result.success(cards)
        coEvery { getBalance(any()) } returns balanceResult
        val savedStateHandle = SavedStateHandle(mapOf("cardIdentifier" to cardIdentifier))
        return CardDetailViewModel(getBalance, loadCards, saveCards, savedStateHandle)
    }

    @Test
    fun `cardIdentifier is read from SavedStateHandle`() {
        val vm = createViewModel("my_card_id", cards = emptyList())
        assertEquals("my_card_id", vm.cardIdentifier)
    }

    @Test
    fun `cardIdentifier defaults to empty when not in SavedStateHandle`() {
        coEvery { loadCards() } returns Result.success(emptyList())
        val vm = CardDetailViewModel(getBalance, loadCards, saveCards, SavedStateHandle())
        assertEquals("", vm.cardIdentifier)
    }

    @Test
    fun `init loads card and populates displayName and slots`() = runTest {
        val vm = createViewModel()

        assertEquals("My Card", vm.uiState.value.displayName)
        assertEquals("My Card", vm.uiState.value.label)
        assertEquals(3, vm.uiState.value.slots.size)
    }

    @Test
    fun `init fetches balance for active slot`() = runTest {
        val vm = createViewModel(balanceResult = Result.success(50_000L))

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(50_000L, vm.uiState.value.slots[1].balance)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `loadSlotDetails sets error on balance failure`() = runTest {
        val vm = createViewModel(balanceResult = Result.failure(RuntimeException("Network error")))

        assertEquals("Network error", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadSlotDetails uses card address when no active slot has address`() = runTest {
        val card = SatsCardInfo(
            pubkey = "pk1",
            cardIdent = "ident_1",
            address = "bc1qcard",
            slots = listOf(
                SlotInfo(slotNumber = 0, isActive = true, isUsed = false, address = null)
            )
        )
        coEvery { getBalance("bc1qcard") } returns Result.success(30_000L)

        val vm = createViewModel(cards = listOf(card), balanceResult = Result.success(30_000L))

        assertEquals(30_000L, vm.uiState.value.slots[0].balance)
    }

    @Test
    fun `loadSlotDetails with no address does not fetch balance`() = runTest {
        val card = SatsCardInfo(
            pubkey = "pk1",
            cardIdent = "ident_1",
            address = null,
            slots = listOf(
                SlotInfo(slotNumber = 0, isActive = true, isUsed = false, address = null)
            )
        )
        val vm = createViewModel(cards = listOf(card))

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.slots[0].balance)
    }

    @Test
    fun `init with card not found does not crash`() = runTest {
        val vm = createViewModel(cardIdentifier = "nonexistent", cards = listOf(testCard))

        assertEquals("", vm.uiState.value.displayName)
        assertTrue(vm.uiState.value.slots.isEmpty())
    }

    // region updateLabel

    @Test
    fun `updateLabel updates displayName and persists`() = runTest {
        val vm = createViewModel()

        vm.updateLabel("New Name")

        assertEquals("New Name", vm.uiState.value.displayName)
        assertEquals("New Name", vm.uiState.value.label)
        coVerify { saveCards(match { cards -> cards.any { it.label == "New Name" } }) }
    }

    @Test
    fun `updateLabel with blank sets null label and falls back to pubkey`() = runTest {
        val vm = createViewModel()

        vm.updateLabel("   ")

        assertNull(vm.uiState.value.label)
        assertEquals("pk1", vm.uiState.value.displayName) // falls back to pubkey
    }

    // endregion

    private fun assertTrue(value: Boolean) = org.junit.Assert.assertTrue(value)
}
