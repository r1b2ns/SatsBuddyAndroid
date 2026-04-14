package com.satsbuddy.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.satsbuddy.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    override val swipeToDeleteTipDismissed: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_SWIPE_TIP_DISMISSED] ?: false }

    override suspend fun setSwipeToDeleteTipDismissed(dismissed: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_SWIPE_TIP_DISMISSED] = dismissed }
    }

    private companion object {
        val KEY_SWIPE_TIP_DISMISSED = booleanPreferencesKey("swipe_to_delete_tip_dismissed")
    }
}
