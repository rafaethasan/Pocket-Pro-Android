package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeUser
import com.softx.app.data.pocketMergeLocalProfile
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary

val LocalPocketPremiumPlansLauncher = staticCompositionLocalOf<(() -> Unit)?> { null }

@Suppress("UNUSED_PARAMETER")
fun pocketNativeHasFullAccess(user: PocketNativeUser?): Boolean {
    return true
}

fun pocketNativePreferredFeatureUser(
    responseUser: PocketNativeUser?,
    signedInUser: PocketNativeUser?,
): PocketNativeUser? {
    val preferred = when {
        pocketNativeHasFullAccess(responseUser) -> responseUser
        pocketNativeHasFullAccess(signedInUser) -> signedInUser
        responseUser != null -> responseUser
        else -> signedInUser
    }
    val localProfile = if (preferred === responseUser) signedInUser else responseUser
    return pocketMergeLocalProfile(preferred, localProfile)
}

data class PocketNativeFeatureGate(
    val featureKey: String,
    val title: String,
    val granted: Boolean,
    val previewAllowed: Boolean,
    val message: String,
)

@Suppress("UNUSED_PARAMETER")
fun pocketNativeFeatureGate(
    user: PocketNativeUser?,
    featureKey: String,
): PocketNativeFeatureGate {
    val cleanKey = featureKey.trim().lowercase()
    val title = when (cleanKey) {
        "pdf_export" -> "PDF Export"
        "statement_export" -> "Bank Statement Export"
        "advanced_reports" -> "Advanced Reports"
        "calendar_view" -> "Calendar View"
        "recurring_transactions" -> "Recurring Transactions"
        "bills_planner" -> "Bills Planner"
        "advanced_reminders" -> "Advanced Reminders"
        "receipt_attach" -> "Receipt Attach"
        "backup_restore" -> "Backup & Restore"
        "app_lock" -> "App Lock"
        "smart_insights" -> "Smart Insights"
        "advanced_search" -> "Advanced Search Filters"
        "installment_insights" -> "Installment Insights"
        else -> cleanKey.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
    return PocketNativeFeatureGate(
        featureKey = cleanKey,
        title = title,
        granted = true,
        previewAllowed = true,
        message = "",
    )
}

@Composable
fun PocketNativeProBadge() {
    Box(
        modifier = Modifier
            .background(AccentPurple.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = "Free",
            color = AccentPurple,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun PocketNativePreviewNotice(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(AccentPurple.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.StarOutline,
                    contentDescription = null,
                    tint = AccentPurple,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Text(body, color = TextSecondary, lineHeight = 20.sp)
            }
        }
        HorizontalDivider(color = SurfaceCard)
    }
}

@Composable
fun PocketNativeUpgradeDialog(
    gate: PocketNativeFeatureGate,
    onDismiss: () -> Unit,
) {
    val openPremiumPlans = LocalPocketPremiumPlansLauncher.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (openPremiumPlans != null) {
                        onDismiss()
                        openPremiumPlans.invoke()
                    } else {
                        onDismiss()
                    }
                },
            ) {
                Text("Continue", color = AccentPurple, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now", color = TextSecondary)
            }
        },
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .background(AccentPurple.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.StarOutline,
                        contentDescription = null,
                        tint = AccentPurple,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(gate.title, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Pocket Pro feature", color = TextSecondary, fontSize = 12.sp)
                }
            }
        },
        text = {
            Text(
                text = gate.message.ifBlank { "This feature is available for free." },
                color = TextSecondary,
                lineHeight = 20.sp,
            )
        },
        containerColor = SurfaceCardSoft,
        shape = RoundedCornerShape(20.dp),
    )
}
