package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.softx.app.data.PocketNativeRecordItem
import com.softx.app.data.PocketNativeRecordsResponse
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun SearchNativeScreen(
    repository: PocketNativeRepository,
    signedInUser: PocketNativeUser? = null,
    onBack: () -> Unit,
    onOpenRecord: (PocketNativeRecordItem) -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }
    var response by remember { mutableStateOf<PocketNativeRecordsResponse?>(null) }
    var query by remember { mutableStateOf("") }
    var activeTypeFilter by remember { mutableStateOf("all") }
    var activeDirectionFilter by remember { mutableStateOf("all") }
    var lockedFeature by remember { mutableStateOf<PocketNativeFeatureGate?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        errorText = ""
        val result = repository.records(searchCurrentMonthValue())
        loading = false
        if (result.isSuccess) {
            response = result.getOrNull()
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Search could not be loaded."
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

    val payload = response?.payload
    if (payload == null) {
        NativeSimpleErrorCard(
            title = "Search paused",
            body = errorText.ifBlank { "Search is not ready yet." },
            onBack = onBack,
        )
        return
    }

    val allRows = payload.groups.flatMap { it.rows }
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchFocusRequester = remember { FocusRequester() }
    val cleanQuery = query.trim().lowercase()
    val advancedSearchGate = remember(signedInUser) { pocketNativeFeatureGate(signedInUser, "advanced_search") }
    val filteredRows = allRows.filter { item ->
        val queryMatch = if (cleanQuery.isBlank()) {
            true
        } else {
            listOf(item.label, item.note, item.date, item.mode, item.kind)
                .joinToString(" ")
                .lowercase()
                .contains(cleanQuery)
        }
        val typeMatch = when (activeTypeFilter) {
            "income" -> item.mode == "income"
            "expense" -> item.mode == "expense"
            "loan" -> item.mode in listOf("lent", "borrow", "receive", "repay")
            else -> true
        }
        val directionMatch = when (activeDirectionFilter) {
            "in" -> item.signedAmount >= 0
            "out" -> item.signedAmount < 0
            else -> true
        }
        queryMatch && typeMatch && directionMatch
    }
    val suggestionRows = remember(filteredRows, cleanQuery) {
        if (cleanQuery.isBlank()) {
            emptyList()
        } else {
            filteredRows
                .sortedByDescending { searchScore(it, cleanQuery) }
                .take(5)
        }
    }

    LaunchedEffect(Unit) {
        searchFocusRequester.requestFocus()
        keyboardController?.show()
    }
    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceCard.copy(alpha = 0.18f)),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 30.dp, end = 12.dp, bottom = 132.dp),
            color = SurfaceCard,
            shape = RoundedCornerShape(30.dp),
        ) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 18.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { Spacer(modifier = Modifier.size(18.dp)) }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.clickable(onClick = onBack),
                            color = SurfaceCard.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(999.dp),
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close search", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SurfaceCard.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        TextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester),
                            placeholder = { Text("Search notes, details, amount flow, or date", color = TextSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = AccentBlue,
                                unfocusedIndicatorColor = TextSecondary.copy(alpha = 0.18f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = AccentBlue,
                            ),
                        )
                    }
                }
                if (suggestionRows.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("Quick suggestions", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(
                                "Live matches from title, note, type, date, and details.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                        }
                    }
                    items(suggestionRows, key = { "suggestion-${it.id}-${it.date}" }) { item ->
                        SearchSuggestionRow(
                            item = item,
                            query = cleanQuery,
                            onClick = { onOpenRecord(item) },
                        )
                    }
                }
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Advanced filters", color = TextPrimary, fontWeight = FontWeight.Bold)
                            PocketNativeProBadge()
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SearchFilterChip("All", activeTypeFilter == "all") { activeTypeFilter = "all" }
                            SearchFilterChip("Income", activeTypeFilter == "income") {
                                if (advancedSearchGate.granted) activeTypeFilter = "income" else lockedFeature = advancedSearchGate
                            }
                            SearchFilterChip("Expense", activeTypeFilter == "expense") {
                                if (advancedSearchGate.granted) activeTypeFilter = "expense" else lockedFeature = advancedSearchGate
                            }
                            SearchFilterChip("Loans", activeTypeFilter == "loan") {
                                if (advancedSearchGate.granted) activeTypeFilter = "loan" else lockedFeature = advancedSearchGate
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SearchFilterChip("Any flow", activeDirectionFilter == "all") { activeDirectionFilter = "all" }
                            SearchFilterChip("Money In", activeDirectionFilter == "in") {
                                if (advancedSearchGate.granted) activeDirectionFilter = "in" else lockedFeature = advancedSearchGate
                            }
                            SearchFilterChip("Money Out", activeDirectionFilter == "out") {
                                if (advancedSearchGate.granted) activeDirectionFilter = "out" else lockedFeature = advancedSearchGate
                            }
                        }
                    }
                }
                item {
                    Text(
                        text = "${filteredRows.size} result${if (filteredRows.size == 1) "" else "s"}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                    )
                }
                if (filteredRows.isEmpty()) {
                    item { NativeEmptyCard("No matching record found.") }
                } else {
                    items(filteredRows) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenRecord(item) }
                                .padding(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            val amountText = if (item.signedAmount >= 0) "+ ৳${item.signedAmount.toInt()}" else "- ৳${kotlin.math.abs(item.signedAmount).toInt()}"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(item.label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text(amountText, color = if (item.signedAmount >= 0) AccentBlue else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Text(item.note.ifBlank { item.mode.ifBlank { item.kind } }, color = TextSecondary, fontSize = 12.sp)
                            Text(item.date, color = TextSecondary, fontSize = 12.sp)
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 6.dp),
                                color = TextSecondary.copy(alpha = 0.12f),
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.size(14.dp)) }
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
private fun SearchSuggestionRow(
    item: PocketNativeRecordItem,
    query: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(16.dp),
                )
                Text(item.label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Open", color = AccentBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Text(
            searchSuggestionContext(item, query),
            color = TextSecondary,
            fontSize = 12.sp,
        )
        HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f))
    }
}

@Composable
private fun SearchFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) AccentPurple.copy(alpha = 0.18f) else SurfaceCard.copy(alpha = 0.55f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                color = if (selected) AccentPurple else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
            )
            if (selected) {
                HorizontalDivider(color = AccentPurple.copy(alpha = 0.45f))
            }
        }
    }
}

private fun searchScore(
    item: PocketNativeRecordItem,
    query: String,
): Int {
    if (query.isBlank()) return 0
    val label = item.label.lowercase()
    val note = item.note.lowercase()
    val date = item.date.lowercase()
    val mode = item.mode.lowercase()
    val kind = item.kind.lowercase()
    var score = 0
    if (label == query) score += 120
    if (label.startsWith(query)) score += 80
    if (label.contains(query)) score += 60
    if (note.startsWith(query)) score += 45
    if (note.contains(query)) score += 35
    if (date.contains(query)) score += 20
    if (mode.contains(query)) score += 15
    if (kind.contains(query)) score += 10
    return score
}

private fun searchSuggestionContext(
    item: PocketNativeRecordItem,
    query: String,
): String {
    val note = item.note.trim()
    return when {
        note.isNotBlank() && note.lowercase().contains(query) -> "Matched in note: $note"
        item.label.lowercase().contains(query) -> "Matched in title • ${item.date}"
        item.mode.lowercase().contains(query) -> "Matched in type: ${item.mode} • ${item.date}"
        item.kind.lowercase().contains(query) -> "Matched in details: ${item.kind} • ${item.date}"
        else -> item.date
    }
}

private fun searchCurrentMonthValue(): String {
    val calendar = Calendar.getInstance()
    return "%04d-%02d".format(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
    )
}
