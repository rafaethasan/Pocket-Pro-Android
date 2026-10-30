package com.softx.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Visibility
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
import com.softx.app.data.PocketNativeAccountCard
import com.softx.app.data.PocketNativeAccountsPayload
import com.softx.app.data.PocketNativeSummary
import com.softx.app.data.PocketNativeAccountsResponse
import com.softx.app.data.PocketNativeRecentActivity
import com.softx.app.data.PocketNativeRepository
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AccountsNativeScreen(
    repository: PocketNativeRepository,
    signedInUser: com.softx.app.data.PocketNativeUser?,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val initialCachedResponse = remember(repository) { repository.cachedAccounts() }
    var loading by remember { mutableStateOf(initialCachedResponse == null) }
    var errorText by remember { mutableStateOf("") }
    var payload by remember { mutableStateOf<PocketNativeAccountsPayload?>(initialCachedResponse?.payload) }
    var responseUser by remember { mutableStateOf<com.softx.app.data.PocketNativeUser?>(initialCachedResponse?.user) }
    var selectedFilter by remember { mutableStateOf(initialCachedResponse?.payload?.activeFilter ?: "ALL") }
    var lockedFeature by remember { mutableStateOf<PocketNativeFeatureGate?>(null) }

    LaunchedEffect(Unit) {
        errorText = ""
        if (payload == null) {
            loading = true
        }
        val result = repository.accounts()
        loading = false
        if (result.isSuccess) {
            val response: PocketNativeAccountsResponse? = result.getOrNull()
            payload = response?.payload
            responseUser = response?.user
            selectedFilter = response?.payload?.activeFilter ?: "ALL"
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Accounts could not be loaded."
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 48.dp),
                color = AccentBlue,
            )
        }
        return
    }

    val data = payload
    if (data == null) {
        NativeSimpleErrorCard(
            title = "Accounts paused",
            body = errorText.ifBlank { "Pocket Pro accounts are not ready yet." },
            onBack = onBack,
        )
        return
    }
    val currentUser = pocketNativePreferredFeatureUser(responseUser, signedInUser)
    val exportGate = remember(currentUser) { pocketNativeFeatureGate(currentUser, "statement_export") }

    val visibleAccounts = remember(data, selectedFilter) {
        data.accounts.filter { item ->
            when (selectedFilter) {
                "ALL" -> true
                "CASH" -> item.type == "CASH" || item.type == "MOBILE_WALLET"
                else -> item.type == selectedFilter
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            NativeFeatureTopBar(
                title = "Accounts",
                subtitle = "Track every wallet, bank, card and savings balance in one native view.",
                onBack = onBack,
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                data.filters.forEach { filter ->
                    val active = filter.key == selectedFilter
                    AccountFilterChip(
                        title = filter.label,
                        active = active,
                        onClick = { selectedFilter = filter.key },
                    )
                }
            }
        }
        item {
            AccountsSummaryStrip(
                payload = data,
                visibleAccounts = visibleAccounts,
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NativeMiniMetricLine("Accounts", "${visibleAccounts.size}", AccentBlue, Modifier.weight(1f))
                NativeMiniMetricLine(
                    "Opening",
                    nativeMoney(data.summary.openingBalance, data.currencySymbol),
                    AccentOrange,
                    Modifier.weight(1f),
                )
                NativeMiniMetricLine(
                    "Net Change",
                    nativeMoney(data.summary.netChange, data.currencySymbol),
                    AccentGreen,
                    Modifier.weight(1f),
                )
            }
        }
        item {
            SectionLabel(
                title = "All Accounts",
                trailing = "${visibleAccounts.size} accounts",
            )
        }
        if (visibleAccounts.isEmpty()) {
            item {
                NativeEmptyCard("No accounts match this filter yet.")
            }
        } else {
            items(visibleAccounts) { item ->
                AccountBalanceCard(
                    item = item,
                    currencySymbol = data.currencySymbol,
                )
            }
        }
        item {
            SectionLabel(
                title = "Recent Activity",
                trailing = if (data.recentActivity.isEmpty()) "" else "Latest",
            )
        }
        if (data.recentActivity.isEmpty()) {
            item {
                NativeEmptyCard("Recent account activity will appear here.")
            }
        } else {
            items(data.recentActivity) { item ->
                AccountActivityRow(item = item, currencySymbol = data.currencySymbol)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!exportGate.granted) {
                                lockedFeature = exportGate
                                return@clickable
                            }
                            val pdfUrl = data.statementPdfBaseUrl.ifBlank { "" }
                            if (pdfUrl.isNotBlank()) {
                                scope.launch {
                                    val result = repository.downloadPdfToCache(
                                        pdfUrl,
                                        "pocket-pro-statement.pdf",
                                    )
                                    result.onSuccess { file ->
                                        openPdfFile(context, file)
                                    }.onFailure {
                                        val localPdf = PocketNativePdfGenerator.generateReportPdf(
                                            context = context,
                                            accountName = "Statement Account",
                                            currencySymbol = data.currencySymbol,
                                            summary = PocketNativeSummary(
                                                totalBalance = data.summary.totalBalance,
                                                monthIncome = data.summary.totalIncome,
                                                monthExpense = data.summary.totalExpense,
                                            ),
                                            recentActivity = data.recentActivity,
                                        )
                                        openPdfFile(context, localPdf)
                                    }
                                }
                            } else {
                                val localPdf = PocketNativePdfGenerator.generateReportPdf(
                                    context = context,
                                    accountName = "Statement Account",
                                    currencySymbol = data.currencySymbol,
                                    summary = PocketNativeSummary(
                                        totalBalance = data.summary.totalBalance,
                                        monthIncome = data.summary.totalIncome,
                                        monthExpense = data.summary.totalExpense,
                                    ),
                                    recentActivity = data.recentActivity,
                                )
                                openPdfFile(context, localPdf)
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
                        Text("Statement Export", color = TextPrimary, fontWeight = FontWeight.Bold)
                        if (!exportGate.granted) {
                            PocketNativeProBadge()
                        }
                    }
                    Icon(Icons.Rounded.Download, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
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
private fun AccountsSummaryStrip(
    payload: PocketNativeAccountsPayload,
    visibleAccounts: List<PocketNativeAccountCard>,
) {
    val visibleBalance = visibleAccounts.sumOf { it.currentBalance }
    val visibleIncome = visibleAccounts.sumOf { it.incomeTotal }
    val visibleExpense = visibleAccounts.sumOf { it.expenseTotal }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Total Balance", color = TextPrimary.copy(alpha = 0.86f), fontWeight = FontWeight.SemiBold)
                    Text(
                        nativeMoney(visibleBalance, payload.currencySymbol),
                        color = TextPrimary,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "Across ${visibleAccounts.size} accounts",
                        color = TextPrimary.copy(alpha = 0.82f),
                    )
                }
                Text("Live", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(
                "Cash, bank, card, and wallet balances stay together here for a quick live overview.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryLine(
                    title = "Income",
                    value = nativeMoney(visibleIncome, payload.currencySymbol),
                    accent = AccentBlue,
                    modifier = Modifier.weight(1f),
                )
                SummaryLine(
                    title = "Expense",
                    value = nativeMoney(visibleExpense, payload.currencySymbol),
                    accent = AccentPurple,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = SurfaceCardSoft)
        }
    }
}

@Composable
private fun SummaryLine(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(title, color = TextPrimary.copy(alpha = 0.84f))
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
    }
}

@Composable
private fun AccountFilterChip(
    title: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(108.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            color = if (active) TextPrimary else TextSecondary,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(3.dp)
                .background(if (active) AccentPurple else Color.Transparent, RoundedCornerShape(50)),
        )
    }
}

@Composable
private fun AccountBalanceCard(
    item: PocketNativeAccountCard,
    currencySymbol: String,
) {
    val accent = colorFromHex(item.accentHex)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.typeLabel.take(1).uppercase(Locale.US),
                    color = accent,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text("${item.typeLabel} • Opening ${nativeMoney(item.openingBalance, currencySymbol)}", color = TextSecondary)
                }
            }
            Text(
                signedMoney(item.netChange, currencySymbol),
                color = if (item.netChange >= 0) AccentGreen else Color(0xFFFF5B6E),
                fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDivider(color = SurfaceCardSoft)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Current Balance", color = TextSecondary)
                Text(nativeMoney(item.currentBalance, currencySymbol), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            Text(
                text = item.typeLabel,
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
        HorizontalDivider(color = SurfaceCardSoft)
    }
}

@Composable
private fun AccountActivityRow(
    item: PocketNativeRecentActivity,
    currencySymbol: String,
) {
    val positive = item.amount >= 0
    val accent = if (positive) AccentGreen else Color(0xFFFF5B6E)
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
            Text(text = if (positive) "↗" else "↘", color = accent, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.label, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(item.note, color = TextSecondary)
            }
        }
        Text(
            signedMoney(item.amount, currencySymbol),
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
    HorizontalDivider(color = SurfaceCardSoft)
}

@Composable
private fun NativeMiniMetricLine(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 1.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
    }
}

private fun nativeMoney(amount: Double, symbol: String): String {
    val clean = if (amount % 1.0 == 0.0) amount.toLong().toString() else String.format(Locale.US, "%,.2f", amount)
    return "$symbol$clean"
}

private fun signedMoney(amount: Double, symbol: String): String {
    val clean = nativeMoney(kotlin.math.abs(amount), symbol)
    return if (amount >= 0) "+$clean" else "-$clean"
}
