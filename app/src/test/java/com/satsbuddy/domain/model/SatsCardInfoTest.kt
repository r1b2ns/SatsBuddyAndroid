package com.satsbuddy.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SatsCardInfoTest {

    // region displayName

    @Test
    fun `displayName returns trimmed label when present`() {
        val card = SatsCardInfo(label = "  My Card  ", pubkey = "pk1")
        assertEquals("My Card", card.displayName)
    }

    @Test
    fun `displayName returns pubkey when label is null`() {
        val card = SatsCardInfo(label = null, pubkey = "pk_abc123")
        assertEquals("pk_abc123", card.displayName)
    }

    @Test
    fun `displayName returns pubkey when label is blank`() {
        val card = SatsCardInfo(label = "   ", pubkey = "pk_def456")
        assertEquals("pk_def456", card.displayName)
    }

    @Test
    fun `displayName returns pubkey when label is empty`() {
        val card = SatsCardInfo(label = "", pubkey = "pk_ghi789")
        assertEquals("pk_ghi789", card.displayName)
    }

    @Test
    fun `displayName returns address when label and pubkey are empty`() {
        val card = SatsCardInfo(label = null, pubkey = "", address = "bc1qxyz")
        assertEquals("bc1qxyz", card.displayName)
    }

    @Test
    fun `displayName returns SATSCARD when all identifiers are empty`() {
        val card = SatsCardInfo(label = null, pubkey = "", address = null)
        assertEquals("SATSCARD", card.displayName)
    }

    @Test
    fun `displayName returns SATSCARD when address is empty string`() {
        val card = SatsCardInfo(label = null, pubkey = "", address = "")
        assertEquals("SATSCARD", card.displayName)
    }

    // endregion

    // region cardIdentifier

    @Test
    fun `cardIdentifier returns cardIdent when present and not empty`() {
        val card = SatsCardInfo(cardIdent = "ident_abc", pubkey = "pk_123")
        assertEquals("ident_abc", card.cardIdentifier)
    }

    @Test
    fun `cardIdentifier returns pubkey when cardIdent is null`() {
        val card = SatsCardInfo(cardIdent = null, pubkey = "pk_fallback")
        assertEquals("pk_fallback", card.cardIdentifier)
    }

    @Test
    fun `cardIdentifier returns pubkey when cardIdent is empty`() {
        val card = SatsCardInfo(cardIdent = "", pubkey = "pk_fallback2")
        assertEquals("pk_fallback2", card.cardIdentifier)
    }

    // endregion

    // region displayActiveSlotNumber

    @Test
    fun `displayActiveSlotNumber returns slot plus 1`() {
        val card = SatsCardInfo(activeSlot = 0)
        assertEquals(1, card.displayActiveSlotNumber)
    }

    @Test
    fun `displayActiveSlotNumber returns correct for higher slots`() {
        val card = SatsCardInfo(activeSlot = 5)
        assertEquals(6, card.displayActiveSlotNumber)
    }

    @Test
    fun `displayActiveSlotNumber returns null when activeSlot is null`() {
        val card = SatsCardInfo(activeSlot = null)
        assertNull(card.displayActiveSlotNumber)
    }

    // endregion

    // region activeSlotInfo

    @Test
    fun `activeSlotInfo returns first active slot`() {
        val slots = listOf(
            SlotInfo(slotNumber = 0, isActive = false, isUsed = true),
            SlotInfo(slotNumber = 1, isActive = true, isUsed = false),
            SlotInfo(slotNumber = 2, isActive = false, isUsed = false)
        )
        val card = SatsCardInfo(slots = slots)

        assertEquals(1, card.activeSlotInfo?.slotNumber)
    }

    @Test
    fun `activeSlotInfo returns null when no active slot`() {
        val slots = listOf(
            SlotInfo(slotNumber = 0, isActive = false, isUsed = true),
            SlotInfo(slotNumber = 1, isActive = false, isUsed = false)
        )
        val card = SatsCardInfo(slots = slots)

        assertNull(card.activeSlotInfo)
    }

    @Test
    fun `activeSlotInfo returns null when slots is empty`() {
        val card = SatsCardInfo(slots = emptyList())
        assertNull(card.activeSlotInfo)
    }

    // endregion
}
