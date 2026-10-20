package com.softx.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BackupRestoreNativeScreen(
    repository: PocketNativeRepository,
    preferencesStore: PocketNativePreferencesStore,
    signedInUser: PocketNativeUser?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gate = pocketNativeFeatureGate(signedInUser, "backup_restore")
    var lockedFeature by remember { mutableStateOf<PocketNativeFeatureGate?>(null) }
    var busy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    val stats = remember(statusText) { repository.backupStats(preferencesStore) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            val result = repository.buildLocalBackupJson(preferencesStore)
            if (result.isSuccess) {
                val backupJson = result.getOrNull().orEmpty()
                val writeResult = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                            writer.write(backupJson)
                        } ?: throw IllegalStateException("Pocket Pro could not create the backup file.")
                    }
                }
                statusText = if (writeResult.isSuccess) {
                    "Backup exported successfully."
                } else {
                    writeResult.exceptionOrNull()?.message ?: "Pocket Pro could not export the backup."
                }
            } else {
                statusText = result.exceptionOrNull()?.message ?: "Pocket Pro could not prepare the backup."
            }
            busy = false
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            val readResult = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                        reader.readText()
                    } ?: throw IllegalStateException("Pocket Pro could not read the backup file.")
                }
            }
            if (readResult.isSuccess) {
                val restoreResult = repository.restoreLocalBackupJson(
                    readResult.getOrThrow(),
                    preferencesStore,
                )
                statusText = restoreResult.getOrElse {
                    it.message ?: "Pocket Pro could not restore this backup."
                }
            } else {
                statusText = readResult.exceptionOrNull()?.message ?: "Pocket Pro could not read the backup file."
            }
            busy = false
        }
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(34.dp)) }
        item {
            NativeFeatureTopBar(
                title = "Backup & Restore",
                subtitle = "Keep your local Pocket Pro data safe and move it to a new phone when needed.",
                onBack = onBack,
            )
        }
        item {
            NativeEmptyCard(
                if (gate.granted) {
                    "Your local cache, queued offline entries, reminders, language, and currency settings can be exported to one backup file."
                } else {
                    "Backup export and restore are available for free."
                },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BackupMetricCard("Cache files", stats.cacheFileCount.toString(), AccentBlue, Modifier.weight(1f))
                BackupMetricCard("Queued", (stats.queuedTransactions + stats.queuedBudgets + stats.queuedGoals).toString(), AccentGreen, Modifier.weight(1f))
                BackupMetricCard("Reminders", stats.reminderCount.toString(), AccentPurple, Modifier.weight(1f))
            }
        }
        item {
            BackupActionRow(
                title = "Export backup",
                subtitle = "Save your local records, budgets, goals, reminders, and cached data to a JSON backup file.",
                icon = Icons.Rounded.CloudUpload,
                accent = AccentBlue,
                onClick = {
                    if (gate.granted) {
                        exportLauncher.launch("pocket-pro-backup.json")
                    } else {
                        lockedFeature = gate
                    }
                },
            )
        }
        item {
            BackupActionRow(
                title = "Restore backup",
                subtitle = "Import a previously saved Pocket Pro backup file on this phone.",
                icon = Icons.Rounded.CloudDownload,
                accent = AccentGreen,
                onClick = {
                    if (gate.granted) {
                        importLauncher.launch(arrayOf("application/json", "text/plain"))
                    } else {
                        lockedFeature = gate
                    }
                },
            )
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
                    Text("What is included", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    PocketNativeProBadge()
                }
                Text("Offline cached screens, queued unsynced entries, monthly reminders, language, and currency preferences are included in the backup.", color = TextSecondary, lineHeight = 21.sp)
                HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
            }
        }
        if (statusText.isNotBlank()) {
            item {
                Text(
                    text = statusText,
                    color = if (statusText.contains("success", ignoreCase = true) || statusText.contains("restored", ignoreCase = true)) AccentGreen else TextSecondary,
                    lineHeight = 21.sp,
                )
            }
        }
        if (busy) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(color = AccentPurple, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                }
            }
        }
        item { Spacer(modifier = Modifier.height(34.dp)) }
    }

    lockedFeature?.let { feature ->
        PocketNativeUpgradeDialog(
            gate = feature,
            onDismiss = { lockedFeature = null },
        )
    }
}

@Composable
private fun BackupMetricCard(
    title: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(title, color = TextSecondary)
        Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        HorizontalDivider(color = accent.copy(alpha = 0.2f))
    }
}

@Composable
private fun BackupActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceCard,
            contentColor = TextPrimary,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, color = TextSecondary, lineHeight = 20.sp)
            }
            PocketNativeProBadge()
        }
    }
}
