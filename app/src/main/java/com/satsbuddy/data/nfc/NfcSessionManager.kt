package com.satsbuddy.data.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _tagFlow = MutableSharedFlow<Tag>(extraBufferCapacity = 1)
    val tagFlow: SharedFlow<Tag> = _tagFlow.asSharedFlow()

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    val isNfcAvailable: Boolean get() = nfcAdapter != null
    val isNfcEnabled: Boolean get() = nfcAdapter?.isEnabled == true

    fun enableReaderMode(activity: Activity) {
        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        nfcAdapter?.enableReaderMode(
            activity,
            { tag -> _tagFlow.tryEmit(tag) },
            flags,
            Bundle()
        )
    }

    fun disableReaderMode(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }

    fun handleTag(tag: Tag) {
        _tagFlow.tryEmit(tag)
    }
}
