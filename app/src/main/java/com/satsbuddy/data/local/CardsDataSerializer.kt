package com.satsbuddy.data.local

import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException

class CardsDataSerializer(private val aead: Aead) : Serializer<String> {

    override val defaultValue: String = "[]"

    override suspend fun readFrom(input: InputStream): String {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return defaultValue
        return try {
            String(aead.decrypt(bytes, ASSOCIATED_DATA), Charsets.UTF_8)
        } catch (_: GeneralSecurityException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: String, output: OutputStream) {
        output.write(aead.encrypt(t.toByteArray(Charsets.UTF_8), ASSOCIATED_DATA))
    }

    companion object {
        private val ASSOCIATED_DATA = "satsbuddy_cards_v1".toByteArray(Charsets.UTF_8)
    }
}
