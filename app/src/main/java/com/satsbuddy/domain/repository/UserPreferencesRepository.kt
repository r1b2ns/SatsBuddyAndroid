package com.satsbuddy.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Lightweight key/value store for UI-only preferences (tips already seen,
 * onboarding flags, etc).
 */
interface UserPreferencesRepository {
    val swipeToDeleteTipDismissed: Flow<Boolean>
    suspend fun setSwipeToDeleteTipDismissed(dismissed: Boolean)
}
