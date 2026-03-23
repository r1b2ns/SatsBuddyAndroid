package com.satsbuddy.domain.usecase

import android.nfc.Tag
import com.satsbuddy.domain.model.SatsCardInfo
import com.satsbuddy.domain.repository.CardRepository
import javax.inject.Inject

class ReadCardInfoUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(tag: Tag): Result<SatsCardInfo> = runCatching {
        cardRepository.readCardInfo(tag)
    }
}
