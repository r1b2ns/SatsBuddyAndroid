package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.RecommendedFees
import com.satsbuddy.domain.repository.FeeRepository
import javax.inject.Inject

class GetFeesUseCase @Inject constructor(
    private val feeRepository: FeeRepository
) {
    suspend operator fun invoke(): Result<RecommendedFees> = runCatching {
        feeRepository.fetchRecommendedFees()
    }
}
