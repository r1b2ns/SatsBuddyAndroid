package com.satsbuddy.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.satsbuddy.data.local.CardsDataSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideAead(@ApplicationContext context: Context): Aead {
        AeadConfig.register()
        return AndroidKeysetManager.Builder()
            .withSharedPref(context, "satsbuddy_keyset", "satsbuddy_keyset_prefs")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://satsbuddy_master_key")
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    @Provides
    @Singleton
    fun provideCardsDataStore(
        @ApplicationContext context: Context,
        aead: Aead
    ): DataStore<String> = DataStoreFactory.create(
        serializer = CardsDataSerializer(aead),
        produceFile = { context.dataStoreFile("satsbuddy_cards.pb") }
    )
}
