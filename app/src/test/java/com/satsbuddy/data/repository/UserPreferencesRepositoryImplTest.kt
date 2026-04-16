package com.satsbuddy.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesRepositoryImplTest {

    private val key = booleanPreferencesKey("swipe_to_delete_tip_dismissed")

    private fun prefsWithValue(value: Boolean): Preferences {
        val prefs = mockk<Preferences>()
        every { prefs[key] } returns value
        return prefs
    }

    private fun emptyPrefs(): Preferences {
        val prefs = mockk<Preferences>()
        every { prefs[key] } returns null
        return prefs
    }

    private fun createRepository(prefs: Preferences): Pair<DataStore<Preferences>, UserPreferencesRepositoryImpl> {
        val dataStore = mockk<DataStore<Preferences>>(relaxed = true)
        every { dataStore.data } returns flowOf(prefs)
        return dataStore to UserPreferencesRepositoryImpl(dataStore)
    }

    @Test
    fun `swipeToDeleteTipDismissed emits true when preference is set`() = runTest {
        val (_, repository) = createRepository(prefsWithValue(true))

        val result = repository.swipeToDeleteTipDismissed.first()

        assertTrue(result)
    }

    @Test
    fun `swipeToDeleteTipDismissed emits false when preference is not set`() = runTest {
        val (_, repository) = createRepository(emptyPrefs())

        val result = repository.swipeToDeleteTipDismissed.first()

        assertFalse(result)
    }

    @Test
    fun `swipeToDeleteTipDismissed emits false when preference is false`() = runTest {
        val (_, repository) = createRepository(prefsWithValue(false))

        val result = repository.swipeToDeleteTipDismissed.first()

        assertFalse(result)
    }

    @Test
    fun `setSwipeToDeleteTipDismissed calls dataStore updateData`() = runTest {
        val (dataStore, repository) = createRepository(emptyPrefs())

        repository.setSwipeToDeleteTipDismissed(true)

        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `setSwipeToDeleteTipDismissed with false calls dataStore updateData`() = runTest {
        val (dataStore, repository) = createRepository(emptyPrefs())

        repository.setSwipeToDeleteTipDismissed(false)

        coVerify { dataStore.updateData(any()) }
    }
}
