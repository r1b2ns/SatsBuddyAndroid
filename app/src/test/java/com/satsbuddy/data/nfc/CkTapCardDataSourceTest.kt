package com.satsbuddy.data.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import org.bitcoindevkit.cktap.CardException
import org.bitcoindevkit.cktap.CkTapCard
import org.bitcoindevkit.cktap.CkTapException
import org.bitcoindevkit.cktap.SatsCard
import org.bitcoindevkit.cktap.SatsCardStatus
import org.bitcoindevkit.cktap.SignPsbtException
import org.bitcoindevkit.cktap.SlotDetails
import org.bitcoindevkit.cktap.UnsealException
import com.satsbuddy.data.bitcoin.BdkDataSource
import com.satsbuddy.domain.model.AppError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CkTapCardDataSourceTest {

    private lateinit var dataSource: CkTapCardDataSource
    private lateinit var tag: Tag
    private lateinit var isoDep: IsoDep
    private lateinit var satsCard: SatsCard
    private lateinit var status: SatsCardStatus
    private lateinit var bdkDataSource: BdkDataSource

    @Before
    fun setUp() {
        mockkStatic(IsoDep::class)
        mockkStatic("org.bitcoindevkit.cktap.Cktap_ffiKt")

        tag = mockk(relaxed = true)
        isoDep = mockk(relaxed = true) {
            every { isConnected } returns true
        }
        satsCard = mockk(relaxed = true)
        status = mockk(relaxed = true)
        bdkDataSource = mockk(relaxed = true)
        coEvery { bdkDataSource.deriveAddress(any(), any()) } returns "bc1qderived"

        every { IsoDep.get(tag) } returns isoDep

        dataSource = CkTapCardDataSource(bdkDataSource)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun stubStatus(
        ver: String = "1.0.0",
        birth: UInt = 800000u,
        addr: String? = "bc1qactive",
        pubkey: String = "pubkey_abc",
        cardIdent: String = "ident_123",
        activeSlot: UByte = 0u,
        numSlots: UByte = 10u,
    ) {
        every { status.ver } returns ver
        every { status.birth } returns birth
        every { status.addr } returns addr
        every { status.pubkey } returns pubkey
        every { status.cardIdent } returns cardIdent
        every { status.activeSlot } returns activeSlot
        every { status.numSlots } returns numSlots
    }

    private fun stubToCktapReturnsSatsCard() {
        val ckTapCard = mockk<CkTapCard.SatsCard>(relaxed = true) {
            every { v1 } returns satsCard
        }
        coEvery { org.bitcoindevkit.cktap.toCktap(any()) } returns ckTapCard
    }

    // region readCard

    @Test
    fun `readCard returns SatsCardInfo with correct fields`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus()
        coEvery { satsCard.status() } returns status
        coEvery { satsCard.address() } returns "bc1qactivefulladdress"

        val result = dataSource.readCard(tag)

        assertEquals("1.0.0", result.version)
        assertEquals(800000L, result.birth)
        assertEquals("bc1qactivefulladdress", result.address)
        assertEquals("pubkey_abc", result.pubkey)
        assertEquals("ident_123", result.cardIdent)
        assertEquals(0, result.activeSlot)
        assertEquals(10, result.totalSlots)
        assertTrue(result.isActive)
    }

    @Test
    fun `readCard sets isActive false when addr is null`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(addr = null)
        coEvery { satsCard.status() } returns status

        val result = dataSource.readCard(tag)

        assertFalse(result.isActive)
        assertNull(result.address)
    }

    @Test
    fun `readCard falls back to null address when address() throws`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus()
        coEvery { satsCard.status() } returns status
        coEvery { satsCard.address() } throws RuntimeException("derive failed")

        val result = dataSource.readCard(tag)

        // isActive remains true because status.addr is present
        assertTrue(result.isActive)
        assertNull(result.address)
        assertNull(result.slots[0].address)
    }

    @Test
    fun `readCard builds slot list with active and used slots`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(activeSlot = 2u, numSlots = 5u)
        coEvery { satsCard.status() } returns status
        coEvery { satsCard.address() } returns "bc1qactivefulladdress"

        // Slot 0 and 1 are used (index < activeSlot), slot 2 is active, 3 and 4 are empty
        val dump0 = SlotDetails("priv0", "pub0", "desc0")
        val dump1 = SlotDetails("priv1", "pub1", "desc1")
        coEvery { satsCard.dump(0u, null) } returns dump0
        coEvery { satsCard.dump(1u, null) } returns dump1

        val result = dataSource.readCard(tag)

        assertEquals(5, result.slots.size)

        // Used slots (0, 1)
        val slot0 = result.slots[0]
        assertEquals(0, slot0.slotNumber)
        assertFalse(slot0.isActive)
        assertTrue(slot0.isUsed)
        assertEquals("pub0", slot0.pubkey)
        assertEquals("desc0", slot0.pubkeyDescriptor)
        assertEquals("bc1qderived", slot0.address)

        val slot1 = result.slots[1]
        assertEquals(1, slot1.slotNumber)
        assertTrue(slot1.isUsed)
        assertEquals("pub1", slot1.pubkey)
        assertEquals("bc1qderived", slot1.address)

        // Active slot (2)
        val slot2 = result.slots[2]
        assertEquals(2, slot2.slotNumber)
        assertTrue(slot2.isActive)
        assertFalse(slot2.isUsed)
        assertEquals("pubkey_abc", slot2.pubkey)
        assertEquals("bc1qactivefulladdress", slot2.address)

        // Empty slots (3, 4)
        val slot3 = result.slots[3]
        assertFalse(slot3.isActive)
        assertFalse(slot3.isUsed)
        assertNull(slot3.pubkey)
        assertNull(slot3.address)
    }

    @Test
    fun `readCard throws TransportError when IsoDep is null`() = runTest {
        every { IsoDep.get(tag) } returns null

        try {
            dataSource.readCard(tag)
            assert(false) { "Expected AppError.TransportError" }
        } catch (e: AppError.TransportError) {
            assertEquals("Card does not support ISO-DEP", e.message)
        }
    }

    @Test
    fun `readCard throws WrongCard for TapSigner`() = runTest {
        val ckTapCard = mockk<CkTapCard.TapSigner>(relaxed = true)
        coEvery { org.bitcoindevkit.cktap.toCktap(any()) } returns ckTapCard

        try {
            dataSource.readCard(tag)
            assert(false) { "Expected AppError.WrongCard" }
        } catch (e: AppError.WrongCard) {
            // expected
        }
    }

    @Test
    fun `readCard throws WrongCard for SatsChip`() = runTest {
        val ckTapCard = mockk<CkTapCard.SatsChip>(relaxed = true)
        coEvery { org.bitcoindevkit.cktap.toCktap(any()) } returns ckTapCard

        try {
            dataSource.readCard(tag)
            assert(false) { "Expected AppError.WrongCard" }
        } catch (e: AppError.WrongCard) {
            // expected
        }
    }

    @Test
    fun `readCard maps CkTapException Transport to AppError TransportError`() = runTest {
        coEvery { org.bitcoindevkit.cktap.toCktap(any()) } throws CkTapException.Transport("NFC lost")

        try {
            dataSource.readCard(tag)
            assert(false) { "Expected AppError.TransportError" }
        } catch (e: AppError.TransportError) {
            assertEquals("NFC lost", e.message)
        }
    }

    @Test
    fun `readCard derives address from descriptor for used slots`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(activeSlot = 1u, numSlots = 2u)
        coEvery { satsCard.status() } returns status
        coEvery { satsCard.address() } returns "bc1qactive"
        coEvery { satsCard.dump(0u, null) } returns SlotDetails("priv0", "pub0", "wpkh(pub0)")
        coEvery { bdkDataSource.deriveAddress("wpkh(pub0)", any()) } returns "bc1qhistorical"

        val result = dataSource.readCard(tag)

        assertEquals("bc1qhistorical", result.slots[0].address)
        coVerify { bdkDataSource.deriveAddress("wpkh(pub0)", any()) }
    }

    @Test
    fun `readCard leaves used slot address null when derive fails`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(activeSlot = 1u, numSlots = 2u)
        coEvery { satsCard.status() } returns status
        coEvery { satsCard.address() } returns "bc1qactive"
        coEvery { satsCard.dump(0u, null) } returns SlotDetails("priv0", "pub0", "bad_desc")
        coEvery { bdkDataSource.deriveAddress("bad_desc", any()) } throws RuntimeException("invalid descriptor")

        val result = dataSource.readCard(tag)

        assertNull(result.slots[0].address)
        assertEquals("pub0", result.slots[0].pubkey)
    }

    @Test
    fun `readCard skips derive when descriptor missing`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(activeSlot = 1u, numSlots = 2u)
        coEvery { satsCard.status() } returns status
        coEvery { satsCard.address() } returns "bc1qactive"
        coEvery { satsCard.dump(0u, null) } throws Exception("dump failed")

        val result = dataSource.readCard(tag)

        assertNull(result.slots[0].address)
        coVerify(exactly = 0) { bdkDataSource.deriveAddress(any(), any()) }
    }

    @Test
    fun `readCard handles dump failure gracefully for used slots`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(activeSlot = 1u, numSlots = 3u)
        coEvery { satsCard.status() } returns status
        coEvery { satsCard.dump(0u, null) } throws Exception("dump failed")

        val result = dataSource.readCard(tag)

        val slot0 = result.slots[0]
        assertTrue(slot0.isUsed)
        assertNull(slot0.pubkey) // dump failed, so pubkey is null
    }

    // endregion

    // region setupNextSlot

    @Test
    fun `setupNextSlot throws WrongCard when cardIdent does not match`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(cardIdent = "actual_id")
        coEvery { satsCard.status() } returns status

        try {
            dataSource.setupNextSlot(tag, "123456", "expected_id")
            assert(false) { "Expected AppError.WrongCard" }
        } catch (e: AppError.WrongCard) {
            // expected
        }
    }

    @Test
    fun `setupNextSlot throws NoUnusedSlots when only one slot remaining`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(activeSlot = 9u, numSlots = 10u, cardIdent = "id1")
        coEvery { satsCard.status() } returns status

        try {
            dataSource.setupNextSlot(tag, "123456", "id1")
            assert(false) { "Expected AppError.NoUnusedSlots" }
        } catch (e: AppError.NoUnusedSlots) {
            // expected
        }
    }

    @Test
    fun `setupNextSlot calls unseal and newSlot then returns card info`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(activeSlot = 2u, numSlots = 10u, cardIdent = "id1")
        coEvery { satsCard.status() } returns status
        coEvery { satsCard.unseal("123456") } returns mockk(relaxed = true)
        coEvery { satsCard.newSlot("123456") } returns mockk(relaxed = true)

        val result = dataSource.setupNextSlot(tag, "123456", "id1")

        coVerify { satsCard.unseal("123456") }
        coVerify { satsCard.newSlot("123456") }
        assertEquals("id1", result.cardIdent)
    }

    @Test
    fun `setupNextSlot skips card identity check when expectedId is empty`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(activeSlot = 0u, numSlots = 10u, cardIdent = "anything")
        coEvery { satsCard.status() } returns status
        coEvery { satsCard.unseal("000000") } returns mockk(relaxed = true)
        coEvery { satsCard.newSlot("000000") } returns mockk(relaxed = true)

        // Should NOT throw WrongCard
        val result = dataSource.setupNextSlot(tag, "000000", "")

        assertEquals("anything", result.cardIdent)
    }

    @Test
    fun `setupNextSlot maps UnsealException BadAuth to IncorrectCvc`() = runTest {
        stubToCktapReturnsSatsCard()
        stubStatus(activeSlot = 0u, numSlots = 10u, cardIdent = "id1")
        coEvery { satsCard.status() } returns status

        val cardError = mockk<CardException.BadAuth>(relaxed = true)
        val ckTapError = CkTapException.Card(cardError)
        coEvery { satsCard.unseal("wrong") } throws UnsealException.CkTap(ckTapError)

        try {
            dataSource.setupNextSlot(tag, "wrong", "id1")
            assert(false) { "Expected AppError.IncorrectCvc" }
        } catch (e: AppError.IncorrectCvc) {
            // expected
        }
    }

    // endregion

    // region signPsbt

    @Test
    fun `signPsbt returns signed PSBT string`() = runTest {
        stubToCktapReturnsSatsCard()
        val signedPsbt = "signed_psbt_hex"
        coEvery { satsCard.signPsbt(0u, "raw_psbt", "123456") } returns signedPsbt

        val result = dataSource.signPsbt(tag, 0, "raw_psbt", "123456")

        assertEquals(signedPsbt, result)
    }

    @Test
    fun `signPsbt maps SignPsbtException BadAuth to IncorrectCvc`() = runTest {
        stubToCktapReturnsSatsCard()

        val cardError = mockk<CardException.BadAuth>(relaxed = true)
        val ckTapError = CkTapException.Card(cardError)
        coEvery { satsCard.signPsbt(0u, "psbt", "wrong") } throws SignPsbtException.CkTap(ckTapError)

        try {
            dataSource.signPsbt(tag, 0, "psbt", "wrong")
            assert(false) { "Expected AppError.IncorrectCvc" }
        } catch (e: AppError.IncorrectCvc) {
            // expected
        }
    }

    // endregion
}
