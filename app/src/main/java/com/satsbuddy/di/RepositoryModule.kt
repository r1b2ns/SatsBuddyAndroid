package com.satsbuddy.di

import com.satsbuddy.data.repository.BalanceRepositoryImpl
import com.satsbuddy.data.repository.CardRepositoryImpl
import com.satsbuddy.data.repository.CardStorageRepositoryImpl
import com.satsbuddy.data.repository.FeeRepositoryImpl
import com.satsbuddy.data.repository.PriceRepositoryImpl
import com.satsbuddy.data.repository.PsbtRepositoryImpl
import com.satsbuddy.data.repository.TransactionRepositoryImpl
import com.satsbuddy.domain.repository.BalanceRepository
import com.satsbuddy.domain.repository.CardRepository
import com.satsbuddy.domain.repository.CardStorageRepository
import com.satsbuddy.domain.repository.FeeRepository
import com.satsbuddy.domain.repository.PriceRepository
import com.satsbuddy.domain.repository.PsbtRepository
import com.satsbuddy.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    @Binds @Singleton
    abstract fun bindBalanceRepository(impl: BalanceRepositoryImpl): BalanceRepository

    @Binds @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds @Singleton
    abstract fun bindPriceRepository(impl: PriceRepositoryImpl): PriceRepository

    @Binds @Singleton
    abstract fun bindFeeRepository(impl: FeeRepositoryImpl): FeeRepository

    @Binds @Singleton
    abstract fun bindCardStorageRepository(impl: CardStorageRepositoryImpl): CardStorageRepository

    @Binds @Singleton
    abstract fun bindPsbtRepository(impl: PsbtRepositoryImpl): PsbtRepository
}
