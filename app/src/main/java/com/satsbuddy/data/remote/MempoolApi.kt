package com.satsbuddy.data.remote

import com.satsbuddy.data.remote.dto.AddressStatsDto
import com.satsbuddy.data.remote.dto.FeeEstimatesDto
import com.satsbuddy.data.remote.dto.PriceDto
import com.satsbuddy.data.remote.dto.TransactionDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MempoolApi {

    @GET("address/{address}")
    suspend fun getAddressStats(@Path("address") address: String): AddressStatsDto

    @GET("address/{address}/txs")
    suspend fun getTransactions(@Path("address") address: String): List<TransactionDto>

    @GET("v1/prices")
    suspend fun getPrices(): PriceDto

    @GET("v1/fees/recommended")
    suspend fun getRecommendedFees(): FeeEstimatesDto

    @POST("tx")
    suspend fun broadcastTx(@Body hexTx: String): String
}
