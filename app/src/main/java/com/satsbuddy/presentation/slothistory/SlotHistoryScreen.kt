package com.satsbuddy.presentation.slothistory

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.satsbuddy.domain.model.SlotTransaction
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSend: (String, Int) -> Unit,
    viewModel: SlotHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                Spacer(Modifier.height(24.dp))
                SlotBalanceHeader(balance = uiState.slotBalance)
                Spacer(Modifier.height(24.dp))

                uiState.address?.let { address ->
                    AddressBlock(
                        address = address,
                        onCopy = { clipboardManager.setText(AnnotatedString(address)) }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                SlotStatusPill(isActive = uiState.isActive, isUsed = uiState.isUsed)

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                if (uiState.isLoading && uiState.transactions.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            items(uiState.transactions) { tx ->
                TransactionRow(
                    tx = tx,
                    onOpenExplorer = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://mempool.space/tx/${tx.txid}".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
                HorizontalDivider()
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SlotBalanceHeader(balance: Long?) {
    val amount = balance ?: 0
    val text = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {
            append(formatSats(amount))
        }
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            append(" sats")
        }
    }
    Text(
        text = text,
        fontSize = 44.sp,
        fontFamily = FontFamily.Default
    )
}

@Composable
private fun AddressBlock(address: String, onCopy: () -> Unit) {
    Text(
        text = "Address",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = shortenAddress(address),
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onCopy) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Copy address",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SlotStatusPill(isActive: Boolean, isUsed: Boolean) {
    val label = when {
        isActive -> "SEALED"
        isUsed -> "UNSEALED"
        else -> "UNUSED"
    }
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TransactionRow(tx: SlotTransaction, onOpenExplorer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append(if (tx.direction == SlotTransaction.Direction.INCOMING) "+ " else "– ")
                    append(formatSats(kotlin.math.abs(tx.amount)))
                }
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    append(" sats")
                }
            },
            fontSize = 20.sp
        )

        tx.timestamp?.let { ts ->
            Text(
                text = formatTxTimestamp(ts),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier
                .clickable(onClick = onOpenExplorer)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "mempool.space",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(6.dp))
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open in mempool.space",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val satsFormatter: NumberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())

private fun formatSats(amount: Long): String = satsFormatter.format(amount)

private fun shortenAddress(address: String, head: Int = 12, tail: Int = 12): String {
    if (address.length <= head + tail + 1) return address
    return "${address.take(head)}…${address.takeLast(tail)}"
}

private val txTimestampFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy 'at' H:mm", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())

private fun formatTxTimestamp(epochSeconds: Long): String =
    txTimestampFormatter.format(Instant.ofEpochSecond(epochSeconds))
