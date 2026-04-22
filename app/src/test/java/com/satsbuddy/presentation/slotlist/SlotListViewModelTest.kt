package com.satsbuddy.presentation.slotlist

import androidx.lifecycle.SavedStateHandle
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.model.SlotInfo
import com.satsbuddy.domain.usecase.GetBalanceUseCase
import com.satsbuddy.domain.usecase.LoadCardsUseCase
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SlotListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val loadCards = mockk<LoadCardsUseCase>()
    private val getBalance = mockk<GetBalanceUseCase>()

    private val testCard = SatsCardInfo(
        pubkey = "pk1",
        cardIdent = "ident_1",
        address = "bc1qactive",
        label = "My Card",
        slots = listOf(
            SlotInfo(slotNumber = 0, isActive = false, isUsed = true, address = "bc1qold"),
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
        cards: List<SatsCardInfo> = listOf(testCard)
    ): SlotListViewModel {
        coEvery { loadCards() } returns Result.success(cards)
        coEvery { getBalance(any()) } returns Result.success(0L)
        val savedStateHandle = SavedStateHandle(mapOf("cardIdentifier" to cardIdentifier))
        return SlotListViewModel(loadCards, getBalance, savedStateHandle)
    }

    @Test
    fun `cardIdentifier is read from SavedStateHandle`() {
        val vm = createViewModel("my_card_id", cards = emptyList())
        assertEquals("my_card_id", vm.cardIdentifier)
    }

    @Test
    fun `cardIdentifier defaults to empty when not in SavedStateHandle`() {
        coEvery { loadCards() } returns Result.success(emptyList())
        val vm = SlotListViewModel(loadCards, getBalance, SavedStateHandle())
        assertEquals("", vm.cardIdentifier)
    }

    @Test
    fun `init populates slots and displayName for matching card`() = runTest {
        val vm = createViewModel()

        assertEquals("My Card", vm.uiState.value.displayName)
        assertEquals(3, vm.uiState.value.slots.size)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `init leaves slots empty when card not found`() = runTest {
        val vm = createViewModel(cardIdentifier = "missing", cards = listOf(testCard))

        assertEquals("", vm.uiState.value.displayName)
        assertTrue(vm.uiState.value.slots.isEmpty())
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `init leaves slots empty when no cards exist`() = runTest {
        val vm = createViewModel(cards = emptyList())

        assertTrue(vm.uiState.value.slots.isEmpty())
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `init sets errorMessage when load fails`() = runTest {
        coEvery { loadCards() } returns Result.failure(RuntimeException("Storage error"))
        val savedStateHandle = SavedStateHandle(mapOf("cardIdentifier" to "ident_1"))

        val vm = SlotListViewModel(loadCards, getBalance, savedStateHandle)

        assertEquals("Storage error", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.slots.isEmpty())
    }

    @Test
    fun `init selects the correct card when multiple are stored`() = runTest {
        val otherCard = SatsCardInfo(
            pubkey = "pk2",
            cardIdent = "ident_2",
            slots = listOf(SlotInfo(slotNumber = 0, isActive = true, isUsed = false))
        )

        val vm = createViewModel(
            cardIdentifier = "ident_1",
            cards = listOf(otherCard, testCard)
        )

        assertEquals(3, vm.uiState.value.slots.size)
        assertNotNull(vm.uiState.value.slots.firstOrNull { it.slotNumber == 1 && it.isActive })
    }

    @Test
    fun `init fetches balance for sealed and unsealed slots with address`() = runTest {
        coEvery { loadCards() } returns Result.success(listOf(testCard))
        coEvery { getBalance("bc1qold") } returns Result.success(5_000L)
        coEvery { getBalance("bc1qactive") } returns Result.success(42_000L)
        val savedStateHandle = SavedStateHandle(mapOf("cardIdentifier" to "ident_1"))

        val vm = SlotListViewModel(loadCards, getBalance, savedStateHandle)

        coVerify { getBalance("bc1qold") }
        coVerify { getBalance("bc1qactive") }
        assertEquals(5_000L, vm.uiState.value.slots[0].balance)
        assertEquals(42_000L, vm.uiState.value.slots[1].balance)
    }

    @Test
    fun `init does not fetch balance for unused slots`() = runTest {
        val vm = createViewModel()

        coVerify(exactly = 0) { getBalance("") }
        assertNull(vm.uiState.value.slots[2].balance)
    }

    @Test
    fun `init skips balance fetch when slot has no address`() = runTest {
        val card = SatsCardInfo(
            pubkey = "pk1",
            cardIdent = "ident_1",
            slots = listOf(
                SlotInfo(slotNumber = 0, isActive = false, isUsed = true, address = null),
                SlotInfo(slotNumber = 1, isActive = true, isUsed = false, address = null)
            )
        )

        val vm = createViewModel(cards = listOf(card))

        coVerify(exactly = 0) { getBalance(any()) }
        assertNull(vm.uiState.value.slots[0].balance)
        assertNull(vm.uiState.value.slots[1].balance)
    }

    @Test
    fun `balance failure on one slot does not block others`() = runTest {
        coEvery { loadCards() } returns Result.success(listOf(testCard))
        coEvery { getBalance("bc1qold") } returns Result.failure(RuntimeException("boom"))
        coEvery { getBalance("bc1qactive") } returns Result.success(10_000L)
        val savedStateHandle = SavedStateHandle(mapOf("cardIdentifier" to "ident_1"))

        val vm = SlotListViewModel(loadCards, getBalance, savedStateHandle)

        assertNull(vm.uiState.value.slots[0].balance)
        assertEquals(10_000L, vm.uiState.value.slots[1].balance)
    }
}
