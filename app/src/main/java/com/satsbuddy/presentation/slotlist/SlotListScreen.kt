package com.satsbuddy.presentation.slotlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.satsbuddy.domain.model.SlotInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotListScreen(
    onNavigateBack: () -> Unit,
    onSlotClick: (String, Int) -> Unit,
    slots: List<SlotInfo> = emptyList(),
    cardIdentifier: String = ""
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Slots") },
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
        ) {
            items(slots) { slot ->
                SlotSummaryRow(
                    slot = slot,
                    onClick = { onSlotClick(cardIdentifier, slot.slotNumber) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SlotSummaryRow(slot: SlotInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Slot ${slot.displaySlotNumber}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.width(8.dp))
        if (slot.isActive) {
            Badge { Text("Active") }
        } else if (slot.isUsed) {
            Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) { Text("Used") }
        }
        Spacer(Modifier.weight(1f))
        slot.address?.let { addr ->
            Text(
                addr.take(10) + "...",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
