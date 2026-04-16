package com.satsbuddy.presentation.cardlist

import com.satsbuddy.data.nfc.NfcSessionManager
import com.satsbuddy.domain.model.Price
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.UserPreferencesRepository
import com.satsbuddy.domain.usecase.GetPriceUseCase
import com.satsbuddy.domain.usecase.LoadCardsUseCase
import com.satsbuddy.domain.usecase.ReadCardInfoUseCase
import com.satsbuddy.domain.usecase.SaveCardsUseCase
import com.satsbuddy.domain.usecase.UpsertCardUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
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
class CardListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val readCardInfo = mockk<ReadCardInfoUseCase>()
    private val loadCards = mockk<LoadCardsUseCase>()
    private val saveCards = mockk<SaveCardsUseCase>(relaxed = true)
    private val upsertCard = UpsertCardUseCase()
    private val getPrice = mockk<GetPriceUseCase>()
    private val nfcSessionManager = mockk<NfcSessionManager>()
    private val userPreferences = mockk<UserPreferencesRepository>(relaxed = true)

    private val tagFlow = MutableSharedFlow<android.nfc.Tag>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { nfcSessionManager.tagFlow } returns tagFlow
        every { userPreferences.swipeToDeleteTipDismissed } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        cards: List<SatsCardInfo> = emptyList(),
        price: Price? = null
    ): CardListViewModel {
        coEvery { loadCards() } returns Result.success(cards)
        coEvery { getPrice() } returns if (price != null) Result.success(price) else Result.failure(RuntimeException("No price"))
        return CardListViewModel(readCardInfo, loadCards, saveCards, upsertCard, getPrice, nfcSessionManager, userPreferences)
    }

    // region init

    @Test
    fun `init loads persisted cards`() = runTest {
        val cards = listOf(SatsCardInfo(pubkey = "pk1", version = "1.0"))
        val vm = createViewModel(cards = cards)

        assertEquals(1, vm.uiState.value.cards.size)
        assertEquals("pk1", vm.uiState.value.cards[0].pubkey)
    }

    @Test
    fun `init loads price on success`() = runTest {
        val price = Price(usd = 43000.0)
        val vm = createViewModel(price = price)

        assertEquals(43000.0, vm.uiState.value.price?.usd ?: 0.0, 0.01)
    }

    @Test
    fun `init shows swipe tip when not dismissed`() = runTest {
        val vm = createViewModel()

        assertTrue(vm.uiState.value.showSwipeToDeleteTip)
    }

    @Test
    fun `init hides swipe tip when dismissed`() = runTest {
        every { userPreferences.swipeToDeleteTipDismissed } returns flowOf(true)
        val vm = createViewModel()

        assertFalse(vm.uiState.value.showSwipeToDeleteTip)
    }

    // endregion

    // region beginScan / cancelScan

    @Test
    fun `beginScan sets scanning state`() = runTest {
        val vm = createViewModel()

        vm.beginScan()

        assertTrue(vm.uiState.value.isScanning)
        assertEquals("Hold phone near SATSCARD", vm.uiState.value.statusMessage)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `cancelScan resets scanning state`() = runTest {
        val vm = createViewModel()
        vm.beginScan()

        vm.cancelScan()

        assertFalse(vm.uiState.value.isScanning)
        assertEquals("", vm.uiState.value.statusMessage)
    }

    // endregion

    // region NFC tag handling

    @Test
    fun `tag received while scanning reads card and updates list`() = runTest {
        val vm = createViewModel()
        val tag = mockk<android.nfc.Tag>(relaxed = true)
        val cardInfo = SatsCardInfo(pubkey = "pk_scanned", version = "1.0")
        coEvery { readCardInfo(tag) } returns Result.success(cardInfo)

        vm.beginScan()
        tagFlow.emit(tag)

        assertFalse(vm.uiState.value.isScanning)
        assertEquals(1, vm.uiState.value.cards.size)
        assertEquals("pk_scanned", vm.uiState.value.cards[0].pubkey)
        assertNull(vm.uiState.value.errorMessage)
        coVerify { saveCards(any()) }
    }

    @Test
    fun `tag received while not scanning is ignored`() = runTest {
        val vm = createViewModel()
        val tag = mockk<android.nfc.Tag>(relaxed = true)

        // Not scanning — tag should be ignored
        tagFlow.emit(tag)

        assertTrue(vm.uiState.value.cards.isEmpty())
    }

    @Test
    fun `tag read failure sets error message`() = runTest {
        val vm = createViewModel()
        val tag = mockk<android.nfc.Tag>(relaxed = true)
        coEvery { readCardInfo(tag) } returns Result.failure(RuntimeException("NFC lost"))

        vm.beginScan()
        tagFlow.emit(tag)

        assertFalse(vm.uiState.value.isScanning)
        assertEquals("NFC lost", vm.uiState.value.errorMessage)
    }

    // endregion

    // region removeCard / deletion flow

    @Test
    fun `requestCardDeletion sets cardPendingDeletion`() = runTest {
        val card = SatsCardInfo(pubkey = "pk1", version = "1.0")
        val vm = createViewModel(cards = listOf(card))

        vm.requestCardDeletion(card)

        assertEquals(card, vm.uiState.value.cardPendingDeletion)
    }

    @Test
    fun `cancelCardDeletion clears cardPendingDeletion`() = runTest {
        val card = SatsCardInfo(pubkey = "pk1", version = "1.0")
        val vm = createViewModel(cards = listOf(card))
        vm.requestCardDeletion(card)

        vm.cancelCardDeletion()

        assertNull(vm.uiState.value.cardPendingDeletion)
    }

    @Test
    fun `confirmCardDeletion removes card and saves`() = runTest {
        val card1 = SatsCardInfo(pubkey = "pk1", version = "1.0")
        val card2 = SatsCardInfo(pubkey = "pk2", version = "1.0")
        val vm = createViewModel(cards = listOf(card1, card2))
        vm.requestCardDeletion(card1)

        vm.confirmCardDeletion()

        assertEquals(1, vm.uiState.value.cards.size)
        assertEquals("pk2", vm.uiState.value.cards[0].pubkey)
        assertNull(vm.uiState.value.cardPendingDeletion)
        coVerify { saveCards(match { it.size == 1 }) }
    }

    // endregion

    // region updateLabel

    @Test
    fun `updateLabel changes card label and saves`() = runTest {
        val card = SatsCardInfo(pubkey = "pk1", version = "1.0", label = null)
        val vm = createViewModel(cards = listOf(card))

        vm.updateLabel(card, "My Card")

        assertEquals("My Card", vm.uiState.value.cards[0].label)
        coVerify { saveCards(match { it[0].label == "My Card" }) }
    }

    @Test
    fun `updateLabel with blank string sets null label`() = runTest {
        val card = SatsCardInfo(pubkey = "pk1", version = "1.0", label = "Old")
        val vm = createViewModel(cards = listOf(card))

        vm.updateLabel(card, "  ")

        assertNull(vm.uiState.value.cards[0].label)
    }

    // endregion

    // region dismissSwipeToDeleteTip

    @Test
    fun `dismissSwipeToDeleteTip hides tip and persists`() = runTest {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.showSwipeToDeleteTip)

        vm.dismissSwipeToDeleteTip()

        assertFalse(vm.uiState.value.showSwipeToDeleteTip)
        coVerify { userPreferences.setSwipeToDeleteTipDismissed(true) }
    }

    // endregion
}
