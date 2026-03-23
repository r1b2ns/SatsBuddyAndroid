package com.satsbuddy.presentation.send.destination

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendDestinationScreen(
    onBack: () -> Unit,
    onNext: (String) -> Unit
) {
    var address by rememberSaveable { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Destination") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = address,
                onValueChange = {
                    address = it.removePrefix("bitcoin:").trim()
                },
                label = { Text("Bitcoin Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = {
                clipboardManager.getText()?.text?.let { text ->
                    address = text.removePrefix("bitcoin:").trim()
                }
            }) {
                Icon(Icons.Default.ContentPaste, contentDescription = null)
                Text("  Paste from Clipboard")
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onNext(address) },
                enabled = address.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }
        }
    }
}
