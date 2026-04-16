package com.satsbuddy.presentation.send.sign

import android.nfc.Tag
import com.satsbuddy.data.nfc.NfcSessionManager
import com.satsbuddy.domain.usecase.BuildPsbtUseCase
import com.satsbuddy.domain.usecase.SignAndBroadcastUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
class SendSignViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val buildPsbt = mockk<BuildPsbtUseCase>()
    private val signAndBroadcast = mockk<SignAndBroadcastUseCase>()
    private val nfcSessionManager = mockk<NfcSessionManager>()
    private val tagFlow = MutableSharedFlow<Tag>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { nfcSessionManager.tagFlow } returns tagFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SendSignViewModel(buildPsbt, signAndBroadcast, nfcSessionManager)

    // region initial state

    @Test
    fun `initial state is Idle`() {
        val vm = createViewModel()

        assertEquals(SendSignState.Idle, vm.uiState.value.state)
        assertEquals("", vm.uiState.value.cvc)
        assertNull(vm.uiState.value.signedTxid)
        assertFalse(vm.uiState.value.isBusy)
        assertFalse(vm.uiState.value.canSign)
    }

    // endregion

    // region updateCvc

    @Test
    fun `updateCvc updates cvc in state`() {
        val vm = createViewModel()

        vm.updateCvc("123456")

        assertEquals("123456", vm.uiState.value.cvc)
    }

    // endregion

    // region preparePsbt

    @Test
    fun `preparePsbt on success transitions to Ready`() = runTest {
        coEvery { buildPsbt("desc", "bc1qdest", 10) } returns Result.success("psbt_data")

        val vm = createViewModel()
        vm.preparePsbt("desc", "bc1qdest", 10)

        assertEquals(SendSignState.Ready, vm.uiState.value.state)
        assertEquals("Enter CVC and tap card to sign", vm.uiState.value.statusMessage)
    }

    @Test
    fun `preparePsbt on failure transitions to Error`() = runTest {
        coEvery { buildPsbt("desc", "bc1qdest", 10) } returns Result.failure(RuntimeException("Build failed"))

        val vm = createViewModel()
        vm.preparePsbt("desc", "bc1qdest", 10)

        assertTrue(vm.uiState.value.state is SendSignState.Error)
        assertEquals("Build failed", (vm.uiState.value.state as SendSignState.Error).message)
    }

    // endregion

    // region startSign

    @Test
    fun `startSign does nothing when psbt is null`() = runTest {
        val vm = createViewModel()
        vm.updateCvc("123456")

        vm.startSign(0)

        // Should remain in Idle since preparePsbt was never called
        assertEquals(SendSignState.Idle, vm.uiState.value.state)
    }

    @Test
    fun `startSign does nothing when cvc is blank`() = runTest {
        coEvery { buildPsbt("desc", "bc1qdest", 10) } returns Result.success("psbt_data")

        val vm = createViewModel()
        vm.preparePsbt("desc", "bc1qdest", 10)
        // cvc is empty

        vm.startSign(0)

        // Should remain in Ready since cvc is blank
        assertEquals(SendSignState.Ready, vm.uiState.value.state)
    }

    @Test
    fun `startSign transitions to Tapping and then Done on success`() = runTest {
        val tag = mockk<Tag>(relaxed = true)
        coEvery { buildPsbt("desc", "bc1qdest", 10) } returns Result.success("psbt_data")
        coEvery { signAndBroadcast(tag, 0, "psbt_data", "123456") } returns Result.success("txid_abc")

        val vm = createViewModel()
        vm.preparePsbt("desc", "bc1qdest", 10)
        vm.updateCvc("123456")
        vm.startSign(0)

        // Emit NFC tag
        tagFlow.emit(tag)

        assertEquals(SendSignState.Done, vm.uiState.value.state)
        assertEquals("txid_abc", vm.uiState.value.signedTxid)
        assertEquals("Transaction broadcast!", vm.uiState.value.statusMessage)
    }

    @Test
    fun `startSign transitions to Error on signAndBroadcast failure`() = runTest {
        val tag = mockk<Tag>(relaxed = true)
        coEvery { buildPsbt("desc", "bc1qdest", 10) } returns Result.success("psbt_data")
        coEvery { signAndBroadcast(tag, 0, "psbt_data", "123456") } returns
                Result.failure(RuntimeException("Sign failed"))

        val vm = createViewModel()
        vm.preparePsbt("desc", "bc1qdest", 10)
        vm.updateCvc("123456")
        vm.startSign(0)

        tagFlow.emit(tag)

        assertTrue(vm.uiState.value.state is SendSignState.Error)
        assertEquals("Sign failed", (vm.uiState.value.state as SendSignState.Error).message)
    }

    // endregion

    // region SendSignUiState computed properties

    @Test
    fun `isBusy is true during PreparingPsbt`() {
        val state = SendSignUiState(state = SendSignState.PreparingPsbt)
        assertTrue(state.isBusy)
    }

    @Test
    fun `isBusy is true during Tapping`() {
        val state = SendSignUiState(state = SendSignState.Tapping)
        assertTrue(state.isBusy)
    }

    @Test
    fun `isBusy is false in other states`() {
        assertFalse(SendSignUiState(state = SendSignState.Idle).isBusy)
        assertFalse(SendSignUiState(state = SendSignState.Ready).isBusy)
        assertFalse(SendSignUiState(state = SendSignState.Done).isBusy)
        assertFalse(SendSignUiState(state = SendSignState.Error("err")).isBusy)
    }

    @Test
    fun `canSign is true when Ready and cvc is not blank`() {
        val state = SendSignUiState(state = SendSignState.Ready, cvc = "123456")
        assertTrue(state.canSign)
    }

    @Test
    fun `canSign is false when Ready but cvc is blank`() {
        val state = SendSignUiState(state = SendSignState.Ready, cvc = "")
        assertFalse(state.canSign)
    }

    @Test
    fun `canSign is false when not Ready`() {
        assertFalse(SendSignUiState(state = SendSignState.Idle, cvc = "123").canSign)
        assertFalse(SendSignUiState(state = SendSignState.Tapping, cvc = "123").canSign)
    }

    // endregion
}
