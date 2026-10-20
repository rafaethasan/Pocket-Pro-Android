package com.softx.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.SsidChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeAnalyticsCategory
import com.softx.app.data.PocketNativeDashboardResponse
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ReportsNativeScreen(
    repository: PocketNativeRepository,
    signedInUser: PocketNativeUser?,
    onOpenCalendar: () -> Unit = {},
    onOpenSmartInsights: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val initialCachedResponse = remember(repository) { repository.cachedDashboard() }
    var loading by remember { mutableStateOf(initialCachedResponse == null) }
    var errorText by remember { mutableStateOf("") }
    var dashboardResponse by remember { mutableStateOf<PocketNativeDashboardResponse?>(initialCachedResponse) }
    var activeSection by remember { mutableStateOf("overview") }
    var lockedFeature by remember { mutableStateOf<PocketNativeFeatureGate?>(null) }

    LaunchedEffect(Unit) {
        errorText = ""
        if (dashboardResponse == null) {
            loading = true
        }
        val result = repository.dashboard()
        loading = false
        if (result.isSuccess) {
            dashboardResponse = result.getOrNull()
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Reports could not be loaded."
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

    val dashboard = dashboardResponse?.dashboard
    if (dashboard == null) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 176.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                NativeReportErrorCard(
                    errorText = errorText.ifBlank { "Reports are not ready yet." },
                    signedInUser = signedInUser,
                )
            }
        }
        return
    }

    val summary = dashboard.summary
    val currencySymbol = dashboard.currencySymbol
    val topCategory = dashboard.analytics.categories.maxByOrNull { it.amount }
    val currentUser = pocketNativePreferredFeatureUser(dashboardResponse?.user, signedInUser)
    val exportGate = remember(currentUser) { pocketNativeFeatureGate(currentUser, "pdf_export") }
    val advancedReportsGate = remember(currentUser) { pocketNativeFeatureGate(currentUser, "advanced_reports") }
    val calendarGate = remember(currentUser) { pocketNativeFeatureGate(currentUser, "calendar_view") }
    val smartInsightsGate = remember(currentUser) { pocketNativeFeatureGate(currentUser, "smart_insights") }
    val weeklyAverage = if (dashboard.analytics.weekly.isNotEmpty()) {
        dashboard.analytics.weekly.sumOf { it.amount } / dashboard.analytics.weekly.size
    } else {
        0.0
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 176.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Reports", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                Text(
                    "See summary, budget progress, categories and weekly spending in one place.",
                    color = TextSecondary,
                    lineHeight = 20.sp,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCalendar() }
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                    Text("Calendar View", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    if (!calendarGate.granted) {
                        PocketNativeProBadge()
                    }
                }
                Text(
                    "Monthly calendar, guided insights, and advanced analytics are free for everyone.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSmartInsights() }
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.TipsAndUpdates, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                    Text("Smart Insights", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    if (!smartInsightsGate.granted) {
                        PocketNativeProBadge()
                    }
                }
            }
            HorizontalDivider(color = SurfaceCardSoft)
        }

        if (!advancedReportsGate.granted) {
            item {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                    PocketNativePreviewNotice(
                        title = "Free access",
                        body = "Analytics, exports, and guided insights are available without a subscription.",
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportMetricCard("Balance", nativeCurrency(summary.totalBalance, currencySymbol), AccentGreen, Modifier.weight(1f))
                ReportMetricCard("Budget Left", nativeCurrency(summary.budgetRemaining, currencySymbol), AccentBlue, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportMetricCard("Budget Used", "${summary.budgetUsedPercent}%", AccentOrange, Modifier.weight(1f))
                ReportMetricCard("Goals", "${summary.goalProgressPercent}%", AccentPink, Modifier.weight(1f))
            }
        }

        item {
            ReportSectionTabs(
                activeSection = activeSection,
                advancedReportsGate = advancedReportsGate,
                onSectionChange = { section ->
                    activeSection = section
                    if (section != "overview" && !advancedReportsGate.granted) {
                        lockedFeature = advancedReportsGate
                    }
                },
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReportMetricCard(
                    "Top Category",
                    topCategory?.label ?: "No data",
                    AccentPurple,
                    Modifier.weight(1f),
                    showProBadge = !advancedReportsGate.granted,
                )
                ReportMetricCard(
                    "Weekly Avg",
                    nativeCurrency(weeklyAverage, currencySymbol),
                    AccentBlue,
                    Modifier.weight(1f),
                    showProBadge = !advancedReportsGate.granted,
                )
            }
        }

        if (!advancedReportsGate.granted) {
            item {
                NativeReportPreviewNotice(
                    title = "Free access",
                    body = "Categories and weekly analytics are available without a subscription.",
                )
            }
        }

        if (activeSection == "overview" || activeSection == "categories") {
            item {
                NativeReportsSectionTitle(
                    title = "Category breakdown",
                    showProBadge = !advancedReportsGate.granted,
                )
            }

            items(dashboard.analytics.categories) { item ->
                CategoryBreakdownCard(item = item, currencySymbol = currencySymbol)
            }
        }

        if (activeSection == "overview" || activeSection == "weekly") {
            item {
                NativeReportsSectionTitle(
                    title = "Weekly spending",
                    showProBadge = !advancedReportsGate.granted,
                )
            }

            items(dashboard.analytics.weekly) { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.SsidChart, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        Text(point.label, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                    Text(nativeCurrency(point.amount, currencySymbol), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                HorizontalDivider(color = SurfaceCardSoft)
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Export & share", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Button(
                    onClick = {
                        if (!exportGate.granted) {
                            lockedFeature = exportGate
                            return@Button
                        }
                        val pdfUrl = dashboard.reportsPdfUrl
                        if (pdfUrl.isBlank()) {
                            Toast.makeText(context, "Report PDF is not ready yet.", Toast.LENGTH_SHORT).show()
                        } else {
                            scope.launch {
                                val result = repository.downloadPdfToCache(
                                    pdfUrl,
                                    "pocket-pro-reports.pdf",
                                )
                                result.onSuccess { file ->
                                    openPdfFile(context, file)
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        it.message ?: "Report PDF could not be downloaded.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple.copy(alpha = 0.18f),
                        contentColor = TextPrimary,
                    ),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Export PDF", fontWeight = FontWeight.Bold)
                    if (!exportGate.granted) {
                        Spacer(modifier = Modifier.size(8.dp))
                        PocketNativeProBadge()
                    }
                }
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
private fun ReportSectionTabs(
    activeSection: String,
    advancedReportsGate: PocketNativeFeatureGate,
    onSectionChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        listOf(
            "overview" to "Overview",
            "categories" to "Categories",
            "weekly" to "Weekly",
        ).forEach { (key, label) ->
            val selected = key == activeSection
            val proTab = key != "overview"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSectionChange(key) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        label,
                        color = if (selected) TextPrimary else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                    if (proTab && !advancedReportsGate.granted) {
                        PocketNativeProBadge()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(3.dp)
                        .background(if (selected) AccentPurple else Color.Transparent, RoundedCornerShape(50)),
                )
            }
        }
    }
    HorizontalDivider(color = SurfaceCardSoft)
}

@Composable
private fun NativeReportErrorCard(
    errorText: String,
    signedInUser: PocketNativeUser?,
) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Reports are getting ready", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "${signedInUser?.fullName ?: "Pocket Pro User"}, $errorText",
                color = TextSecondary,
                lineHeight = 22.sp,
            )
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    showProBadge: Boolean = false,
) {
    Column(
        modifier = modifier
            .background(SurfaceCard)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
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
    }
}

@Composable
private fun NativeReportsSectionTitle(
    title: String,
    showProBadge: Boolean = false,
) {
    Row(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        if (showProBadge) {
            PocketNativeProBadge()
        }
    }
}

@Composable
private fun NativeReportPreviewNotice(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(title, color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(body, color = TextSecondary, lineHeight = 18.sp)
        HorizontalDivider(color = SurfaceCardSoft)
    }
}

@Composable
private fun CategoryBreakdownCard(
    item: PocketNativeAnalyticsCategory,
    currencySymbol: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colorFromHex(item.colorHex)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.label, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(nativeCurrency(item.amount, currencySymbol), color = TextSecondary)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.PieChart, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
            Text("${item.percent}%", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
    HorizontalDivider(color = SurfaceCardSoft)
}

private fun nativeCurrency(amount: Double, symbol: String): String {
    val clean = if (amount % 1.0 == 0.0) amount.toLong().toString() else String.format(Locale.US, "%.2f", amount)
    return "$symbol$clean"
}
