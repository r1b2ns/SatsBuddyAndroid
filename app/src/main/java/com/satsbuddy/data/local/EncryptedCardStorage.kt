package com.satsbuddy.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.satsbuddy.domain.model.SatsCardInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedCardStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILE = "satsbuddy_secure_prefs"
        private const val KEY_CARDS = "cards"
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun loadCards(): List<SatsCardInfo> {
        val raw = prefs.getString(KEY_CARDS, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<SatsCardInfo>>(raw) }.getOrDefault(emptyList())
    }

    fun saveCards(cards: List<SatsCardInfo>) {
        prefs.edit().putString(KEY_CARDS, json.encodeToString(cards)).apply()
    }
}
