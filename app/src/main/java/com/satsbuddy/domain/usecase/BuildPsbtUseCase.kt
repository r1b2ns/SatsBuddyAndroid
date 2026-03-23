package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.repository.PsbtRepository
import javax.inject.Inject

class BuildPsbtUseCase @Inject constructor(
    private val psbtRepository: PsbtRepository
) {
    suspend operator fun invoke(
        descriptor: String,
        destination: String,
        feeRate: Long
    ): Result<String> = runCatching {
        psbtRepository.buildSweepPsbt(descriptor, destination, feeRate)
    }
}
