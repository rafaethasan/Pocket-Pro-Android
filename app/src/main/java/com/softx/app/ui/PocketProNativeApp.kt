package com.softx.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.NorthWest
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SouthEast
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.max
import com.softx.app.data.PocketNativeLoanDueReminder
import com.softx.app.data.PocketNativeMonthlyReminder
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeRecordItem
import com.softx.app.data.PocketNativeRepository
import com.softx.app.data.PocketNativeUser
import com.softx.app.data.PocketNativeInstallmentPlan
import com.softx.app.data.pocketInstallmentPaidAmount
import com.softx.app.data.pocketLoanReminderOriginalAmount
import com.softx.app.data.pocketLoanReminderRemainingAmount
import com.softx.app.data.pocketNormalizeInstallmentTitle
import com.softx.app.ui.screens.AccountsNativeScreen
import com.softx.app.ui.screens.AddTransactionNativeScreen
import com.softx.app.ui.screens.AppLockNativeScreen
import com.softx.app.ui.screens.BudgetNativeScreen
import com.softx.app.ui.screens.BackupRestoreNativeScreen
import com.softx.app.ui.screens.CalendarNativeScreen
import com.softx.app.ui.screens.CategorySettingsNativeScreen
import com.softx.app.ui.screens.ExportCenterNativeScreen
import com.softx.app.ui.screens.ForgotPasswordScreen
import com.softx.app.ui.screens.GetStartedScreen
import com.softx.app.ui.screens.GoalsNativeScreen
import com.softx.app.ui.screens.GuestModeScreen
import com.softx.app.ui.screens.HelpSupportNativeScreen
import com.softx.app.ui.screens.HomeNativeScreen
import com.softx.app.ui.screens.NotificationSettingsNativeScreen
import com.softx.app.ui.screens.DataToolsNativeScreen
import com.softx.app.ui.screens.InitialSetupScreen
import com.softx.app.ui.screens.LoginScreen
import com.softx.app.ui.screens.MoreNativeScreen
import com.softx.app.ui.screens.UserGuideNativeScreen
import com.softx.app.ui.screens.OnboardingSlidesScreen
import com.softx.app.ui.screens.PocketBrandBadge
import com.softx.app.ui.screens.ProfileNativeScreen
import com.softx.app.ui.screens.RecordsNativeScreen
import com.softx.app.ui.screens.RecurringNativeScreen
import com.softx.app.ui.screens.RegisterScreen
import com.softx.app.ui.screens.pocketFeatureIcon
import com.softx.app.ui.screens.pocketFeatureAssetName
import com.softx.app.ui.screens.pocketNativePreferredFeatureUser
import com.softx.app.ui.screens.pocketQuickAddIcon
import com.softx.app.ui.screens.pocketQuickAddAssetName
import com.softx.app.ui.screens.PocketSvgIcon
import com.softx.app.ui.screens.ReportsNativeScreen
import com.softx.app.ui.screens.SearchNativeScreen
import com.softx.app.ui.screens.SettingsNativeScreen
import com.softx.app.ui.screens.SmartInsightsNativeScreen
import com.softx.app.ui.screens.LocalPocketPremiumPlansLauncher
import com.softx.app.ui.theme.AccentPurple
import com.softx.app.ui.theme.SurfaceBase
import com.softx.app.ui.theme.SurfaceCard
import com.softx.app.ui.theme.TextPrimary
import com.softx.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class RootScreen {
    Splash,
    Onboarding,
    GetStarted,
    Login,
    Register,
    Guest,
    ForgotPassword,
    InitialSetup,
    NativeApp,
    Settings,
}

private enum class BottomDestination(val label: String, val assetName: String?, val icon: ImageVector) {
    Home("Home", pocketFeatureAssetName("home"), pocketFeatureIcon("home")),
    Records("Records", pocketFeatureAssetName("records"), pocketFeatureIcon("records")),
    Add("Add", pocketFeatureAssetName("add"), pocketFeatureIcon("add")),
    Reports("Reports", pocketFeatureAssetName("reports"), pocketFeatureIcon("reports")),
    Profile("More", null, Icons.Outlined.Settings),
}

private enum class NativeFeatureScreen {
    Settings,
    ProfileDetails,
    Accounts,
    Budget,
    Goals,
    Recurring,
    BackupRestore,
    AppLock,
    NotificationSettings,
    ExportCenter,
    UserGuide,
    HelpSupport,
    DataTools,
    Calendar,
    SmartInsights,
    Categories,
    Search,
}

private data class PocketNotificationCenterItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val details: String,
    val dueDate: String,
    val typeLabel: String,
    val accent: Color,
    val icon: ImageVector,
    val targetType: String = "",
    val targetId: String = "",
)

private fun normalizeRecordEditKind(record: PocketNativeRecordItem): String {
    val rawKind = record.kind.trim().lowercase()
    if (rawKind == "income" || rawKind == "expense") {
        return rawKind
    }
    val mode = record.mode.trim().lowercase()
    return when (mode) {
        "income", "borrow", "receive" -> "income"
        else -> "expense"
    }
}

private val BottomBarPureBlack = Color.White
private val BottomBarSurface = Color(0xFF121722)
private val BottomBarSurfaceSoft = Color(0xFF1A2130)
private val BottomBarStroke = Color(0xFFDCE5F5)
private val BottomBarIconGrey = Color(0xFF8A98B2)
private val BottomBarAccentOrange = Color(0xFFF09A3E)
private val BottomBarAccentOrangeDark = Color(0xFFD97A1A)
private val BottomBarAccentPurple = Color(0xFF596CFF)
private val BottomBarAccentPink = Color(0xFFF05B78)
private val BottomBarAccentBlue = Color(0xFF2E6BFF)
private val BottomBarSelectedGlow = Color(0x1A2E6BFF)
private val NotificationOverlayScrim = Color(0x660F172A)
private val NotificationOverlaySurface = Color(0xFFFFFFFF)
private val NotificationOverlaySoft = Color(0xFFF0F4FF)

@Composable
fun PocketProNativeApp() {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { PocketNativeRepository(context) }
    val preferencesStore = remember(context) { PocketNativePreferencesStore(context) }
    var rootScreen by rememberSaveable { mutableStateOf(RootScreen.Splash) }
    var activeTab by rememberSaveable { mutableStateOf(BottomDestination.Home) }
    var signedInUser by remember { mutableStateOf<PocketNativeUser?>(null) }

    LaunchedEffect(Unit) {
        val cachedUser = preferencesStore.signedInUser()
        val hasCompletedSetup = preferencesStore.hasCompletedInitialSetup()
        val hasSeenOnboarding = preferencesStore.hasSeenOnboarding()

        if (cachedUser != null) {
            signedInUser = cachedUser
            activeTab = BottomDestination.Home
            rootScreen = if (hasCompletedSetup) {
                RootScreen.NativeApp
            } else {
                RootScreen.InitialSetup
            }
            launch {
                val sessionResult = repository.me()
                if (sessionResult.isSuccess) {
                    signedInUser = pocketNativePreferredFeatureUser(sessionResult.getOrNull(), cachedUser)
                    preferencesStore.saveSignedInUser(signedInUser)
                }
            }
            return@LaunchedEffect
        }

        delay(if (hasSeenOnboarding) 120 else 260)
        val sessionResult = repository.me()
        if (sessionResult.isSuccess) {
            signedInUser = pocketNativePreferredFeatureUser(sessionResult.getOrNull(), cachedUser)
            preferencesStore.saveSignedInUser(signedInUser)
            activeTab = BottomDestination.Home
            rootScreen = if (hasCompletedSetup) {
                RootScreen.NativeApp
            } else {
                RootScreen.InitialSetup
            }
        } else {
            rootScreen = if (hasSeenOnboarding) {
                RootScreen.GetStarted
            } else {
                RootScreen.Onboarding
            }
        }
    }

    when (rootScreen) {
        RootScreen.Splash -> NativeSplashScreen()

        RootScreen.Onboarding -> OnboardingSlidesScreen(
            onSkip = {
                preferencesStore.setHasSeenOnboarding(true)
                rootScreen = RootScreen.GetStarted
            },
            onComplete = {
                preferencesStore.setHasSeenOnboarding(true)
                rootScreen = RootScreen.GetStarted
            },
        )

        RootScreen.GetStarted -> GetStartedScreen(
            onLogin = { rootScreen = RootScreen.Login },
            onRegister = { rootScreen = RootScreen.Register },
            onGuest = { rootScreen = RootScreen.Guest },
        )

        RootScreen.Login -> LoginScreen(
            repository = repository,
            onBack = { rootScreen = RootScreen.GetStarted },
            onForgotPassword = { rootScreen = RootScreen.ForgotPassword },
            onLoginSuccess = { user ->
                preferencesStore.setHasSeenOnboarding(true)
                signedInUser = user
                preferencesStore.saveSignedInUser(user)
                activeTab = BottomDestination.Home
                rootScreen = if (preferencesStore.hasCompletedInitialSetup()) {
                    RootScreen.NativeApp
                } else {
                    RootScreen.InitialSetup
                }
            },
        )

        RootScreen.Register -> RegisterScreen(
            repository = repository,
            preferencesStore = preferencesStore,
            onBack = { rootScreen = RootScreen.GetStarted },
            onRegisterSuccess = { user ->
                preferencesStore.setHasSeenOnboarding(true)
                signedInUser = user
                preferencesStore.saveSignedInUser(user)
                activeTab = BottomDestination.Home
                rootScreen = if (preferencesStore.hasCompletedInitialSetup()) {
                    RootScreen.NativeApp
                } else {
                    RootScreen.InitialSetup
                }
            },
        )

        RootScreen.Guest -> GuestModeScreen(
            repository = repository,
            preferencesStore = preferencesStore,
            onBack = { rootScreen = RootScreen.GetStarted },
            onCreateAccount = { rootScreen = RootScreen.Register },
            onContinue = { user ->
                preferencesStore.setHasSeenOnboarding(true)
                signedInUser = user
                preferencesStore.saveSignedInUser(user)
                activeTab = BottomDestination.Home
                rootScreen = if (preferencesStore.hasCompletedInitialSetup()) {
                    RootScreen.NativeApp
                } else {
                    RootScreen.InitialSetup
                }
            },
        )

        RootScreen.ForgotPassword -> ForgotPasswordScreen(
            onBack = { rootScreen = RootScreen.Login },
        )

        RootScreen.InitialSetup -> InitialSetupScreen(
            preferencesStore = preferencesStore,
            onDone = { rootScreen = RootScreen.NativeApp },
        )

        RootScreen.NativeApp -> NativeShell(
            repository = repository,
            preferencesStore = preferencesStore,
            signedInUser = signedInUser,
            activeTab = activeTab,
            onTabChange = { activeTab = it },
            onSignedOut = {
                repository.clearLocalSession()
                preferencesStore.clearSignedInUser()
                signedInUser = null
                activeTab = BottomDestination.Home
                rootScreen = RootScreen.GetStarted
            },
        )

        RootScreen.Settings -> SettingsNativeScreen(
            preferencesStore = preferencesStore,
            onBack = { rootScreen = RootScreen.NativeApp },
        )
    }
}

@Composable
private fun NativeShell(
    repository: PocketNativeRepository,
    preferencesStore: PocketNativePreferencesStore,
    signedInUser: PocketNativeUser?,
    activeTab: BottomDestination,
    onTabChange: (BottomDestination) -> Unit,
    onSignedOut: () -> Unit,
) {
    var featureScreen by rememberSaveable { mutableStateOf<NativeFeatureScreen?>(null) }
    var showSearchOverlay by rememberSaveable { mutableStateOf(false) }
    var showNotificationsOverlay by rememberSaveable { mutableStateOf(false) }
    var categoryManagerKind by rememberSaveable { mutableStateOf("expense") }
    var showAddChooser by rememberSaveable { mutableStateOf(false) }
    var returnToAddChooser by rememberSaveable { mutableStateOf(false) }
    var addMode by rememberSaveable { mutableStateOf("expense") }
    var addPrefillAmount by rememberSaveable { mutableStateOf(0.0) }
    var addPrefillCategoryKey by rememberSaveable { mutableStateOf("") }
    var addPendingAmount by rememberSaveable { mutableStateOf(0.0) }
    var addPendingOriginalAmount by rememberSaveable { mutableStateOf(0.0) }
    var addPendingReminderId by rememberSaveable { mutableStateOf("") }
    var addPendingDueDate by rememberSaveable { mutableStateOf("") }
    var addPrefillPartyName by rememberSaveable { mutableStateOf("") }
    var addPrefillNote by rememberSaveable { mutableStateOf("") }
    var addDraftAmount by rememberSaveable { mutableStateOf("") }
    var addSessionKey by rememberSaveable { mutableStateOf(0) }
    var pendingLoanPickerMode by rememberSaveable { mutableStateOf("") }
    var pendingLoanPickerTitle by rememberSaveable { mutableStateOf("") }
    var pendingLoanPickerEmptyText by rememberSaveable { mutableStateOf("") }
    var showPendingLoanPicker by rememberSaveable { mutableStateOf(false) }
    var pendingLoanPickerItems by remember { mutableStateOf<List<PocketNativeLoanDueReminder>>(emptyList()) }
    var editKind by rememberSaveable { mutableStateOf("") }
    var editId by rememberSaveable { mutableStateOf(0) }
    var editUrl by rememberSaveable { mutableStateOf("") }
    var editingRecord by remember { mutableStateOf<PocketNativeRecordItem?>(null) }
    var appUnlocked by rememberSaveable { mutableStateOf(!preferencesStore.appLockEnabled()) }
    var unlockPin by remember { mutableStateOf("") }
    var unlockError by remember { mutableStateOf("") }
    var budgetJumpType by rememberSaveable { mutableStateOf("") }
    var budgetJumpId by rememberSaveable { mutableStateOf("") }
    var goalJumpId by rememberSaveable { mutableStateOf(0) }
    val currentSignedInUser = signedInUser

    fun notificationCenterItems(): List<PocketNotificationCenterItem> {
        val loanItems = preferencesStore.loanDueReminders()
            .filter { it.status == "pending" }
            .sortedBy { it.dueDate.ifBlank { "9999-99-99" } }
            .map { reminder ->
                val remaining = pocketLoanReminderRemainingAmount(reminder)
                PocketNotificationCenterItem(
                    id = "loan-${reminder.id}",
                    title = if (reminder.kind == "lent") {
                        "Get back from ${reminder.counterparty.ifBlank { "someone" }}"
                    } else {
                        "Pay back to ${reminder.counterparty.ifBlank { "someone" }}"
                    },
                    subtitle = "${reminder.dueDate} • ${if (reminder.kind == "lent") "Pending collection" else "Pending payment"}",
                    details = buildString {
                        append("Remaining ${remaining.formatNotificationMoney()}")
                        val original = pocketLoanReminderOriginalAmount(reminder)
                        if (original > 0.0) append(" of ${original.formatNotificationMoney()}")
                        if (reminder.note.isNotBlank()) append(" • ${reminder.note}")
                    },
                    dueDate = reminder.dueDate,
                    typeLabel = if (reminder.kind == "lent") "Loan follow-up" else "Payback follow-up",
                    accent = if (reminder.kind == "lent") BottomBarAccentBlue else BottomBarAccentOrange,
                    icon = if (reminder.kind == "lent") Icons.Outlined.NorthWest else Icons.Outlined.SouthEast,
                    targetType = "loan",
                    targetId = reminder.id,
                )
            }
        val installmentItems = preferencesStore.installmentPlans()
            .filter { it.status == "active" }
            .sortedBy { it.nextDueDate.ifBlank { "9999-99-99" } }
            .map { plan ->
                val paidAmount = pocketInstallmentPaidAmount(plan)
                val remaining = (plan.loanAmount - paidAmount).coerceAtLeast(0.0)
                PocketNotificationCenterItem(
                    id = "installment-${plan.id}",
                    title = pocketNormalizeInstallmentTitle(plan.title, plan.categoryLabel),
                    subtitle = "${plan.nextDueDate} • ${plan.frequency.replaceFirstChar(Char::uppercase)}",
                    details = buildString {
                        append("Installment ${plan.installmentAmount.formatNotificationMoney()} • Remaining ${remaining.formatNotificationMoney()}")
                        if (plan.note.isNotBlank()) append(" • ${plan.note}")
                    },
                    dueDate = plan.nextDueDate,
                    typeLabel = "Installment plan",
                    accent = BottomBarAccentPurple,
                    icon = Icons.Outlined.CalendarMonth,
                    targetType = "installment",
                    targetId = plan.id,
                )
            }
        val monthlyItems = preferencesStore.monthlyReminders()
            .sortedBy { it.dayOfMonth }
            .map { reminder ->
                PocketNotificationCenterItem(
                    id = "monthly-${reminder.id}",
                    title = reminder.title,
                    subtitle = "Every month on day ${reminder.dayOfMonth}",
                    details = when {
                        reminder.kind == "budget" && reminder.amount > 0.0 ->
                            "Monthly budget ${reminder.amount.formatNotificationMoney()}${if (reminder.note.isNotBlank()) " • ${reminder.note}" else ""}"
                        else -> reminder.note.ifBlank { "Pocket Pro will remind you before the due date and on the same day." }
                    },
                    dueDate = "Day ${reminder.dayOfMonth}",
                    typeLabel = if (reminder.kind == "budget") "Budget reminder" else "Monthly reminder",
                    accent = BottomBarAccentPink,
                    icon = Icons.Outlined.Notifications,
                    targetType = if (reminder.kind == "budget" && reminder.linkedBudgetId > 0) "budget" else "monthly",
                    targetId = if (reminder.kind == "budget" && reminder.linkedBudgetId > 0) reminder.linkedBudgetId.toString() else reminder.id,
                )
            }
        return (loanItems + installmentItems + monthlyItems)
            .sortedBy { notificationDueSortKey(it.dueDate) }
    }

    LaunchedEffect(preferencesStore.appLockEnabled()) {
        appUnlocked = !preferencesStore.appLockEnabled()
        unlockPin = ""
        unlockError = ""
    }

    fun openSettlementFromReminder(mode: String, reminder: PocketNativeLoanDueReminder, fromQuickAdd: Boolean = false) {
        featureScreen = null
        showAddChooser = false
        returnToAddChooser = fromQuickAdd
        showPendingLoanPicker = false
        editKind = ""
        editId = 0
        editUrl = ""
        editingRecord = null
        addMode = mode
        addPrefillAmount = 0.0
        addPendingAmount = pocketLoanReminderRemainingAmount(reminder)
        addPendingOriginalAmount = pocketLoanReminderOriginalAmount(reminder)
        addPendingReminderId = reminder.id
        addPendingDueDate = reminder.dueDate
        addPrefillPartyName = reminder.counterparty
        addPrefillNote = reminder.note
        addSessionKey += 1
        onTabChange(BottomDestination.Add)
    }

    fun openPendingLoanPicker(mode: String) {
        val targetKind = when (mode) {
            "receive" -> "lent"
            "repay" -> "borrow"
            else -> ""
        }
        if (targetKind.isBlank()) return
        val pendingItems = preferencesStore.loanDueReminders()
            .filter { it.status == "pending" && it.kind == targetKind && pocketLoanReminderRemainingAmount(it) > 0.0 }
            .sortedWith(compareBy<PocketNativeLoanDueReminder> { it.dueDate.ifBlank { "9999-99-99" } }.thenBy { it.counterparty.lowercase() })
        pendingLoanPickerMode = mode
        pendingLoanPickerTitle = if (mode == "receive") "Choose who is paying you back" else "Choose who you want to pay"
        pendingLoanPickerEmptyText = if (mode == "receive") {
            "No pending due collection found right now."
        } else {
            "No pending payback found right now."
        }
        pendingLoanPickerItems = pendingItems
        showAddChooser = false
        returnToAddChooser = true
        showPendingLoanPicker = true
    }

    fun openQuickTarget(
        targetType: String,
        targetId: String = "",
        label: String = "",
        note: String = "",
    ) {
        when (targetType.trim().lowercase()) {
            "goal", "saving", "savings" -> {
                val matchedGoal = repository.cachedGoals()?.payload?.goals?.firstOrNull { goal ->
                    (targetId.isNotBlank() && goal.id.toString() == targetId) ||
                        goal.name.equals(label, ignoreCase = true) ||
                        goal.name.equals(note, ignoreCase = true) ||
                        label.contains(goal.name, ignoreCase = true)
                }
                goalJumpId = matchedGoal?.id ?: 0
                featureScreen = NativeFeatureScreen.Goals
            }

            "loan" -> {
                val reminder = preferencesStore.loanDueReminders().firstOrNull { reminder ->
                    ((targetId.isNotBlank() && reminder.id == targetId) ||
                        reminder.counterparty.equals(label, ignoreCase = true) ||
                        reminder.counterparty.equals(note, ignoreCase = true) ||
                        note.contains(reminder.counterparty, ignoreCase = true)) &&
                        reminder.status == "pending"
                }
                if (reminder != null) {
                    openSettlementFromReminder(
                        mode = if (reminder.kind == "lent") "receive" else "repay",
                        reminder = reminder,
                    )
                } else {
                    budgetJumpType = "loan"
                    budgetJumpId = targetId
                    featureScreen = NativeFeatureScreen.Budget
                }
            }

            "installment" -> {
                val matchedInstallment = preferencesStore.installmentPlans().firstOrNull { plan ->
                    (targetId.isNotBlank() && plan.id == targetId) ||
                        pocketNormalizeInstallmentTitle(plan.title, plan.categoryLabel).equals(label, ignoreCase = true) ||
                        plan.title.equals(label, ignoreCase = true) ||
                        plan.title.equals(note, ignoreCase = true) ||
                        plan.categoryLabel.equals(label, ignoreCase = true)
                }
                budgetJumpType = "installment"
                budgetJumpId = matchedInstallment?.id ?: targetId
                featureScreen = NativeFeatureScreen.Budget
            }

            "budget" -> {
                val matchedBudget = repository.cachedBudget()?.payload?.budgets?.firstOrNull { budget ->
                    (targetId.isNotBlank() && budget.id.toString() == targetId) ||
                        budget.categoryLabel.equals(label, ignoreCase = true) ||
                        budget.categoryKey.equals(label.trim().lowercase().replace(" ", "_"), ignoreCase = true) ||
                        budget.name.equals(note, ignoreCase = true) ||
                        budget.name.equals(label, ignoreCase = true)
                }
                budgetJumpType = "budget"
                budgetJumpId = matchedBudget?.id?.toString() ?: targetId
                featureScreen = NativeFeatureScreen.Budget
            }

            "monthly", "reminder" -> {
                val matchedReminder = preferencesStore.monthlyReminders().firstOrNull { reminder ->
                    (targetId.isNotBlank() && reminder.id == targetId) ||
                        reminder.title.equals(label, ignoreCase = true) ||
                        reminder.title.equals(note, ignoreCase = true)
                }
                budgetJumpType = if (matchedReminder?.kind == "budget" && matchedReminder.linkedBudgetId > 0) "budget" else "monthly"
                budgetJumpId = when {
                    matchedReminder == null -> targetId
                    matchedReminder.kind == "budget" && matchedReminder.linkedBudgetId > 0 -> matchedReminder.linkedBudgetId.toString()
                    else -> matchedReminder.id
                }
                featureScreen = NativeFeatureScreen.Budget
            }

            "report", "chart", "insight" -> {
                featureScreen = null
                onTabChange(BottomDestination.Reports)
            }

            else -> {
                featureScreen = null
                onTabChange(BottomDestination.Records)
            }
        }
    }

    fun resetAddState() {
        featureScreen = null
        editKind = ""
        editId = 0
        editUrl = ""
        addPrefillAmount = 0.0
        addPrefillCategoryKey = ""
        addPendingAmount = 0.0
        addPendingOriginalAmount = 0.0
        addPendingReminderId = ""
        addPendingDueDate = ""
        addPrefillPartyName = ""
        addPrefillNote = ""
        addDraftAmount = ""
        editingRecord = null
    }

    fun closeAddFlow() {
        resetAddState()
        if (returnToAddChooser) {
            returnToAddChooser = false
            showPendingLoanPicker = false
            showAddChooser = true
            onTabChange(BottomDestination.Home)
        } else {
            onTabChange(BottomDestination.Home)
        }
    }

    fun closePendingLoanPicker() {
        showPendingLoanPicker = false
        if (returnToAddChooser) {
            showAddChooser = true
            onTabChange(BottomDestination.Home)
        }
    }

    BackHandler(
        enabled = showNotificationsOverlay || showSearchOverlay || showPendingLoanPicker || showAddChooser || featureScreen != null || activeTab != BottomDestination.Home,
    ) {
        when {
            showNotificationsOverlay -> showNotificationsOverlay = false
            showSearchOverlay -> showSearchOverlay = false
            showPendingLoanPicker -> closePendingLoanPicker()
            showAddChooser -> showAddChooser = false
            featureScreen != null -> featureScreen = null
            activeTab == BottomDestination.Add && returnToAddChooser -> {
                returnToAddChooser = false
                showPendingLoanPicker = false
                showAddChooser = true
                onTabChange(BottomDestination.Home)
            }
            activeTab != BottomDestination.Home -> onTabChange(BottomDestination.Home)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (activeTab != BottomDestination.Add) {
                PocketFloatingBottomBar(
                    activeTab = activeTab,
                    onSelect = { destination ->
                        featureScreen = null
                        if (destination == BottomDestination.Add) {
                            showAddChooser = true
                        } else {
                            onTabChange(destination)
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalPocketPremiumPlansLauncher provides null,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceBase)
                    .padding(
                        start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                        top = innerPadding.calculateTopPadding(),
                        end = innerPadding.calculateRightPadding(LayoutDirection.Ltr),
                        bottom = innerPadding.calculateBottomPadding(),
                    ),
            ) {
                when (featureScreen) {
                NativeFeatureScreen.Settings -> SettingsNativeScreen(
                    preferencesStore = preferencesStore,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.Accounts -> AccountsNativeScreen(
                    repository = repository,
                    signedInUser = currentSignedInUser,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.Budget -> BudgetNativeScreen(
                    repository = repository,
                    preferencesStore = preferencesStore,
                    signedInUser = currentSignedInUser,
                    onOpenRecurring = { featureScreen = NativeFeatureScreen.Recurring },
                    onOpenBudgetPayShortcut = { amount, categoryKey, title, note ->
                        budgetJumpType = ""
                        budgetJumpId = ""
                        featureScreen = null
                        showAddChooser = false
                        editKind = ""
                        editId = 0
                        editUrl = ""
                        editingRecord = null
                        addMode = "expense"
                        addPrefillAmount = amount
                        addPrefillCategoryKey = categoryKey
                        addPendingAmount = 0.0
                        addPendingOriginalAmount = 0.0
                        addPendingReminderId = ""
                        addPendingDueDate = ""
                        addPrefillPartyName = ""
                        addPrefillNote = note.ifBlank { title }
                        addSessionKey += 1
                        onTabChange(BottomDestination.Add)
                    },
                    initialJumpType = budgetJumpType,
                    initialJumpId = budgetJumpId,
                    onInitialJumpHandled = {
                        budgetJumpType = ""
                        budgetJumpId = ""
                    },
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.Goals -> GoalsNativeScreen(
                    repository = repository,
                    initialJumpGoalId = goalJumpId,
                    onInitialJumpHandled = { goalJumpId = 0 },
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.Recurring -> RecurringNativeScreen(
                    repository = repository,
                    signedInUser = currentSignedInUser,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.BackupRestore -> BackupRestoreNativeScreen(
                    repository = repository,
                    preferencesStore = preferencesStore,
                    signedInUser = currentSignedInUser,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.AppLock -> AppLockNativeScreen(
                    preferencesStore = preferencesStore,
                    signedInUser = currentSignedInUser,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.NotificationSettings -> NotificationSettingsNativeScreen(
                    preferencesStore = preferencesStore,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.ExportCenter -> ExportCenterNativeScreen(
                    onBack = { featureScreen = null },
                    onOpenReports = {
                        featureScreen = null
                        onTabChange(BottomDestination.Reports)
                    },
                    onOpenAccounts = {
                        featureScreen = NativeFeatureScreen.Accounts
                    },
                    onOpenBackupRestore = {
                        featureScreen = NativeFeatureScreen.BackupRestore
                    },
                )

                NativeFeatureScreen.HelpSupport -> HelpSupportNativeScreen(
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.UserGuide -> UserGuideNativeScreen(
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.DataTools -> DataToolsNativeScreen(
                    repository = repository,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.Calendar -> CalendarNativeScreen(
                    repository = repository,
                    signedInUser = currentSignedInUser,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.SmartInsights -> SmartInsightsNativeScreen(
                    repository = repository,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.Categories -> CategorySettingsNativeScreen(
                    repository = repository,
                    initialKind = categoryManagerKind,
                    onBack = { featureScreen = null },
                )

                NativeFeatureScreen.Search -> SearchNativeScreen(
                    repository = repository,
                    signedInUser = currentSignedInUser,
                    onBack = { featureScreen = null },
                    onOpenRecord = { record ->
                        featureScreen = null
                        editKind = normalizeRecordEditKind(record)
                        editId = record.id
                        editUrl = record.editUrl
                        addMode = record.mode.ifBlank {
                            if (record.signedAmount >= 0) "income" else "expense"
                        }
                        onTabChange(BottomDestination.Add)
                    },
                )

                NativeFeatureScreen.ProfileDetails -> ProfileNativeScreen(
                    repository = repository,
                    preferencesStore = preferencesStore,
                    signedInUser = currentSignedInUser,
                    onBack = { featureScreen = null },
                    onSignedOut = onSignedOut,
                )

                null -> when (activeTab) {
                    BottomDestination.Home -> HomeNativeScreen(
                        repository = repository,
                        signedInUser = currentSignedInUser,
                        onOpenAccounts = { featureScreen = NativeFeatureScreen.Accounts },
                        onOpenBudget = { featureScreen = NativeFeatureScreen.Budget },
                        onOpenBudgetTarget = { type, id ->
                            budgetJumpType = type
                            budgetJumpId = id
                            featureScreen = NativeFeatureScreen.Budget
                        },
                        onOpenGoals = {
                            goalJumpId = 0
                            featureScreen = NativeFeatureScreen.Goals
                        },
                        onOpenAdd = {
                            featureScreen = null
                            editKind = ""
                            editId = 0
                            editUrl = ""
                            addPrefillAmount = 0.0
                            addPrefillCategoryKey = ""
                            addPendingAmount = 0.0
                            addPendingOriginalAmount = 0.0
                            addPendingReminderId = ""
                            addPendingDueDate = ""
                            addPrefillPartyName = ""
                            addPrefillNote = ""
                            addSessionKey += 1
                            editingRecord = null
                            showAddChooser = true
                        },
                        onOpenLoanShortcut = { mode, reminderId, counterparty, amount, originalAmount, dueDateValue, note ->
                            featureScreen = null
                            showAddChooser = false
                            editKind = ""
                            editId = 0
                            editUrl = ""
                            editingRecord = null
                            addMode = mode
                            addPrefillAmount = 0.0
                            addPrefillCategoryKey = ""
                            addPendingAmount = amount
                            addPendingOriginalAmount = originalAmount
                            addPendingReminderId = reminderId
                            addPendingDueDate = dueDateValue
                            addPrefillPartyName = counterparty
                            addPrefillNote = note
                            addSessionKey += 1
                            onTabChange(BottomDestination.Add)
                        },
                        onOpenRecords = {
                            featureScreen = null
                            onTabChange(BottomDestination.Records)
                        },
                        onOpenSearch = {
                            showSearchOverlay = true
                        },
                        onOpenNotifications = {
                            showNotificationsOverlay = true
                        },
                        onOpenReports = {
                            featureScreen = null
                            onTabChange(BottomDestination.Reports)
                        },
                        onOpenMore = {
                            featureScreen = null
                            onTabChange(BottomDestination.Profile)
                        },
                        onOpenProfile = { featureScreen = NativeFeatureScreen.ProfileDetails },
                        onOpenRecentActivity = { item ->
                            val key = "${item.kind} ${item.label} ${item.note}".lowercase()
                            when {
                                "goal" in key || "saving" in key -> {
                                    openQuickTarget("goal", label = item.label, note = item.note)
                                }
                                "budget" in key || "installment" in key || "reminder" in key || "loan" in key || "pay back" in key || "get back" in key -> {
                                    val inferredType = when {
                                        "installment" in key -> "installment"
                                        "loan" in key || "pay back" in key || "get back" in key -> "loan"
                                        "reminder" in key -> "monthly"
                                        else -> "budget"
                                    }
                                    openQuickTarget(inferredType, label = item.label, note = item.note)
                                }
                                "report" in key || "chart" in key || "insight" in key -> {
                                    openQuickTarget("report", label = item.label, note = item.note)
                                }
                                else -> {
                                    openQuickTarget("record", label = item.label, note = item.note)
                                }
                            }
                        },
                    )

                    BottomDestination.Records -> Column(modifier = Modifier.fillMaxSize()) {
                        FloatingTabBackBar(
                            title = "Records",
                            onBack = { onTabChange(BottomDestination.Home) },
                        )
                        RecordsNativeScreen(
                            repository = repository,
                            signedInUser = currentSignedInUser,
                            onEditRecord = { record ->
                                editingRecord = record
                                editKind = normalizeRecordEditKind(record)
                                editId = record.id
                                editUrl = record.editUrl
                                addMode = record.mode.ifBlank {
                                    if (record.signedAmount >= 0) "income" else "expense"
                                }
                                onTabChange(BottomDestination.Add)
                            },
                        )
                    }

                    BottomDestination.Add -> key(addMode, editKind, editId, addPrefillAmount, addPrefillCategoryKey, addPendingAmount, addPendingOriginalAmount, addPendingReminderId, addPendingDueDate, addPrefillPartyName, addPrefillNote, addSessionKey) {
                        AddTransactionNativeScreen(
                            repository = repository,
                            signedInUser = currentSignedInUser,
                            initialMode = addMode,
                            initialEditKind = editKind,
                            initialEditId = editId,
                            initialEditUrl = editUrl,
                            initialFallbackDate = editingRecord?.date.orEmpty(),
                            initialFallbackAmount = kotlin.math.abs(editingRecord?.signedAmount ?: 0.0),
                            initialFallbackNote = editingRecord?.note.orEmpty(),
                            initialPrefillAmount = addPrefillAmount,
                            initialPrefillCategoryKey = addPrefillCategoryKey,
                            initialPendingAmount = addPendingAmount,
                            initialPendingOriginalAmount = addPendingOriginalAmount,
                            initialPendingReminderId = addPendingReminderId,
                            initialPendingDueDate = addPendingDueDate,
                            initialPrefillPartyName = addPrefillPartyName,
                            initialPrefillNote = addPrefillNote,
                            restoredAmountText = addDraftAmount,
                            onAmountChanged = { addDraftAmount = it },
                            showModeTabs = false,
                            onClose = { closeAddFlow() },
                            onOpenCategoryManager = { kind ->
                                categoryManagerKind = kind
                                featureScreen = NativeFeatureScreen.Categories
                            },
                        )
                    }

                    BottomDestination.Reports -> Column(modifier = Modifier.fillMaxSize()) {
                        FloatingTabBackBar(
                            title = "Reports",
                            onBack = { onTabChange(BottomDestination.Home) },
                        )
                        ReportsNativeScreen(
                            repository = repository,
                            signedInUser = currentSignedInUser,
                            onOpenCalendar = { featureScreen = NativeFeatureScreen.Calendar },
                            onOpenSmartInsights = { featureScreen = NativeFeatureScreen.SmartInsights },
                        )
                    }

                    BottomDestination.Profile -> Column(modifier = Modifier.fillMaxSize()) {
                        FloatingTabBackBar(
                            title = "More",
                            onBack = { onTabChange(BottomDestination.Home) },
                        )
                        MoreNativeScreen(
                            preferencesStore = preferencesStore,
                            onBack = { onTabChange(BottomDestination.Home) },
                            showTopBackButton = false,
                            onOpenSettings = { featureScreen = NativeFeatureScreen.Settings },
                            onOpenBackupRestore = { featureScreen = NativeFeatureScreen.BackupRestore },
                            onOpenAppLock = { featureScreen = NativeFeatureScreen.AppLock },
                            onOpenNotifications = { showNotificationsOverlay = true },
                            onOpenNotificationSettings = { featureScreen = NativeFeatureScreen.NotificationSettings },
                            onOpenPlans = { featureScreen = NativeFeatureScreen.Budget },
                            onOpenCategoryManager = {
                                categoryManagerKind = "expense"
                                featureScreen = NativeFeatureScreen.Categories
                            },
                            onOpenReports = { onTabChange(BottomDestination.Reports) },
                            onOpenSearch = { showSearchOverlay = true },
                            onOpenAccounts = { featureScreen = NativeFeatureScreen.Accounts },
                            onOpenExportCenter = { featureScreen = NativeFeatureScreen.ExportCenter },
                            onOpenUserGuide = { featureScreen = NativeFeatureScreen.UserGuide },
                            onOpenHelpSupport = { featureScreen = NativeFeatureScreen.HelpSupport },
                            onOpenDataTools = { featureScreen = NativeFeatureScreen.DataTools },
                        )
                    }
                    }
                }

                if (showSearchOverlay) {
                    SearchNativeScreen(
                        repository = repository,
                        signedInUser = currentSignedInUser,
                        onBack = { showSearchOverlay = false },
                        onOpenRecord = { record ->
                            showSearchOverlay = false
                            featureScreen = null
                            editKind = normalizeRecordEditKind(record)
                            editId = record.id
                            editUrl = record.editUrl
                            addMode = record.mode.ifBlank {
                                if (record.signedAmount >= 0) "income" else "expense"
                            }
                            onTabChange(BottomDestination.Add)
                        },
                    )
                }

                if (showNotificationsOverlay) {
                    PocketNotificationCenterOverlay(
                        items = notificationCenterItems(),
                        onBack = { showNotificationsOverlay = false },
                        onOpenItem = { item ->
                            showNotificationsOverlay = false
                            openQuickTarget(
                                targetType = item.targetType,
                                targetId = item.targetId,
                                label = item.title,
                                note = item.details,
                            )
                        },
                    )
                }

                if (preferencesStore.appLockEnabled() && !appUnlocked) {
                    AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (preferencesStore.validateAppLockPin(unlockPin)) {
                                    appUnlocked = true
                                    unlockError = ""
                                    unlockPin = ""
                                } else {
                                    unlockError = "Wrong PIN. Try again."
                                }
                            },
                        ) {
                            Text("Unlock", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Text("Unlock Pocket Pro", color = TextPrimary, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            androidx.compose.material3.OutlinedTextField(
                                value = unlockPin,
                                onValueChange = {
                                    unlockPin = it.filter(Char::isDigit).take(6)
                                    if (unlockError.isNotBlank()) unlockError = ""
                                },
                                label = { Text("Enter PIN") },
                                singleLine = true,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword,
                                ),
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = BottomBarAccentPurple,
                                    unfocusedLabelColor = TextSecondary,
                                    focusedBorderColor = BottomBarAccentPurple,
                                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                                ),
                            )
                            if (unlockError.isNotBlank()) {
                                Text(unlockError, color = Color(0xFFFF6B81))
                            }
                        }
                    },
                    containerColor = BottomBarSurface,
                    shape = RoundedCornerShape(20.dp),
                )
            }

            if (showAddChooser) {
                Dialog(onDismissRequest = { showAddChooser = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 106.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(BottomBarSurface)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Quick Add", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { showAddChooser = false }) {
                                        Text("Close", color = TextSecondary)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AddChooserTile("Income", pocketQuickAddAssetName("income"), pocketQuickAddIcon("income"), BottomBarAccentBlue, Modifier.weight(1f)) {
                                        addMode = "income"
                                        editKind = ""
                                        editId = 0
                                        editUrl = ""
                                        returnToAddChooser = true
                                        showAddChooser = false
                                        onTabChange(BottomDestination.Add)
                                    }
                                    AddChooserTile("Expense", pocketQuickAddAssetName("expense"), pocketQuickAddIcon("expense"), BottomBarAccentPink, Modifier.weight(1f)) {
                                        addMode = "expense"
                                        editKind = ""
                                        editId = 0
                                        editUrl = ""
                                        returnToAddChooser = true
                                        showAddChooser = false
                                        onTabChange(BottomDestination.Add)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AddChooserTile("Give Loan", pocketQuickAddAssetName("give loan"), pocketQuickAddIcon("give loan"), BottomBarAccentPurple, Modifier.weight(1f)) {
                                        addMode = "lent"
                                        editKind = ""
                                        editId = 0
                                        editUrl = ""
                                        returnToAddChooser = true
                                        showAddChooser = false
                                        onTabChange(BottomDestination.Add)
                                    }
                                    AddChooserTile("Get Back", pocketQuickAddAssetName("get back"), pocketQuickAddIcon("get back"), Color(0xFF22C55E), Modifier.weight(1f)) {
                                        openPendingLoanPicker("receive")
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AddChooserTile("Take Loan", pocketQuickAddAssetName("take loan"), pocketQuickAddIcon("take loan"), BottomBarAccentBlue, Modifier.weight(1f)) {
                                        addMode = "borrow"
                                        editKind = ""
                                        editId = 0
                                        editUrl = ""
                                        returnToAddChooser = true
                                        showAddChooser = false
                                        onTabChange(BottomDestination.Add)
                                    }
                                    AddChooserTile("Pay Back", pocketQuickAddAssetName("pay back"), pocketQuickAddIcon("pay back"), BottomBarAccentOrange, Modifier.weight(1f)) {
                                        openPendingLoanPicker("repay")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showPendingLoanPicker) {
                AlertDialog(
                    onDismissRequest = { closePendingLoanPicker() },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { closePendingLoanPicker() }) {
                            Text(if (returnToAddChooser) "Back" else "Close", color = TextSecondary)
                        }
                    },
                    title = {
                        Text(pendingLoanPickerTitle, color = TextPrimary, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        if (pendingLoanPickerItems.isEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(pendingLoanPickerEmptyText, color = TextSecondary)
                                TextButton(
                                    onClick = {
                                        showPendingLoanPicker = false
                                        addMode = pendingLoanPickerMode
                                        editKind = ""
                                        editId = 0
                                        editUrl = ""
                                        addPrefillAmount = 0.0
                                        addPendingAmount = 0.0
                                        addPendingOriginalAmount = 0.0
                                        addPendingReminderId = ""
                                        addPendingDueDate = ""
                                        addPrefillPartyName = ""
                                        addPrefillNote = ""
                                        addSessionKey += 1
                                        returnToAddChooser = true
                                        onTabChange(BottomDestination.Add)
                                    },
                                ) {
                                    Text("Open empty form", color = BottomBarAccentBlue, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "Select the exact person first, so amount and due date stay correct.",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                )
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(318.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    items(pendingLoanPickerItems, key = { it.id }) { reminder ->
                                        PendingLoanPickerRow(
                                            reminder = reminder,
                                            actionLabel = if (pendingLoanPickerMode == "receive") "Get Back" else "Pay Back",
                                            onClick = { openSettlementFromReminder(pendingLoanPickerMode, reminder, fromQuickAdd = true) },
                                        )
                                    }
                                }
                            }
                        }
                    },
                    containerColor = SurfaceBase,
                    shape = RoundedCornerShape(24.dp),
                    )
                }

            }
        }
    }
}

@Composable
private fun FloatingTabBackBar(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 26.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard.copy(alpha = 0.96f))
                .clickable(onClick = onBack)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = null,
                tint = BottomBarIconGrey,
                modifier = Modifier.size(16.dp),
            )
            Text("Back", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("•", color = TextSecondary, fontSize = 12.sp)
            Text(title, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

private fun Double.formatNotificationMoney(): String {
    val whole = toLong()
    return if (kotlin.math.abs(this - whole.toDouble()) < 0.009) {
        "৳$whole"
    } else {
        "৳${"%.2f".format(this)}"
    }
}

private fun notificationDueSortKey(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        return trimmed
    }
    val dayMatch = Regex("""Day\s+(\d{1,2})""", RegexOption.IGNORE_CASE).find(trimmed)
    if (dayMatch != null) {
        val day = dayMatch.groupValues[1].toIntOrNull()?.coerceIn(1, 31) ?: 31
        return "9999-99-${day.toString().padStart(2, '0')}"
    }
    return "9999-99-99"
}

@Composable
private fun PocketNotificationCenterOverlay(
    items: List<PocketNotificationCenterItem>,
    onBack: () -> Unit,
    onOpenItem: (PocketNotificationCenterItem) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NotificationOverlayScrim),
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 30.dp, end = 12.dp, bottom = 132.dp),
            color = NotificationOverlaySurface,
            shape = RoundedCornerShape(30.dp),
        ) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { Spacer(modifier = Modifier.size(18.dp)) }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.Surface(
                            modifier = Modifier.clickable(onClick = onBack),
                            color = NotificationOverlaySoft,
                            shape = RoundedCornerShape(999.dp),
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close notifications", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                if (items.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp, bottom = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                            Text("No notifications right now", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Pocket Pro will show your reminders and updates here.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(items, key = { it.id }) { item ->
                        NotificationCenterRow(item = item, onClick = { onOpenItem(item) })
                    }
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }
        }
    }
}

@Composable
private fun NotificationCenterRow(
    item: PocketNotificationCenterItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NotificationOverlaySoft, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(item.accent.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(item.icon, contentDescription = null, tint = item.accent, modifier = Modifier.size(18.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(item.subtitle, color = TextSecondary, fontSize = 12.sp)
                    Text(item.typeLabel, color = item.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Text(item.details, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.10f)))
    }
}

@Composable
private fun PocketNotificationDetailOverlay(
    item: PocketNotificationCenterItem,
    onBack: () -> Unit,
    onOpenRelated: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NotificationOverlayScrim),
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 30.dp, end = 12.dp, bottom = 132.dp),
            color = NotificationOverlaySurface,
            shape = RoundedCornerShape(30.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(item.typeLabel, color = item.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    androidx.compose.material3.Surface(
                        modifier = Modifier.clickable(onClick = onBack),
                        color = NotificationOverlaySoft,
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close notification details", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Text(item.title, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 28.sp)
                Text(item.subtitle, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.10f)))
                DetailLine("Due", item.dueDate)
                DetailLine("Details", item.details)
                Spacer(modifier = Modifier.weight(1f))
                androidx.compose.material3.Button(
                    onClick = onOpenRelated,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open related section", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailLine(
    title: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextSecondary.copy(alpha = 0.10f)))
    }
}

@Composable
private fun NativeSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF090C14),
                        Color(0xFF121826),
                        Color(0xFF171F30),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 110.dp, y = (-70).dp)
                .size(240.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF7C5CFF).copy(alpha = 0.20f), Color.Transparent),
                    ),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-70).dp, y = 70.dp)
                .size(220.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2F8CFF).copy(alpha = 0.16f), Color.Transparent),
                    ),
                    CircleShape,
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "POCKET PRO",
                color = Color.White.copy(alpha = 0.48f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.6.sp,
            )
            Box(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color(0xFF121826).copy(alpha = 0.92f),
                            ),
                        ),
                        shape = RoundedCornerShape(38.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(38.dp),
                    )
                    .padding(horizontal = 28.dp, vertical = 34.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PocketBrandBadge(
                        iconSize = 92.dp,
                        containerPadding = 18.dp,
                        containerAlpha = 0.10f,
                    )
                    Text(
                        text = "Pocket Pro",
                        color = TextPrimary,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                    Text(
                        text = "Track money, dues, and reminders with calm daily control.",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 22.dp)
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.58f)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF6F7BFF), Color(0xFF2FA4FF)),
                                    ),
                                    RoundedCornerShape(999.dp),
                                ),
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == 1) 16.dp else 7.dp, 7.dp)
                                    .background(
                                        color = if (index == 1) Color.White.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(99.dp),
                                    ),
                            )
                        }
                    }
                    Text(
                        text = "Loading your workspace",
                        color = Color.White.copy(alpha = 0.56f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddChooserTile(
    label: String,
    assetName: String?,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(BottomBarSurfaceSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(accent.copy(alpha = 0.14f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            PocketSvgIcon(
                assetName = assetName,
                fallback = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(28.dp),
            )
        }
        Box(
            modifier = Modifier.height(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(accent.copy(alpha = 0.85f), RoundedCornerShape(99.dp)),
        )
    }
}

@Composable
private fun PendingLoanPickerRow(
    reminder: PocketNativeLoanDueReminder,
    actionLabel: String,
    onClick: () -> Unit,
) {
    val remaining = pocketLoanReminderRemainingAmount(reminder)
    val original = pocketLoanReminderOriginalAmount(reminder)
    val accent = if (reminder.kind == "lent") Color(0xFF22C55E) else BottomBarAccentOrange
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 1.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (reminder.kind == "lent") Icons.Outlined.NorthWest else Icons.Outlined.SouthEast,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = reminder.counterparty.ifBlank { "Unknown person" },
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
            Text(
                text = actionLabel,
                color = accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Payments, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                Text(
                    text = "Remaining ৳${"%.2f".format(remaining)} / Total ৳${"%.2f".format(original)}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                Text(
                    text = if (reminder.dueDate.isBlank()) "No due date" else reminder.dueDate,
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.PersonOutline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                Text(
                    text = reminder.note.ifBlank { "No note" },
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(accent.copy(alpha = 0.45f)),
        )
    }
}

@Composable
private fun PocketFloatingBottomBar(
    activeTab: BottomDestination,
    onSelect: (BottomDestination) -> Unit,
) {
    val destinations = BottomDestination.entries
    val navigationBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val floatingBottomPadding = max(navigationBottomInset.value + 22f, 28f).dp
    val barShape = RoundedCornerShape(36.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 18.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = floatingBottomPadding)
                .height(88.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(20.dp, barShape, clip = false)
                    .clip(barShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1C2230), Color(0xFF151A26)),
                        ),
                        shape = barShape,
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.12f), barShape),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 20.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    val selected = destination == activeTab
                    if (destination == BottomDestination.Add) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(y = (-16).dp)
                                    .size(78.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = Color(0xFF0F1118),
                                        shape = CircleShape,
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(66.dp)
                                        .clip(CircleShape)
                                        .shadow(20.dp, CircleShape, clip = false)
                                        .border(
                                            width = 2.dp,
                                            color = Color(0xFF8E78FF),
                                            shape = CircleShape,
                                        )
                                        .background(
                                            color = AccentPurple,
                                            shape = CircleShape,
                                        )
                                        .clickable { onSelect(destination) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp),
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) AccentPurple.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { onSelect(destination) }
                                .padding(top = 6.dp, bottom = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (selected) AccentPurple else BottomBarIconGrey.copy(alpha = 0.98f),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = destination.label,
                                color = if (selected) Color.White else BottomBarIconGrey.copy(alpha = 0.96f),
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(width = if (selected) 16.dp else 0.dp, height = 2.dp)
                                    .background(AccentPurple, RoundedCornerShape(99.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}
