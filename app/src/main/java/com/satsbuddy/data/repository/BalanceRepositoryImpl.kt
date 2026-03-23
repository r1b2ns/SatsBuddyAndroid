package com.satsbuddy.data.repository

import com.satsbuddy.data.remote.MempoolApi
import com.satsbuddy.domain.repository.BalanceRepository
import javax.inject.Inject

class BalanceRepositoryImpl @Inject constructor(
    private val mempoolApi: MempoolApi
) : BalanceRepository {

    override suspend fun getBalance(address: String): Long {
        val stats = mempoolApi.getAddressStats(address)
        return stats.totalBalance
    }
}
