package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.CalendarMonth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeRecordGroup
import com.softx.app.data.PocketNativeRecordsPayload
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
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarNativeScreen(
    repository: PocketNativeRepository,
    signedInUser: PocketNativeUser?,
    onBack: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }
    var payload by remember { mutableStateOf<PocketNativeRecordsPayload?>(null) }
    val gate = remember(signedInUser) { pocketNativeFeatureGate(signedInUser, "calendar_view") }

    LaunchedEffect(Unit) {
        loading = true
        errorText = ""
        val result = repository.records("")
        loading = false
        if (result.isSuccess) {
            payload = result.getOrNull()?.payload
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Calendar could not be loaded."
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

    val recordsPayload = payload
    if (recordsPayload == null) {
        NativeSimpleErrorCard(
            title = "Calendar is getting ready",
            body = errorText.ifBlank { "Pocket Pro calendar view is not ready yet." },
            onBack = onBack,
        )
        return
    }

    val calendarCells = remember(recordsPayload.monthValue, recordsPayload.groups) {
        buildCalendarCells(recordsPayload)
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(modifier = Modifier.height(34.dp)) }
        item {
            NativeFeatureTopBar(
                title = "Calendar View",
                subtitle = "See your month day by day with income and expense totals in one clean calendar.",
                onBack = onBack,
            )
        }
        if (!gate.granted) {
            item {
                PocketNativePreviewNotice(
                    title = "Free access",
                    body = "The complete calendar view is available without a subscription.",
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CalendarMetricCard("Month", recordsPayload.monthLabel.ifBlank { recordsPayload.monthValue }, AccentBlue, Modifier.weight(1f), showProBadge = !gate.granted)
                CalendarMetricCard("Records", recordsPayload.summary.records.toString(), AccentGreen, Modifier.weight(1f), showProBadge = !gate.granted)
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CalendarMetricCard("Income", fullMoney(recordsPayload.summary.income, recordsPayload.currencySymbol), AccentBlue, Modifier.weight(1f))
                CalendarMetricCard("Expense", fullMoney(recordsPayload.summary.expense, recordsPayload.currencySymbol), AccentPink, Modifier.weight(1f))
            }
        }
        item {
            CalendarWeekHeader()
        }
        items(calendarCells.chunked(7)) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                week.forEach { cell ->
                    CalendarDayCell(
                        cell = cell,
                        currencySymbol = recordsPayload.currencySymbol,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        item {
            CalendarSectionTitle(
                title = "Daily entries",
                showProBadge = !gate.granted,
            )
        }
        items(recordsPayload.groups) { group ->
            CalendarRecordSummaryRow(
                group = group,
                currencySymbol = recordsPayload.currencySymbol,
            )
        }
        item { Spacer(modifier = Modifier.height(34.dp)) }
    }
}

private data class CalendarDayCellModel(
    val day: Int? = null,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val hasEntries: Boolean = false,
)

private fun buildCalendarCells(payload: PocketNativeRecordsPayload): List<CalendarDayCellModel> {
    val monthValue = payload.monthValue.ifBlank { currentMonthValue() }
    val parts = monthValue.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
    val month = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 12) ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)

    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val leadingEmpty = ((calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val byDay = payload.groups.associateBy { group ->
        group.dateIso.split("-").lastOrNull()?.toIntOrNull() ?: 0
    }

    val cells = mutableListOf<CalendarDayCellModel>()
    repeat(leadingEmpty) { cells += CalendarDayCellModel() }
    for (day in 1..daysInMonth) {
        val group = byDay[day]
        cells += CalendarDayCellModel(
            day = day,
            income = group?.incomeTotal ?: 0.0,
            expense = group?.expenseTotal ?: 0.0,
            hasEntries = group != null,
        )
    }
    return cells
}

private fun currentMonthValue(): String {
    val calendar = Calendar.getInstance()
    return String.format(Locale.US, "%04d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
}

@Composable
private fun CalendarWeekHeader() {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    cell: CalendarDayCellModel,
    currencySymbol: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = if (cell.hasEntries) SurfaceCard else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = cell.day?.toString().orEmpty(),
            color = if (cell.day != null) TextPrimary else Color.Transparent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
        if (cell.day != null && cell.hasEntries) {
            Text(
                text = shortMoney(cell.income, currencySymbol),
                color = AccentBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = shortMoney(cell.expense, currencySymbol),
                color = AccentPink,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalendarRecordSummaryRow(
    group: PocketNativeRecordGroup,
    currencySymbol: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(group.dateLabel.ifBlank { group.dateIso }, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("${group.rows.size} entries", color = TextSecondary, fontSize = 12.sp)
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("+ ${shortMoney(group.incomeTotal, currencySymbol)}", color = AccentBlue, fontWeight = FontWeight.SemiBold)
            Text("- ${shortMoney(group.expenseTotal, currencySymbol)}", color = AccentPink, fontWeight = FontWeight.SemiBold)
        }
    }
    HorizontalDivider(color = SurfaceCard)
}

private fun shortMoney(amount: Double, symbol: String): String {
    if (amount == 0.0) return "${symbol}0"
    return if (amount >= 1000) {
        "$symbol${String.format(Locale.US, "%.1fk", amount / 1000.0)}"
    } else {
        "$symbol${amount.toInt()}"
    }
}

private fun fullMoney(amount: Double, symbol: String): String {
    val clean = if (amount % 1.0 == 0.0) {
        String.format(Locale.US, "%,d", amount.toLong())
    } else {
        String.format(Locale.US, "%,.2f", amount)
    }
    return "$symbol$clean"
}

@Composable
private fun CalendarMetricCard(
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
private fun CalendarSectionTitle(
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
