package com.satsbuddy.presentation.send.review

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendReviewScreen(
    destinationAddress: String,
    feeRate: Int,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review") },
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
            Text("Destination", style = MaterialTheme.typography.labelMedium)
            Text(
                destinationAddress,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Amount", style = MaterialTheme.typography.labelMedium)
            Text("ALL (sweep)", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Fee Rate", style = MaterialTheme.typography.labelMedium)
            Text("$feeRate sat/vB", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}
