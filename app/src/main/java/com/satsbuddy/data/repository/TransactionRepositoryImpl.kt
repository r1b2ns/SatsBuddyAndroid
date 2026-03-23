package com.satsbuddy.data.repository

import com.satsbuddy.data.remote.MempoolApi
import com.satsbuddy.domain.model.SlotTransaction
import com.satsbuddy.domain.repository.TransactionRepository
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val mempoolApi: MempoolApi
) : TransactionRepository {

    override suspend fun getTransactions(address: String): List<SlotTransaction> {
        return mempoolApi.getTransactions(address).map { it.toDomain(address) }
    }
}
