package com.satsbuddy.domain.repository

import android.nfc.Tag

interface PsbtRepository {
    suspend fun buildSweepPsbt(descriptor: String, destination: String, feeRate: Long): String
    suspend fun signOnCard(tag: Tag, slot: Int, psbt: String, cvc: String): String
    suspend fun broadcast(signedPsbt: String): String
}
