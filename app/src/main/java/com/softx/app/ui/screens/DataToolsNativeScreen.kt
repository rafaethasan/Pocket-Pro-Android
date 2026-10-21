package com.softx.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.PocketReminderScheduler
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeRepository
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun DataToolsNativeScreen(
    repository: PocketNativeRepository,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var statusText by remember { mutableStateOf("") }
    var pendingAction by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            NativeFeatureTopBar(
                title = "Data Tools",
                subtitle = "Repair local app helpers, clean cache, and reset unsynced queues when Pocket Pro feels out of sync.",
                onBack = onBack,
            )
        }
        item {
            NativeEmptyCard(
                "These tools are for maintenance. Pocket Pro will ask before risky cleanup actions so users do not lose data by mistake.",
            )
        }
        item {
            DataToolRow(
                title = "Repair reminder schedule",
                subtitle = "Rebuild the daily smart reminder schedule using your current notification settings.",
                icon = Icons.Rounded.Schedule,
                accent = AccentPurple,
                onClick = {
                    PocketReminderScheduler.ensureDailyReminder(context)
                    statusText = "Reminder schedule refreshed."
                },
            )
        }
        item {
            DataToolRow(
                title = "Clear local cache",
                subtitle = "Remove cached JSON screens so Pocket Pro can rebuild fresh local views later.",
                icon = Icons.Rounded.CleaningServices,
                accent = AccentOrange,
                onClick = { pendingAction = "clear_cache" },
            )
        }
        item {
            DataToolRow(
                title = "Clear offline queue",
                subtitle = "Remove unsynced pending saves if old offline items are causing trouble.",
                icon = Icons.Rounded.Storage,
                accent = AccentBlue,
                onClick = { pendingAction = "clear_queue" },
            )
        }
        item {
            DataToolRow(
                title = "Refresh app helpers",
                subtitle = "Quickly rebuild local reminder timing and keep Pocket Pro helper services aligned.",
                icon = Icons.Rounded.Refresh,
                accent = AccentGreen,
                onClick = {
                    PocketReminderScheduler.ensureDailyReminder(context)
                    statusText = "Pocket Pro helpers refreshed."
                },
            )
        }
        if (statusText.isNotBlank()) {
            item {
                Text(
                    text = statusText,
                    color = AccentGreen,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                )
            }
        }
    }

    pendingAction?.let { action ->
        val (title, message) = when (action) {
            "clear_cache" -> "Clear local cache?" to "Are you sure you want to remove Pocket Pro cached screen files? Live data stays safe, but offline cached views will rebuild later."
            else -> "Clear offline queue?" to "Are you sure you want to remove unsynced offline queue files? Pending unsent changes may be lost."
        }
        PocketDeleteConfirmationDialog(
            title = title,
            message = message,
            onDismiss = { pendingAction = null },
            onConfirm = {
                statusText = when (action) {
                    "clear_cache" -> repository.clearLocalCache().getOrElse { it.message ?: "Cache could not be cleared." }
                    else -> repository.clearOfflineQueues().getOrElse { it.message ?: "Offline queue could not be cleared." }
                }
                pendingAction = null
            },
        )
    }
}

@Composable
private fun DataToolRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = accent)
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            NativeInlineInfoButton(title = title, message = subtitle)
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color = TextSecondary.copy(alpha = 0.12f),
    )
}
