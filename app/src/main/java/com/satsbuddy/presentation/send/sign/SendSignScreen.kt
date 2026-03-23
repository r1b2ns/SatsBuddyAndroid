package com.satsbuddy.presentation.send.sign

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSignScreen(
    destinationAddress: String,
    feeRate: Int,
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: SendSignViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign & Broadcast") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(uiState.statusMessage, style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(24.dp))

            if (uiState.isBusy) {
                CircularProgressIndicator()
            }

            if (uiState.state is SendSignState.Ready || uiState.state is SendSignState.Idle) {
                OutlinedTextField(
                    value = uiState.cvc,
                    onValueChange = { viewModel.updateCvc(it) },
                    label = { Text("Card CVC") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.startSign(0) },
                    enabled = uiState.canSign,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tap Card to Sign & Broadcast")
                }
            }

            uiState.signedTxid?.let { txid ->
                Spacer(Modifier.height(24.dp))
                Text("Transaction ID:", style = MaterialTheme.typography.labelMedium)
                Text(
                    txid,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Done")
                }
            }

            if (uiState.state is SendSignState.Error) {
                Spacer(Modifier.height(16.dp))
                Text(
                    (uiState.state as SendSignState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
