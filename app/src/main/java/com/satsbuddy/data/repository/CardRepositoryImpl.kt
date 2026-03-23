package com.satsbuddy.data.repository

import android.nfc.Tag
import com.satsbuddy.data.nfc.CkTapCardDataSource
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.CardRepository
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val ckTapCardDataSource: CkTapCardDataSource
) : CardRepository {

    override suspend fun readCardInfo(tag: Tag): SatsCardInfo {
        return ckTapCardDataSource.readCard(tag)
    }

    override suspend fun setupNextSlot(tag: Tag, cvc: String, expectedId: String): SatsCardInfo {
        return ckTapCardDataSource.setupNextSlot(tag, cvc, expectedId)
    }
}
