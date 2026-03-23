package com.satsbuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.satsbuddy.data.nfc.NfcSessionManager
import com.satsbuddy.presentation.navigation.SatsBuddyNavHost
import com.satsbuddy.presentation.theme.SatsBuddyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var nfcSessionManager: NfcSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SatsBuddyTheme {
                SatsBuddyNavHost()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcSessionManager.enableReaderMode(this)
    }

    override fun onPause() {
        super.onPause()
        nfcSessionManager.disableReaderMode(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // NFC tags are handled via Reader Mode callback in NfcSessionManager.
        // Intent-based NFC dispatch (TECH_DISCOVERED) is a fallback for older APIs.
        intent.getParcelableExtra<android.nfc.Tag>(android.nfc.NfcAdapter.EXTRA_TAG)?.let { tag ->
            nfcSessionManager.handleTag(tag)
        }
    }
}
