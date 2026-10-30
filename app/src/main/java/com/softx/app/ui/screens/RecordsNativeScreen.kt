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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Liquor
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SmokingRooms
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softx.app.data.PocketNativeRecordGroup
import com.softx.app.data.PocketNativeRecordItem
import com.softx.app.data.PocketNativeRecordsPayload
import com.softx.app.data.PocketNativeRecordsResponse
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceBase
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch

@Composable
fun RecordsNativeScreen(
    repository: PocketNativeRepository,
    signedInUser: PocketNativeUser?,
    onEditRecord: (PocketNativeRecordItem) -> Unit = {},
) {
    val context = LocalContext.current
    val preferencesStore = remember(context) { PocketNativePreferencesStore(context) }
    val scope = rememberCoroutineScope()
    val initialMonthValue = remember { currentMonthValue() }
    val initialCachedResponse = remember(repository, initialMonthValue) {
        repository.cachedRecords(initialMonthValue)
    }
    var loading by remember { mutableStateOf(initialCachedResponse == null) }
    var errorText by remember { mutableStateOf("") }
    var response by remember { mutableStateOf<PocketNativeRecordsResponse?>(initialCachedResponse) }
    var monthValue by remember { mutableStateOf(initialMonthValue) }
    var activeFilter by remember { mutableStateOf("all") }
    var selectedRecord by remember { mutableStateOf<PocketNativeRecordItem?>(null) }
    var pendingDeleteRecord by remember { mutableStateOf<PocketNativeRecordItem?>(null) }
    var deleting by remember { mutableStateOf(false) }

    fun loadRecords() {
        scope.launch {
            errorText = ""
            if (response?.payload == null) {
                loading = true
            }
            val result = repository.records(monthValue)
            loading = false
            if (result.isSuccess) {
                response = result.getOrNull()
            } else {
                errorText = result.exceptionOrNull()?.message ?: "Records could not be loaded."
            }
        }
    }

    LaunchedEffect(monthValue) {
        loadRecords()
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

    val payload = response?.payload
    if (payload == null) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                RecordsErrorCard(
                    errorText = errorText.ifBlank { "Records are not ready yet." },
                    signedInUser = signedInUser,
                )
            }
        }
        return
    }

    // The public guest endpoint returns showcase rows. Keep a brand-new local space
    // genuinely empty until the owner creates the first real entry.
    if (signedInUser?.demoAccessEnabled == true && !preferencesStore.hasRecordedAnyMoneyEntry()) {
        FreshRecordsEmptyState()
        return
    }

    val filteredGroups = payload.groups.mapNotNull { group ->
        val filteredRows = when (activeFilter) {
            "income" -> group.rows.filter { it.mode.equals("income", ignoreCase = true) || it.signedAmount > 0 }
            "expense" -> group.rows.filter { it.mode.equals("expense", ignoreCase = true) || it.signedAmount < 0 }
            "lent" -> group.rows.filter {
                val mode = it.mode.lowercase(Locale.US)
                mode in setOf("lent", "borrow", "receive", "repay")
            }
            else -> group.rows
        }
        if (filteredRows.isEmpty()) {
            null
        } else {
            group.copy(
                rows = filteredRows,
                incomeTotal = filteredRows.filter { it.signedAmount > 0 }.sumOf { it.signedAmount },
                expenseTotal = filteredRows.filter { it.signedAmount < 0 }.sumOf { abs(it.signedAmount) },
            )
        }
    }
    val visibleRecordCount = filteredGroups.sumOf { it.rows.size }
    val visibleIncome = filteredGroups.sumOf { group -> group.rows.filter { it.signedAmount > 0 }.sumOf { it.signedAmount } }
    val visibleExpense = filteredGroups.sumOf { group -> group.rows.filter { it.signedAmount < 0 }.sumOf { abs(it.signedAmount) } }
    val visibleNet = visibleIncome - visibleExpense

    selectedRecord?.let { record ->
        AlertDialog(
            onDismissRequest = {
                if (!deleting) selectedRecord = null
            },
            title = {
                Text(recordPrimaryTitle(record), color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(friendlyRecordMode(record), color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = formatNativeSignedAmount(record.signedAmount, payload.currencySymbol),
                        color = if (record.signedAmount >= 0) AccentBlue else AccentPink,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(recordSecondaryText(record).ifBlank { "Tap edit to change this entry." }, color = TextSecondary)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { selectedRecord = null }) {
                        Icon(Icons.Rounded.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Close", color = TextSecondary)
                    }
                    TextButton(
                        onClick = {
                            selectedRecord = null
                            onEditRecord(record)
                        },
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Edit", color = AccentBlue, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = {
                        pendingDeleteRecord = record
                        selectedRecord = null
                    },
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = AccentPink, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Delete", color = AccentPink, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp),
        )
    }

    pendingDeleteRecord?.let { record ->
        PocketDeleteConfirmationDialog(
            title = "Delete record?",
            message = "Are you sure you want to delete ${recordPrimaryTitle(record)}? This cannot be undone.",
            deleting = deleting,
            onDismiss = { pendingDeleteRecord = null },
            onConfirm = {
                deleting = true
                scope.launch {
                    val result = repository.deleteRecord(
                        kind = normalizedRecordActionKind(record),
                        id = record.id,
                        deleteUrl = record.deleteUrl,
                    )
                    deleting = false
                    if (result.isSuccess) {
                        pendingDeleteRecord = null
                        Toast.makeText(context, "Record deleted.", Toast.LENGTH_SHORT).show()
                        loadRecords()
                    } else {
                        Toast.makeText(
                            context,
                            result.exceptionOrNull()?.message ?: "Delete failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().background(SurfaceBase),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            RecordsHeroFlat(
                payload = payload,
                visibleCount = visibleRecordCount,
                visibleNet = visibleNet,
                onPreviousMonth = { monthValue = shiftMonth(monthValue, -1) },
                onNextMonth = { monthValue = shiftMonth(monthValue, 1) },
            )
        }
        item {
            RecordsFlatTabs(
                activeFilter = activeFilter,
                onFilterChange = { activeFilter = it },
            )
        }
        item {
            RecordsSummaryFlat(
                income = visibleIncome,
                expense = visibleExpense,
                balance = visibleNet,
                currencySymbol = payload.currencySymbol,
            )
        }
        if (filteredGroups.isEmpty()) {
            item {
                EmptyRecordsCard()
            }
        } else {
            items(filteredGroups) { group ->
                FlatDivider()
                RecordsDayGroupFlat(
                    group = group,
                    currencySymbol = payload.currencySymbol,
                    onRecordClick = { selectedRecord = it },
                )
            }
        }
        item { FlatDivider() }
    }
}

@Composable
private fun FreshRecordsEmptyState() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().background(SurfaceBase),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 28.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("No records yet", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Your income, expenses and loan activity will appear here after you add your first entry.",
                    color = TextSecondary,
                    lineHeight = 21.sp,
                )
            }
        }
    }
}

@Composable
private fun RecordsHeroFlat(
    payload: PocketNativeRecordsPayload,
    visibleCount: Int,
    visibleNet: Double,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBase),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month", tint = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(payload.monthLabel, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                Text(payload.yearLabel, color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month", tint = TextPrimary)
            }
        }
        FlatDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$visibleCount entries", color = TextSecondary, fontSize = 13.sp)
            Text(
                formatNativeSignedAmount(visibleNet, payload.currencySymbol),
                color = if (visibleNet >= 0) AccentBlue else AccentPink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun RecordsFlatTabs(
    activeFilter: String,
    onFilterChange: (String) -> Unit,
) {
    val tabs = listOf(
        "all" to "Daily",
        "income" to "Income",
        "expense" to "Expense",
        "lent" to "Loan",
        "note" to "Note",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBase),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            tabs.forEach { (key, label) ->
                val selected = activeFilter == key || (key == "all" && activeFilter !in listOf("income", "expense", "lent"))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onFilterChange(
                                when (key) {
                                    "note" -> "all"
                                    else -> key
                                },
                            )
                        }
                        .padding(top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        label,
                        color = if (selected) TextPrimary else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .height(3.dp)
                            .background(
                                if (selected) AccentPink else Color.Transparent,
                                RoundedCornerShape(50),
                            ),
                    )
                }
            }
        }
        FlatDivider()
    }
}

@Composable
private fun RecordsSummaryFlat(
    income: Double,
    expense: Double,
    balance: Double,
    currencySymbol: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBase),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SummaryColumnFlat(
                title = "Income",
                value = formatNativeCurrency(income, currencySymbol),
                color = AccentBlue,
                modifier = Modifier.weight(1f),
            )
            SummaryColumnFlat(
                title = "Expenses",
                value = formatNativeCurrency(expense, currencySymbol),
                color = AccentPink,
                modifier = Modifier.weight(1f),
            )
            SummaryColumnFlat(
                title = "Total",
                value = formatNativeCurrency(balance, currencySymbol),
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
        }
        FlatDivider()
    }
}

@Composable
private fun SummaryColumnFlat(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp, textAlign = TextAlign.Center)
        Text(value, color = color, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RecordsDayGroupFlat(
    group: PocketNativeRecordGroup,
    currencySymbol: String,
    onRecordClick: (PocketNativeRecordItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBase),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    dayNumber(group.dateLabel),
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(group.weekdayLabel, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(group.dateIso.takeLast(7).replace("-", "."), color = TextSecondary, fontSize = 14.sp)
                }
            }
            Text(
                formatNativeCurrency(group.incomeTotal, currencySymbol),
                color = AccentBlue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
            Text(
                formatNativeCurrency(group.expenseTotal, currencySymbol),
                color = AccentPink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
        }
        FlatDivider()
        group.rows.forEachIndexed { index, item ->
            RecordRowFlat(
                item = item,
                currencySymbol = currencySymbol,
                onClick = { onRecordClick(item) },
            )
            if (index != group.rows.lastIndex) {
                FlatDivider()
            }
        }
    }
}

@Composable
private fun RecordRowFlat(
    item: PocketNativeRecordItem,
    currencySymbol: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(SurfaceBase)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = recordIconFor(item),
                contentDescription = null,
                tint = parseNativeColor(item.colorHex),
                modifier = Modifier.size(22.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    recordPrimaryTitle(item),
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )
                Text(
                    recordSecondaryText(item).ifBlank { friendlyRecordMode(item) },
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
        Text(
            text = formatNativeCurrency(abs(item.signedAmount), currencySymbol),
            color = if (item.signedAmount >= 0) AccentBlue else AccentPink,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun FlatDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SurfaceCardSoft),
    )
}

@Composable
private fun EmptyRecordsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBase)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("No records in this month", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "You have no entries in this month yet. New income, expense or loan records will appear here.",
            color = TextSecondary,
            lineHeight = 20.sp,
        )
        FlatDivider()
    }
}

@Composable
private fun RecordsErrorCard(
    errorText: String,
    signedInUser: PocketNativeUser?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceBase)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Records are getting ready", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            "${signedInUser?.fullName ?: "Pocket Pro User"}, $errorText",
            color = TextSecondary,
            lineHeight = 22.sp,
        )
    }
}

private fun currentMonthValue(): String {
    val calendar = Calendar.getInstance()
    return String.format(Locale.US, "%04d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
}

private fun shiftMonth(monthValue: String, delta: Int): String {
    val parts = monthValue.split("-")
    if (parts.size != 2) {
        return currentMonthValue()
    }
    val year = parts[0].toIntOrNull() ?: return currentMonthValue()
    val month = parts[1].toIntOrNull() ?: return currentMonthValue()
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, delta)
    }
    return String.format(Locale.US, "%04d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
}

private fun formatNativeCurrency(amount: Double, symbol: String): String {
    val clean = if (amount % 1.0 == 0.0) amount.toLong().toString() else String.format(Locale.US, "%.2f", amount)
    return "$symbol$clean"
}

private fun formatNativeSignedAmount(amount: Double, symbol: String): String {
    val sign = if (amount >= 0) "+" else "-"
    return "$sign ${formatNativeCurrency(abs(amount), symbol)}"
}

private fun parseNativeColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    AccentPurple
}

private fun normalizedRecordActionKind(item: PocketNativeRecordItem): String {
    val rawKind = item.kind.trim().lowercase()
    if (rawKind == "income" || rawKind == "expense") {
        return rawKind
    }
    return when (item.mode.trim().lowercase()) {
        "income", "borrow", "receive" -> "income"
        else -> "expense"
    }
}

private fun friendlyRecordMode(item: PocketNativeRecordItem): String = when (item.mode.trim().lowercase()) {
    "income" -> "Income"
    "expense" -> "Expense"
    "lent" -> "Give Loan"
    "borrow" -> "Take Loan"
    "receive" -> "Get Back"
    "repay" -> "Pay Back"
    else -> item.kind.replaceFirstChar { it.uppercase() }
}

private fun recordPrimaryTitle(item: PocketNativeRecordItem): String {
    return when (item.mode.trim().lowercase()) {
        "lent", "borrow", "receive", "repay" -> friendlyRecordMode(item)
        else -> item.label.ifBlank { friendlyRecordMode(item) }
    }
}

private fun recordSecondaryText(item: PocketNativeRecordItem): String {
    val mode = item.mode.trim().lowercase()
    return when (mode) {
        "lent", "borrow", "receive", "repay" -> {
            val loanMeta = item.label.takeIf {
                it.isNotBlank() && !it.equals(recordPrimaryTitle(item), ignoreCase = true)
            }
            loanMeta ?: item.note.ifBlank { if (item.signedAmount >= 0) "Money in" else "Money out" }
        }
        else -> item.note.ifBlank { friendlyRecordMode(item) }
    }
}

private fun dayNumber(dateLabel: String): String {
    val match = Regex("(\\d{1,2})").find(dateLabel)
    return match?.value ?: dateLabel.take(2)
}

private fun recordIconFor(item: PocketNativeRecordItem): ImageVector {
    return pocketCategoryVisualIcon(item.label, item.iconClass)
}
