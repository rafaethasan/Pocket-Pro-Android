package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeRecurringItem
import com.softx.app.data.PocketNativeRecurringPayload
import com.softx.app.data.PocketNativeRecurringResponse
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun RecurringNativeScreen(
    repository: PocketNativeRepository,
    signedInUser: PocketNativeUser?,
    onBack: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }
    var payload by remember { mutableStateOf<PocketNativeRecurringPayload?>(null) }
    var responseUser by remember { mutableStateOf<PocketNativeUser?>(null) }
    var lockedFeature by remember { mutableStateOf<PocketNativeFeatureGate?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        errorText = ""
        val result = repository.recurring()
        loading = false
        if (result.isSuccess) {
            val response: PocketNativeRecurringResponse? = result.getOrNull()
            payload = response?.payload
            responseUser = response?.user
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Recurring templates could not be loaded."
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 48.dp),
                color = AccentPurple,
            )
        }
        return
    }

    val data = payload
    if (data == null) {
        NativeSimpleErrorCard(
            title = "Recurring paused",
            body = errorText.ifBlank { "Pocket Pro recurring templates are not ready yet." },
            onBack = onBack,
        )
        return
    }

    val currentUser = pocketNativePreferredFeatureUser(responseUser, signedInUser)
    val recurringGate = remember(currentUser) { pocketNativeFeatureGate(currentUser, "recurring_transactions") }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(modifier = Modifier.height(34.dp)) }
        item {
            NativeFeatureTopBar(
                title = "Recurring",
                subtitle = "Manage monthly income and expense templates that post on time.",
                onBack = onBack,
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!recurringGate.granted) {
                            lockedFeature = recurringGate
                        }
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Create Recurring", color = AccentBlue, fontWeight = FontWeight.Bold)
                    if (!recurringGate.granted) {
                        PocketNativeProBadge()
                    }
                }
                Icon(Icons.Rounded.Repeat, contentDescription = null, tint = AccentBlue)
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RecurringMetricLine("Templates", data.summary.totalTemplates.toString(), AccentBlue, Modifier.weight(1f))
                RecurringMetricLine("Active", data.summary.activeTemplates.toString(), AccentGreen, Modifier.weight(1f))
                RecurringMetricLine("Next Due", data.summary.nextDueDate.ifBlank { "-" }, AccentOrange, Modifier.weight(1f))
            }
        }
        if (!recurringGate.granted) {
            item {
                RecurringPreviewNotice(
                    title = "Free access",
                    body = "Create and manage recurring transactions without a subscription.",
                )
            }
        }
        item {
            SectionLabel("Recurring Templates", if (data.templates.isEmpty()) "" else "${data.templates.size} total")
        }
        if (data.templates.isEmpty()) {
            item {
                NativeEmptyCard("No recurring templates are available yet.")
            }
        } else {
            items(data.templates, key = { it.id }) { item ->
                RecurringTemplateRow(item = item, currencySymbol = data.currencySymbol)
            }
        }
    }

    lockedFeature?.let { gate ->
        PocketNativeUpgradeDialog(
            gate = gate,
            onDismiss = { lockedFeature = null },
        )
    }
}

@Composable
private fun RecurringPreviewNotice(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(title, color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(body, color = TextSecondary, lineHeight = 18.sp)
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
    }
}

@Composable
private fun RecurringMetricLine(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
    }
}

@Composable
private fun RecurringTemplateRow(
    item: PocketNativeRecurringItem,
    currencySymbol: String,
) {
    val accent = if (item.kind == "income") AccentGreen else Color(0xFFFF5B6E)
    val leadingIcon: ImageVector = when (item.kind) {
        "income" -> Icons.AutoMirrored.Rounded.TrendingUp
        "expense" -> Icons.AutoMirrored.Rounded.TrendingDown
        else -> Icons.Rounded.Receipt
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(leadingIcon, contentDescription = null, tint = accent)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    val meta = buildString {
                        append(item.label.ifBlank { item.kind.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() } })
                        if (item.category.isNotBlank()) append(" • ${item.category.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }}")
                        append(" • Day ${item.dayOfMonth}")
                    }
                    Text(meta, color = TextSecondary)
                    if (item.note.isNotBlank()) {
                        Text(item.note, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
            Text(
                nativeSharedMoney(item.amount, currencySymbol),
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                if (item.isActive) "Active • Due ${item.nextDueDate}" else "Paused",
                color = if (item.isActive) AccentBlue else TextSecondary,
                fontSize = 12.sp,
            )
            Text(
                if (item.dueInDays == 0) "Today" else "In ${item.dueInDays} day(s)",
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
    }
}
