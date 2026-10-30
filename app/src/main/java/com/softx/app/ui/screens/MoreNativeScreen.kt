package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpCenter
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun MoreNativeScreen(
    preferencesStore: PocketNativePreferencesStore,
    onBack: () -> Unit,
    showTopBackButton: Boolean = true,
    onOpenSettings: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onOpenAppLock: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenCategoryManager: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenExportCenter: () -> Unit,
    onOpenUserGuide: () -> Unit,
    onOpenHelpSupport: () -> Unit,
    onOpenDataTools: () -> Unit,
    onOpenPaywall: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item {
            if (showTopBackButton) {
                NativeFeatureTopBar(
                    title = "More",
                    subtitle = "Tools, alerts, exports, search, and app controls in one clean place.",
                    onBack = onBack,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("More", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                    NativeInlineInfoButton(
                        title = "More",
                        message = "Tools, alerts, exports, search, and app controls in one clean place.",
                    )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
        item {
            val isPro = preferencesStore.isProMember()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenPaywall),
                color = Color(0xFF131826),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isPro) Color(0xFFFFD54F).copy(alpha = 0.5f) else Color(0xFF00E5FF).copy(alpha = 0.35f)
                ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isPro) Color(0xFFFFD54F).copy(alpha = 0.16f) else Color(0xFF00E5FF).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WorkspacePremium,
                                contentDescription = null,
                                tint = if (isPro) Color(0xFFFFD54F) else Color(0xFF00E5FF),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isPro) "Pocket Pro Plus Active" else "Upgrade to Pro Plus",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isPro) Color(0xFFFFD54F).copy(alpha = 0.18f) else Color(0xFF00E5FF).copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp),
                                ) {
                                    Text(
                                        text = if (isPro) "VIP" else "PRO",
                                        color = if (isPro) Color(0xFFFFD54F) else Color(0xFF00E5FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Text(
                                text = if (isPro) "Full VIP features unlocked • Lifetime access" else "Auto SMS reader, unlimited reminders & cloud sync",
                                color = TextSecondary,
                                fontSize = 11.sp,
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        item { SectionLabel("App", "") }
        item {
            MorePrimaryActionRow(
                title = "App settings",
                subtitle = "Language, currency and your app preferences.",
                accent = AccentBlue,
                icon = Icons.Rounded.Settings,
                onClick = onOpenSettings,
            )
        }
        item {
            MorePrimaryActionRow(
                title = "Preferred language",
                subtitle = PocketNativePreferencesStore.languageOptionFor(preferencesStore.languageCode()).label,
                accent = AccentGreen,
                icon = Icons.Rounded.AccountCircle,
                onClick = onOpenSettings,
            )
        }
        item { SectionLabel("Safety", "") }
        item {
            MorePrimaryActionRow(
                title = "Backup & Restore",
                subtitle = "Keep device backup ready and recover safely if needed.",
                accent = AccentPurple,
                icon = Icons.Rounded.SettingsBackupRestore,
                onClick = onOpenBackupRestore,
            )
        }
        item {
            MorePrimaryActionRow(
                title = "App Lock",
                subtitle = "Protect Pocket Pro with a private PIN lock.",
                accent = AccentBlue,
                icon = Icons.Rounded.Lock,
                onClick = onOpenAppLock,
            )
        }
        item { SectionLabel("Smart tools", "") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                MorePrimaryActionRow(
                    title = "Notifications",
                    subtitle = "",
                    accent = AccentOrange,
                    icon = Icons.Rounded.NotificationsActive,
                    onClick = onOpenNotifications,
                )
                MorePrimaryActionRow(
                    title = "Notification settings",
                    subtitle = "",
                    accent = AccentPink,
                    icon = Icons.Rounded.Settings,
                    onClick = onOpenNotificationSettings,
                )
                MorePrimaryActionRow(
                    title = "Plans & reminders",
                    subtitle = "",
                    accent = AccentGreen,
                    icon = Icons.Rounded.Badge,
                    onClick = onOpenPlans,
                )
                MorePrimaryActionRow(
                    title = "Category manager",
                    subtitle = "",
                    accent = AccentPurple,
                    icon = Icons.Rounded.Category,
                    onClick = onOpenCategoryManager,
                )
                MorePrimaryActionRow(
                    title = "Reports & exports",
                    subtitle = "",
                    accent = AccentBlue,
                    icon = Icons.Rounded.PieChart,
                    onClick = onOpenReports,
                )
                MorePrimaryActionRow(
                    title = "Export center",
                    subtitle = "",
                    accent = AccentPurple,
                    icon = Icons.Rounded.SettingsBackupRestore,
                    onClick = onOpenExportCenter,
                )
                MorePrimaryActionRow(
                    title = "Search records",
                    subtitle = "",
                    accent = AccentGreen,
                    icon = Icons.Rounded.Search,
                    onClick = onOpenSearch,
                )
                MorePrimaryActionRow(
                    title = "Accounts & statements",
                    subtitle = "",
                    accent = AccentBlue,
                    icon = Icons.Rounded.AccountBalanceWallet,
                    onClick = onOpenAccounts,
                )
                MorePrimaryActionRow(
                    title = "User guide",
                    subtitle = "A complete Pocket Pro walkthrough, from your first entry to backup and reports.",
                    accent = AccentBlue,
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    onClick = onOpenUserGuide,
                )
                MorePrimaryActionRow(
                    title = "Help & support",
                    subtitle = "",
                    accent = AccentOrange,
                    icon = Icons.AutoMirrored.Rounded.HelpCenter,
                    onClick = onOpenHelpSupport,
                )
                MorePrimaryActionRow(
                    title = "Data tools",
                    subtitle = "",
                    accent = AccentGreen,
                    icon = Icons.Rounded.Tune,
                    onClick = onOpenDataTools,
                )
            }
        }
    }
}

@Composable
private fun MorePrimaryActionRow(
    title: String,
    subtitle: String,
    accent: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    trailing: (@Composable (() -> Unit))? = null,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (subtitle.isNotBlank()) {
                    NativeInlineInfoButton(title = title, message = subtitle)
                }
            }
            trailing?.invoke()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TextSecondary.copy(alpha = 0.12f), RoundedCornerShape(99.dp)),
        )
    }
}
