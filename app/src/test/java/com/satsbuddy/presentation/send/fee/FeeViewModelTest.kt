package com.satsbuddy.presentation.send.fee

import com.satsbuddy.domain.model.RecommendedFees
import com.satsbuddy.domain.usecase.GetFeesUseCase
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
class FeeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getFees = mockk<GetFeesUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region init / fetchFees

    @Test
    fun `init fetches fees and updates state on success`() = runTest {
        val fees = RecommendedFees(50, 30, 15, 8, 4)
        coEvery { getFees() } returns Result.success(fees)

        val vm = FeeViewModel(getFees)

        assertEquals(fees, vm.uiState.value.recommendedFees)
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isManualFallback)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `init sets manual fallback on failure`() = runTest {
        coEvery { getFees() } returns Result.failure(RuntimeException("Network error"))

        val vm = FeeViewModel(getFees)

        assertNull(vm.uiState.value.recommendedFees)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.isManualFallback)
        assertEquals("Network error", vm.uiState.value.error)
    }

    @Test
    fun `availableFees uses recommended fees when available`() = runTest {
        val fees = RecommendedFees(
            fastestFee = 50,
            halfHourFee = 30,
            hourFee = 15,
            economyFee = 8,
            minimumFee = 4
        )
        coEvery { getFees() } returns Result.success(fees)

        val vm = FeeViewModel(getFees)

        // Order: economy, hour, halfHour, fastest
        assertEquals(listOf(8, 15, 30, 50), vm.uiState.value.availableFees)
    }

    @Test
    fun `availableFees uses manual fallback when fees unavailable`() = runTest {
        coEvery { getFees() } returns Result.failure(RuntimeException("Error"))

        val vm = FeeViewModel(getFees)

        assertEquals(listOf(1, 2, 5, 10), vm.uiState.value.availableFees)
    }

    // endregion

    // region selectIndex

    @Test
    fun `selectIndex updates selected index`() = runTest {
        coEvery { getFees() } returns Result.success(RecommendedFees(50, 30, 15, 8, 4))

        val vm = FeeViewModel(getFees)
        vm.selectIndex(3)

        assertEquals(3, vm.uiState.value.selectedIndex)
    }

    @Test
    fun `selectIndex coerces to valid range`() = runTest {
        coEvery { getFees() } returns Result.success(RecommendedFees(50, 30, 15, 8, 4))

        val vm = FeeViewModel(getFees)

        vm.selectIndex(-1)
        assertEquals(0, vm.uiState.value.selectedIndex)

        vm.selectIndex(99)
        assertEquals(3, vm.uiState.value.selectedIndex)
    }

    @Test
    fun `selectedFee returns fee at selected index`() = runTest {
        val fees = RecommendedFees(fastestFee = 50, halfHourFee = 30, hourFee = 15, economyFee = 8, minimumFee = 4)
        coEvery { getFees() } returns Result.success(fees)

        val vm = FeeViewModel(getFees)

        // Default selectedIndex is 2 → halfHourFee (30)
        assertEquals(30, vm.uiState.value.selectedFee)

        vm.selectIndex(0) // economyFee
        assertEquals(8, vm.uiState.value.selectedFee)

        vm.selectIndex(3) // fastestFee
        assertEquals(50, vm.uiState.value.selectedFee)
    }

    // endregion

    // region FeeUiState computed properties

    @Test
    fun `default selectedIndex is 2`() = runTest {
        coEvery { getFees() } returns Result.success(RecommendedFees(50, 30, 15, 8, 4))

        val vm = FeeViewModel(getFees)

        assertEquals(2, vm.uiState.value.selectedIndex)
    }

    // endregion
}
