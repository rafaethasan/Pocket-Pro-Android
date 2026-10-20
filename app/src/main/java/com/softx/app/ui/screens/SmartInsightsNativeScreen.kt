package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.WarningAmber
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
import com.softx.app.data.PocketNativeDashboard
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlin.math.abs
import java.util.Locale

@Composable
fun SmartInsightsNativeScreen(
    repository: PocketNativeRepository,
    signedInUser: PocketNativeUser?,
    onBack: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }
    var dashboard by remember { mutableStateOf<PocketNativeDashboard?>(null) }
    val gate = remember(signedInUser) { pocketNativeFeatureGate(signedInUser, "smart_insights") }

    LaunchedEffect(Unit) {
        loading = true
        errorText = ""
        val result = repository.dashboard()
        loading = false
        if (result.isSuccess) {
            dashboard = result.getOrNull()?.dashboard
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Insights could not be loaded."
        }
    }

    if (loading) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 48.dp),
                color = AccentPurple,
            )
        }
        return
    }

    val data = dashboard
    if (data == null) {
        NativeSimpleErrorCard(
            title = "Smart Insights",
            body = errorText.ifBlank { "Pocket Pro insights are not ready yet." },
            onBack = onBack,
        )
        return
    }

    val summary = data.summary
    val currencySymbol = data.currencySymbol
    val insights = remember(data) { buildSmartInsights(data) }
    val topCategory = data.analytics.categories.maxByOrNull { it.amount }
    val weeklyAverage = if (data.analytics.weekly.isNotEmpty()) {
        data.analytics.weekly.sumOf { it.amount } / data.analytics.weekly.size
    } else 0.0

    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(34.dp)) }
        item {
            NativeFeatureTopBar(
                title = "Smart Insights",
                subtitle = "Read simple money signals from your current Pocket Pro data.",
                onBack = onBack,
            )
        }
        if (!gate.granted) {
            item {
                PocketNativePreviewNotice(
                    title = "Free access",
                    body = "Smart Insights are available without a subscription.",
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmartMetricCard("Top Category", topCategory?.label ?: "No data", AccentPurple, Modifier.weight(1f), showProBadge = !gate.granted)
                SmartMetricCard("Weekly Avg", smartShortMoney(weeklyAverage, currencySymbol), AccentBlue, Modifier.weight(1f), showProBadge = !gate.granted)
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmartMetricCard("Budget Used", "${summary.budgetUsedPercent}%", AccentOrange, Modifier.weight(1f))
                SmartMetricCard("Goal Saved", smartShortMoney(summary.goalSaved, currencySymbol), AccentGreen, Modifier.weight(1f))
            }
        }
        item {
            SmartSectionTitle(
                title = "Actionable insights",
                showProBadge = !gate.granted,
            )
        }
        items(insights) { insight ->
            InsightRowCard(
                title = insight.title,
                body = insight.body,
                accent = insight.accent,
                icon = insight.icon,
            )
        }
        item { Spacer(modifier = Modifier.height(34.dp)) }
    }
}

private data class SmartInsightModel(
    val title: String,
    val body: String,
    val accent: Color,
    val icon: ImageVector,
)

private fun buildSmartInsights(dashboard: PocketNativeDashboard): List<SmartInsightModel> {
    val summary = dashboard.summary
    val currencySymbol = dashboard.currencySymbol
    val topCategory = dashboard.analytics.categories.maxByOrNull { it.amount }
    val highestWeek = dashboard.analytics.weekly.maxByOrNull { it.amount }
    val monthlyNet = summary.monthIncome - summary.monthExpense

    val budgetInsight = when {
        summary.budgetUsedPercent >= 90 -> SmartInsightModel(
            "Budget pressure is high",
            "You already used ${summary.budgetUsedPercent}% of your budget. Try slowing down non-essential spending for the rest of the month.",
            AccentPink,
            Icons.Rounded.WarningAmber,
        )
        summary.budgetUsedPercent >= 70 -> SmartInsightModel(
            "Budget is moving fast",
            "You used ${summary.budgetUsedPercent}% of your budget so far. Keep an eye on daily expenses to stay safe.",
            AccentOrange,
            Icons.Rounded.AutoGraph,
        )
        else -> SmartInsightModel(
            "Budget is under control",
            "Your current budget use is ${summary.budgetUsedPercent}%, which is still in a healthy range.",
            AccentGreen,
            Icons.Rounded.Savings,
        )
    }

    val categoryInsight = SmartInsightModel(
        "Top spending category",
        if (topCategory != null) {
            "${topCategory.label} is your highest expense area at ${smartShortMoney(topCategory.amount, currencySymbol)}. Review this category first if you want to save more."
        } else {
            "Add a few expenses and Pocket Pro will point out your highest spending category here."
        },
        AccentPurple,
        Icons.Rounded.PieChart,
    )

    val cashflowInsight = when {
        monthlyNet > 0 -> SmartInsightModel(
            "Cashflow is positive",
            "This month you are ahead by ${smartShortMoney(monthlyNet, currencySymbol)}. Consider moving part of it into savings.",
            AccentGreen,
            Icons.Rounded.Savings,
        )
        monthlyNet < 0 -> SmartInsightModel(
            "Cashflow needs attention",
            "This month expenses are ahead by ${smartShortMoney(abs(monthlyNet), currencySymbol)}. A small cut in your top category could balance things faster.",
            AccentPink,
            Icons.Rounded.WarningAmber,
        )
        else -> SmartInsightModel(
            "Cashflow is balanced",
            "Income and expense are currently even this month. One extra saving step can move you into a positive month.",
            AccentBlue,
            Icons.Rounded.AutoGraph,
        )
    }

    val weeklyInsight = SmartInsightModel(
        "Weekly pattern",
        if (highestWeek != null) {
            "${highestWeek.label} is your highest spending point at ${smartShortMoney(highestWeek.amount, currencySymbol)}. That is a good moment to review where money moved fastest."
        } else {
            "Weekly spending patterns will appear here after you build a little more activity."
        },
        AccentBlue,
        Icons.Rounded.Lightbulb,
    )

    return listOf(categoryInsight, budgetInsight, cashflowInsight, weeklyInsight)
}

@Composable
private fun InsightRowCard(
    title: String,
    body: String,
    accent: Color,
    icon: ImageVector,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Text(body, color = TextSecondary, lineHeight = 21.sp)
        HorizontalDivider(color = SurfaceCard)
    }
}

private fun smartShortMoney(amount: Double, symbol: String): String {
    if (amount == 0.0) return "${symbol}0"
    return if (amount >= 1000) {
        "$symbol${String.format(Locale.US, "%.1fk", amount / 1000.0)}"
    } else {
        "$symbol${amount.toInt()}"
    }
}

@Composable
private fun SmartMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    showProBadge: Boolean = false,
) {
    Column(
        modifier = modifier
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, color = TextSecondary)
            if (showProBadge) {
                PocketNativeProBadge()
            }
        }
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        HorizontalDivider(color = SurfaceCard)
    }
}

@Composable
private fun SmartSectionTitle(
    title: String,
    showProBadge: Boolean = false,
) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        if (showProBadge) {
            PocketNativeProBadge()
        }
    }
}
