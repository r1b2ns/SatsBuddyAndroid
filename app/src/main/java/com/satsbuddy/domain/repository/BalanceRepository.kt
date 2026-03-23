package com.satsbuddy.domain.repository

interface BalanceRepository {
    suspend fun getBalance(address: String): Long
}
