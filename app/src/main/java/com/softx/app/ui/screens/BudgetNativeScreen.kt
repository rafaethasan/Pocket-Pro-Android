package com.softx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Share
import com.softx.app.util.PocketShareUtils
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import android.widget.Toast
import com.softx.app.PocketReminderScheduler
import com.softx.app.data.PocketNativeInstallmentPlan
import com.softx.app.data.PocketNativeInstallmentPayment
import com.softx.app.data.PocketNativeCollectionPlan
import com.softx.app.data.PocketNativeUser
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeLoanDueReminder
import com.softx.app.data.PocketNativeMonthlyReminder
import com.softx.app.data.PocketNativeTransactionCategoryOption
import com.softx.app.data.PocketNativeBudgetItem
import com.softx.app.data.PocketNativeBudgetPayload
import com.softx.app.data.PocketNativeBudgetResponse
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.pocketInstallmentCompletedCount
import com.softx.app.data.pocketInstallmentPaidAmount
import com.softx.app.data.pocketInstallmentReminderEnabled
import com.softx.app.data.pocketLoanReminderOriginalAmount
import com.softx.app.data.pocketLoanReminderRemainingAmount
import com.softx.app.data.pocketLoanReminderSettledAmount
import com.softx.app.data.pocketNormalizeInstallmentTitle
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.SurfaceCardSoft
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.util.Locale
import java.util.Calendar
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.app.DatePickerDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetNativeScreen(
    repository: PocketNativeRepository,
    preferencesStore: PocketNativePreferencesStore,
    signedInUser: PocketNativeUser?,
    onOpenRecurring: () -> Unit,
    onOpenBudgetPayShortcut: (amount: Double, categoryKey: String, title: String, note: String) -> Unit,
    initialJumpType: String = "",
    initialJumpId: String = "",
    onInitialJumpHandled: () -> Unit = {},
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val initialCachedResponse = remember(repository) { repository.cachedBudget() }
    var loading by remember { mutableStateOf(initialCachedResponse == null) }
    var errorText by remember { mutableStateOf("") }
    var payload by remember { mutableStateOf<PocketNativeBudgetPayload?>(initialCachedResponse?.payload) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showInstallmentDialog by remember { mutableStateOf(false) }
    var showCollectionDialog by remember { mutableStateOf(false) }
    var showQuickBudgetCategoryDialog by remember { mutableStateOf(false) }
    var budgetCategorySaving by remember { mutableStateOf(false) }
    var showInstallmentInsights by remember { mutableStateOf(false) }
    var editingBudgetItem by remember { mutableStateOf<PocketNativeBudgetItem?>(null) }
    var selectedInstallmentDetails by remember { mutableStateOf<PocketNativeInstallmentPlan?>(null) }
    var selectedPartialPaymentPlan by remember { mutableStateOf<PocketNativeInstallmentPlan?>(null) }
    var editingInstallmentPlan by remember { mutableStateOf<PocketNativeInstallmentPlan?>(null) }
    var editingLoanReminder by remember { mutableStateOf<PocketNativeLoanDueReminder?>(null) }
    var editingCollectionPlan by remember { mutableStateOf<PocketNativeCollectionPlan?>(null) }
    var snoozingLoanReminder by remember { mutableStateOf<PocketNativeLoanDueReminder?>(null) }
    var editingMonthlyReminder by remember { mutableStateOf<PocketNativeMonthlyReminder?>(null) }
    var pendingMarkPaidInstallmentPlan by remember { mutableStateOf<PocketNativeInstallmentPlan?>(null) }
    var pendingDoneLoanReminder by remember { mutableStateOf<PocketNativeLoanDueReminder?>(null) }
    var pendingDeleteBudgetItem by remember { mutableStateOf<PocketNativeBudgetItem?>(null) }
    var pendingPayBudgetItem by remember { mutableStateOf<PocketNativeBudgetItem?>(null) }
    var activePartialBudgetExpenseItem by remember { mutableStateOf<PocketNativeBudgetItem?>(null) }
    var pendingDeleteInstallmentPlan by remember { mutableStateOf<PocketNativeInstallmentPlan?>(null) }
    var pendingDeleteLoanReminder by remember { mutableStateOf<PocketNativeLoanDueReminder?>(null) }
    var pendingDeleteMonthlyReminder by remember { mutableStateOf<PocketNativeMonthlyReminder?>(null) }
    var pendingDeleteCollectionPlan by remember { mutableStateOf<PocketNativeCollectionPlan?>(null) }
    var pendingFullCollectCollectionPlan by remember { mutableStateOf<PocketNativeCollectionPlan?>(null) }
    var pendingCollectCollectionPlan by remember { mutableStateOf<PocketNativeCollectionPlan?>(null) }
    var activeCollectionPlan by remember { mutableStateOf<PocketNativeCollectionPlan?>(null) }
    var budgetToastMessage by remember { mutableStateOf<String?>(null) }
    var showOverdueOnly by remember { mutableStateOf(false) }
    var reminders by remember { mutableStateOf(preferencesStore.monthlyReminders()) }
    var loanDueReminders by remember { mutableStateOf(preferencesStore.loanDueReminders()) }
    var installmentPlans by remember { mutableStateOf(preferencesStore.installmentPlans()) }
    var collectionPlans by remember { mutableStateOf(preferencesStore.normalizeCollectionPlansForToday()) }
    var lockedFeature by remember { mutableStateOf<PocketNativeFeatureGate?>(null) }
    var highlightedJumpType by remember { mutableStateOf("") }
    var highlightedJumpId by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        errorText = ""
        if (payload == null) {
            loading = true
        }
        val result = repository.budget()
        loading = false
        if (result.isSuccess) {
            val response: PocketNativeBudgetResponse? = result.getOrNull()
            payload = response?.payload
            collectionPlans = preferencesStore.normalizeCollectionPlansForToday()
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Budget could not be loaded."
        }
    }

    LaunchedEffect(budgetToastMessage) {
        budgetToastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            budgetToastMessage = null
        }
    }

    val today = remember { LocalDate.now() }
    val filteredCollectionPlans = remember(collectionPlans, showOverdueOnly) {
        if (!showOverdueOnly) {
            collectionPlans
        } else {
            collectionPlans.filter { plan ->
                val nextDate = runCatching { LocalDate.parse(plan.nextCollectionDate) }.getOrNull()
                plan.currentDueAmount > 0.0 && ((nextDate?.isBefore(today) == true) || plan.currentDueAmount > plan.baseAmount + 0.009)
            }
        }
    }
    val filteredInstallmentPlans = remember(installmentPlans, showOverdueOnly) {
        if (!showOverdueOnly) {
            installmentPlans
        } else {
            installmentPlans.filter { plan -> pocketInstallmentIsOverdue(plan) && plan.status != "completed" }
        }
    }
    val filteredBudgets = remember(payload?.budgets, showOverdueOnly) {
        val budgets = payload?.budgets.orEmpty()
        if (!showOverdueOnly) {
            budgets
        } else {
            budgets.filter { budget -> budget.progress >= 75.0 || budget.remaining <= 0.0 }
        }
    }
    val filteredReminders = remember(reminders, showOverdueOnly) {
        if (!showOverdueOnly) {
            reminders
        } else {
            reminders.filter { reminder -> reminder.dayOfMonth <= today.dayOfMonth }
        }
    }

    LaunchedEffect(initialJumpType, initialJumpId, payload, reminders, installmentPlans, collectionPlans) {
        if (initialJumpType.isBlank() || initialJumpId.isBlank()) return@LaunchedEffect
        highlightedJumpType = initialJumpType
        highlightedJumpId = initialJumpId
        showOverdueOnly = false

        val collectionBlockCount = if (collectionPlans.isNotEmpty()) 1 + collectionPlans.size else 0
        val loanBlockCount = 0
        val installmentBlockCount = if (installmentPlans.isNotEmpty()) 1 + installmentPlans.size else 0
        val monthlySummaryIndex = 7 + collectionBlockCount + loanBlockCount + installmentBlockCount
        val budgetHeaderIndex = monthlySummaryIndex + 1
        val budgetContentStart = budgetHeaderIndex + 1
        val budgetContentCount = payload?.budgets?.size ?: 0
        val monthlyHeaderIndex = budgetContentStart + budgetContentCount
        val monthlyContentStart = monthlyHeaderIndex + 1

        val targetIndex = when (initialJumpType) {
            "collection" -> {
                collectionPlans.indexOfFirst { it.id == initialJumpId }
                    .takeIf { it >= 0 }
                    ?.let { 7 + it }
                    ?: 6
            }
            "loan" -> null
            "installment" -> {
                installmentPlans.indexOfFirst { it.id == initialJumpId }
                    .takeIf { it >= 0 }
                    ?.let { 8 + collectionBlockCount + loanBlockCount + it }
                    ?: (7 + collectionBlockCount + loanBlockCount)
            }
            "budget" -> {
                payload?.budgets?.indexOfFirst { it.id.toString() == initialJumpId }
                    ?.takeIf { it >= 0 }
                    ?.let { budgetContentStart + it }
                    ?: budgetHeaderIndex
            }
            "monthly" -> {
                reminders.indexOfFirst { it.id == initialJumpId }
                    .takeIf { it >= 0 }
                    ?.let { monthlyContentStart + it }
                    ?: monthlyHeaderIndex
            }
            else -> null
        }

        targetIndex?.let { listState.animateScrollToItem(index = it, scrollOffset = -24) }
        onInitialJumpHandled()
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
            title = "Budget paused",
            body = errorText.ifBlank { "Pocket Pro budgets are not ready yet." },
            onBack = onBack,
        )
        return
    }
    var budgetCategoryOptions by remember(data.categoryOptions) {
        mutableStateOf(
            if (data.categoryOptions.isNotEmpty()) {
                data.categoryOptions
            } else {
                budgetFallbackCategoryOptions()
            }
        )
    }
    val recurringGate = remember(signedInUser) { pocketNativeFeatureGate(signedInUser, "recurring_transactions") }
    val installmentInsightsGate = remember(signedInUser) { pocketNativeFeatureGate(signedInUser, "installment_insights") }
    val hasLowerDataSections = filteredCollectionPlans.isNotEmpty() ||
        filteredInstallmentPlans.isNotEmpty() ||
        filteredBudgets.isNotEmpty() ||
        filteredReminders.isNotEmpty()

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            NativeFeatureTopBar(
                title = "Budget Manager",
                subtitle = "Set monthly budgets, reminders, and installment plans from one clear control page.",
                onBack = onBack,
            )
        }
        item {
            BudgetGuideStrip(
                totalBudget = nativeBudgetMoney(data.summary.totalBudget, data.currencySymbol),
                activeBudgets = data.summary.activeBudgets,
            )
        }
        item {
            BudgetFilterStrip(
                showOverdueOnly = showOverdueOnly,
                onShowAll = { showOverdueOnly = false },
                onShowOverdueOnly = { showOverdueOnly = true },
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlatActionTile(
                    title = "Collection Planner",
                    accent = AccentBlue,
                    icon = Icons.Rounded.Payments,
                    modifier = Modifier.weight(1f),
                    onClick = { showCollectionDialog = true },
                )
                FlatActionTile(
                    title = "Installment Planner",
                    accent = AccentOrange,
                    icon = Icons.Rounded.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    onClick = { showInstallmentDialog = true },
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlatActionTile(
                    title = "Add Budget",
                    accent = AccentBlue,
                    icon = Icons.Rounded.Sell,
                    modifier = Modifier.weight(1f),
                    onClick = { showBudgetDialog = true },
                )
                FlatActionTile(
                    title = "Monthly Reminder",
                    accent = AccentGreen,
                    icon = Icons.Rounded.NotificationsActive,
                    modifier = Modifier.weight(1f),
                    onClick = { showReminderDialog = true },
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlatActionTile(
                    title = "Installment Insights",
                    accent = AccentPurple,
                    icon = Icons.Rounded.PieChart,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (installmentInsightsGate.granted) {
                            showInstallmentInsights = true
                        } else {
                            lockedFeature = installmentInsightsGate
                        }
                    },
                    trailingContent = {
                        if (!installmentInsightsGate.granted) {
                            PocketNativeProBadge()
                        }
                    },
                )
                FlatActionTile(
                    title = "Recurring",
                    accent = AccentPurple,
                    icon = Icons.Rounded.History,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenRecurring,
                    trailingContent = {
                        if (!recurringGate.granted) {
                            PocketNativeProBadge()
                        }
                    },
                )
            }
        }
        if (!hasLowerDataSections) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceCard.copy(alpha = 0.68f))
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        "No planner entries yet",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                    Text(
                        "Use Collection Planner, Installment Planner, Add Budget, or Monthly Reminder to start tracking items here.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                }
            }
        }
        if (filteredCollectionPlans.isNotEmpty()) {
            item {
                val activeCollections = filteredCollectionPlans.filter { it.status == "active" }
                val monthlyCollectTotal = activeCollections.sumOf { it.baseAmount }
                val dueCollectTotal = activeCollections.sumOf { it.currentDueAmount }
                val collectedThisMonth = activeCollections.sumOf { plan ->
                    plan.paymentHistory.sumOf { entry ->
                        if (entry.collectedDate.startsWith(LocalDate.now().toString().take(7))) entry.amount else 0.0
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, bottom = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Collection Planner", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(
                            "New entry",
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showCollectionDialog = true },
                        )
                    }
                    if (activeCollections.isNotEmpty()) {
                        Text(
                            "${activeCollections.size} active • Monthly ${nativeBudgetMoney(monthlyCollectTotal, "৳")}",
                            color = TextSecondary,
                            fontSize = 13.sp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                BudgetSummaryItem("Due now", nativeBudgetMoney(dueCollectTotal, "৳"))
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                BudgetSummaryItem("Collected", nativeBudgetMoney(collectedThisMonth, "৳"))
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                BudgetSummaryItem("Plans", activeCollections.size.toString())
                            }
                        }
                    }
                }
            }
            items(filteredCollectionPlans, key = { it.id }) { plan ->
                CollectionPlanRow(
                    plan = plan,
                    highlighted = highlightedJumpType == "collection" && highlightedJumpId == plan.id,
                    onOpen = { editingCollectionPlan = plan },
                    onCollect = { pendingFullCollectCollectionPlan = plan },
                    onSomeCollect = { pendingCollectCollectionPlan = plan },
                    onHistory = { editingCollectionPlan = plan },
                )
            }
        }
        if (filteredInstallmentPlans.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel("Installment Plans", "${filteredInstallmentPlans.count { it.status == "active" }} active")
                    Text(
                        "New entry",
                        color = AccentOrange,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showInstallmentDialog = true },
                    )
                }
            }
            items(filteredInstallmentPlans, key = { it.id }) { plan ->
                InstallmentPlanRow(
                    plan = plan,
                    highlighted = highlightedJumpType == "installment" && highlightedJumpId == plan.id,
                    onOpenPlan = {
                        editingInstallmentPlan = plan
                    },
                    onPay = {
                        pendingMarkPaidInstallmentPlan = plan
                    },
                    onSomePay = {
                        selectedPartialPaymentPlan = plan
                    },
                    onHistory = {
                        editingInstallmentPlan = plan
                    },
                )
            }
        }
        if (filteredBudgets.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Monthly Budget", color = TextPrimary.copy(alpha = 0.84f), fontWeight = FontWeight.SemiBold)
                    Text(
                        nativeBudgetMoney(data.summary.totalBudget, data.currencySymbol),
                        color = TextPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BudgetSummaryItem("Spent", nativeBudgetMoney(data.summary.spent, data.currencySymbol))
                        BudgetSummaryItem("Remaining", nativeBudgetMoney(data.summary.remaining, data.currencySymbol))
                        BudgetSummaryItem("Used", "${data.summary.usedPercent}%")
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                    SectionLabel("Overall Progress", "${data.summary.usedPercent}%")
                    LinearProgressIndicator(
                        progress = { (data.summary.usedPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = AccentBlue,
                        trackColor = Color(0xFF27314A),
                    )
                }
            }
            item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                    SectionLabel("Category Budgets", "${filteredBudgets.size} active")
                    Text(
                        "New entry",
                        color = AccentBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showBudgetDialog = true },
                    )
                }
            }
            items(filteredBudgets) { item ->
                BudgetCategoryCard(
                    item = item,
                    currencySymbol = data.currencySymbol,
                    highlighted = highlightedJumpType == "budget" && highlightedJumpId == item.id.toString(),
                    onPay = { pendingPayBudgetItem = item },
                    onSomeExpense = { activePartialBudgetExpenseItem = item },
                    onHistory = { editingBudgetItem = item },
                    onEdit = { editingBudgetItem = item },
                    onDelete = { pendingDeleteBudgetItem = item },
                )
            }
        }
        if (filteredReminders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel("Monthly Bills & Reminders", "${filteredReminders.size} active")
                    Text(
                        "New entry",
                        color = AccentGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showReminderDialog = true },
                    )
                }
            }
            items(filteredReminders, key = { it.id }) { reminder ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceCardSoft.copy(alpha = 0.92f))
                        .then(pocketSectionHighlight(highlightedJumpType == "monthly" && highlightedJumpId == reminder.id, AccentGreen))
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(reminder.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Monthly reminder", color = TextSecondary, fontSize = 12.sp)
                            Text("Every month on day ${reminder.dayOfMonth}", color = TextSecondary)
                            if (reminder.amount > 0.0) {
                                Text(
                                    "Amount ${nativeBudgetMoney(reminder.amount, "৳")}",
                                    color = AccentGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(AccentGreen.copy(alpha = 0.14f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                "Active",
                                color = AccentGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Alert flow", color = TextSecondary, fontSize = 11.sp)
                            Text("10d • 5d • 3d • 1d • same day", color = AccentGreen, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Type", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                when (reminder.kind.lowercase()) {
                                    "rent" -> "Rent"
                                    "school" -> "School Fee"
                                    "internet" -> "Internet"
                                    "subscription" -> "Subscription"
                                    "bill" -> "Bill"
                                    else -> "General"
                                },
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    if (reminder.note.isNotBlank()) {
                        Text(reminder.note, color = TextSecondary, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FlatInlineAction(
                            label = "History",
                            icon = Icons.Rounded.History,
                            tint = AccentPurple,
                            compact = true,
                            onClick = {
                                editingMonthlyReminder = reminder
                            },
                        )
                        FlatInlineAction(
                            label = "Edit",
                            icon = Icons.Rounded.CalendarMonth,
                            tint = AccentBlue,
                            compact = true,
                            onClick = {
                                editingMonthlyReminder = reminder
                            },
                        )
                        FlatInlineAction(
                            label = "Delete",
                            icon = Icons.Rounded.Delete,
                            tint = AccentOrange,
                            compact = true,
                            onClick = {
                                pendingDeleteMonthlyReminder = reminder
                            },
                        )
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        BudgetCreateDialog(
            categories = budgetCategoryOptions,
            categorySaving = budgetCategorySaving,
            initialBudget = null,
            onCreateCategoryRequest = { showQuickBudgetCategoryDialog = true },
            onDismiss = { showBudgetDialog = false },
            onSave = { name, categoryKey, amount, note, dueDayOfMonth ->
                scope.launch {
                    val safeCategoryKey = categoryKey.takeIf { it.isNotBlank() && it != "all" }
                        ?: budgetCategoryOptions.firstOrNull { it.key != "all" }?.key
                        ?: "food"
                    val duplicateCategory = payload?.budgets?.any {
                        it.categoryKey.equals(safeCategoryKey, ignoreCase = true)
                    } == true
                    if (duplicateCategory) {
                        errorText = "This category budget already exists. Edit the existing one instead."
                        return@launch
                    }
                    val result = repository.saveBudget(
                        budgetName = name,
                        categoryKey = safeCategoryKey,
                        amount = amount,
                        periodType = "MONTHLY",
                        startDate = LocalDate.now().withDayOfMonth(dueDayOfMonth.coerceIn(1, 28)).toString(),
                        endDate = "",
                        note = note,
                    )
                    if (result.isSuccess) {
                        payload = result.getOrNull()?.payload ?: payload
                        val latestBudget = payload?.budgets?.firstOrNull {
                            it.name.equals(name.trim(), ignoreCase = true) && it.categoryKey.equals(safeCategoryKey, ignoreCase = true)
                        }
                        reminders = (reminders + PocketNativeMonthlyReminder(
                            id = "budget-${latestBudget?.id ?: System.currentTimeMillis()}",
                            title = name.trim(),
                            dayOfMonth = dueDayOfMonth.coerceIn(1, 28),
                            note = note.trim(),
                            kind = "budget",
                            amount = amount,
                            categoryKey = safeCategoryKey,
                            linkedBudgetId = latestBudget?.id ?: 0,
                        )).distinctBy { it.id }
                        preferencesStore.saveMonthlyReminders(reminders)
                        PocketReminderScheduler.ensureDailyReminder(context)
                        errorText = ""
                        showBudgetDialog = false
                    } else {
                        errorText = result.exceptionOrNull()?.message ?: "Budget could not be saved."
                    }
                }
            },
        )
    }

    editingBudgetItem?.let { budgetItem ->
        BudgetCreateDialog(
            categories = budgetCategoryOptions,
            categorySaving = budgetCategorySaving,
            initialBudget = budgetItem,
            onCreateCategoryRequest = { showQuickBudgetCategoryDialog = true },
            onDismiss = { editingBudgetItem = null },
            onSave = { name, categoryKey, amount, note, dueDayOfMonth ->
                val duplicateCategory = payload?.budgets?.any {
                    it.id != budgetItem.id && it.categoryKey.equals(categoryKey, ignoreCase = true)
                } == true
                if (duplicateCategory) {
                    errorText = "This category budget already exists. Change the existing one instead."
                    return@BudgetCreateDialog
                }
                val updated = repository.updateBudgetItemLocal(
                    budgetId = budgetItem.id,
                    budgetName = name,
                    categoryKey = categoryKey,
                    amount = amount,
                    note = note,
                    dueDayOfMonth = dueDayOfMonth,
                )
                if (updated?.payload != null) {
                    payload = updated.payload
                    reminders = reminders.map { reminder ->
                        if (reminder.linkedBudgetId == budgetItem.id || reminder.id == "budget-${budgetItem.id}") {
                            reminder.copy(
                                title = name.trim(),
                                dayOfMonth = dueDayOfMonth.coerceIn(1, 28),
                                note = note.trim(),
                                kind = "budget",
                                amount = amount,
                                categoryKey = categoryKey,
                                linkedBudgetId = budgetItem.id,
                            )
                        } else reminder
                    }
                    preferencesStore.saveMonthlyReminders(reminders)
                    PocketReminderScheduler.ensureDailyReminder(context)
                    editingBudgetItem = null
                } else {
                    errorText = "This category budget already exists. Change the existing one instead."
                }
            },
        )
    }

    if (showQuickBudgetCategoryDialog) {
        QuickBudgetCategoryDialog(
            saving = budgetCategorySaving,
            onDismiss = { showQuickBudgetCategoryDialog = false },
            onSave = { label ->
                scope.launch {
                    budgetCategorySaving = true
                    val result = repository.saveCategory(
                        kind = "expense",
                        originalKey = "",
                        label = label,
                        iconKey = "shapes",
                        colorHex = "#7C4DFF",
                    )
                    budgetCategorySaving = false
                    if (result.isSuccess) {
                        val savedCategories = result.getOrNull()?.payload?.categories.orEmpty()
                        budgetCategoryOptions = if (savedCategories.isNotEmpty()) {
                            savedCategories
                        } else {
                            listOf(
                                PocketNativeTransactionCategoryOption(
                                    key = label.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "custom_category" },
                                    label = label.trim(),
                                    colorHex = "#7C4DFF",
                                    iconClass = "",
                                    iconKey = "shapes",
                                )
                            ) + budgetCategoryOptions
                        }.distinctBy { it.key.lowercase() }
                        showQuickBudgetCategoryDialog = false
                    } else {
                        errorText = result.exceptionOrNull()?.message ?: "Category could not be created."
                    }
                }
            },
        )
    }

    if (showReminderDialog) {
        MonthlyReminderDialog(
            initialReminder = null,
            previewOnly = false,
            onDismiss = { showReminderDialog = false },
            onSave = { title, dayOfMonth, note, kind, amount ->
                reminders = reminders + PocketNativeMonthlyReminder(
                    id = "${System.currentTimeMillis()}",
                    title = title,
                    dayOfMonth = dayOfMonth,
                    note = note,
                    kind = kind,
                    amount = amount,
                )
                preferencesStore.saveMonthlyReminders(reminders)
                PocketReminderScheduler.ensureDailyReminder(context)
                showReminderDialog = false
                budgetToastMessage = "Monthly reminder saved"
            },
        )
    }

    if (showCollectionDialog) {
        CollectionPlanDialog(
            initialPlan = null,
            onDismiss = { showCollectionDialog = false },
            onSave = { title, counterparty, baseAmount, nextDate, frequency, note, autoReminderEnabled, carryOverEnabled ->
                preferencesStore.addCollectionPlan(
                    title = title,
                    counterparty = counterparty,
                    baseAmount = baseAmount,
                    nextCollectionDate = nextDate,
                    frequency = frequency,
                    note = note,
                    autoReminderEnabled = autoReminderEnabled,
                    carryOverEnabled = carryOverEnabled,
                )
                collectionPlans = preferencesStore.normalizeCollectionPlansForToday()
                PocketReminderScheduler.ensureDailyReminder(context)
                showCollectionDialog = false
                budgetToastMessage = "Collection plan saved"
            },
        )
    }

    editingCollectionPlan?.let { plan ->
        CollectionPlanDialog(
            initialPlan = plan,
            onDismiss = { editingCollectionPlan = null },
            onSave = { title, counterparty, baseAmount, nextDate, frequency, note, autoReminderEnabled, carryOverEnabled ->
                preferencesStore.updateCollectionPlan(
                    planId = plan.id,
                    title = title,
                    counterparty = counterparty,
                    baseAmount = baseAmount,
                    nextCollectionDate = nextDate,
                    frequency = frequency,
                    note = note,
                    autoReminderEnabled = autoReminderEnabled,
                    carryOverEnabled = carryOverEnabled,
                )
                collectionPlans = preferencesStore.normalizeCollectionPlansForToday()
                PocketReminderScheduler.ensureDailyReminder(context)
                editingCollectionPlan = null
                budgetToastMessage = "Collection plan updated"
            },
            onDelete = {
                pendingDeleteCollectionPlan = plan
            },
        )
    }

    editingMonthlyReminder?.let { reminder ->
        MonthlyReminderDialog(
            initialReminder = reminder,
            previewOnly = false,
            onDismiss = { editingMonthlyReminder = null },
            onSave = { title, dayOfMonth, note, kind, amount ->
                reminders = reminders.map { existing ->
                    if (existing.id == reminder.id) {
                        existing.copy(title = title, dayOfMonth = dayOfMonth, note = note, kind = kind, amount = amount)
                    } else {
                        existing
                    }
                }
                preferencesStore.saveMonthlyReminders(reminders)
                PocketReminderScheduler.ensureDailyReminder(context)
                editingMonthlyReminder = null
                budgetToastMessage = "Monthly reminder updated"
            },
        )
    }

    if (showInstallmentDialog) {
        InstallmentPlanDialog(
            initialPlan = null,
            onDismiss = { showInstallmentDialog = false },
            onSave = { title, lender, categoryKey, categoryLabel, loanAmount, installmentAmount, frequency, firstDueDate, totalInstallments, autoReminderEnabled, note ->
                preferencesStore.addInstallmentPlan(
                    title = title,
                    lender = lender,
                    categoryKey = categoryKey,
                    categoryLabel = categoryLabel,
                    loanAmount = loanAmount,
                    installmentAmount = installmentAmount,
                    frequency = frequency,
                    firstDueDate = firstDueDate,
                    totalInstallments = totalInstallments,
                    autoReminderEnabled = autoReminderEnabled,
                    note = note,
                )
                installmentPlans = preferencesStore.installmentPlans()
                PocketReminderScheduler.ensureDailyReminder(context)
                showInstallmentDialog = false
            },
        )
    }

    editingInstallmentPlan?.let { plan ->
        InstallmentPlanDialog(
            initialPlan = plan,
            onDismiss = { editingInstallmentPlan = null },
            onSave = { title, lender, categoryKey, categoryLabel, loanAmount, installmentAmount, frequency, firstDueDate, totalInstallments, autoReminderEnabled, note ->
                preferencesStore.updateInstallmentPlan(
                    planId = plan.id,
                    title = title,
                    lender = lender,
                    categoryKey = categoryKey,
                    categoryLabel = categoryLabel,
                    loanAmount = loanAmount,
                    installmentAmount = installmentAmount,
                    frequency = frequency,
                    nextDueDate = firstDueDate,
                    totalInstallments = totalInstallments,
                    autoReminderEnabled = autoReminderEnabled,
                    note = note,
                )
                installmentPlans = preferencesStore.installmentPlans()
                PocketReminderScheduler.ensureDailyReminder(context)
                editingInstallmentPlan = null
            },
            onMarkPaid = {
                pendingMarkPaidInstallmentPlan = plan
            },
            onPartialPayment = {
                selectedPartialPaymentPlan = plan
            },
            onDelete = {
                pendingDeleteInstallmentPlan = plan
            },
        )
    }

    if (showInstallmentInsights) {
        InstallmentInsightsDialog(
            plans = installmentPlans,
            onDismiss = { showInstallmentInsights = false },
        )
    }

    selectedInstallmentDetails?.let { plan ->
        InstallmentPlanDetailsDialog(
            plan = plan,
            onDismiss = { selectedInstallmentDetails = null },
        )
    }

    selectedPartialPaymentPlan?.let { plan ->
        InstallmentPartialPaymentDialog(
            plan = plan,
            onDismiss = { selectedPartialPaymentPlan = null },
            onSave = { amount, note ->
                preferencesStore.addInstallmentPayment(planId = plan.id, amount = amount, noteOverride = note)
                installmentPlans = preferencesStore.installmentPlans()
                selectedPartialPaymentPlan = null
                PocketReminderScheduler.ensureDailyReminder(context)
            },
        )
    }

    editingLoanReminder?.let { reminder ->
        LoanDueReminderEditDialog(
            reminder = reminder,
            onDismiss = { editingLoanReminder = null },
            onSave = { counterparty, originalAmount, remainingAmount, dueDate, note ->
                preferencesStore.updateLoanDueReminder(
                    reminderId = reminder.id,
                    kind = reminder.kind,
                    counterparty = counterparty,
                    originalAmount = originalAmount,
                    remainingAmount = remainingAmount,
                    dueDate = dueDate,
                    note = note,
                )
                loanDueReminders = preferencesStore.loanDueReminders()
                PocketReminderScheduler.ensureDailyReminder(context)
                editingLoanReminder = null
            },
        )
    }

    snoozingLoanReminder?.let { reminder ->
        QuickSnoozeDialog(
            title = if (reminder.kind == "lent") "Snooze Get Back" else "Snooze Pay Back",
            currentDate = reminder.dueDate,
            onDismiss = { snoozingLoanReminder = null },
            onPickDays = { days ->
                val nextDate = runCatching { LocalDate.parse(reminder.dueDate).plusDays(days.toLong()).toString() }
                    .getOrElse { reminder.dueDate }
                preferencesStore.updateLoanDueReminderDate(reminder.id, nextDate)
                loanDueReminders = preferencesStore.loanDueReminders()
                PocketReminderScheduler.ensureDailyReminder(context)
                snoozingLoanReminder = null
            },
        )
    }

    lockedFeature?.let { gate ->
        PocketNativeUpgradeDialog(
            gate = gate,
            onDismiss = { lockedFeature = null },
        )
    }

    pendingDeleteInstallmentPlan?.let { plan ->
        PocketDeleteConfirmationDialog(
            title = "Delete installment plan?",
            message = "Are you sure you want to delete ${pocketNormalizeInstallmentTitle(plan.title, plan.categoryLabel)}? This will remove its reminders and saved progress.",
            onDismiss = { pendingDeleteInstallmentPlan = null },
            onConfirm = {
                preferencesStore.deleteInstallmentPlan(plan.id)
                installmentPlans = preferencesStore.installmentPlans()
                pendingDeleteInstallmentPlan = null
                PocketReminderScheduler.ensureDailyReminder(context)
            },
        )
    }

    pendingMarkPaidInstallmentPlan?.let { plan ->
        PocketDeleteConfirmationDialog(
            title = "Mark this installment as paid?",
            message = "Are you sure you want to mark ${pocketNormalizeInstallmentTitle(plan.title, plan.categoryLabel)} as paid for this due cycle? Pocket Pro will move the next due date forward.",
            onDismiss = { pendingMarkPaidInstallmentPlan = null },
            onConfirm = {
                preferencesStore.markInstallmentPaid(plan.id)
                installmentPlans = preferencesStore.installmentPlans()
                pendingMarkPaidInstallmentPlan = null
                PocketReminderScheduler.ensureDailyReminder(context)
            },
        )
    }

    pendingDoneLoanReminder?.let { reminder ->
        PocketDeleteConfirmationDialog(
            title = "Mark this follow-up as done?",
            message = "Are you sure you want to close this ${if (reminder.kind == "lent") "collection" else "payback"} reminder for ${reminder.counterparty.ifBlank { "this person" }}?",
            onDismiss = { pendingDoneLoanReminder = null },
            onConfirm = {
                preferencesStore.deleteLoanDueReminder(reminder.id)
                loanDueReminders = preferencesStore.loanDueReminders()
                pendingDoneLoanReminder = null
                PocketReminderScheduler.ensureDailyReminder(context)
            },
        )
    }

    pendingDeleteLoanReminder?.let { reminder ->
        PocketDeleteConfirmationDialog(
            title = "Delete follow-up reminder?",
            message = "Are you sure you want to delete this ${if (reminder.kind == "lent") "collect" else "pay"} reminder for ${reminder.counterparty.ifBlank { "this person" }}?",
            onDismiss = { pendingDeleteLoanReminder = null },
            onConfirm = {
                preferencesStore.deleteLoanDueReminder(reminder.id)
                loanDueReminders = preferencesStore.loanDueReminders()
                pendingDeleteLoanReminder = null
                PocketReminderScheduler.ensureDailyReminder(context)
            },
        )
    }

    pendingDeleteMonthlyReminder?.let { reminder ->
        PocketDeleteConfirmationDialog(
            title = "Delete monthly reminder?",
            message = "Are you sure you want to delete ${reminder.title}? Pocket Pro will stop reminding you every month.",
            onDismiss = { pendingDeleteMonthlyReminder = null },
            onConfirm = {
                reminders = reminders.filterNot { it.id == reminder.id }
                preferencesStore.saveMonthlyReminders(reminders)
                pendingDeleteMonthlyReminder = null
                PocketReminderScheduler.ensureDailyReminder(context)
            },
        )
    }

    pendingFullCollectCollectionPlan?.let { plan ->
        PocketDeleteConfirmationDialog(
            title = "Collect full due?",
            message = "Pocket Pro will collect the full due amount ${nativeBudgetMoney(plan.currentDueAmount, "৳")} from ${plan.counterparty}.",
            onDismiss = { pendingFullCollectCollectionPlan = null },
            onConfirm = {
                preferencesStore.collectFromPlan(plan.id, plan.currentDueAmount, "")
                collectionPlans = preferencesStore.normalizeCollectionPlansForToday()
                PocketReminderScheduler.ensureDailyReminder(context)
                pendingFullCollectCollectionPlan = null
                budgetToastMessage = "Full collection saved"
            },
        )
    }

    pendingCollectCollectionPlan?.let { plan ->
        PocketDeleteConfirmationDialog(
            title = "Open collect amount?",
            message = "Pocket Pro will open the collect sheet for ${plan.counterparty} with ${nativeBudgetMoney(plan.currentDueAmount, "৳")} due. You can confirm the exact collected amount before saving.",
            onDismiss = { pendingCollectCollectionPlan = null },
            onConfirm = {
                activeCollectionPlan = plan
                pendingCollectCollectionPlan = null
            },
        )
    }

    activeCollectionPlan?.let { plan ->
        CollectionPaymentDialog(
            plan = plan,
            onDismiss = { activeCollectionPlan = null },
            onSave = { amount, note ->
                preferencesStore.collectFromPlan(plan.id, amount, note)
                collectionPlans = preferencesStore.normalizeCollectionPlansForToday()
                PocketReminderScheduler.ensureDailyReminder(context)
                activeCollectionPlan = null
                budgetToastMessage = "Collection saved"
            },
        )
    }

    pendingDeleteCollectionPlan?.let { plan ->
        PocketDeleteConfirmationDialog(
            title = "Delete collection plan?",
            message = "Are you sure you want to delete ${plan.title}? Pocket Pro will remove its due amount, history, and reminder tracking.",
            onDismiss = { pendingDeleteCollectionPlan = null },
            onConfirm = {
                preferencesStore.deleteCollectionPlan(plan.id)
                collectionPlans = preferencesStore.normalizeCollectionPlansForToday()
                PocketReminderScheduler.ensureDailyReminder(context)
                pendingDeleteCollectionPlan = null
                budgetToastMessage = "Collection plan deleted"
            },
        )
    }

    pendingDeleteBudgetItem?.let { budgetItem ->
        PocketDeleteConfirmationDialog(
            title = "Delete budget?",
            message = "Are you sure you want to delete ${budgetItem.name}? Pocket Pro will also remove its monthly reminder.",
            onDismiss = { pendingDeleteBudgetItem = null },
            onConfirm = {
                val updated = repository.deleteBudgetItemLocal(budgetItem.id)
                if (updated?.payload != null) {
                    payload = updated.payload
                }
                reminders = reminders.filterNot { it.linkedBudgetId == budgetItem.id || it.id == "budget-${budgetItem.id}" }
                preferencesStore.saveMonthlyReminders(reminders)
                PocketReminderScheduler.ensureDailyReminder(context)
                pendingDeleteBudgetItem = null
            },
        )
    }

    pendingPayBudgetItem?.let { budgetItem ->
        PocketDeleteConfirmationDialog(
            title = "Add full expense from this budget?",
            message = "Pocket Pro will open the expense form for ${budgetItem.name} with ${nativeBudgetMoney(budgetItem.amount, payload?.currencySymbol ?: "৳")} prefilled. You can still change it before saving.",
            onDismiss = { pendingPayBudgetItem = null },
            onConfirm = {
                pendingPayBudgetItem = null
                onOpenBudgetPayShortcut(
                    budgetItem.amount,
                    budgetItem.categoryKey,
                    budgetItem.name,
                    budgetItem.note,
                )
            },
        )
    }

    activePartialBudgetExpenseItem?.let { budgetItem ->
        BudgetPartialExpenseDialog(
            item = budgetItem,
            currencySymbol = payload?.currencySymbol ?: "৳",
            onDismiss = { activePartialBudgetExpenseItem = null },
            onSave = { amount, note ->
                activePartialBudgetExpenseItem = null
                onOpenBudgetPayShortcut(
                    amount,
                    budgetItem.categoryKey,
                    budgetItem.name,
                    note.ifBlank { budgetItem.note },
                )
            },
        )
    }
}

private fun pocketSectionHighlight(
    highlighted: Boolean,
    accent: Color,
): Modifier {
    return if (highlighted) {
        Modifier.border(
            width = 1.6.dp,
            color = accent.copy(alpha = 0.92f),
            shape = RoundedCornerShape(24.dp),
        )
    } else {
        Modifier
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LoanDueReminderRow(
    reminder: PocketNativeLoanDueReminder,
    highlighted: Boolean = false,
    onOpenSettlement: () -> Unit,
    onEdit: () -> Unit,
    onHistory: () -> Unit,
    onSnooze: () -> Unit,
    onDone: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val accent = if (reminder.kind == "lent") AccentGreen else AccentOrange
    val originalAmount = pocketLoanReminderOriginalAmount(reminder)
    val settledAmount = pocketLoanReminderSettledAmount(reminder)
    val remainingAmount = pocketLoanReminderRemainingAmount(reminder)
    val title = if (reminder.kind == "lent") {
        "Get back from ${reminder.counterparty.ifBlank { "someone" }}"
    } else {
        "Pay back to ${reminder.counterparty.ifBlank { "someone" }}"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(pocketSectionHighlight(highlighted, AccentGreen))
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    "Due ${reminder.dueDate} • Remaining ${nativeBudgetMoney(remainingAmount, "৳")}",
                    color = TextSecondary,
                )
                if (settledAmount > 0.0 && originalAmount > 0.0) {
                    Text(
                        if (reminder.kind == "lent") {
                            "Collected ${nativeBudgetMoney(settledAmount, "৳")} of ${nativeBudgetMoney(originalAmount, "৳")}"
                        } else {
                            "Paid ${nativeBudgetMoney(settledAmount, "৳")} of ${nativeBudgetMoney(originalAmount, "৳")}"
                        },
                        color = accent.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (reminder.note.isNotBlank()) {
                    Text(reminder.note, color = TextSecondary, fontSize = 12.sp)
                }
            }
            FlatInlineAction(
                label = if (reminder.kind == "lent") "Collect" else "Pay",
                icon = Icons.Rounded.MonetizationOn,
                tint = accent,
                onClick = onOpenSettlement,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3,
        ) {
            if (reminder.kind == "lent") {
                FlatInlineAction(
                    label = "Reminder",
                    icon = Icons.Rounded.Share,
                    tint = Color(0xFF22C55E),
                    onClick = {
                        PocketShareUtils.shareLoanReminder(
                            context = context,
                            counterparty = reminder.counterparty,
                            amount = remainingAmount,
                            currencySymbol = "৳",
                            dueDate = reminder.dueDate,
                            note = reminder.note,
                        )
                    },
                )
            }
            FlatInlineAction(
                label = "Edit",
                icon = Icons.Rounded.CalendarMonth,
                tint = AccentBlue,
                onClick = onEdit,
            )
            FlatInlineAction(
                label = "History",
                icon = Icons.Rounded.History,
                tint = AccentPurple,
                onClick = onHistory,
            )
            FlatInlineAction(
                label = "Snooze",
                icon = Icons.Rounded.NotificationsOff,
                tint = AccentPurple,
                onClick = onSnooze,
            )
            FlatInlineAction(
                label = "Done",
                icon = Icons.Rounded.Check,
                tint = AccentGreen,
                onClick = onDone,
            )
            FlatInlineAction(
                label = "Delete",
                icon = Icons.Rounded.Delete,
                tint = AccentOrange,
                onClick = onDelete,
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
    }
}

@Composable
private fun QuickSnoozeDialog(
    title: String,
    currentDate: String,
    onDismiss: () -> Unit,
    onPickDays: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Current due $currentDate", color = TextSecondary, fontSize = 12.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Move this reminder forward without opening full edit.", color = TextSecondary, lineHeight = 18.sp)
                FlatActionLine(title = "Snooze 1 day", accent = AccentBlue, icon = Icons.Rounded.CalendarMonth, modifier = Modifier.fillMaxWidth(), onClick = { onPickDays(1) })
                FlatActionLine(title = "Snooze 3 days", accent = AccentPurple, icon = Icons.Rounded.CalendarMonth, modifier = Modifier.fillMaxWidth(), onClick = { onPickDays(3) })
                FlatActionLine(title = "Snooze 7 days", accent = AccentOrange, icon = Icons.Rounded.CalendarMonth, modifier = Modifier.fillMaxWidth(), onClick = { onPickDays(7) })
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun LoanDueReminderEditDialog(
    reminder: PocketNativeLoanDueReminder,
    onDismiss: () -> Unit,
    onSave: (counterparty: String, originalAmount: Double, remainingAmount: Double, dueDate: String, note: String) -> Unit,
) {
    var counterparty by remember(reminder.id) { mutableStateOf(reminder.counterparty) }
    var originalAmount by remember(reminder.id) { mutableStateOf(pocketLoanReminderOriginalAmount(reminder).toInt().toString()) }
    var remainingAmount by remember(reminder.id) { mutableStateOf(pocketLoanReminderRemainingAmount(reminder).toInt().toString()) }
    var dueDate by remember(reminder.id) { mutableStateOf(reminder.dueDate) }
    var note by remember(reminder.id) { mutableStateOf(reminder.note) }
    val modeLabel = if (reminder.kind == "lent") "Get Back" else "Pay Back"
    val helperText = if (reminder.kind == "lent") {
        "Update who will pay you back, how much is still left, and the new receiving date if needed."
    } else {
        "Update who you need to pay, how much is still left, and the new pay date if needed."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Edit $modeLabel", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(helperText, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
                Text(
                    "Reminder type: $modeLabel",
                    color = if (reminder.kind == "lent") AccentGreen else AccentOrange,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = counterparty,
                    onValueChange = { counterparty = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Person") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = TextSecondary,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentBlue,
                    ),
                )
                OutlinedTextField(
                    value = originalAmount,
                    onValueChange = { originalAmount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Total amount (৳)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = TextSecondary,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentBlue,
                    ),
                )
                OutlinedTextField(
                    value = remainingAmount,
                    onValueChange = { remainingAmount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Remaining amount (৳)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = TextSecondary,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentBlue,
                    ),
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Due date") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = TextSecondary,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentBlue,
                    ),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note") },
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = TextSecondary,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentBlue,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val originalValue = originalAmount.toDoubleOrNull() ?: 0.0
                    val remainingValue = remainingAmount.toDoubleOrNull() ?: 0.0
                    onSave(
                        counterparty.trim(),
                        originalValue.coerceAtLeast(0.0),
                        remainingValue.coerceIn(0.0, originalValue.coerceAtLeast(0.0)),
                        dueDate.trim(),
                        note.trim(),
                    )
                },
                enabled = counterparty.isNotBlank() &&
                    dueDate.isNotBlank() &&
                    (originalAmount.toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = SurfaceCard,
        textContentColor = TextPrimary,
        titleContentColor = TextPrimary,
    )
}

@Composable
private fun CollectionPlanRow(
    plan: PocketNativeCollectionPlan,
    highlighted: Boolean = false,
    onOpen: () -> Unit,
    onCollect: () -> Unit,
    onSomeCollect: () -> Unit,
    onHistory: () -> Unit,
) {
    val accent = if (plan.currentDueAmount > plan.baseAmount + 0.009) AccentOrange else AccentBlue
    val collectedThisCycle = (plan.baseAmount - plan.currentDueAmount).coerceAtLeast(0.0)
    val carryLabel = if (plan.currentDueAmount > plan.baseAmount + 0.009) "Carry-over active" else "On track"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCardSoft.copy(alpha = 0.92f))
            .then(pocketSectionHighlight(highlighted, AccentBlue))
            .clickable(onClick = onOpen)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(plan.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Collect from ${plan.counterparty}", color = TextSecondary, fontSize = 12.sp)
                Text(
                    "Next ${plan.nextCollectionDate} • Due ${nativeBudgetMoney(plan.currentDueAmount, "৳")}",
                    color = TextSecondary,
                )
                Text(
                    "Monthly target ${nativeBudgetMoney(plan.baseAmount, "৳")} • Collected ${nativeBudgetMoney(collectedThisCycle, "৳")}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(accent.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(carryLabel, color = accent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Text(
                    if (plan.autoReminderEnabled) "Reminder on" else "Reminder off",
                    color = if (plan.autoReminderEnabled) AccentGreen else TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
        if (plan.note.isNotBlank()) {
            Text(plan.note, color = TextSecondary, fontSize = 12.sp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            InstallmentQuickBoxAction(
                label = "Collect",
                icon = Icons.Rounded.Check,
                tint = AccentGreen,
                onClick = onCollect,
            )
            InstallmentQuickBoxAction(
                label = "Some Collect",
                icon = Icons.Rounded.MonetizationOn,
                tint = AccentBlue,
                onClick = onSomeCollect,
            )
            InstallmentQuickBoxAction(
                label = "History",
                icon = Icons.Rounded.History,
                tint = AccentPurple,
                onClick = onHistory,
            )
        }
    }
}

@Composable
private fun CollectionPlanDialog(
    initialPlan: PocketNativeCollectionPlan?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        counterparty: String,
        baseAmount: Double,
        nextDate: String,
        frequency: String,
        note: String,
        autoReminderEnabled: Boolean,
        carryOverEnabled: Boolean,
    ) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val dateSeed = remember(initialPlan?.id) {
        runCatching { LocalDate.parse(initialPlan?.nextCollectionDate ?: LocalDate.now().toString()) }.getOrElse { LocalDate.now() }
    }
    var title by remember(initialPlan?.id) { mutableStateOf(initialPlan?.title ?: "") }
    var counterparty by remember(initialPlan?.id) { mutableStateOf(initialPlan?.counterparty ?: "") }
    var baseAmount by remember(initialPlan?.id) { mutableStateOf(initialPlan?.baseAmount?.takeIf { it > 0.0 }?.toInt()?.toString() ?: "") }
    var nextDate by remember(initialPlan?.id) { mutableStateOf(initialPlan?.nextCollectionDate ?: LocalDate.now().toString()) }
    var note by remember(initialPlan?.id) { mutableStateOf(initialPlan?.note ?: "") }
    var autoReminderEnabled by remember(initialPlan?.id) { mutableStateOf(initialPlan?.autoReminderEnabled ?: true) }
    var carryOverEnabled by remember(initialPlan?.id) { mutableStateOf(initialPlan?.carryOverEnabled ?: true) }
    val saveEnabled = title.isNotBlank() && counterparty.isNotBlank() && (baseAmount.toDoubleOrNull() ?: 0.0) > 0.0 && nextDate.isNotBlank()
    val dateDialog = remember(dateSeed.year, dateSeed.monthValue, dateSeed.dayOfMonth) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                nextDate = LocalDate.of(year, month + 1, dayOfMonth).toString()
            },
            dateSeed.year,
            dateSeed.monthValue - 1,
            dateSeed.dayOfMonth,
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title.trim(),
                        counterparty.trim(),
                        baseAmount.toDoubleOrNull() ?: 0.0,
                        nextDate.trim(),
                        "MONTHLY",
                        note.trim(),
                        autoReminderEnabled,
                        carryOverEnabled,
                    )
                },
                enabled = saveEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
            ) {
                Text(if (initialPlan == null) "Save Plan" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AccentPurple)
            }
        },
        title = {
            Text(if (initialPlan == null) "Collection Planner" else "Edit Collection Plan", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Plan title") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    OutlinedTextField(value = counterparty, onValueChange = { counterparty = it }, label = { Text("Collect from") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    OutlinedTextField(value = baseAmount, onValueChange = { baseAmount = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Monthly collect amount") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    FlatActionLine(
                        title = "Next collection date • $nextDate",
                        accent = AccentBlue,
                        icon = Icons.Rounded.CalendarMonth,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { dateDialog.show() },
                    )
                }
                item {
                    FlatActionLine(
                        title = if (autoReminderEnabled) "Auto reminder on" else "Auto reminder off",
                        accent = if (autoReminderEnabled) AccentGreen else AccentPurple,
                        icon = if (autoReminderEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { autoReminderEnabled = !autoReminderEnabled },
                    )
                }
                item {
                    FlatActionLine(
                        title = if (carryOverEnabled) "Unpaid amount carries to next month" else "Only current month amount stays active",
                        accent = AccentOrange,
                        icon = Icons.Rounded.History,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { carryOverEnabled = !carryOverEnabled },
                    )
                }
                item {
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                if (initialPlan != null && onDelete != null) {
                    item { Text("Plan actions", color = TextSecondary, fontWeight = FontWeight.SemiBold) }
                    item {
                        FlatActionLine(
                            title = "Delete this plan",
                            accent = AccentOrange,
                            icon = Icons.Rounded.Delete,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onDelete,
                        )
                    }
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun CollectionPaymentDialog(
    plan: PocketNativeCollectionPlan,
    onDismiss: () -> Unit,
    onSave: (amount: Double, note: String) -> Unit,
) {
    var amountInput by remember(plan.id) { mutableStateOf("") }
    var noteInput by remember(plan.id) { mutableStateOf("") }
    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val overpay = (parsedAmount - plan.currentDueAmount).coerceAtLeast(0.0)
    val saveEnabled = parsedAmount > 0.0
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(parsedAmount, noteInput) },
                enabled = saveEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.White),
            ) {
                Text("Save Collection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AccentPurple)
            }
        },
        title = {
            Text("Collect Amount", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${plan.title} • ${plan.counterparty}", color = TextSecondary)
                InstallmentDetailLine("Current due", nativeBudgetMoney(plan.currentDueAmount, "৳"))
                InstallmentDetailLine("Monthly target", nativeBudgetMoney(plan.baseAmount, "৳"))
                InstallmentDetailLine("Due date", plan.nextCollectionDate)
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Collected amount") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = budgetDialogFieldColors(),
                )
                if (overpay > 0.0) {
                    Text(
                        "Extra ${nativeBudgetMoney(overpay, "৳")} will reduce the next monthly due automatically.",
                        color = AccentGreen,
                        fontSize = 12.sp,
                    )
                } else {
                    Text(
                        "If this is less than due, the remaining amount will stay active. If month changes, it will carry over to the next due cycle.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                }
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Collection note") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = budgetDialogFieldColors(),
                )
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun CollectionHistoryDialog(
    plan: PocketNativeCollectionPlan,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPurple)
            }
        },
        title = {
            Text("Collection History", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("${plan.title} • ${plan.counterparty}", color = TextSecondary)
                }
                if (plan.paymentHistory.isEmpty()) {
                    item {
                        Text(
                            "No collection history yet.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                } else {
                    items(plan.paymentHistory.sortedByDescending { it.collectedDate }, key = { it.id }) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceCardSoft.copy(alpha = 0.9f))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    nativeBudgetMoney(entry.amount, "৳"),
                                    color = AccentGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                )
                                Text(
                                    entry.collectedDate,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                )
                            }
                            Text(
                                "Covered due ${entry.coveredDueDate}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                            )
                            if (entry.note.isNotBlank()) {
                                Text(entry.note, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun InlineHistorySection(
    title: String,
    trailing: String,
    emptyText: String,
    hasItems: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardSoft.copy(alpha = 0.9f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (trailing.isNotBlank()) {
                    Text(trailing, color = TextSecondary, fontSize = 12.sp)
                }
            }
            if (hasItems) {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    content()
                }
            } else {
                Text(emptyText, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun InlineHistoryRow(
    title: String,
    subtitle: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Text(
                value,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.06f)),
        )
    }
}

@Composable
private fun LoanPaymentHistoryDialog(
    reminder: PocketNativeLoanDueReminder,
    onDismiss: () -> Unit,
) {
    val history = reminder.paymentHistory.orEmpty().sortedByDescending { it.paidDate }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPurple)
            }
        },
        title = {
            Text(
                if (reminder.kind == "lent") "Collection History" else "Payment History",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(
                        if (reminder.kind == "lent") "Get back from ${reminder.counterparty}" else "Pay back to ${reminder.counterparty}",
                        color = TextSecondary,
                    )
                }
                if (history.isEmpty()) {
                    item {
                        Text("No payment history yet.", color = TextSecondary, fontSize = 12.sp)
                    }
                } else {
                    items(history, key = { it.id }) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceCardSoft.copy(alpha = 0.9f))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(nativeBudgetMoney(entry.amount, "৳"), color = if (reminder.kind == "lent") AccentGreen else AccentOrange, fontWeight = FontWeight.Bold)
                                Text(entry.paidDate, color = TextSecondary, fontSize = 12.sp)
                            }
                            if (entry.note.isNotBlank()) {
                                Text(entry.note, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun BudgetHistoryDialog(
    item: PocketNativeBudgetItem,
    currencySymbol: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPurple)
            }
        },
        title = {
            Text("Budget History", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(item.categoryLabel, color = TextSecondary)
                InstallmentDetailLine("Budget", nativeBudgetMoney(item.amount, currencySymbol))
                InstallmentDetailLine("Spent", nativeBudgetMoney(item.spent, currencySymbol))
                InstallmentDetailLine("Remaining", nativeBudgetMoney(item.remaining, currencySymbol))
                InstallmentDetailLine("Progress", "${item.progress.toInt()}%")
                InstallmentDetailLine("Due day", "Day ${item.dueDayOfMonth}")
                InstallmentDetailLine("Period", item.periodType.ifBlank { "MONTHLY" })
                if (item.note.isNotBlank()) {
                    Text(item.note, color = TextSecondary, fontSize = 12.sp)
                } else {
                    Text("No extra budget note saved.", color = TextSecondary, fontSize = 12.sp)
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun MonthlyReminderHistoryDialog(
    reminder: PocketNativeMonthlyReminder,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPurple)
            }
        },
        title = {
            Text("Reminder Details", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(reminder.title, color = TextSecondary)
                InstallmentDetailLine("Type", when (reminder.kind.lowercase()) {
                    "rent" -> "Rent"
                    "school" -> "School Fee"
                    "internet" -> "Internet"
                    "subscription" -> "Subscription"
                    "bill" -> "Bill"
                    "budget" -> "Budget"
                    else -> "General"
                })
                InstallmentDetailLine("Monthly due", "Day ${reminder.dayOfMonth}")
                if (reminder.amount > 0.0) {
                    InstallmentDetailLine("Amount", nativeBudgetMoney(reminder.amount, "৳"))
                }
                InstallmentDetailLine("Alert flow", "10d • 5d • 3d • 1d • same day")
                if (reminder.note.isNotBlank()) {
                    Text(reminder.note, color = TextSecondary, fontSize = 12.sp)
                } else {
                    Text("No extra reminder note saved.", color = TextSecondary, fontSize = 12.sp)
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

private val installmentCategoryOptions = listOf(
    "mobile" to "Mobile",
    "laptop" to "Laptop",
    "car" to "Car",
    "bike" to "Bike",
    "house" to "House",
    "land" to "Land",
    "education" to "Education",
    "tv" to "TV",
    "fridge" to "Fridge",
    "furniture" to "Furniture",
    "appliance" to "Appliance",
    "business" to "Business",
    "medical" to "Medical",
    "wedding" to "Wedding",
    "renovation" to "Renovation",
    "other" to "Other",
)

private fun installmentTypeKey(label: String): String =
    label.trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .ifBlank { "custom_installment_type" }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InstallmentPlanRow(
    plan: PocketNativeInstallmentPlan,
    highlighted: Boolean = false,
    onOpenPlan: () -> Unit,
    onPay: () -> Unit,
    onSomePay: () -> Unit,
    onHistory: () -> Unit,
) {
    val overdueAccent = Color(0xFFFF6B86)
    val isOverdue = pocketInstallmentIsOverdue(plan)
    val accent = when {
        plan.status == "completed" -> AccentGreen
        isOverdue -> overdueAccent
        else -> AccentOrange
    }
    val displayTitle = pocketNormalizeInstallmentTitle(plan.title, plan.categoryLabel)
    val completedInstallments = pocketInstallmentCompletedCount(plan)
    val remainingInstallments = (plan.totalInstallments - completedInstallments).coerceAtLeast(0)
    val paidAmount = pocketInstallmentPaidAmount(plan)
    val remainingAmount = (plan.loanAmount - paidAmount).coerceAtLeast(0.0)
    val progress = if (plan.loanAmount <= 0.0) 0f else (paidAmount / plan.loanAmount).toFloat().coerceIn(0f, 1f)
    val historyCount = plan.paymentHistory.orEmpty().size
    val currentDuePaid = if (plan.installmentAmount <= 0.0) 0.0 else (paidAmount % plan.installmentAmount)
    val currentDueRemaining = if (plan.status == "completed") 0.0 else {
        if (currentDuePaid == 0.0) {
            plan.installmentAmount
        } else {
            (plan.installmentAmount - currentDuePaid).coerceAtLeast(0.0)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCardSoft.copy(alpha = 0.92f))
            .then(pocketSectionHighlight(highlighted, AccentOrange))
            .clickable(onClick = onOpenPlan)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(displayTitle, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("${plan.categoryLabel} • ${plan.lender.ifBlank { "Installment" }}", color = TextSecondary)
                Text(
                    if (plan.status == "completed") {
                        "Completed on schedule • ${completedInstallments}/${plan.totalInstallments} paid"
                    } else {
                        "Next ${plan.nextDueDate} • ${nativeBudgetMoney(plan.installmentAmount, "৳")} • ${completedInstallments}/${plan.totalInstallments} paid"
                    },
                    color = if (isOverdue) overdueAccent else TextSecondary,
                    fontSize = 12.sp,
                )
                Text(
                    if (plan.status == "completed") {
                        "Paid off • ${nativeBudgetMoney(plan.loanAmount, "৳")} completed"
                    } else {
                        "Remaining ${nativeBudgetMoney(remainingAmount, "৳")} • $remainingInstallments installments left"
                    },
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
                if (plan.status != "completed") {
                    Text(
                        "This due still needs ${nativeBudgetMoney(currentDueRemaining, "৳")}",
                        color = AccentBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (isOverdue && plan.status != "completed") {
                    Text(
                        "Overdue. Pay or update the date so this plan stays on track.",
                        color = overdueAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = Color(0xFF27314A),
                )
                if (plan.note.isNotBlank()) {
                    Text(plan.note, color = TextSecondary, fontSize = 12.sp)
                }
                if (historyCount > 0) {
                    Text(
                        "$historyCount payment records saved",
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                }
            }
            Text(
                if (plan.status == "completed") "Done" else plan.frequency.lowercase().replaceFirstChar { it.uppercase() },
                color = accent,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            InstallmentQuickBoxAction(
                label = "Pay",
                icon = Icons.Rounded.Check,
                tint = AccentGreen,
                onClick = onPay,
            )
            InstallmentQuickBoxAction(
                label = "Some Pay",
                icon = Icons.Rounded.MonetizationOn,
                tint = AccentBlue,
                onClick = onSomePay,
            )
            InstallmentQuickBoxAction(
                label = "History",
                icon = Icons.Rounded.History,
                tint = AccentPurple,
                onClick = onHistory,
            )
        }
    }
}

@Composable
private fun InstallmentQuickBoxAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 80.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(tint.copy(alpha = 0.85f), RoundedCornerShape(999.dp)),
        )
    }
}

@Composable
private fun InstallmentPlanDetailsDialog(
    plan: PocketNativeInstallmentPlan,
    onDismiss: () -> Unit,
) {
    val paidAmount = pocketInstallmentPaidAmount(plan)
    val completedInstallments = pocketInstallmentCompletedCount(plan)
    val remainingAmount = (plan.loanAmount - paidAmount).coerceAtLeast(0.0)
    val remainingInstallments = (plan.totalInstallments - completedInstallments).coerceAtLeast(0)
    val title = pocketNormalizeInstallmentTitle(plan.title, plan.categoryLabel)
    val frequencyLabel = when (plan.frequency.trim().lowercase(Locale.ROOT)) {
        "yearly" -> "Yearly"
        else -> "Monthly"
    }
    val reminderLabel = if (pocketInstallmentReminderEnabled(plan)) "On" else "Off"
    val statusLabel = if (plan.status == "completed") "Completed" else if (pocketInstallmentIsOverdue(plan)) "Overdue" else "Active"
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPurple, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    "${plan.categoryLabel} • ${plan.lender.ifBlank { "Installment" }}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InstallmentDetailLine("Type", plan.categoryLabel)
                InstallmentDetailLine("Pay cycle", frequencyLabel)
                InstallmentDetailLine("Status", statusLabel)
                InstallmentDetailLine("First due", plan.firstDueDate.ifBlank { "-" })
                InstallmentDetailLine("Next due", plan.nextDueDate.ifBlank { "-" })
                InstallmentDetailLine("Total amount", nativeBudgetMoney(plan.loanAmount, "৳"))
                InstallmentDetailLine("Installment amount", nativeBudgetMoney(plan.installmentAmount, "৳"))
                InstallmentDetailLine("Paid so far", nativeBudgetMoney(paidAmount, "৳"))
                InstallmentDetailLine("Remaining", nativeBudgetMoney(remainingAmount, "৳"))
                InstallmentDetailLine("Installments", "$completedInstallments/${plan.totalInstallments} paid")
                InstallmentDetailLine("Installments left", remainingInstallments.toString())
                InstallmentDetailLine("Auto reminder", reminderLabel)
                if (plan.note.isNotBlank()) {
                    InstallmentDetailLine("Note", plan.note)
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun InstallmentDetailLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.weight(1.2f),
        )
    }
}

@Composable
private fun FlatInlineAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    val horizontalPadding = if (compact) 4.dp else 6.dp
    val verticalPadding = if (compact) 4.dp else 6.dp
    val minWidth = if (compact) 0.dp else 78.dp
    val iconSize = if (compact) 16.dp else 24.dp
    val fontSize = if (compact) 11.sp else 12.sp
    val gap = if (compact) 4.dp else 6.dp
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .widthIn(min = minWidth),
        verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
            Text(
                text = label,
                color = tint,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(tint.copy(alpha = 0.78f), RoundedCornerShape(99.dp)),
        )
    }
}

@Composable
private fun InstallmentPaymentHistoryDialog(
    plan: PocketNativeInstallmentPlan,
    onDismiss: () -> Unit,
) {
    val history = plan.paymentHistory.orEmpty().sortedByDescending { entry ->
        runCatching { LocalDate.parse(entry.paidDate) }.getOrElse { LocalDate.MIN }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPurple, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${pocketNormalizeInstallmentTitle(plan.title, plan.categoryLabel)} History",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${pocketInstallmentCompletedCount(plan)}/${plan.totalInstallments} paid • ${nativeBudgetMoney(pocketInstallmentPaidAmount(plan), "৳")}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        },
        text = {
            if (history.isEmpty()) {
                Text(
                    "No payment has been marked yet. Once you tap Mark Paid, each installment will be saved here.",
                    color = TextSecondary,
                    lineHeight = 20.sp,
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(280.dp),
                ) {
                    items(history, key = { it.id }) { entry ->
                        InstallmentPaymentHistoryRow(entry = entry)
                    }
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun InstallmentPartialPaymentDialog(
    plan: PocketNativeInstallmentPlan,
    onDismiss: () -> Unit,
    onSave: (amount: Double, note: String) -> Unit,
) {
    var amountInput by remember(plan.id) { mutableStateOf("") }
    var noteInput by remember(plan.id) { mutableStateOf("") }
    val paidAmount = pocketInstallmentPaidAmount(plan)
    val remainingAmount = (plan.loanAmount - paidAmount).coerceAtLeast(0.0)
    val duePaid = if (plan.installmentAmount <= 0.0) 0.0 else paidAmount % plan.installmentAmount
    val dueRemaining = if (duePaid == 0.0) plan.installmentAmount else (plan.installmentAmount - duePaid).coerceAtLeast(0.0)
    val parsedAmount = amountInput.trim().toDoubleOrNull() ?: 0.0
    val saveEnabled = parsedAmount > 0.0 && parsedAmount <= remainingAmount
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(parsedAmount, noteInput) },
                enabled = saveEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AccentPurple)
            }
        },
        title = {
            Text(
                "Partial Payment",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${pocketNormalizeInstallmentTitle(plan.title, plan.categoryLabel)} • ${plan.lender.ifBlank { "Installment" }}",
                    color = TextSecondary,
                )
                Text(
                    "Current due needs ${nativeBudgetMoney(dueRemaining, "৳")} • Total remaining ${nativeBudgetMoney(remainingAmount, "৳")}",
                    color = TextSecondary,
                    lineHeight = 20.sp,
                )
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { value -> amountInput = value.filter { it.isDigit() || it == '.' } },
                    label = { Text("Payment amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextSecondary,
                    ),
                )
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Payment note") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextSecondary,
                    ),
                )
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun InstallmentPaymentHistoryRow(
    entry: PocketNativeInstallmentPayment,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Installment ${entry.installmentNumber}",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                nativeBudgetMoney(entry.amount, "৳"),
                color = AccentGreen,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            "Paid ${entry.paidDate} • Covered due ${entry.coveredDueDate}",
            color = TextSecondary,
            fontSize = 12.sp,
        )
        if (entry.note.isNotBlank()) {
            Text(entry.note, color = TextSecondary, fontSize = 12.sp)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
    }
}

@Composable
private fun InstallmentInsightsDialog(
    plans: List<PocketNativeInstallmentPlan>,
    onDismiss: () -> Unit,
) {
    val activePlans = plans.filter { it.status == "active" }
    val totalRemainingInstallments = activePlans.sumOf { (it.totalInstallments - it.paidInstallments).coerceAtLeast(0) }
    val totalRemainingAmount = activePlans.sumOf { (it.loanAmount - pocketInstallmentPaidAmount(it)).coerceAtLeast(0.0) }
    val totalPaidAmount = activePlans.sumOf { pocketInstallmentPaidAmount(it) }
    val overduePlans = activePlans.count { pocketInstallmentIsOverdue(it) }
    val upcomingPlan = activePlans.minByOrNull { runCatching { LocalDate.parse(it.nextDueDate) }.getOrElse { LocalDate.MAX } }
    val farthestPlan = activePlans.maxByOrNull { runCatching { LocalDate.parse(it.nextDueDate) }.getOrElse { LocalDate.MIN } }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPurple, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Installment Insights", color = TextPrimary, fontWeight = FontWeight.Bold)
                PocketNativeProBadge()
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "See the bigger picture before each payment: remaining money, installments left, and your next payoff date.",
                    color = TextSecondary,
                    lineHeight = 20.sp,
                )
                InsightLine("Active plans", activePlans.size.toString())
                InsightLine("Paid so far", nativeBudgetMoney(totalPaidAmount, "৳"))
                InsightLine("Still remaining", nativeBudgetMoney(totalRemainingAmount, "৳"))
                InsightLine("Installments left", totalRemainingInstallments.toString())
                InsightLine("Overdue plans", overduePlans.toString())
                upcomingPlan?.let {
                    InsightLine("Next to pay", "${pocketNormalizeInstallmentTitle(it.title, it.categoryLabel)} • ${it.nextDueDate}")
                }
                farthestPlan?.let {
                    InsightLine("Last expected payoff", it.nextDueDate)
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

private fun pocketInstallmentIsOverdue(plan: PocketNativeInstallmentPlan): Boolean {
    if (plan.status == "completed") return false
    val dueDate = runCatching { LocalDate.parse(plan.nextDueDate) }.getOrNull() ?: return false
    return dueDate.isBefore(LocalDate.now())
}

@Composable
private fun InsightLine(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextSecondary)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BudgetSummaryItem(
    title: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = TextPrimary.copy(alpha = 0.78f), fontWeight = FontWeight.SemiBold)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Composable
private fun BudgetFilterStrip(
    showOverdueOnly: Boolean,
    onShowAll: () -> Unit,
    onShowOverdueOnly: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FlatInlineAction(
            label = "All items",
            icon = Icons.Rounded.PieChart,
            tint = if (!showOverdueOnly) AccentBlue else TextSecondary,
            compact = true,
            onClick = onShowAll,
        )
        FlatInlineAction(
            label = "Overdue only",
            icon = Icons.Rounded.NotificationsActive,
            tint = if (showOverdueOnly) AccentOrange else TextSecondary,
            compact = true,
            onClick = onShowOverdueOnly,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BudgetCategoryCard(
    item: PocketNativeBudgetItem,
    currencySymbol: String,
    highlighted: Boolean = false,
    onPay: () -> Unit,
    onSomeExpense: () -> Unit,
    onHistory: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val accent = colorFromHex(item.accentHex)
    val statusAccent = when {
        item.progress >= 100.0 -> Color(0xFFFF6B86)
        item.progress >= 75.0 -> AccentOrange
        else -> AccentGreen
    }
    val statusLabel = when {
        item.progress >= 100.0 -> "Over limit"
        item.progress >= 75.0 -> "Watch"
        else -> "On track"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCardSoft.copy(alpha = 0.92f))
            .then(pocketSectionHighlight(highlighted, accent))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(item.categoryLabel, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Monthly budget", color = TextSecondary, fontSize = 12.sp)
                Text(
                    "${nativeBudgetMoney(item.spent, currencySymbol)} / ${nativeBudgetMoney(item.amount, currencySymbol)}",
                    color = TextSecondary,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(statusAccent.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        statusLabel,
                        color = statusAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
                Text("${item.progress.toInt()}%", color = AccentOrange, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            }
        }
        LinearProgressIndicator(
            progress = { (item.progress.toFloat() / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = accent,
            trackColor = Color(0xFF27314A),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Remaining", color = TextSecondary, fontSize = 11.sp)
                Text(nativeBudgetMoney(item.remaining, currencySymbol), color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Due day", color = TextSecondary, fontSize = 11.sp)
                Text("Day ${item.dueDayOfMonth}", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FlatInlineAction(label = "Expense", icon = Icons.Rounded.Payments, tint = AccentGreen, compact = true, onClick = onPay)
            FlatInlineAction(label = "Some Expense", icon = Icons.Rounded.MonetizationOn, tint = AccentBlue, compact = true, onClick = onSomeExpense)
            FlatInlineAction(label = "History", icon = Icons.Rounded.History, tint = AccentPurple, compact = true, onClick = onHistory)
            FlatInlineAction(label = "Edit", icon = Icons.Rounded.Edit, tint = AccentBlue, compact = true, onClick = onEdit)
            FlatInlineAction(label = "Delete", icon = Icons.Rounded.Delete, tint = AccentOrange, compact = true, onClick = onDelete)
        }
    }
}

@Composable
private fun BudgetPartialExpenseDialog(
    item: PocketNativeBudgetItem,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit,
) {
    var amountText by remember(item.id) {
        mutableStateOf(
            when {
                item.remaining > 0.0 && item.remaining < item.amount -> item.remaining.toString()
                else -> item.amount.toString()
            },
        )
    }
    var noteText by remember(item.id) { mutableStateOf(item.note) }
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsedAmount > 0.0,
                onClick = { onSave(parsedAmount, noteText.trim()) },
            ) {
                Text("Open Expense", color = AccentBlue, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text("Some Expense", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(item.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    "Budget ${nativeBudgetMoney(item.amount, currencySymbol)} • Remaining ${nativeBudgetMoney(item.remaining, currencySymbol)}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Expense amount") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextSecondary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    ),
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Expense note") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextSecondary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    ),
                )
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun FlatActionLine(
    title: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
            )
            Text(title, color = accent, fontWeight = FontWeight.Bold)
            trailingContent?.invoke()
        }
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(accent.copy(alpha = 0.85f), RoundedCornerShape(99.dp)))
    }
}

@Composable
private fun FlatActionTile(
    title: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp)),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.08f),
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.56f)
                        .height(3.dp)
                        .background(accent.copy(alpha = 0.9f), RoundedCornerShape(99.dp)),
                )
            }
            if (trailingContent != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp),
                ) {
                    trailingContent()
                }
            }
        }
    }
}

@Composable
private fun NativeFlatEmptySection(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(body, color = TextSecondary, lineHeight = 20.sp)
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
    }
}

private fun budgetFallbackCategoryOptions(): List<PocketNativeTransactionCategoryOption> = listOf(
    PocketNativeTransactionCategoryOption(key = "food", label = "Food", colorHex = "#7C4DFF", iconClass = "", iconKey = "utensils"),
    PocketNativeTransactionCategoryOption(key = "bills", label = "Bills", colorHex = "#3D8DFF", iconClass = "", iconKey = "receipt"),
    PocketNativeTransactionCategoryOption(key = "transport", label = "Transport", colorHex = "#00BFA5", iconClass = "", iconKey = "car"),
    PocketNativeTransactionCategoryOption(key = "shopping", label = "Shopping", colorHex = "#FF8A65", iconClass = "", iconKey = "bag"),
)

@Composable
private fun BudgetGuideStrip(
    totalBudget: String,
    activeBudgets: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BudgetGuideCard(
                icon = Icons.Rounded.PieChart,
                title = "Total Monthly Budget",
                value = totalBudget,
                accent = AccentBlue,
                modifier = Modifier.weight(1f),
            )
            BudgetGuideCard(
                icon = Icons.Rounded.Sell,
                title = "Active Budgets",
                value = "$activeBudgets",
                accent = AccentOrange,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            "Start with Add Budget, then use Monthly Reminder for rent, internet, school fee, or any repeated bill.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
    }
}

@Composable
private fun BudgetGuideCard(
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCardSoft.copy(alpha = 0.92f))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.44f)
                .height(3.dp)
                .background(accent.copy(alpha = 0.85f), RoundedCornerShape(99.dp)),
        )
    }
}

private fun nativeBudgetMoney(amount: Double, symbol: String): String {
    val clean = if (amount % 1.0 == 0.0) String.format(Locale.US, "%,d", amount.toLong()) else String.format(Locale.US, "%,.2f", amount)
    return "$symbol$clean"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BudgetCreateDialog(
    categories: List<PocketNativeTransactionCategoryOption>,
    categorySaving: Boolean,
    initialBudget: PocketNativeBudgetItem? = null,
    onCreateCategoryRequest: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, Int) -> Unit,
) {
    var name by remember(initialBudget) { mutableStateOf(initialBudget?.name.orEmpty()) }
    var amountText by remember(initialBudget) { mutableStateOf(initialBudget?.amount?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }.orEmpty()) }
    var note by remember(initialBudget) { mutableStateOf(initialBudget?.note.orEmpty()) }
    var dueDayOfMonth by remember(initialBudget) { mutableStateOf(initialBudget?.dueDayOfMonth?.coerceIn(1, 28) ?: 1) }
    var selectedCategoryKey by remember(categories, initialBudget) { mutableStateOf(initialBudget?.categoryKey ?: categories.firstOrNull()?.key ?: "all") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(name, selectedCategoryKey, amountText.toDoubleOrNull() ?: 0.0, note, dueDayOfMonth) },
                enabled = name.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = TextPrimary),
            ) { Text("Save Budget") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Create Budget", color = TextPrimary) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Budget name") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Monthly amount") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    Text("Monthly due day", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = 7) {
                        listOf(1, 5, 10, 15, 20, 25, 28).forEach { day ->
                            Card(
                                modifier = Modifier.clickable { dueDayOfMonth = day },
                                colors = CardDefaults.cardColors(containerColor = if (dueDayOfMonth == day) AccentGreen.copy(alpha = 0.24f) else SurfaceCardSoft),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text("Day $day", color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), fontSize = 12.sp)
                            }
                        }
                    }
                }
                item { Text("Choose category", color = TextSecondary, fontWeight = FontWeight.SemiBold) }
                item {
                    FlatInlineAction(
                        label = if (categorySaving) "Adding..." else "Add Category",
                        icon = Icons.Rounded.Add,
                        tint = AccentOrange,
                        onClick = onCreateCategoryRequest,
                    )
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = 3) {
                        categories.forEach { item ->
                            Card(
                                modifier = Modifier.clickable { selectedCategoryKey = item.key },
                                colors = CardDefaults.cardColors(containerColor = if (selectedCategoryKey == item.key) AccentPurple.copy(alpha = 0.24f) else SurfaceCardSoft),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(item.label, color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), fontSize = 12.sp)
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthlyReminderDialog(
    initialReminder: PocketNativeMonthlyReminder?,
    previewOnly: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Int, String, String, Double) -> Unit,
) {
    val context = LocalContext.current
    var title by remember(initialReminder) { mutableStateOf(initialReminder?.title ?: "") }
    var dayText by remember(initialReminder) { mutableStateOf(initialReminder?.dayOfMonth?.toString() ?: "") }
    var selectedDateText by remember(initialReminder) {
        mutableStateOf(
            initialReminder?.dayOfMonth?.let { budgetMonthlyPreviewDate(it) }.orEmpty()
        )
    }
    var note by remember(initialReminder) { mutableStateOf(initialReminder?.note ?: "") }
    var amountText by remember(initialReminder) {
        mutableStateOf(
            initialReminder?.amount?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }.orEmpty()
        )
    }
    val reminderKinds = listOf("bill" to "Bill", "rent" to "Rent", "school" to "School Fee", "internet" to "Internet", "subscription" to "Subscription", "general" to "General")
    var selectedKind by remember(initialReminder) { mutableStateOf(initialReminder?.kind?.ifBlank { "general" } ?: "general") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(title, dayText.toIntOrNull() ?: 1, note, selectedKind, amountText.toDoubleOrNull() ?: 0.0) },
                enabled = title.isNotBlank() && (dayText.toIntOrNull() ?: 0) in 1..28,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.Black),
            ) { Text("Save Reminder") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Monthly Reminder", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (previewOnly) {
                    Text(
                        "Monthly bills and reminders are available for free.",
                        color = AccentPurple,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp,
                    )
                }
                Text(
                    "Pocket Pro will remind you 10 days, 5 days, 3 days, 1 day, and on the due date.",
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    fontSize = 12.sp,
                )
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                Text("Reminder type", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3,
                ) {
                    reminderKinds.forEach { option ->
                        Card(
                            modifier = Modifier.clickable { selectedKind = option.first },
                            colors = CardDefaults.cardColors(containerColor = if (selectedKind == option.first) AccentBlue.copy(alpha = 0.22f) else SurfaceCardSoft),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                option.second,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = budgetDialogFieldColors(),
                )
                FlatActionLine(
                    title = if (selectedDateText.isBlank()) "Pick monthly due date" else "Due date: $selectedDateText",
                    accent = AccentBlue,
                    icon = Icons.Rounded.CalendarMonth,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val current = runCatching { LocalDate.parse(selectedDateText) }.getOrNull() ?: LocalDate.now().withDayOfMonth((dayText.toIntOrNull() ?: LocalDate.now().dayOfMonth).coerceIn(1, 28))
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val safeDay = day.coerceIn(1, 28)
                                selectedDateText = "%04d-%02d-%02d".format(year, month + 1, safeDay)
                                dayText = safeDay.toString()
                            },
                            current.year,
                            current.monthValue - 1,
                            current.dayOfMonth,
                        ).show()
                    },
                )
                Text(
                    if (dayText.isBlank()) "Select a date. Pocket Pro will repeat this every month on that date." else "Repeats every month on day $dayText.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

private fun budgetMonthlyPreviewDate(dayOfMonth: Int): String {
    val safeDay = dayOfMonth.coerceIn(1, 28)
    val today = LocalDate.now()
    val candidate = today.withDayOfMonth(safeDay)
    return if (!candidate.isBefore(today)) candidate.toString() else today.plusMonths(1).withDayOfMonth(safeDay).toString()
}

@Composable
private fun QuickBudgetCategoryDialog(
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Category", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "If your category is not here, create one your own way. You can refine icon and color later.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Category name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = budgetDialogFieldColors(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(label.trim()) },
                enabled = label.isNotBlank() && !saving,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = TextPrimary),
            ) {
                Text(if (saving) "Adding..." else "Add Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InstallmentPlanDialog(
    initialPlan: PocketNativeInstallmentPlan?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Double, Double, String, String, Int, Boolean, String) -> Unit,
    onMarkPaid: (() -> Unit)? = null,
    onPartialPayment: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val initialCustomType = initialPlan?.let { plan ->
        val exists = installmentCategoryOptions.any { it.first.equals(plan.categoryKey, ignoreCase = true) }
        if (!exists && plan.categoryLabel.isNotBlank()) {
            plan.categoryKey to plan.categoryLabel
        } else {
            null
        }
    }
    var customTypeOptions by remember(initialPlan) {
        mutableStateOf(initialCustomType?.let { listOf(it) } ?: emptyList())
    }
    var title by remember(initialPlan) { mutableStateOf(initialPlan?.title ?: "") }
    var lender by remember(initialPlan) { mutableStateOf(initialPlan?.lender ?: "") }
    var categoryKey by remember(initialPlan) { mutableStateOf(initialPlan?.categoryKey ?: installmentCategoryOptions.first().first) }
    var categoryLabel by remember(initialPlan) { mutableStateOf(initialPlan?.categoryLabel ?: installmentCategoryOptions.first().second) }
    var showLoanTypeOptions by remember(initialPlan) { mutableStateOf(false) }
    var showCreateLoanTypeDialog by remember { mutableStateOf(false) }
    var loanAmountText by remember(initialPlan) { mutableStateOf(initialPlan?.loanAmount?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var installmentAmountText by remember(initialPlan) { mutableStateOf(initialPlan?.installmentAmount?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var frequency by remember(initialPlan) { mutableStateOf(initialPlan?.frequency ?: "MONTHLY") }
    var firstDueDate by remember(initialPlan) { mutableStateOf(initialPlan?.nextDueDate ?: "") }
    var totalInstallmentsText by remember(initialPlan) { mutableStateOf(initialPlan?.totalInstallments?.takeIf { it > 0 }?.toString() ?: "") }
    var autoReminderEnabled by remember(initialPlan) { mutableStateOf(initialPlan?.autoReminderEnabled ?: true) }
    var note by remember(initialPlan) { mutableStateOf(initialPlan?.note ?: "") }
    val availableTypeOptions = (installmentCategoryOptions + customTypeOptions).distinctBy { it.first.lowercase() }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        lender,
                        categoryKey,
                        categoryLabel,
                        loanAmountText.toDoubleOrNull() ?: 0.0,
                        installmentAmountText.toDoubleOrNull() ?: 0.0,
                        frequency,
                        firstDueDate,
                        totalInstallmentsText.toIntOrNull() ?: 0,
                        autoReminderEnabled,
                        note,
                    )
                },
                enabled = title.isNotBlank() &&
                    firstDueDate.isNotBlank() &&
                    (loanAmountText.toDoubleOrNull() ?: 0.0) > 0 &&
                    (installmentAmountText.toDoubleOrNull() ?: 0.0) > 0 &&
                    (totalInstallmentsText.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
            ) { Text("Save Plan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (initialPlan == null) "Installment / Loan Plan" else "Edit Installment Plan", color = TextPrimary) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Plan name") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    OutlinedTextField(value = lender, onValueChange = { lender = it }, label = { Text("Bank / Person / Shop") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    FlatActionLine(
                        title = "Loan type: $categoryLabel",
                        accent = AccentOrange,
                        icon = Icons.Rounded.Edit,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showLoanTypeOptions = !showLoanTypeOptions },
                    )
                }
                if (showLoanTypeOptions) {
                    item { Text("Choose loan type", color = TextSecondary, fontWeight = FontWeight.SemiBold) }
                    item {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = 3) {
                            availableTypeOptions.forEach { option ->
                                Card(
                                    modifier = Modifier.clickable {
                                        categoryKey = option.first
                                        categoryLabel = option.second
                                        showLoanTypeOptions = false
                                    },
                                    colors = CardDefaults.cardColors(containerColor = if (categoryKey == option.first) AccentOrange.copy(alpha = 0.24f) else SurfaceCardSoft),
                                    shape = RoundedCornerShape(14.dp),
                                ) {
                                    Text(option.second, color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    item {
                        FlatActionLine(
                            title = "Create new loan type",
                            accent = AccentBlue,
                            icon = Icons.Rounded.Add,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showCreateLoanTypeDialog = true },
                        )
                    }
                }
                item {
                    OutlinedTextField(value = loanAmountText, onValueChange = { loanAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Total loan amount") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    OutlinedTextField(value = installmentAmountText, onValueChange = { installmentAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Installment amount") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    OutlinedTextField(value = totalInstallmentsText, onValueChange = { totalInstallmentsText = it.filter { ch -> ch.isDigit() } }, label = { Text("Total installments") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            modifier = Modifier.weight(1f).clickable { frequency = "MONTHLY" },
                            colors = CardDefaults.cardColors(containerColor = if (frequency == "MONTHLY") AccentPurple.copy(alpha = 0.24f) else SurfaceCardSoft),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Monthly", color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp))
                        }
                        Card(
                            modifier = Modifier.weight(1f).clickable { frequency = "YEARLY" },
                            colors = CardDefaults.cardColors(containerColor = if (frequency == "YEARLY") AccentPurple.copy(alpha = 0.24f) else SurfaceCardSoft),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Yearly", color = TextPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp))
                        }
                    }
                }
                item {
                    FlatActionLine(
                        title = if (firstDueDate.isBlank()) "Set first pay date" else "First pay date: $firstDueDate",
                        accent = AccentBlue,
                        icon = Icons.Rounded.CalendarMonth,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, pickedYear, pickedMonth, pickedDay ->
                                    firstDueDate = "%04d-%02d-%02d".format(pickedYear, pickedMonth + 1, pickedDay)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH),
                            ).show()
                        },
                    )
                }
                item {
                    FlatActionLine(
                        title = if (autoReminderEnabled) "Auto reminder: On" else "Auto reminder: Off",
                        accent = if (autoReminderEnabled) AccentGreen else AccentPurple,
                        icon = if (autoReminderEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { autoReminderEnabled = !autoReminderEnabled },
                    )
                }
                if (initialPlan != null) {
                    item { Text("Plan actions", color = TextSecondary, fontWeight = FontWeight.SemiBold) }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (onMarkPaid != null && initialPlan.status != "completed") {
                                FlatInlineAction(
                                    label = "Paid",
                                    icon = Icons.Rounded.Check,
                                    tint = AccentGreen,
                                    compact = true,
                                    onClick = onMarkPaid,
                                )
                            }
                            if (onPartialPayment != null && initialPlan.status != "completed") {
                                FlatInlineAction(
                                    label = "Part",
                                    icon = Icons.Rounded.MonetizationOn,
                                    tint = AccentBlue,
                                    compact = true,
                                    onClick = onPartialPayment,
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), colors = budgetDialogFieldColors())
                }
                if (initialPlan != null && onDelete != null) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    item {
                        FlatActionLine(
                            title = "Delete this plan",
                            accent = AccentOrange,
                            icon = Icons.Rounded.Delete,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onDelete,
                        )
                    }
                }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
    if (showCreateLoanTypeDialog) {
        QuickInstallmentTypeDialog(
            onDismiss = { showCreateLoanTypeDialog = false },
            onSave = { label ->
                val key = installmentTypeKey(label)
                val option = key to label.trim()
                customTypeOptions = (customTypeOptions + option).distinctBy { it.first.lowercase() }
                categoryKey = option.first
                categoryLabel = option.second
                showCreateLoanTypeDialog = false
                showLoanTypeOptions = false
            },
        )
    }
}

@Composable
private fun QuickInstallmentTypeDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Loan Type", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "If your loan type is not in the list, create one and use it right away.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Loan type name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = budgetDialogFieldColors(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(label.trim()) },
                enabled = label.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = TextPrimary),
            ) {
                Text("Add Type")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun budgetDialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentPurple,
    unfocusedBorderColor = SurfaceCardSoft,
    focusedContainerColor = SurfaceCard,
    unfocusedContainerColor = SurfaceCard,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
)
