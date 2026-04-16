package com.satsbuddy.data.local

import com.google.crypto.tink.Aead
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.GeneralSecurityException

class CardsDataSerializerTest {

    private lateinit var serializer: CardsDataSerializer

    private val fakeAead = object : Aead {
        override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray {
            // Simple XOR-based fake encryption for testing
            return plaintext.map { (it.toInt() xor 0x42).toByte() }.toByteArray()
        }

        override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray {
            return ciphertext.map { (it.toInt() xor 0x42).toByte() }.toByteArray()
        }
    }

    @Before
    fun setUp() {
        serializer = CardsDataSerializer(fakeAead)
    }

    @Test
    fun `defaultValue is empty JSON array`() {
        assertEquals("[]", serializer.defaultValue)
    }

    // region writeTo + readFrom round-trip

    @Test
    fun `writeTo then readFrom returns original string`() = runTest {
        val original = """[{"pubkey":"abc123","version":"1.0"}]"""

        val output = ByteArrayOutputStream()
        serializer.writeTo(original, output)

        val input = ByteArrayInputStream(output.toByteArray())
        val result = serializer.readFrom(input)

        assertEquals(original, result)
    }

    @Test
    fun `round-trip with empty array string`() = runTest {
        val original = "[]"

        val output = ByteArrayOutputStream()
        serializer.writeTo(original, output)

        val input = ByteArrayInputStream(output.toByteArray())
        val result = serializer.readFrom(input)

        assertEquals(original, result)
    }

    @Test
    fun `round-trip with complex JSON`() = runTest {
        val original = """[{"pubkey":"pk1","version":"0.1","address":"bc1qxyz","activeSlot":2,"slots":[{"slotNumber":0,"isActive":false,"isUsed":true}]}]"""

        val output = ByteArrayOutputStream()
        serializer.writeTo(original, output)

        val input = ByteArrayInputStream(output.toByteArray())
        val result = serializer.readFrom(input)

        assertEquals(original, result)
    }

    // endregion

    // region readFrom edge cases

    @Test
    fun `readFrom empty input returns defaultValue`() = runTest {
        val input = ByteArrayInputStream(ByteArray(0))

        val result = serializer.readFrom(input)

        assertEquals("[]", result)
    }

    @Test
    fun `readFrom returns defaultValue when decryption fails`() = runTest {
        val failingAead = object : Aead {
            override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray =
                throw GeneralSecurityException("encrypt fail")

            override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray =
                throw GeneralSecurityException("decrypt fail")
        }
        val failSerializer = CardsDataSerializer(failingAead)

        val input = ByteArrayInputStream(byteArrayOf(1, 2, 3))
        val result = failSerializer.readFrom(input)

        assertEquals("[]", result)
    }

    // endregion

    // region writeTo

    @Test
    fun `writeTo produces encrypted bytes different from plaintext`() = runTest {
        val plaintext = "hello world"
        val output = ByteArrayOutputStream()

        serializer.writeTo(plaintext, output)

        val encrypted = output.toByteArray()
        val plaintextBytes = plaintext.toByteArray(Charsets.UTF_8)

        // Encrypted bytes should not equal plaintext
        assert(!encrypted.contentEquals(plaintextBytes)) {
            "Encrypted output should differ from plaintext"
        }
    }

    @Test
    fun `writeTo with unicode content round-trips correctly`() = runTest {
        val original = """[{"label":"Meu Cartão 💰","pubkey":"pk"}]"""

        val output = ByteArrayOutputStream()
        serializer.writeTo(original, output)

        val input = ByteArrayInputStream(output.toByteArray())
        val result = serializer.readFrom(input)

        assertEquals(original, result)
    }

    // endregion
}
