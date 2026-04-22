package com.satsbuddy.data.bitcoin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bitcoindevkit.Descriptor
import org.bitcoindevkit.KeychainKind
import org.bitcoindevkit.Network
import org.bitcoindevkit.Persister
import org.bitcoindevkit.Wallet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BdkDataSource @Inject constructor() {

    suspend fun buildSweepPsbt(
        descriptor: String,
        destination: String,
        feeRate: Long
    ): String = withContext(Dispatchers.IO) {
        throw NotImplementedError("bdk-android sweep not yet integrated")
    }

    suspend fun broadcast(signedPsbt: String): String = withContext(Dispatchers.IO) {
        throw NotImplementedError("bdk-android broadcast not yet integrated")
    }

    suspend fun deriveAddress(
        descriptor: String,
        network: Network = Network.BITCOIN
    ): String = withContext(Dispatchers.IO) {
        Descriptor(descriptor, network).use { parsed ->
            Persister.newInMemory().use { persister ->
                Wallet.createSingle(parsed, network, persister).use { wallet ->
                    wallet.peekAddress(KeychainKind.EXTERNAL, 0u).address.toString()
                }
            }
        }
    }
}
