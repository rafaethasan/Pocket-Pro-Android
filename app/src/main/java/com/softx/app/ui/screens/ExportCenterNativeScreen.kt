package com.softx.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun ExportCenterNativeScreen(
    onBack: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenBackupRestore: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            NativeFeatureTopBar(
                title = "Export Center",
                subtitle = "Open every Pocket Pro export path from one place: reports, statements, and local backup.",
                onBack = onBack,
            )
        }
        item {
            NativeEmptyCard(
                "Keep your important finance outputs together so users can quickly find PDFs, statements, and backup tools without hunting through different sections.",
            )
        }
        item {
            ExportCenterRow(
                title = "Reports PDF export",
                subtitle = "Open reports, charts, calendar view, and the main PDF export action.",
                icon = Icons.Rounded.PictureAsPdf,
                accent = AccentPurple,
                onClick = onOpenReports,
            )
        }
        item {
            ExportCenterRow(
                title = "Statement export",
                subtitle = "Open accounts and export wallet or statement PDFs when users need account proof.",
                icon = Icons.Rounded.Description,
                accent = AccentBlue,
                onClick = onOpenAccounts,
            )
        }
        item {
            ExportCenterRow(
                title = "Backup & restore",
                subtitle = "Export a JSON backup or restore Pocket Pro data on this or another phone.",
                icon = Icons.Rounded.CloudUpload,
                accent = AccentGreen,
                onClick = onOpenBackupRestore,
            )
        }
        item {
            NativeEmptyCard(
                "Best workflow: use Reports for insights, Accounts for statements, and Backup for portable data safety.",
            )
        }
    }
}

@Composable
private fun ExportCenterRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
        )
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
