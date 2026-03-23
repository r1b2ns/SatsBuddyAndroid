package com.satsbuddy.domain.repository

import android.nfc.Tag
import com.satsbuddy.domain.model.SatsCardInfo

interface CardRepository {
    suspend fun readCardInfo(tag: Tag): SatsCardInfo
    suspend fun setupNextSlot(tag: Tag, cvc: String, expectedId: String): SatsCardInfo
}
