package com.satsbuddy.domain.usecase

import com.satsbuddy.domain.model.SlotTransaction
import com.satsbuddy.domain.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(address: String): Result<List<SlotTransaction>> = runCatching {
        transactionRepository.getTransactions(address)
    }
}
