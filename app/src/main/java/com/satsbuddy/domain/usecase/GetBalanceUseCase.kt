package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.repository.BalanceRepository
import javax.inject.Inject

class GetBalanceUseCase @Inject constructor(
    private val balanceRepository: BalanceRepository
) {
    suspend operator fun invoke(address: String): Result<Long> = runCatching {
        balanceRepository.getBalance(address)
    }
}
