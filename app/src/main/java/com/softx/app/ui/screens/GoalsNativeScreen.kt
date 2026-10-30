package com.softx.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FlagCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.softx.app.data.PocketNativeGoalHistoryItem
import com.softx.app.data.PocketNativeGoalItem
import com.softx.app.data.PocketNativeGoalsPayload
import com.softx.app.data.PocketNativeGoalsResponse
import com.softx.app.data.PocketNativeRepository
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import java.util.Locale
import java.util.Calendar
import kotlinx.coroutines.launch

private data class PendingGoalQuickAdd(
    val goal: PocketNativeGoalItem,
    val amount: Double,
    val note: String = "",
)

@Composable
fun GoalsNativeScreen(
    repository: PocketNativeRepository,
    initialJumpGoalId: Int = 0,
    onInitialJumpHandled: () -> Unit = {},
    onBack: () -> Unit,
) {
    val initialCachedResponse = remember(repository) { repository.cachedGoals() }
    var loading by remember { mutableStateOf(initialCachedResponse == null) }
    var errorText by remember { mutableStateOf("") }
    var payload by remember { mutableStateOf<PocketNativeGoalsPayload?>(initialCachedResponse?.payload) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<PocketNativeGoalItem?>(null) }
    var pendingQuickAdd by remember { mutableStateOf<PendingGoalQuickAdd?>(null) }
    var partialSaveGoal by remember { mutableStateOf<PocketNativeGoalItem?>(null) }
    var deletingGoal by remember { mutableStateOf<PocketNativeGoalItem?>(null) }
    var successText by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var highlightedGoalId by remember { mutableStateOf(0) }
    var showFocusedOnly by remember { mutableStateOf(false) }
    val savingStates = remember { mutableStateMapOf<Int, Boolean>() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        errorText = ""
        if (payload == null) {
            loading = true
        }
        val result = repository.goals()
        loading = false
        if (result.isSuccess) {
            val response: PocketNativeGoalsResponse? = result.getOrNull()
            payload = response?.payload
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Goals could not be loaded."
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 48.dp),
                color = AccentGreen,
            )
        }
        return
    }

    val data = payload
    if (data == null) {
        NativeSimpleErrorCard(
            title = "Goals paused",
            body = errorText.ifBlank { "Pocket Pro goals are not ready yet." },
            onBack = onBack,
        )
        return
    }

    if (showSuccessDialog && successText.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Done", color = AccentGreen, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                    )
                    Text("Save done", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(successText, color = TextSecondary)
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp),
        )
    }

    LaunchedEffect(initialJumpGoalId, data.goals) {
        if (initialJumpGoalId <= 0) return@LaunchedEffect
        highlightedGoalId = initialJumpGoalId
        showFocusedOnly = false
        val index = data.goals.indexOfFirst { it.id == initialJumpGoalId }
        if (index >= 0) {
            listState.animateScrollToItem(index = 6 + index, scrollOffset = -24)
        }
        onInitialJumpHandled()
    }

    val filteredGoals = remember(data.goals, showFocusedOnly, highlightedGoalId) {
        if (!showFocusedOnly || highlightedGoalId <= 0) {
            data.goals
        } else {
            data.goals.filter { it.id == highlightedGoalId }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item {
            NativeFeatureTopBar(
                title = "My Goals",
                subtitle = "Track savings goals, target progress, and quick add momentum in one focused view.",
                onBack = onBack,
            )
        }
        item {
            FlatGoalActionCard(
                title = "Create New Goal",
                subtitle = "Set date, cycle, reminder, note, and your planned save amount.",
                accent = AccentBlue,
                onClick = { showGoalDialog = true },
            )
        }
        item {
            GoalsPremiumSummaryCard(
                progressPercent = data.summary.totalProgressPercent,
                savedAmount = nativeGoalsMoney(data.summary.savedAmount, data.currencySymbol),
                targetAmount = nativeGoalsMoney(data.summary.targetAmount, data.currencySymbol),
                activeCount = data.goals.size,
            )
        }
        item {
            GoalMomentumStrip(
                progressPercent = data.summary.totalProgressPercent,
                activeCount = data.goals.size,
                remainingAmount = nativeGoalsMoney(
                    (data.summary.targetAmount - data.summary.savedAmount).coerceAtLeast(0.0),
                    data.currencySymbol,
                ),
            )
        }
        item {
            GoalsFilterStrip(
                showFocusedOnly = showFocusedOnly,
                canFocus = highlightedGoalId > 0,
                onShowAll = { showFocusedOnly = false },
                onShowFocused = { if (highlightedGoalId > 0) showFocusedOnly = true },
            )
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SectionLabel("Active Goals", if (filteredGoals.isEmpty()) "" else "${filteredGoals.size} goals")
                Text(
                    "Tap a goal to edit the full planner, or use the quick save actions below.",
                    color = TextSecondary,
                    lineHeight = 20.sp,
                )
            }
        }
        if (filteredGoals.isEmpty()) {
            item { GoalsFlatEmptyState() }
        } else {
            items(filteredGoals) { item ->
                GoalProgressCard(
                    item = item,
                    highlighted = highlightedGoalId == item.id,
                    currencySymbol = data.currencySymbol,
                    saving = savingStates[item.id] == true,
                    onEdit = {
                        editingGoal = item
                    },
                    onQuickSave = { amount ->
                        pendingQuickAdd = PendingGoalQuickAdd(item, amount, note = item.planFrequency.lowercase().replaceFirstChar { it.titlecase() } + " save")
                    },
                    onPartialSave = { partialSaveGoal = item },
                    onHistory = { editingGoal = item },
                )
            }
        }
        if (errorText.isNotBlank()) {
            item { NativeEmptyCard(errorText) }
        }
        item { Spacer(modifier = Modifier.height(14.dp)) }
    }

    if (showGoalDialog) {
        GoalCreateDialog(
            title = "Create Goal",
            confirmLabel = "Save Goal",
            initialGoal = null,
            onDismiss = { showGoalDialog = false },
            onSave = { name, targetAmount, savedAmount, targetDate, note, planFrequency, planAmount, autoReminderOn ->
                scope.launch {
                    val result = repository.saveGoal(name, targetAmount, savedAmount, targetDate, note, planFrequency, planAmount, autoReminderOn)
                    if (result.isSuccess) {
                        payload = result.getOrNull()?.payload
                        errorText = ""
                        successText = "Goal created successfully."
                        showSuccessDialog = true
                        showGoalDialog = false
                    } else {
                        errorText = result.exceptionOrNull()?.message ?: "Goal could not be saved."
                    }
                }
            },
        )
    }

    editingGoal?.let { goal ->
        GoalCreateDialog(
            title = "Edit Goal",
            confirmLabel = "Save Changes",
            initialGoal = goal,
            onDismiss = { editingGoal = null },
            onSave = { name, targetAmount, savedAmount, targetDate, note, planFrequency, planAmount, autoReminderOn ->
                val result = repository.updateGoalLocally(
                    goalId = goal.id,
                    goalName = name,
                    targetAmount = targetAmount,
                    savedAmount = savedAmount,
                    targetDate = targetDate,
                    note = note,
                    planFrequency = planFrequency,
                    planAmount = planAmount,
                    autoReminderOn = autoReminderOn,
                )
                if (result.isSuccess) {
                    payload = result.getOrNull()?.payload
                    errorText = ""
                    successText = "Goal updated successfully."
                    showSuccessDialog = true
                    editingGoal = null
                } else {
                    errorText = result.exceptionOrNull()?.message ?: "Goal could not be updated."
                }
            },
            onDelete = {
                deletingGoal = goal
                editingGoal = null
            },
        )
    }

    pendingQuickAdd?.let { pending ->
        PocketDeleteConfirmationDialog(
            title = "Add money to this goal?",
            message = "Are you sure you want to add ${nativeGoalsMoney(pending.amount, data.currencySymbol)} to ${pending.goal.name}?",
            onDismiss = { pendingQuickAdd = null },
            onConfirm = {
                if (savingStates[pending.goal.id] == true) {
                    pendingQuickAdd = null
                    return@PocketDeleteConfirmationDialog
                }
                savingStates[pending.goal.id] = true
                scope.launch {
                    val result = repository.addGoalSaved(pending.goal.id, pending.amount, pending.note)
                    savingStates[pending.goal.id] = false
                    pendingQuickAdd = null
                    if (result.isSuccess) {
                        payload = result.getOrNull()?.payload
                        errorText = ""
                        successText = "Goal money saved successfully."
                        showSuccessDialog = true
                    } else {
                        errorText = result.exceptionOrNull()?.message ?: "Goal progress could not be updated."
                    }
                }
            },
        )
    }

    partialSaveGoal?.let { goal ->
        GoalSaveAmountDialog(
            title = "Some Save",
            currencySymbol = data.currencySymbol,
            defaultNote = "",
            onDismiss = { partialSaveGoal = null },
            onSave = { amount, note ->
                if (savingStates[goal.id] == true) return@GoalSaveAmountDialog
                savingStates[goal.id] = true
                scope.launch {
                    val result = repository.addGoalSaved(goal.id, amount, note)
                    savingStates[goal.id] = false
                    partialSaveGoal = null
                    if (result.isSuccess) {
                        payload = result.getOrNull()?.payload
                        errorText = ""
                        successText = "Custom save added successfully."
                        showSuccessDialog = true
                    } else {
                        errorText = result.exceptionOrNull()?.message ?: "Goal progress could not be updated."
                    }
                }
            },
        )
    }

    deletingGoal?.let { goal ->
        PocketDeleteConfirmationDialog(
            title = "Delete this goal?",
            message = "Are you sure you want to delete ${goal.name}?",
            onDismiss = { deletingGoal = null },
            onConfirm = {
                val result = repository.deleteGoalLocally(goal.id)
                deletingGoal = null
                if (result.isSuccess) {
                    payload = result.getOrNull()?.payload
                    errorText = ""
                    successText = "Goal deleted successfully."
                    showSuccessDialog = true
                } else {
                    errorText = result.exceptionOrNull()?.message ?: "Goal could not be deleted."
                }
            },
        )
    }
}

@Composable
private fun GoalSummaryItem(
    title: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = TextPrimary.copy(alpha = 0.78f), fontWeight = FontWeight.SemiBold)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalProgressCard(
    item: PocketNativeGoalItem,
    highlighted: Boolean = false,
    currencySymbol: String,
    saving: Boolean,
    onEdit: () -> Unit,
    onQuickSave: (Double) -> Unit,
    onPartialSave: () -> Unit,
    onHistory: () -> Unit,
) {
    val startColor = colorFromHex(item.accentStartHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(
                if (highlighted) {
                    Modifier.border(
                        width = 1.6.dp,
                        color = startColor.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(28.dp),
                    )
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252E45)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(item.name, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text(item.deadlineText.ifBlank { "Open goal" }, color = TextSecondary, fontSize = 13.sp)
                    if (item.autoReminderOn) {
                        Text("Auto alert on", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${item.progress.toInt()}%", color = startColor, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("Progress", color = TextSecondary, fontSize = 12.sp)
                }
            }
            LinearProgressIndicator(
                progress = { (item.progress.toFloat() / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = startColor,
                trackColor = Color(0xFF27314A),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                GoalMetricMiniCard("Saved", nativeGoalsMoney(item.savedAmount, currencySymbol), startColor, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                GoalMetricMiniCard("Target", nativeGoalsMoney(item.targetAmount, currencySymbol), AccentBlue, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GoalMetricMiniCard("Set amount", nativeGoalsMoney(item.planAmount, currencySymbol), AccentPurple, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                GoalMetricMiniCard("Cycle", item.planFrequency.replaceFirstChar { it.titlecase() }, AccentGreen, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GoalInfoPill(
                    label = if (item.autoReminderOn) "Auto alert on" else "Auto alert off",
                    accent = if (item.autoReminderOn) AccentGreen else AccentPink,
                    icon = Icons.Rounded.NotificationsActive,
                )
                GoalInfoPill(
                    label = if (item.history.isNotEmpty()) "${item.history.size} saves" else "No saves yet",
                    accent = AccentBlue,
                    icon = Icons.Rounded.History,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GoalActionTile(
                    title = "Save",
                    accent = AccentGreen,
                    icon = Icons.Rounded.Payments,
                    modifier = Modifier.weight(1f),
                    enabled = !saving,
                    onClick = { onQuickSave(item.planAmount.takeIf { it > 0.0 } ?: item.remainingAmount.coerceAtMost(item.targetAmount.coerceAtLeast(0.0))) },
                )
                GoalActionTile(
                    title = "Custom",
                    accent = AccentBlue,
                    icon = Icons.Rounded.Savings,
                    modifier = Modifier.weight(1f),
                    enabled = !saving,
                    onClick = onPartialSave,
                )
                GoalActionTile(
                    title = "History",
                    accent = AccentPurple,
                    icon = Icons.Rounded.History,
                    modifier = Modifier.weight(1f),
                    enabled = !saving,
                    onClick = onHistory,
                )
                GoalActionTile(
                    title = "Edit",
                    accent = AccentPink,
                    icon = Icons.Rounded.Edit,
                    modifier = Modifier.weight(1f),
                    enabled = !saving,
                    onClick = onEdit,
                )
            }
            if (item.history.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Recent save history", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    item.history.take(3).forEach { historyItem ->
                        GoalHistoryPreviewRow(
                            item = historyItem,
                            currencySymbol = currencySymbol,
                        )
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
        }
    }
}

@Composable
private fun GoalsFilterStrip(
    showFocusedOnly: Boolean,
    canFocus: Boolean,
    onShowAll: () -> Unit,
    onShowFocused: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GoalStatusPill(
            title = "All goals",
            accent = if (!showFocusedOnly) AccentBlue else TextSecondary,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onShowAll),
            icon = Icons.Rounded.FlagCircle,
        )
        GoalStatusPill(
            title = "Focused goal",
            accent = if (showFocusedOnly && canFocus) AccentPurple else TextSecondary,
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = canFocus, onClick = onShowFocused),
            icon = Icons.Rounded.History,
        )
    }
}

@Composable
private fun GoalInfoPill(
    label: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier
            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(99.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.width(14.dp),
        )
        Text(label, color = accent, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}

@Composable
private fun GoalActionTile(
    title: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
        )
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accent.copy(alpha = 0.92f), RoundedCornerShape(99.dp)),
        )
    }
}

@Composable
private fun GoalMetricMiniCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accent.copy(alpha = 0.92f), RoundedCornerShape(99.dp)),
        )
    }
}

@Composable
private fun FlatGoalActionCard(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(26.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .background(accent.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.AddCircleOutline,
                contentDescription = null,
                tint = accent,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
        }
        Text("New", color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun GoalsFlatEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("No goals yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Create your first goal to start tracking savings and progress in one place.", color = TextSecondary, lineHeight = 20.sp)
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
    }
}

@Composable
private fun GoalsPremiumSummaryCard(
    progressPercent: Int,
    savedAmount: String,
    targetAmount: String,
    activeCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF232C42)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Goals Overview", color = TextPrimary.copy(alpha = 0.88f), fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("$progressPercent%", color = TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Black)
                    Text("Total progress", color = TextSecondary)
                }
                GoalStatusPill(
                    title = if (activeCount > 0) "$activeCount active" else "No active goal",
                    accent = AccentPurple,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GoalOverviewStat("Saved", savedAmount, AccentGreen, Modifier.weight(1f))
                GoalOverviewStat("Target", targetAmount, AccentBlue, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GoalOverviewStat(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accent, RoundedCornerShape(99.dp)),
        )
    }
}

@Composable
private fun GoalMomentumStrip(
    progressPercent: Int,
    activeCount: Int,
    remainingAmount: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GoalStatusPill(
            title = "${activeCount.coerceAtLeast(0)} goals",
            accent = AccentPurple,
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.FlagCircle,
        )
        GoalStatusPill(
            title = "$progressPercent% done",
            accent = AccentGreen,
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Rounded.TrendingUp,
        )
        GoalStatusPill(
            title = remainingAmount,
            accent = AccentBlue,
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Savings,
        )
    }
}

@Composable
private fun GoalStatusPill(
    title: String,
    accent: Color,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Rounded.FlagCircle,
) {
    Row(
        modifier = modifier
            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
        )
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
        )
    }
}

private fun nativeGoalsMoney(amount: Double, symbol: String): String {
    val clean = if (amount % 1.0 == 0.0) String.format(Locale.US, "%,d", amount.toLong()) else String.format(Locale.US, "%,.2f", amount)
    return "$symbol$clean"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalCreateDialog(
    title: String,
    confirmLabel: String,
    initialGoal: PocketNativeGoalItem?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, String, String, String, Double, Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var name by remember(initialGoal) { mutableStateOf(initialGoal?.name ?: "") }
    var targetText by remember(initialGoal) { mutableStateOf(initialGoal?.targetAmount?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var savedText by remember(initialGoal) { mutableStateOf(initialGoal?.savedAmount?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var targetDate by remember(initialGoal) { mutableStateOf(initialGoal?.targetDate ?: "") }
    var note by remember(initialGoal) { mutableStateOf(initialGoal?.note ?: "") }
    var planFrequency by remember(initialGoal) { mutableStateOf(initialGoal?.planFrequency ?: "MONTHLY") }
    var planAmountText by remember(initialGoal) { mutableStateOf(initialGoal?.planAmount?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var autoReminderOn by remember(initialGoal) { mutableStateOf(initialGoal?.autoReminderOn ?: true) }
    val calendar = Calendar.getInstance()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        targetText.toDoubleOrNull() ?: 0.0,
                        savedText.toDoubleOrNull() ?: 0.0,
                        targetDate,
                        note,
                        planFrequency,
                        planAmountText.toDoubleOrNull() ?: 0.0,
                        autoReminderOn,
                    )
                },
                enabled = name.isNotBlank() && (targetText.toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete", color = AccentPink) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        title = { Text(title, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    ),
                    colors = goalDialogFieldColors(),
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Target amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    ),
                    colors = goalDialogFieldColors(),
                )
                OutlinedTextField(
                    value = savedText,
                    onValueChange = { savedText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Already saved") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    ),
                    colors = goalDialogFieldColors(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceCardSoft, RoundedCornerShape(18.dp))
                        .background(SurfaceCard, RoundedCornerShape(18.dp))
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    targetDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH),
                            ).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = AccentBlue)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Target date", color = TextSecondary, fontSize = 12.sp)
                        Text(targetDate.ifBlank { "Tap to pick date" }, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text("Set amount", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("DAY", "WEEKLY", "MONTHLY", "YEARLY").forEach { frequency ->
                        Button(
                            onClick = { planFrequency = frequency },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (planFrequency == frequency) AccentPurple else Color.White.copy(alpha = 0.08f),
                                contentColor = if (planFrequency == frequency) Color.White else TextPrimary,
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text(frequency.replaceFirstChar { it.titlecase() }, fontSize = 12.sp)
                        }
                    }
                }
                OutlinedTextField(
                    value = planAmountText,
                    onValueChange = { planAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount per ${planFrequency.lowercase()}") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    ),
                    colors = goalDialogFieldColors(),
                )
                Button(
                    onClick = { autoReminderOn = !autoReminderOn },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (autoReminderOn) AccentGreen.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f),
                        contentColor = if (autoReminderOn) AccentGreen else TextPrimary,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(if (autoReminderOn) "Auto alert on" else "Auto alert off")
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    ),
                    colors = goalDialogFieldColors(),
                )
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun GoalSaveAmountDialog(
    title: String,
    currencySymbol: String,
    defaultNote: String,
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf(defaultNote) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(amountText.toDoubleOrNull() ?: 0.0, note) },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount ($currencySymbol)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    ),
                    colors = goalDialogFieldColors(),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    ),
                    colors = goalDialogFieldColors(),
                )
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun GoalHistoryPreviewRow(
    item: PocketNativeGoalHistoryItem,
    currencySymbol: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(item.date.ifBlank { "No date" }, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            if (item.note.isNotBlank()) {
                Text(item.note, color = TextSecondary, fontSize = 11.sp)
            }
        }
        Text(nativeGoalsMoney(item.amount, currencySymbol), color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun goalDialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentPurple,
    unfocusedBorderColor = SurfaceCardSoft,
    focusedContainerColor = SurfaceCard,
    unfocusedContainerColor = SurfaceCard,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
)
