package com.satsbuddy.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SlotInfoTest {

    @Test
    fun `displaySlotNumber returns slotNumber plus 1`() {
        assertEquals(1, SlotInfo(slotNumber = 0, isActive = false, isUsed = false).displaySlotNumber)
        assertEquals(2, SlotInfo(slotNumber = 1, isActive = false, isUsed = false).displaySlotNumber)
        assertEquals(10, SlotInfo(slotNumber = 9, isActive = false, isUsed = false).displaySlotNumber)
    }
}
