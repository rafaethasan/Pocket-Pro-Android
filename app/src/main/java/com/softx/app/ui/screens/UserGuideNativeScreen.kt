package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material.icons.rounded.Start
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

/** A self-contained, offline-first guide so a new user never has to guess where a feature lives. */
@Composable
fun UserGuideNativeScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            NativeFeatureTopBar(
                title = "User guide",
                subtitle = "A full Pocket Pro walkthrough. Tap each section to learn what it is for and where to find it.",
                onBack = onBack,
            )
        }
        item {
            Text(
                "Start here",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Use the bottom bar for daily work: Home, Records, Add, Reports and More.",
                color = TextSecondary,
                fontSize = 14.sp,
            )
        }
        item {
            UserGuideCard(
                title = "1. Set up your money space",
                destination = "More → Accounts & statements",
                icon = Icons.Rounded.AccountBalanceWallet,
                accent = AccentBlue,
                steps = listOf(
                    "Add the accounts you use: cash, bank, mobile wallet or card.",
                    "Choose your preferred language and currency from More → App settings.",
                    "Your Home balance combines the accounts you include.",
                ),
            )
        }
        item {
            UserGuideCard(
                title = "2. Add daily income and expense",
                destination = "Bottom bar → Add",
                icon = Icons.Rounded.Payments,
                accent = AccentGreen,
                steps = listOf(
                    "Choose Income or Expense, then enter amount, account, category and date.",
                    "Add a note or receipt when you need proof later.",
                    "Save once; the balance, Records and Reports update together.",
                ),
            )
        }
        item {
            UserGuideCard(
                title = "3. Track loans, dues and planned payments",
                destination = "More → Plans & reminders",
                icon = Icons.Rounded.Start,
                accent = AccentOrange,
                steps = listOf(
                    "Create a plan for installments, bills, money you borrowed or money you lent.",
                    "Record each repayment against the same plan so the remaining amount stays correct.",
                    "Turn on notifications to receive due and follow-up reminders.",
                ),
            )
        }
        item {
            UserGuideCard(
                title = "4. Find or fix a record",
                destination = "Bottom bar → Records, or More → Search records",
                icon = Icons.Rounded.Category,
                accent = AccentPurple,
                steps = listOf(
                    "Filter by date, account, category or transaction type.",
                    "Open any record to edit its amount, category, note or receipt.",
                    "Use Category manager when you need a clearer custom category.",
                ),
            )
        }
        item {
            UserGuideCard(
                title = "5. Understand your progress",
                destination = "Bottom bar → Reports",
                icon = Icons.Rounded.Assessment,
                accent = AccentBlue,
                steps = listOf(
                    "Review spending by category, month and account.",
                    "Use Calendar View for a daily timeline and Smart Insights for patterns.",
                    "Export a statement from More → Export center when you need to share it.",
                ),
            )
        }
        item {
            UserGuideCard(
                title = "6. Keep your data safe",
                destination = "More → Backup & Restore / App Lock",
                icon = Icons.Rounded.SettingsBackupRestore,
                accent = AccentGreen,
                steps = listOf(
                    "Create a backup before changing phone or reinstalling the app.",
                    "Keep the backup file in a place you control, such as Drive or your device storage.",
                    "Enable App Lock on shared devices and never share your PIN.",
                ),
            )
        }
        item {
            UserGuideCard(
                title = "Quick daily habit",
                destination = "Home → Add transaction",
                icon = Icons.Rounded.Lock,
                accent = AccentOrange,
                steps = listOf(
                    "Add a transaction as soon as money moves.",
                    "At night, check your Home balance and any due reminders.",
                    "Review Reports once a week to spot unnecessary spending.",
                ),
            )
        }
    }
}

@Composable
private fun UserGuideCard(
    title: String,
    destination: String,
    icon: ImageVector,
    accent: Color,
    steps: List<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = accent)
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(destination, color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        steps.forEachIndexed { index, step ->
            Text("${index + 1}. $step", color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}
