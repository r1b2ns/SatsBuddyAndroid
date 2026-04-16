package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.SatsCardInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class UpsertCardUseCaseTest {

    private val useCase = UpsertCardUseCase()

    @Test
    fun `inserts new card at beginning when not found`() {
        val existing = listOf(
            SatsCardInfo(pubkey = "pk1", version = "1.0"),
            SatsCardInfo(pubkey = "pk2", version = "1.0")
        )
        val newCard = SatsCardInfo(pubkey = "pk3", version = "1.0")

        val (updated, index) = useCase(existing, newCard)

        assertEquals(3, updated.size)
        assertEquals(0, index)
        assertEquals("pk3", updated[0].pubkey)
        assertEquals("pk1", updated[1].pubkey)
        assertEquals("pk2", updated[2].pubkey)
    }

    @Test
    fun `updates existing card in place by cardIdentifier`() {
        val existing = listOf(
            SatsCardInfo(pubkey = "pk1", version = "1.0", label = "Old Label"),
            SatsCardInfo(pubkey = "pk2", version = "1.0")
        )
        val updatedCard = SatsCardInfo(pubkey = "pk1", version = "2.0", label = "New Label")

        val (updated, index) = useCase(existing, updatedCard)

        assertEquals(2, updated.size)
        assertEquals(0, index)
        assertEquals("New Label", updated[0].label)
        assertEquals("2.0", updated[0].version)
        assertEquals("pk2", updated[1].pubkey)
    }

    @Test
    fun `updates card at correct index when not first`() {
        val existing = listOf(
            SatsCardInfo(pubkey = "pk1", version = "1.0"),
            SatsCardInfo(pubkey = "pk2", version = "1.0", label = "Old"),
            SatsCardInfo(pubkey = "pk3", version = "1.0")
        )
        val updatedCard = SatsCardInfo(pubkey = "pk2", version = "1.0", label = "Updated")

        val (updated, index) = useCase(existing, updatedCard)

        assertEquals(3, updated.size)
        assertEquals(1, index)
        assertEquals("Updated", updated[1].label)
    }

    @Test
    fun `inserts into empty list`() {
        val newCard = SatsCardInfo(pubkey = "pk_new", version = "1.0")

        val (updated, index) = useCase(emptyList(), newCard)

        assertEquals(1, updated.size)
        assertEquals(0, index)
        assertEquals("pk_new", updated[0].pubkey)
    }

    @Test
    fun `matches by cardIdent when available`() {
        val existing = listOf(
            SatsCardInfo(pubkey = "pk1", cardIdent = "ident_A", version = "1.0"),
            SatsCardInfo(pubkey = "pk2", cardIdent = "ident_B", version = "1.0")
        )
        // Same cardIdent but different pubkey fields
        val updatedCard = SatsCardInfo(pubkey = "pk1", cardIdent = "ident_A", version = "2.0", label = "Refreshed")

        val (updated, index) = useCase(existing, updatedCard)

        assertEquals(2, updated.size)
        assertEquals(0, index)
        assertEquals("Refreshed", updated[0].label)
    }

    @Test
    fun `does not modify original list`() {
        val existing = listOf(
            SatsCardInfo(pubkey = "pk1", version = "1.0")
        )
        val newCard = SatsCardInfo(pubkey = "pk2", version = "1.0")

        val (updated, _) = useCase(existing, newCard)

        assertEquals(1, existing.size) // original unchanged
        assertEquals(2, updated.size)
    }

    @Test
    fun `matches by pubkey when cardIdent is null`() {
        val existing = listOf(
            SatsCardInfo(pubkey = "pk_match", cardIdent = null, version = "1.0")
        )
        val updatedCard = SatsCardInfo(pubkey = "pk_match", cardIdent = null, version = "2.0")

        val (updated, index) = useCase(existing, updatedCard)

        assertEquals(1, updated.size)
        assertEquals(0, index)
        assertEquals("2.0", updated[0].version)
    }
}
