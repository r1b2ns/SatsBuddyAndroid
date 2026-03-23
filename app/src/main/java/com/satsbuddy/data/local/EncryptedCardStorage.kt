package com.satsbuddy.data.local

import androidx.datastore.core.DataStore
import com.satsbuddy.domain.model.SatsCardInfo
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedCardStorage @Inject constructor(
    private val dataStore: DataStore<String>
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadCards(): List<SatsCardInfo> {
        val raw = dataStore.data.first()
        return runCatching { json.decodeFromString<List<SatsCardInfo>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun saveCards(cards: List<SatsCardInfo>) {
        dataStore.updateData { json.encodeToString(cards) }
    }
}
