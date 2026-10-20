package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.BuildConfig
import com.softx.app.data.PocketNativeCurrencyOption
import com.softx.app.data.PocketNativeLanguageOption
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun SettingsNativeScreen(
    preferencesStore: PocketNativePreferencesStore,
    onBack: () -> Unit,
) {
    var selectedLanguageCode by remember { mutableStateOf(preferencesStore.languageCode()) }
    var selectedCurrencyCode by remember { mutableStateOf(preferencesStore.currencyCode()) }
    val uriHandler = LocalUriHandler.current
    val privacyUrl = "${BuildConfig.POCKET_BASE_URL}pocket/privacy-policy"
    val accountDeletionUrl = "${BuildConfig.POCKET_BASE_URL}pocket/account-deletion"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1118), Color(0xFF111827), Color(0xFF0B0F15)),
                ),
            )
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(bottom = 176.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(34.dp)) }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = AccentGreen,
                        )
                        Text("Default: English", color = AccentGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text("Pocket Pro Settings", color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Pocket Pro opens in English the first time. You can change language and currency any time from here.",
                    color = TextSecondary,
                    lineHeight = 22.sp,
                )
                HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
            }
        }

        item {
            SectionHeader(title = "Language", subtitle = "Choose the language you want to read the app in.")
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Current setup", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        "${PocketNativePreferencesStore.languageOptionFor(selectedLanguageCode).label} • ${PocketNativePreferencesStore.currencyOptionFor(selectedCurrencyCode).code}",
                        color = TextSecondary,
                    )
                }
                Text(
                    text = "Live",
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        items(PocketNativePreferencesStore.LANGUAGE_OPTIONS) { option ->
            SettingsChoiceCard(
                title = option.label,
                subtitle = "${option.nativeLabel} • Popular currencies: ${option.relatedCurrencies.joinToString(", ")}",
                selected = option.code == selectedLanguageCode,
                accent = AccentBlue,
                icon = Icons.Rounded.Language,
                onClick = {
                    selectedLanguageCode = option.code
                    preferencesStore.setLanguageCode(option.code)
                },
            )
        }

        item {
            SectionHeader(title = "Preferred Currency", subtitle = "Choose the money format you want to see across Pocket Pro.")
        }

        items(PocketNativePreferencesStore.CURRENCY_OPTIONS) { option ->
            SettingsChoiceCard(
                title = "${option.code} • ${option.label}",
                subtitle = "Symbol ${option.symbol}",
                selected = option.code == selectedCurrencyCode,
                accent = AccentGreen,
                icon = Icons.Rounded.Payments,
                onClick = {
                    selectedCurrencyCode = option.code
                    preferencesStore.setCurrencyCode(option.code)
                },
            )
        }

        item {
            SectionHeader(title = "Smart Status", subtitle = "Pocket Pro keeps a few important helpers active in the background.")
        }

        item {
            SettingsChoiceCard(
                title = "Nightly smart reminder",
                subtitle = "Daily check-in reminder runs around 9 PM for bills, entries, and due tasks.",
                selected = true,
                accent = AccentPurple,
                icon = Icons.Rounded.NotificationsActive,
                onClick = {},
            )
        }

        item {
            SettingsChoiceCard(
                title = "Saved sign-in",
                subtitle = if (preferencesStore.signedInUser() != null) {
                    "Your Pocket Pro account is remembered on this device."
                } else {
                    "Sign in once to keep your session ready on this device."
                },
                selected = preferencesStore.signedInUser() != null,
                accent = AccentGreen,
                icon = Icons.Rounded.Person,
                onClick = {},
            )
        }

        item {
            SettingsChoiceCard(
                title = "App lock",
                subtitle = if (preferencesStore.appLockEnabled()) {
                    "PIN protection is on before the app opens."
                } else {
                    "PIN protection is currently off."
                },
                selected = preferencesStore.appLockEnabled(),
                accent = AccentBlue,
                icon = Icons.Rounded.Lock,
                onClick = {},
            )
        }

        item {
            SettingsChoiceCard(
                title = "Offline core mode",
                subtitle = "Pocket Pro keeps core add-entry flows ready even when the internet is unstable.",
                selected = true,
                accent = AccentOrange,
                icon = Icons.Rounded.Storage,
                onClick = {},
            )
        }

        item {
            SectionHeader(title = "Privacy & Account", subtitle = "Open policy pages and account removal options.")
        }

        item {
            SettingsActionCard(
                title = "Privacy policy",
                subtitle = "Review how Pocket Pro handles account and finance data.",
                accent = AccentBlue,
                icon = Icons.Rounded.PrivacyTip,
                actionLabel = "Open",
                onClick = { uriHandler.openUri(privacyUrl) },
            )
        }

        item {
            SettingsActionCard(
                title = "Delete account",
                subtitle = "Open the official Pocket Pro account deletion request page.",
                accent = Color(0xFFFF5B6E),
                icon = Icons.Rounded.DeleteForever,
                actionLabel = "Open",
                onClick = { uriHandler.openUri(accountDeletionUrl) },
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("More tools", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "Open the More tab anytime for notifications, plans, category manager, reports, accounts, backup, app lock, and profile tools.",
                    color = TextSecondary,
                    lineHeight = 22.sp,
                )
                HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
                Text("Good to know", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "These settings only change how Pocket Pro looks for you. Your data stays the same, and you can update language or currency any time later.",
                    color = TextSecondary,
                    lineHeight = 22.sp,
                )
                HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text(subtitle, color = TextSecondary, lineHeight = 21.sp)
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, color = TextSecondary, lineHeight = 20.sp)
            }
            Text(
                text = actionLabel,
                color = accent,
                fontWeight = FontWeight.SemiBold,
            )
        }
        HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
    }
}

@Composable
private fun SettingsChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, color = TextSecondary, lineHeight = 20.sp)
            }
            Text(
                text = if (selected) "Selected" else "Choose",
                color = if (selected) AccentOrange else TextSecondary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
    }
}
