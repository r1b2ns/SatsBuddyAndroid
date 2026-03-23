package com.satsbuddy.domain.usecase

import android.nfc.Tag
import com.satsbuddy.domain.repository.PsbtRepository
import javax.inject.Inject

class SignAndBroadcastUseCase @Inject constructor(
    private val psbtRepository: PsbtRepository
) {
    suspend operator fun invoke(
        tag: Tag,
        slot: Int,
        psbt: String,
        cvc: String
    ): Result<String> = runCatching {
        val signedPsbt = psbtRepository.signOnCard(tag, slot, psbt, cvc)
        psbtRepository.broadcast(signedPsbt)
    }
}
