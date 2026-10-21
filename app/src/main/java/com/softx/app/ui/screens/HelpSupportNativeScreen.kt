package com.softx.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpCenter
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun HelpSupportNativeScreen(
    onBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            NativeFeatureTopBar(
                title = "Help & Support",
                subtitle = "Quick answers, support contact, and privacy guidance for Pocket Pro users.",
                onBack = onBack,
            )
        }
        item {
            NativeEmptyCard(
                "Use this screen when users need help with login, backup, reminders, exports, or privacy-related questions.",
            )
        }
        item {
            HelpSupportRow(
                title = "Contact support",
                subtitle = "Email the Pocket Pro support team for account or app help.",
                icon = Icons.Rounded.Email,
                accent = AccentBlue,
                onClick = { uriHandler.openUri("mailto:support@corexbd.com") },
            )
        }
        item {
            HelpSupportRow(
                title = "Privacy & security",
                subtitle = "Use app lock, backup, and saved sign-in carefully on shared devices.",
                icon = Icons.Rounded.Security,
                accent = AccentGreen,
                onClick = {},
            )
        }
        item {
            HelpSupportRow(
                title = "Reminder guide",
                subtitle = "Pocket Pro supports monthly alerts, installment reminders, loan follow-ups, and nightly smart check-ins.",
                icon = Icons.AutoMirrored.Rounded.HelpCenter,
                accent = AccentOrange,
                onClick = {},
            )
        }
        item {
            HelpSupportRow(
                title = "Data policy note",
                subtitle = "Local cache, reminders, and backup files can be managed from More and Backup tools.",
                icon = Icons.Rounded.Policy,
                accent = AccentPurple,
                onClick = {},
            )
        }
    }
}

@Composable
private fun HelpSupportRow(
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
