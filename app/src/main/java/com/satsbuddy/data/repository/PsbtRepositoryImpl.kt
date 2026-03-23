package com.satsbuddy.data.repository

import android.nfc.Tag
import com.satsbuddy.data.bitcoin.BdkDataSource
import com.satsbuddy.data.nfc.CkTapCardDataSource
import com.satsbuddy.data.remote.MempoolApi
import com.satsbuddy.domain.repository.PsbtRepository
import javax.inject.Inject

class PsbtRepositoryImpl @Inject constructor(
    private val bdkDataSource: BdkDataSource,
    private val ckTapCardDataSource: CkTapCardDataSource,
    private val mempoolApi: MempoolApi
) : PsbtRepository {

    override suspend fun buildSweepPsbt(
        descriptor: String,
        destination: String,
        feeRate: Long
    ): String {
        return bdkDataSource.buildSweepPsbt(descriptor, destination, feeRate)
    }

    override suspend fun signOnCard(tag: Tag, slot: Int, psbt: String, cvc: String): String {
        return ckTapCardDataSource.signPsbt(tag, slot, psbt, cvc)
    }

    override suspend fun broadcast(signedPsbt: String): String {
        return mempoolApi.broadcastTx(signedPsbt)
    }
}
