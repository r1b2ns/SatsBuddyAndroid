package com.satsbuddy.domain.repository

import com.satsbuddy.domain.model.SlotTransaction

interface TransactionRepository {
    suspend fun getTransactions(address: String): List<SlotTransaction>
}
