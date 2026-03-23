package com.satsbuddy.presentation.slothistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satsbuddy.domain.model.SlotTransaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSend: (String, Int) -> Unit,
    viewModel: SlotHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Slot ${viewModel.slotNumber + 1}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                uiState.slotBalance?.let { balance ->
                    Text(
                        "$balance sats",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                if (uiState.transactions.isNotEmpty()) {
                    Text(
                        "Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(uiState.transactions) { tx ->
                TransactionRow(tx)
                HorizontalDivider()
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun TransactionRow(tx: SlotTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                tx.txid.take(16) + "...",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
            Text(
                if (tx.confirmed) "Confirmed" else "Pending",
                style = MaterialTheme.typography.labelSmall,
                color = if (tx.confirmed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${if (tx.amount >= 0) "+" else ""}${tx.amount} sats",
            color = if (tx.amount >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}
