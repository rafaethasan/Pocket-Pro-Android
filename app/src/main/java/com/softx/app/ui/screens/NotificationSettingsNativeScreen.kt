package com.softx.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.PocketReminderScheduler
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

@Composable
fun NotificationSettingsNativeScreen(
    preferencesStore: PocketNativePreferencesStore,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var reminderEnabled by remember { mutableStateOf(preferencesStore.nightlyReminderEnabled()) }
    var soundEnabled by remember { mutableStateOf(preferencesStore.notificationSoundEnabled()) }
    var vibrationEnabled by remember { mutableStateOf(preferencesStore.notificationVibrationEnabled()) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            NativeFeatureTopBar(
                title = "Notification Settings",
                subtitle = "Control nightly smart reminders, alert timing, and how Pocket Pro gets your attention.",
                onBack = onBack,
            )
        }
        item {
            NativeEmptyCard(
                "Pocket Pro can remind you about bills, installments, paybacks, due collections, and missing daily entries from one smart alert system.",
            )
        }
        item {
            NotificationSettingsRow(
                title = "Nightly smart reminder",
                subtitle = "Every day at 10:00 PM in this phone's local time, even when the app is closed.",
                accent = AccentPurple,
                stateLabel = if (reminderEnabled) "On" else "Off",
                onClick = {
                    val next = !reminderEnabled
                    reminderEnabled = next
                    preferencesStore.setNightlyReminderEnabled(next)
                    if (next) {
                        PocketReminderScheduler.ensureDailyReminder(context)
                    } else {
                        PocketReminderScheduler.cancelDailyReminder(context)
                    }
                },
            )
        }
        item {
            NativeEmptyCard(
                "Daily check-in is fixed at 10:00 PM based on the phone's current time zone. It automatically adjusts when the user travels or changes time zones.",
            )
        }
        item {
            SectionLabel(title = "Alert style", trailing = "Control sound & vibration")
        }
        item {
            NotificationSettingsRow(
                title = "Sound",
                subtitle = "Play an alert sound when Pocket Pro sends a reminder notification.",
                accent = AccentGreen,
                stateLabel = if (soundEnabled) "On" else "Off",
                onClick = {
                    soundEnabled = !soundEnabled
                    preferencesStore.setNotificationSoundEnabled(soundEnabled)
                },
            )
        }
        item {
            NotificationSettingsRow(
                title = "Vibration",
                subtitle = "Use vibration together with reminders so important dues are harder to miss.",
                accent = AccentOrange,
                stateLabel = if (vibrationEnabled) "On" else "Off",
                onClick = {
                    vibrationEnabled = !vibrationEnabled
                    preferencesStore.setNotificationVibrationEnabled(vibrationEnabled)
                },
            )
        }
        item {
            NativeEmptyCard(
                "Lead-time alerts stay active for monthly reminders, installments, and loan follow-ups: 10 days, 5 days, 3 days, 1 day, and same day.",
            )
        }
    }
}
@Composable
private fun NotificationSettingsRow(
    title: String,
    subtitle: String,
    accent: Color,
    stateLabel: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            NativeInlineInfoButton(title = title, message = subtitle)
        }
        Text(
            text = stateLabel,
            color = accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
        color = TextSecondary.copy(alpha = 0.12f),
    )
}
