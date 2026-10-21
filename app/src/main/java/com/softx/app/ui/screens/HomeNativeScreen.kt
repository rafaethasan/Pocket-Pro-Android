package com.softx.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.softx.app.PocketReminderScheduler
import com.softx.app.data.PocketNativeAnalyticsCategory
import com.softx.app.data.PocketNativeLoanDueReminder
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeDashboardResponse
import com.softx.app.data.PocketNativeRecentActivity
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.data.pocketLoanReminderOriginalAmount
import com.softx.app.data.pocketLoanReminderRemainingAmount
import com.softx.app.data.pocketLoanReminderSettledAmount
import com.softx.app.data.pocketNormalizeInstallmentTitle
import com.softx.app.ui.screens.pocketFeatureAssetName
import com.softx.app.ui.screens.pocketFeatureIcon
import com.softx.app.ui.theme.AccentBlue
import com.softx.app.ui.theme.AccentGreen
import com.softx.app.ui.theme.AccentOrange
import com.softx.app.ui.theme.AccentPink
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

private val HomeElectricBlue = Color(0xFF36B7F5)
private val HomeFooterBlue = Color(0xFF3774FF)
private val HomeFooterBlueDeep = Color(0xFF2454D6)

@Composable
fun HomeNativeScreen(
    repository: PocketNativeRepository,
    signedInUser: PocketNativeUser?,
    onOpenAccounts: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenBudgetTarget: (String, String) -> Unit,
    onOpenGoals: () -> Unit,
    onOpenAdd: () -> Unit,
    onOpenLoanShortcut: (String, String, String, Double, Double, String, String) -> Unit,
    onOpenRecords: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenMore: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRecentActivity: (PocketNativeRecentActivity) -> Unit,
) {
    val context = LocalContext.current
    val preferencesStore = remember { PocketNativePreferencesStore(context) }
    var loading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }
    var isBalanceVisible by remember { mutableStateOf(false) }
    var dashboardResponse by remember { mutableStateOf<PocketNativeDashboardResponse?>(null) }
    var liveLoanDueReminders by remember { mutableStateOf(preferencesStore.loanDueReminders()) }
    var selectedLoanReminderAction by remember { mutableStateOf<PocketNativeLoanDueReminder?>(null) }
    var editingLoanReminder by remember { mutableStateOf<PocketNativeLoanDueReminder?>(null) }
    var editingLoanReminderFull by remember { mutableStateOf<PocketNativeLoanDueReminder?>(null) }
    var snoozingLoanReminder by remember { mutableStateOf<PocketNativeLoanDueReminder?>(null) }
    var pendingDoneLoanReminder by remember { mutableStateOf<PocketNativeLoanDueReminder?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        errorText = ""
        val result = repository.dashboard()
        loading = false
        if (result.isSuccess) {
            dashboardResponse = result.getOrNull()
            liveLoanDueReminders = preferencesStore.loanDueReminders()
        } else {
            errorText = result.exceptionOrNull()?.message ?: "Dashboard could not be loaded."
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 48.dp), color = AccentGreen)
        }
        return
    }

    if (errorText.isNotBlank() || dashboardResponse?.dashboard == null) {
        LazyColumn(modifier = Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Spacer(modifier = Modifier.height(18.dp)) }
            item { HomeErrorCard(errorText.ifBlank { "Dashboard data not available yet." }) }
        }
        return
    }

    val dashboard = dashboardResponse!!.dashboard!!
    val summary = dashboard.summary
    val user = dashboardResponse!!.user ?: signedInUser
    val currencySymbol = dashboard.currencySymbol
    val analyticsCategories = dashboard.analytics.categories.take(4)
    val recentItems = dashboard.recentActivity.ifEmpty { sampleRecentActivity() }
    val todayNet = summary.todayIncome - summary.todayExpense
    val pendingLoanReminders = remember(liveLoanDueReminders) { liveLoanDueReminders.filter { it.status == "pending" }.take(1) }
    val allInstallmentReminders = remember(dashboardResponse) { preferencesStore.installmentPlans().filter { it.status == "active" } }
    val installmentReminders = remember(allInstallmentReminders) { allInstallmentReminders.take(2) }
    val allMonthlyReminders = remember(dashboardResponse) { preferencesStore.monthlyReminders() }
    val monthlyReminders = remember(allMonthlyReminders) { allMonthlyReminders.take(1) }
    val currentMonth = LocalDate.now()
    val currentYearMonth = remember(currentMonth) { java.time.YearMonth.from(currentMonth) }
    val thisMonthInstallments = remember(allInstallmentReminders, currentYearMonth) {
        allInstallmentReminders.filter { plan ->
            runCatching { LocalDate.parse(plan.nextDueDate) }.getOrNull()?.let { java.time.YearMonth.from(it) == currentYearMonth } == true
        }
    }
    val thisMonthLoanReminders = remember(liveLoanDueReminders, currentYearMonth) {
        liveLoanDueReminders.filter { reminder ->
            reminder.status == "pending" &&
                runCatching { LocalDate.parse(reminder.dueDate) }.getOrNull()?.let { java.time.YearMonth.from(it) == currentYearMonth } == true
        }
    }
    val thisMonthMonthlyReminders = remember(allMonthlyReminders, currentYearMonth) {
        allMonthlyReminders.filter { reminder ->
            reminder.dayOfMonth in 1..currentYearMonth.lengthOfMonth()
        }
    }
    val thisMonthInstallmentTotal = remember(thisMonthInstallments) { thisMonthInstallments.sumOf { it.installmentAmount } }
    val thisMonthLoanTotal = remember(thisMonthLoanReminders) { thisMonthLoanReminders.sumOf { pocketLoanReminderRemainingAmount(it) } }
    val notificationCount = remember(liveLoanDueReminders, allInstallmentReminders, allMonthlyReminders) {
        liveLoanDueReminders.count { it.status == "pending" } +
            allInstallmentReminders.count { it.status == "active" } +
            allMonthlyReminders.size
    }
    val nextDueLabel = remember(thisMonthInstallments, thisMonthLoanReminders, thisMonthMonthlyReminders, currentMonth) {
        val dueDates = buildList {
            addAll(thisMonthInstallments.mapNotNull { runCatching { LocalDate.parse(it.nextDueDate) }.getOrNull() })
            addAll(thisMonthLoanReminders.mapNotNull { runCatching { LocalDate.parse(it.dueDate) }.getOrNull() })
            addAll(
                thisMonthMonthlyReminders.mapNotNull { reminder ->
                    runCatching { currentYearMonth.atDay(reminder.dayOfMonth) }.getOrNull()
                },
            )
        }.filter { !it.isBefore(currentMonth) }
        dueDates.minOrNull()?.toString().orEmpty()
    }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(bottom = 264.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Spacer(modifier = Modifier.height(statusBarPadding + 18.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.clickable(onClick = onOpenProfile),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PocketUserAvatar(user = user, size = 52.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Pocket Pro", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "Private money manager",
                            color = TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeaderActionButton(
                        assetName = pocketFeatureAssetName("search"),
                        icon = pocketFeatureIcon("search"),
                        contentDescription = "Search",
                        onClick = onOpenSearch,
                    )
                    HeaderActionButton(
                        assetName = null,
                        icon = if (notificationCount > 0) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                        contentDescription = "Notifications",
                        onClick = onOpenNotifications,
                        badgeCount = notificationCount,
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier.clickable(onClick = onOpenAccounts),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(HomeFooterBlue, HomeFooterBlueDeep),
                            ),
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Top) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Available balance", color = Color.White.copy(alpha = 0.68f), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                IconButton(
                                    onClick = { isBalanceVisible = !isBalanceVisible },
                                    modifier = Modifier.size(30.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                        contentDescription = if (isBalanceVisible) "Hide available balance" else "Show available balance",
                                        tint = Color.White.copy(alpha = 0.78f),
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                            }
                            Text(
                                text = if (isBalanceVisible) formatCurrency(summary.totalBalance, currencySymbol) else "$currencySymbol ****",
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.clickable { isBalanceVisible = !isBalanceVisible },
                            )
                            Text(
                                if (isBalanceVisible) "All accounts combined" else "Tap balance to show",
                                color = Color.White.copy(alpha = 0.48f),
                                fontSize = 10.sp,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.08f)),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        HeroMiniMetric("Income", formatCurrency(summary.monthIncome, currencySymbol), Modifier.weight(1f))
                        HeroMiniMetric("Expense", formatCurrency(summary.monthExpense, currencySymbol), Modifier.weight(1f))
                        HeroMiniMetric("Net", formatSignedAmount(summary.totalBalance, currencySymbol), Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            DueThisMonthStrip(
                installmentCount = thisMonthInstallments.size,
                installmentTotal = formatCurrency(thisMonthInstallmentTotal, currencySymbol),
                loanCount = thisMonthLoanReminders.size,
                loanTotal = formatCurrency(thisMonthLoanTotal, currencySymbol),
                monthlyCount = thisMonthMonthlyReminders.size,
                nextDueLabel = nextDueLabel,
                onClick = onOpenBudget,
            )
        }
        item {
            TodayStrip(
                income = formatCurrency(summary.todayIncome, currencySymbol),
                expense = formatCurrency(summary.todayExpense, currencySymbol),
                net = formatSignedAmount(todayNet, currencySymbol),
                netAccent = if (todayNet >= 0) AccentBlue else AccentOrange,
            )
        }
        item { SectionHeader("Quick actions", "") }
        item {
            QuickActionRow(
                items = listOf(
                    HomeQuickActionItem("Accounts", "Cash and banks", HomeElectricBlue, pocketFeatureAssetName("accounts"), pocketFeatureIcon("accounts"), onOpenAccounts),
                    HomeQuickActionItem("Plans", "Bills and reminders", AccentOrange, pocketFeatureAssetName("plans"), pocketFeatureIcon("plans"), onOpenBudget),
                    HomeQuickActionItem("Goals", "Savings target", AccentGreen, pocketFeatureAssetName("goals"), pocketFeatureIcon("goals"), onOpenGoals),
                    HomeQuickActionItem("Reports", "Spending insights", AccentPurple, pocketFeatureAssetName("reports"), pocketFeatureIcon("reports"), onOpenReports),
                ),
            )
        }
        item {
            PocketAdBanner(modifier = Modifier.fillMaxWidth())
        }
        item { SectionHeader("Money reminders", "") }
        if (pendingLoanReminders.isNotEmpty() || installmentReminders.isNotEmpty() || monthlyReminders.isNotEmpty()) {
            items(pendingLoanReminders, key = { it.id }) { reminder ->
                val remainingAmount = pocketLoanReminderRemainingAmount(reminder)
                val originalAmount = pocketLoanReminderOriginalAmount(reminder)
                val settledAmount = pocketLoanReminderSettledAmount(reminder)
                HomeReminderRow(
                    icon = if (reminder.kind == "lent") Icons.Rounded.Payments else Icons.AutoMirrored.Rounded.ReceiptLong,
                    title = if (reminder.kind == "lent") {
                        "Collect from ${reminder.counterparty.ifBlank { "someone" }}"
                    } else {
                        "Pay back to ${reminder.counterparty.ifBlank { "someone" }}"
                    },
                    subtitle = if (settledAmount > 0.0 && originalAmount > 0.0) {
                        "${reminder.dueDate} • ${formatCurrency(remainingAmount, currencySymbol)} left of ${formatCurrency(originalAmount, currencySymbol)}"
                    } else {
                        "${reminder.dueDate} • ${formatCurrency(remainingAmount, currencySymbol)}"
                    },
                    accent = if (reminder.kind == "lent") AccentGreen else AccentOrange,
                    onClick = {
                        onOpenBudgetTarget("loan", reminder.id)
                    },
                    actionLabel = if (reminder.kind == "lent") "Get Back" else "Pay Back",
                    onActionClick = {
                        onOpenLoanShortcut(
                            if (reminder.kind == "lent") "receive" else "repay",
                            reminder.id,
                            reminder.counterparty,
                            remainingAmount,
                            originalAmount,
                            reminder.dueDate,
                            reminder.note,
                        )
                    },
                    secondaryActionLabel = "Edit",
                    onSecondaryActionClick = {
                        editingLoanReminderFull = reminder
                    },
                    tertiaryActionLabel = "Done",
                    onTertiaryActionClick = {
                        pendingDoneLoanReminder = reminder
                    },
                )
            }
            items(installmentReminders, key = { it.id }) { plan ->
                val remainingInstallments = (plan.totalInstallments - plan.paidInstallments).coerceAtLeast(0)
                val dueDate = runCatching { LocalDate.parse(plan.nextDueDate) }.getOrNull()
                val isOverdue = dueDate?.isBefore(LocalDate.now()) == true
                HomeReminderRow(
                    icon = Icons.Rounded.CalendarMonth,
                    title = pocketNormalizeInstallmentTitle(plan.title, plan.categoryLabel),
                    subtitle = if (isOverdue) {
                        "Overdue since ${plan.nextDueDate} • ${formatCurrency(plan.installmentAmount, currencySymbol)} • $remainingInstallments left"
                    } else {
                        "${plan.nextDueDate} • ${formatCurrency(plan.installmentAmount, currencySymbol)} • ${plan.categoryLabel} • $remainingInstallments left"
                    },
                    accent = if (isOverdue) AccentPink else AccentOrange,
                    onClick = { onOpenBudgetTarget("installment", plan.id) },
                )
            }
            items(monthlyReminders, key = { it.id }) { reminder ->
                HomeReminderRow(
                    icon = Icons.Rounded.NotificationsNone,
                    title = reminder.title,
                    subtitle = "Every month on ${reminder.dayOfMonth}",
                    accent = AccentBlue,
                    onClick = {
                        if (reminder.kind == "budget" && reminder.linkedBudgetId > 0) {
                            onOpenBudgetTarget("budget", reminder.linkedBudgetId.toString())
                        } else {
                            onOpenBudgetTarget("monthly", reminder.id)
                        }
                    },
                )
            }
        } else {
            item {
                NativeEmptyCard("No due reminders yet. Open Plans to add monthly bills, installments, or loan follow-up reminders.")
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                InsightStrip(
                    title = "Budget used",
                    value = "${summary.budgetUsedPercent}%",
                    subtitle = "${formatCurrency(summary.budgetSpent, currencySymbol)} spent",
                    progress = summary.budgetUsedPercent / 100f,
                    accent = AccentOrange,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenBudget,
                )
                InsightStrip(
                    title = "Goal progress",
                    value = "${summary.goalProgressPercent}%",
                    subtitle = "${formatCurrency(summary.goalSaved, currencySymbol)} saved",
                    progress = summary.goalProgressPercent / 100f,
                    accent = AccentPurple,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenGoals,
                )
            }
        }
        item {
            SectionHeader("Top spending", "")
        }
        item {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (analyticsCategories.isEmpty()) {
                    Text("Expense insights will appear here after you add a few records.", color = TextSecondary)
                } else {
                    analyticsCategories.forEach { item ->
                        FlatAnalyticsRow(item = item, currencySymbol = currencySymbol, onClick = onOpenReports)
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader("Recent activity", "", Modifier.weight(1f))
                Row(
                    modifier = Modifier.clickable(onClick = onOpenRecords),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("View all", color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                }
            }
        }
        items(recentItems.take(4)) { item ->
            RecentActivityRow(
                item = item,
                currencySymbol = currencySymbol,
                onClick = { onOpenRecentActivity(item) },
            )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }

    editingLoanReminder?.let { reminder ->
        val parts = reminder.dueDate.split("-")
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, pickedYear, pickedMonth, pickedDay ->
                val nextDate = "%04d-%02d-%02d".format(pickedYear, pickedMonth + 1, pickedDay)
                preferencesStore.updateLoanDueReminderDate(reminder.id, nextDate)
                liveLoanDueReminders = preferencesStore.loanDueReminders()
                PocketReminderScheduler.ensureDailyReminder(context)
                editingLoanReminder = null
            },
            parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR),
            ((parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1).coerceAtLeast(0),
            parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH),
        ).apply {
            setOnDismissListener { editingLoanReminder = null }
        }.show()
    }

    selectedLoanReminderAction?.let { reminder ->
        val remainingAmount = pocketLoanReminderRemainingAmount(reminder)
        val originalAmount = pocketLoanReminderOriginalAmount(reminder)
        AlertDialog(
            onDismissRequest = { selectedLoanReminderAction = null },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedLoanReminderAction = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (reminder.kind == "lent") "Due to receive" else "Due to pay",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        reminder.counterparty.ifBlank { "Unknown person" },
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${reminder.dueDate} • ${formatCurrency(remainingAmount, currencySymbol)} left of ${formatCurrency(originalAmount, currencySymbol)}",
                        color = TextSecondary,
                        lineHeight = 20.sp,
                    )
                    HomeReminderDialogAction(
                        title = if (reminder.kind == "lent") "Get Back" else "Pay Back",
                        accent = if (reminder.kind == "lent") AccentGreen else AccentOrange,
                        onClick = {
                            selectedLoanReminderAction = null
                            onOpenLoanShortcut(
                                if (reminder.kind == "lent") "receive" else "repay",
                                reminder.id,
                                reminder.counterparty,
                                remainingAmount,
                                originalAmount,
                                reminder.dueDate,
                                reminder.note,
                            )
                        },
                    )
                    HomeReminderDialogAction(
                        title = "Edit",
                        accent = AccentBlue,
                        onClick = {
                            selectedLoanReminderAction = null
                            editingLoanReminderFull = reminder
                        },
                    )
                    HomeReminderDialogAction(
                        title = "Snooze",
                        accent = AccentPurple,
                        onClick = {
                            selectedLoanReminderAction = null
                            snoozingLoanReminder = reminder
                        },
                    )
                    HomeReminderDialogAction(
                        title = "Delete",
                        accent = AccentPink,
                        onClick = {
                            selectedLoanReminderAction = null
                            pendingDoneLoanReminder = reminder
                        },
                    )
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(24.dp),
        )
    }

    editingLoanReminderFull?.let { reminder ->
        LoanDueReminderEditDialog(
            reminder = reminder,
            currencySymbol = currencySymbol,
            onDismiss = { editingLoanReminderFull = null },
            onSave = { party, totalAmount, remainingAmount, dueDate, note ->
                preferencesStore.updateLoanDueReminder(
                    reminderId = reminder.id,
                    kind = reminder.kind,
                    counterparty = party,
                    originalAmount = totalAmount,
                    remainingAmount = remainingAmount,
                    dueDate = dueDate,
                    note = note,
                )
                liveLoanDueReminders = preferencesStore.loanDueReminders()
                PocketReminderScheduler.ensureDailyReminder(context)
                editingLoanReminderFull = null
            },
        )
    }

    snoozingLoanReminder?.let { reminder ->
        HomeQuickSnoozeDialog(
            title = if (reminder.kind == "lent") "Snooze Get Back" else "Snooze Pay Back",
            currentDate = reminder.dueDate,
            onDismiss = { snoozingLoanReminder = null },
            onPickDays = { days ->
                val nextDate = runCatching { LocalDate.parse(reminder.dueDate).plusDays(days.toLong()).toString() }
                    .getOrElse { reminder.dueDate }
                preferencesStore.updateLoanDueReminderDate(reminder.id, nextDate)
                liveLoanDueReminders = preferencesStore.loanDueReminders()
                PocketReminderScheduler.ensureDailyReminder(context)
                snoozingLoanReminder = null
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
                liveLoanDueReminders = preferencesStore.loanDueReminders()
                PocketReminderScheduler.ensureDailyReminder(context)
                pendingDoneLoanReminder = null
            },
        )
    }

}

@Composable
private fun DueThisMonthStrip(
    installmentCount: Int,
    installmentTotal: String,
    loanCount: Int,
    loanTotal: String,
    monthlyCount: Int,
    nextDueLabel: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceCard.copy(alpha = 0.38f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Due this month", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    if (nextDueLabel.isBlank()) "Open plans to review all upcoming dues" else "Next due $nextDueLabel",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeroMiniMetric("Installments", "$installmentCount • $installmentTotal", Modifier.weight(1f), TextSecondary, TextPrimary)
            HeroMiniMetric("Loans", "$loanCount • $loanTotal", Modifier.weight(1f), TextSecondary, TextPrimary)
            HeroMiniMetric("Bills", monthlyCount.toString(), Modifier.weight(1f), TextSecondary, TextPrimary)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TextSecondary.copy(alpha = 0.12f)),
        )
    }
}

@Composable
private fun LoanDueReminderEditDialog(
    reminder: PocketNativeLoanDueReminder,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (party: String, totalAmount: Double, remainingAmount: Double, dueDate: String, note: String) -> Unit,
) {
    val context = LocalContext.current
    var party by remember(reminder.id) { mutableStateOf(reminder.counterparty) }
    var totalAmountText by remember(reminder.id) { mutableStateOf(pocketLoanReminderOriginalAmount(reminder).toString().trimEnd('0').trimEnd('.')) }
    var remainingAmountText by remember(reminder.id) { mutableStateOf(pocketLoanReminderRemainingAmount(reminder).toString().trimEnd('0').trimEnd('.')) }
    var dueDate by remember(reminder.id) { mutableStateOf(reminder.dueDate) }
    var note by remember(reminder.id) { mutableStateOf(reminder.note) }
    val totalAmount = totalAmountText.toDoubleOrNull() ?: 0.0
    val remainingAmount = remainingAmountText.toDoubleOrNull() ?: 0.0
    val saveEnabled = party.isNotBlank() && dueDate.isNotBlank() && totalAmount > 0.0 && remainingAmount > 0.0 && remainingAmount <= totalAmount
    val modeLabel = if (reminder.kind == "lent") "Get Back" else "Pay Back"
    val helperText = if (reminder.kind == "lent") {
        "Update who will pay you back, how much is still left, and the new receiving date if needed."
    } else {
        "Update who you need to pay, how much is still left, and the new pay date if needed."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(party.trim(), totalAmount, remainingAmount, dueDate.trim(), note.trim()) },
                enabled = saveEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AccentPurple)
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Edit $modeLabel", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(helperText, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Reminder type: $modeLabel",
                    color = if (reminder.kind == "lent") AccentGreen else AccentOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = party,
                    onValueChange = { party = it },
                    label = { Text("Person") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = homeDialogFieldColors(),
                )
                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Total amount ($currencySymbol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = homeDialogFieldColors(),
                )
                OutlinedTextField(
                    value = remainingAmountText,
                    onValueChange = { remainingAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Remaining amount ($currencySymbol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = homeDialogFieldColors(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val parts = dueDate.split("-")
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, pickedYear, pickedMonth, pickedDay ->
                                    dueDate = "%04d-%02d-%02d".format(pickedYear, pickedMonth + 1, pickedDay)
                                },
                                parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR),
                                ((parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1).coerceAtLeast(0),
                                parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH),
                            ).show()
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Due date", color = TextSecondary, fontSize = 12.sp)
                        Text(dueDate.ifBlank { "Tap to choose date" }, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TextSecondary.copy(alpha = 0.12f)),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = homeDialogFieldColors(),
                )
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun homeDialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = TextSecondary.copy(alpha = 0.18f),
    focusedLabelColor = AccentBlue,
    unfocusedLabelColor = TextSecondary,
)

@Composable
private fun HomeReminderDialogAction(
    title: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(TextSecondary.copy(alpha = 0.10f)),
    )
}

@Composable
private fun HomeQuickSnoozeDialog(
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
                HomeReminderDialogAction(title = "Snooze 1 day", accent = AccentBlue) { onPickDays(1) }
                HomeReminderDialogAction(title = "Snooze 3 days", accent = AccentPurple) { onPickDays(3) }
                HomeReminderDialogAction(title = "Snooze 7 days", accent = AccentOrange) { onPickDays(7) }
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun HomeReminderRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    actionLabel: String = "",
    onActionClick: (() -> Unit)? = null,
    secondaryActionLabel: String = "",
    onSecondaryActionClick: (() -> Unit)? = null,
    tertiaryActionLabel: String = "",
    onTertiaryActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(subtitle, color = TextSecondary, fontSize = 12.sp)
                    if ((actionLabel.isNotBlank() && onActionClick != null) ||
                        (secondaryActionLabel.isNotBlank() && onSecondaryActionClick != null) ||
                        (tertiaryActionLabel.isNotBlank() && onTertiaryActionClick != null)
                    ) {
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (actionLabel.isNotBlank() && onActionClick != null) {
                                Row(
                                    modifier = Modifier.clickable(onClick = onActionClick),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = if (actionLabel.contains("Pay", ignoreCase = true)) {
                                            Icons.AutoMirrored.Rounded.ReceiptLong
                                        } else {
                                            Icons.Rounded.Payments
                                        },
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = actionLabel,
                                        color = accent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                            if (secondaryActionLabel.isNotBlank() && onSecondaryActionClick != null) {
                                Row(
                                    modifier = Modifier.clickable(onClick = onSecondaryActionClick),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        tint = accent.copy(alpha = 0.88f),
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = secondaryActionLabel,
                                        color = accent.copy(alpha = 0.88f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                            if (tertiaryActionLabel.isNotBlank() && onTertiaryActionClick != null) {
                                Row(
                                    modifier = Modifier.clickable(onClick = onTertiaryActionClick),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = accent.copy(alpha = 0.82f),
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = tertiaryActionLabel,
                                        color = accent.copy(alpha = 0.82f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accent),
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = accent.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.10f)))
    }
}

private data class HomeQuickActionItem(
    val title: String,
    val subtitle: String,
    val accent: Color,
    val assetName: String?,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun SectionHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(title, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (subtitle.isNotBlank()) {
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HeaderActionButton(
    assetName: String?,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
) {
    Surface(
        modifier = Modifier
            .size(54.dp)
            .clickable(onClick = onClick),
        color = SurfaceCard,
        shape = RoundedCornerShape(18.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            PocketSvgIcon(
                assetName = assetName,
                fallback = icon,
                contentDescription = contentDescription,
                tint = TextPrimary,
                modifier = Modifier.size(22.dp),
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AccentPink),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = badgeCount.coerceAtMost(9).toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMiniMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.White.copy(alpha = 0.56f),
    valueColor: Color = Color.White,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, color = titleColor, fontWeight = FontWeight.Medium, fontSize = 10.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TodayStrip(income: String, expense: String, net: String, netAccent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TodayValuePill("Income", income, AccentGreen, Modifier.weight(1f))
        TodayValuePill("Expense", expense, AccentPink, Modifier.weight(1f))
        TodayValuePill("Net", net, netAccent, Modifier.weight(1f))
    }
}

@Composable
private fun TodayValuePill(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard.copy(alpha = 0.38f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(title, color = accent.copy(alpha = 0.96f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(
            value,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(accent.copy(alpha = 0.82f), RoundedCornerShape(99.dp)),
        )
    }
}

@Composable
private fun InsightStrip(
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
        }
        ProgressTrack(progress = progress.coerceIn(0f, 1f), accent = accent)
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.10f)))
    }
}

@Composable
private fun QuickActionRow(items: List<HomeQuickActionItem>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.forEach { item ->
            ActionTile(item = item, modifier = Modifier.weight(1f))
        }
        repeat((4 - items.size).coerceAtLeast(0)) { Spacer(modifier = Modifier.weight(1f)) }
    }
}

@Composable
private fun ActionTile(item: HomeQuickActionItem, modifier: Modifier = Modifier) {
    var showInfo by remember(item.title) { mutableStateOf(false) }
    Card(
        onClick = item.onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.42f)),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(item.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                PocketSvgIcon(
                    assetName = item.assetName,
                    fallback = item.icon,
                    contentDescription = item.title,
                    tint = item.accent,
                    modifier = Modifier.size(30.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    item.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(
                    onClick = { showInfo = true },
                    modifier = Modifier.size(26.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "About ${item.title}",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text("Got it", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                }
            },
            title = { Text(item.title, color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(item.subtitle, color = TextSecondary, lineHeight = 20.sp) },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
private fun ProgressTrack(progress: Float, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(Color.White.copy(alpha = 0.08f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(7.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(accent),
        )
    }
}

@Composable
private fun FlatAnalyticsRow(item: PocketNativeAnalyticsCategory, currencySymbol: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard.copy(alpha = 0.38f))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(parseColor(item.colorHex)))
                Column {
                    Text(item.label, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text(formatCurrency(item.amount, currencySymbol), color = TextSecondary, fontSize = 12.sp)
                }
            }
            Text("${item.percent}%", color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeErrorCard(errorText: String) {
    Surface(color = SurfaceCard, shape = RoundedCornerShape(28.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Home is getting ready", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(errorText, color = TextSecondary, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun RecentActivityRow(item: PocketNativeRecentActivity, currencySymbol: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard.copy(alpha = 0.38f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(homeActivityIcon(item), contentDescription = null, tint = parseColor(item.colorHex), modifier = Modifier.size(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.label, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(item.note.ifBlank { item.date }, color = TextSecondary)
                }
            }
            Text(
                text = formatSignedAmount(item.amount, currencySymbol),
                color = if (item.amount >= 0) AccentGreen else AccentPink,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun homeActivityIcon(item: PocketNativeRecentActivity): ImageVector {
    val key = "${item.label} ${item.note}".lowercase(Locale.US)
    return when {
        "goal" in key || "saving" in key -> Icons.Rounded.Savings
        "income" in key || "salary" in key || "bonus" in key || "received" in key -> Icons.Rounded.Payments
        "search" in key -> Icons.Rounded.Search
        "report" in key || "chart" in key -> Icons.Rounded.BarChart
        "budget" in key || "reminder" in key || "installment" in key -> Icons.Rounded.CalendarMonth
        else -> Icons.AutoMirrored.Rounded.ReceiptLong
    }
}

private fun recentActivityTypeLabel(item: PocketNativeRecentActivity): String {
    val key = "${item.kind} ${item.label} ${item.note}".lowercase(Locale.US)
    return when {
        "goal" in key || "saving" in key -> "Goal or savings"
        "income" in key || "salary" in key || "bonus" in key || "received" in key -> "Income"
        "expense" in key || item.amount < 0 -> "Expense"
        "loan" in key || "pay back" in key || "get back" in key -> "Loan activity"
        "budget" in key -> "Budget activity"
        "installment" in key -> "Installment activity"
        else -> "Recent activity"
    }
}

private fun recentActivityOpenTarget(item: PocketNativeRecentActivity): String {
    val key = "${item.kind} ${item.label} ${item.note}".lowercase(Locale.US)
    return when {
        "goal" in key || "saving" in key -> "goals"
        "budget" in key || "installment" in key || "reminder" in key || "loan" in key || "pay back" in key || "get back" in key -> "budget"
        "report" in key || "chart" in key || "insight" in key -> "reports"
        else -> "records"
    }
}

private fun recentActivityOpenLabel(item: PocketNativeRecentActivity): String = when (recentActivityOpenTarget(item)) {
    "budget" -> "Open plans"
    "goals" -> "Open goals"
    "reports" -> "Open reports"
    else -> "Open records"
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1.4f))
    }
}

private fun formatCurrency(amount: Double, symbol: String): String {
    val clean = if (amount % 1.0 == 0.0) amount.toLong().toString() else String.format(Locale.US, "%.2f", amount)
    return "$symbol$clean"
}

private fun formatSignedAmount(amount: Double, symbol: String): String {
    val sign = if (amount >= 0) "+" else "-"
    return "$sign ${formatCurrency(kotlin.math.abs(amount), symbol)}"
}

private fun parseColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    AccentPurple
}

private fun sampleRecentActivity(): List<PocketNativeRecentActivity> = listOf(
    PocketNativeRecentActivity(id = 1, label = "Salary deposit", note = "Income", date = "Today", amount = 25000.0, colorHex = "#3D8DFF"),
    PocketNativeRecentActivity(id = 2, label = "Groceries", note = "Expense", date = "Today", amount = -2200.0, colorHex = "#FF4DA6"),
    PocketNativeRecentActivity(id = 3, label = "Savings deposit", note = "Goal", date = "Yesterday", amount = 5000.0, colorHex = "#19D38A"),
)
