package com.satsbuddy.presentation.carddetail

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satsbuddy.domain.model.BalanceDisplayFormat
import com.satsbuddy.domain.model.SlotInfo
import com.satsbuddy.presentation.common.AppBottomSheet
import com.satsbuddy.presentation.common.BalanceText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val SWEEP_ENABLED = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSlotList: (String) -> Unit,
    onNavigateToReceive: (String) -> Unit,
    onNavigateToSend: (String, Int) -> Unit,
    viewModel: CardDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var balanceFormat by rememberSaveable { mutableStateOf(BalanceDisplayFormat.SATS) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }

    val activeSlot = uiState.slots.firstOrNull { it.isActive }
    val displayAddress = activeSlot?.address

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showRenameDialog = true }
                    ) {
                        Text(
                            text = uiState.displayName.ifEmpty {
                                viewModel.cardIdentifier.take(12) + "..."
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Rename card",
                            modifier = Modifier.height(18.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
            }

            BalanceText(
                satAmount = activeSlot?.balance ?: 0,
                format = balanceFormat,
                price = null,
                onFormatToggle = { balanceFormat = balanceFormat.next() }
            )
            Spacer(Modifier.height(8.dp))

            @Suppress("ConstantConditionIf")
            if (SWEEP_ENABLED) activeSlot?.let { slot ->
                Button(
                    onClick = { onNavigateToSend(viewModel.cardIdentifier, slot.slotNumber) },
                    enabled = (slot.balance ?: 0) > 0 && displayAddress != null,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sweep Balance")
                }
            }

            uiState.errorMessage?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Receive Row
            if (displayAddress != null) {
                DetailRow(
                    label = "Receive",
                    subtitle = displayAddress,
                    icon = { Icon(Icons.Default.QrCode, contentDescription = "Receive") },
                    onClick = { onNavigateToReceive(displayAddress) }
                )
                HorizontalDivider()
            }

            // Card ID Row
            DetailRow(
                label = "Card ID",
                subtitle = viewModel.cardIdentifier,
                icon = { Icon(Icons.Default.ContentCopy, contentDescription = "Copy") },
                onClick = { clipboardManager.setText(AnnotatedString(viewModel.cardIdentifier)) }
            )
            HorizontalDivider()

            if (displayAddress != null) {
                DetailRow(
                    label = "Explorer",
                    subtitle = "mempool.space",
                    icon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in explorer") },
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://mempool.space/address/$displayAddress".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
                HorizontalDivider()
            }

            // Slot Navigation Row
            if (uiState.slots.isNotEmpty()) {
                val totalSlots = uiState.slots.size
                val subtitle = activeSlot?.let { "${it.displaySlotNumber}/$totalSlots" }
                    ?: "All unsealed"
                DetailRow(
                    label = "Slot",
                    subtitle = subtitle,
                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View Slots") },
                    onClick = { onNavigateToSlotList(viewModel.cardIdentifier) }
                )
                HorizontalDivider()
            }

            // Refresh Row
            DetailRow(
                label = "Card Refresh",
                subtitle = uiState.lastUpdated?.let { formatTimestamp(it) },
                icon = { Icon(Icons.Default.Refresh, contentDescription = "Refresh") },
                onClick = { viewModel.beginRefreshScan() }
            )

            Spacer(Modifier.height(32.dp))

            // Footer
            CardFooter(
                cardVersion = uiState.cardVersion,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showRenameDialog) {
            RenameCardDialog(
                initialValue = uiState.label.orEmpty(),
                onConfirm = { newName ->
                    viewModel.updateLabel(newName)
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false }
            )
        }

        if (uiState.isScanning) {
            AppBottomSheet(
                image = Icons.Default.Contactless,
                title = "Wait",
                subtitle = "Hold phone near SATSCARD",
                titlePrimaryButton = "Cancel",
                primaryButtonAction = { viewModel.cancelRefreshScan() },
                onDismissRequest = { viewModel.cancelRefreshScan() }
            )
        }
    }
}

@Composable
private fun RenameCardDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable(initialValue) { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
        title = { Text("Rename card") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Card name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    subtitle: String? = null,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        icon()
    }
}

private val cardRefreshFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

private fun formatTimestamp(epochMillis: Long): String =
    cardRefreshFormatter.format(Instant.ofEpochMilli(epochMillis))

private const val SATSBUDDY_VERSION = "1.0 ALPHA 1"
private const val SATSBUDDY_CITY_DESIGNED = "NASHVILLE"
private const val SATSBUDDY_CITY_MADE = "RIBEIRÃO PRETO"
private const val SATSCARD_COUNTRY = "CANADA"
private val SatsCardCountryColor = Color(0xFFE53935)
private val SatsBuddyCityColor = Color(0xFF1E88E5)

@Composable
private fun CardFooter(
    cardVersion: String,
    modifier: Modifier = Modifier,
) {
    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val satsCardVersion = cardVersion.ifBlank { "—" }

    val satsCardLine = buildAnnotatedString {
        append("SATSCARD • VERSION ")
        append(satsCardVersion)
        append(" • MADE IN ")
        withStyle(SpanStyle(color = SatsCardCountryColor)) {
            append(SATSCARD_COUNTRY)
        }
    }

    val satsBuddyLine = buildAnnotatedString {
        append("SATSBUDDY")
        append(" • DESIGNED IN ")
        withStyle(SpanStyle(color = SatsBuddyCityColor)) {
            append(SATSBUDDY_CITY_DESIGNED)
        }
    }

    val satsBuddyAndroidLine = buildAnnotatedString {
        append("SATSBUDDY ANDROID • VERSION ")
        append(SATSBUDDY_VERSION)
        append(" • MADE IN ")
        withStyle(SpanStyle(color = SatsBuddyCityColor)) {
            append(SATSBUDDY_CITY_MADE)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = satsCardLine,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = baseColor,
            textAlign = TextAlign.Left
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = satsBuddyLine,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = baseColor,
            textAlign = TextAlign.Left
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = satsBuddyAndroidLine,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = baseColor,
            textAlign = TextAlign.Left
        )
    }
}
