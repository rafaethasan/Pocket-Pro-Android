package com.softx.app.data

import com.google.gson.annotations.SerializedName

data class PocketNativeUser(
    val tenantId: Int = 0,
    val userId: Int = 0,
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val bio: String = "",
    val role: String = "USER",
    val avatarUrl: String = "",
    val avatarInitials: String = "PP",
    val customAvatarKey: String = "",
    val customAvatarUri: String = "",
    val planCode: String = "",
    val planLabel: String = "",
    val demoAccessEnabled: Boolean = false,
)

fun pocketAvatarInitialsFor(
    fullName: String,
    fallback: String = "PP",
): String {
    val parts = fullName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    if (parts.isEmpty()) return fallback
    return parts
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { fallback }
        .take(2)
}

fun pocketMergeLocalProfile(
    preferredUser: PocketNativeUser?,
    localUser: PocketNativeUser?,
): PocketNativeUser? {
    if (preferredUser == null) return localUser
    if (localUser == null) return preferredUser
    val sameIdentity = when {
        preferredUser.userId > 0 && localUser.userId > 0 -> preferredUser.userId == localUser.userId
        preferredUser.email.isNotBlank() && localUser.email.isNotBlank() -> preferredUser.email.equals(localUser.email, ignoreCase = true)
        preferredUser.username.isNotBlank() && localUser.username.isNotBlank() -> preferredUser.username.equals(localUser.username, ignoreCase = true)
        else -> false
    }
    if (!sameIdentity) return preferredUser
    val mergedName = localUser.fullName.ifBlank { preferredUser.fullName }
    return preferredUser.copy(
        fullName = mergedName,
        bio = localUser.bio.ifBlank { preferredUser.bio },
        avatarInitials = localUser.avatarInitials.ifBlank {
            preferredUser.avatarInitials.ifBlank { pocketAvatarInitialsFor(mergedName) }
        },
        customAvatarKey = localUser.customAvatarKey,
        customAvatarUri = localUser.customAvatarUri,
    )
}

data class PocketNativeAuthResponse(
    val ok: Boolean = false,
    val message: String = "",
    val user: PocketNativeUser? = null,
)

data class PocketNativeSummary(
    val totalBalance: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val budgetRemaining: Double = 0.0,
    val budgetAmount: Double = 0.0,
    val budgetSpent: Double = 0.0,
    val budgetUsedPercent: Int = 0,
    val goalSaved: Double = 0.0,
    val goalTarget: Double = 0.0,
    val goalProgressPercent: Int = 0,
    val accounts: Int = 0,
    val openingBalance: Double = 0.0,
)

data class PocketNativeAccount(
    val id: Int = 0,
    val name: String = "",
    val type: String = "CASH",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val isDefault: Boolean = false,
)

data class PocketNativeAnalyticsCategory(
    val key: String = "",
    val label: String = "",
    val colorHex: String = "#7C4DFF",
    val amount: Double = 0.0,
    val percent: Int = 0,
)

data class PocketNativeWeeklyPoint(
    val label: String = "",
    val amount: Double = 0.0,
)

data class PocketNativeAnalytics(
    val categories: List<PocketNativeAnalyticsCategory> = emptyList(),
    val weekly: List<PocketNativeWeeklyPoint> = emptyList(),
)

data class PocketNativeRecentActivity(
    val id: Int = 0,
    val kind: String = "",
    val label: String = "",
    val note: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val colorHex: String = "#7C4DFF",
)

data class PocketNativeDashboard(
    val currencyCode: String = "BDT",
    val currencySymbol: String = "৳",
    val summary: PocketNativeSummary = PocketNativeSummary(),
    val accounts: List<PocketNativeAccount> = emptyList(),
    val analytics: PocketNativeAnalytics = PocketNativeAnalytics(),
    val recentActivity: List<PocketNativeRecentActivity> = emptyList(),
    val reportsPdfUrl: String = "",
)

data class PocketNativeDashboardResponse(
    val ok: Boolean = false,
    val message: String = "",
    val user: PocketNativeUser? = null,
    val dashboard: PocketNativeDashboard? = null,
)

data class PocketNativeLoginRequest(
    val login_identifier: String,
    val password: String,
)

data class PocketNativeRegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val confirm_password: String,
    val ui_language: String,
    val currency_code: String,
)

data class PocketNativeGuestRequest(
    val ui_language: String,
    val currency_code: String,
)

data class PocketNativeRecordItem(
    val id: Int = 0,
    val kind: String = "",
    val mode: String = "",
    val label: String = "",
    val note: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val signedAmount: Double = 0.0,
    val colorHex: String = "#7C4DFF",
    val iconClass: String = "",
    val canDelete: Boolean = false,
    val detailUrl: String = "",
    val editUrl: String = "",
    val deleteUrl: String = "",
)

data class PocketNativeRecordGroup(
    val dateIso: String = "",
    val dateLabel: String = "",
    val weekdayLabel: String = "",
    val incomeTotal: Double = 0.0,
    val expenseTotal: Double = 0.0,
    val rows: List<PocketNativeRecordItem> = emptyList(),
)

data class PocketNativeRecordsSummary(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val lent: Double = 0.0,
    val balance: Double = 0.0,
    val records: Int = 0,
)

data class PocketNativeRecordsPayload(
    val monthValue: String = "",
    val monthLabel: String = "",
    val yearLabel: String = "",
    val currencyCode: String = "BDT",
    val currencySymbol: String = "৳",
    val summary: PocketNativeRecordsSummary = PocketNativeRecordsSummary(),
    val groups: List<PocketNativeRecordGroup> = emptyList(),
)

data class PocketNativeRecordsResponse(
    val ok: Boolean = false,
    val message: String = "",
    val user: PocketNativeUser? = null,
    @SerializedName("records")
    val payload: PocketNativeRecordsPayload? = null,
)

data class PocketNativeAccountsSummary(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val accounts: Int = 0,
    val openingBalance: Double = 0.0,
    val netChange: Double = 0.0,
)

data class PocketNativeAccountFilter(
    val key: String = "ALL",
    val label: String = "",
    val iconToken: String = "all",
)

data class PocketNativeAccountCard(
    val id: Int = 0,
    val name: String = "",
    val type: String = "CASH",
    val typeLabel: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val incomeTotal: Double = 0.0,
    val expenseTotal: Double = 0.0,
    val netChange: Double = 0.0,
    val isDefault: Boolean = false,
    val note: String = "",
    val accentHex: String = "#7C4DFF",
    val iconToken: String = "all",
    val statementUrl: String = "",
    val statementPdfUrl: String = "",
)

data class PocketNativeAccountsPayload(
    val currencyCode: String = "BDT",
    val currencySymbol: String = "৳",
    val activeFilter: String = "ALL",
    val filters: List<PocketNativeAccountFilter> = emptyList(),
    val summary: PocketNativeAccountsSummary = PocketNativeAccountsSummary(),
    val accounts: List<PocketNativeAccountCard> = emptyList(),
    val recentActivity: List<PocketNativeRecentActivity> = emptyList(),
    val statementPdfBaseUrl: String = "",
)

data class PocketNativeAccountsResponse(
    val ok: Boolean = false,
    val message: String = "",
    val user: PocketNativeUser? = null,
    @SerializedName("accounts")
    val payload: PocketNativeAccountsPayload? = null,
)

data class PocketNativeRecurringSummary(
    val totalTemplates: Int = 0,
    val activeTemplates: Int = 0,
    val incomeTemplates: Int = 0,
    val expenseTemplates: Int = 0,
    val nextDueTitle: String = "",
    val nextDueDate: String = "",
)

data class PocketNativeRecurringItem(
    val id: Int = 0,
    val kind: String = "",
    val title: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val dayOfMonth: Int = 1,
    val note: String = "",
    val isActive: Boolean = true,
    val sourceName: String = "",
    val lastGeneratedMonth: String = "",
    val nextDueDate: String = "",
    val dueInDays: Int = 0,
    val iconClass: String = "",
    val label: String = "",
)

data class PocketNativeRecurringPayload(
    val currencyCode: String = "BDT",
    val currencySymbol: String = "৳",
    val summary: PocketNativeRecurringSummary = PocketNativeRecurringSummary(),
    val templates: List<PocketNativeRecurringItem> = emptyList(),
)

data class PocketNativeRecurringResponse(
    val ok: Boolean = false,
    val message: String = "",
    val user: PocketNativeUser? = null,
    val payload: PocketNativeRecurringPayload? = null,
)

data class PocketNativeBudgetSummary(
    val totalBudget: Double = 0.0,
    val spent: Double = 0.0,
    val remaining: Double = 0.0,
    val usedPercent: Int = 0,
    val activeBudgets: Int = 0,
)

data class PocketNativeBudgetItem(
    val id: Int = 0,
    val name: String = "",
    val categoryKey: String = "all",
    val categoryLabel: String = "",
    val amount: Double = 0.0,
    val spent: Double = 0.0,
    val remaining: Double = 0.0,
    val progress: Double = 0.0,
    val periodType: String = "MONTHLY",
    val note: String = "",
    val accentHex: String = "#7C4DFF",
    val isActive: Boolean = false,
    val dueDayOfMonth: Int = 1,
)

data class PocketNativeBudgetPayload(
    val currencyCode: String = "BDT",
    val currencySymbol: String = "৳",
    val summary: PocketNativeBudgetSummary = PocketNativeBudgetSummary(),
    val budgets: List<PocketNativeBudgetItem> = emptyList(),
    val categoryOptions: List<PocketNativeTransactionCategoryOption> = emptyList(),
    val tipTitle: String = "",
    val tipBody: String = "",
)

data class PocketNativeBudgetResponse(
    val ok: Boolean = false,
    val message: String = "",
    val user: PocketNativeUser? = null,
    @SerializedName("budget")
    val payload: PocketNativeBudgetPayload? = null,
)

data class PocketNativeGoalsSummary(
    val totalProgressPercent: Int = 0,
    val savedAmount: Double = 0.0,
    val targetAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val goalsCount: Int = 0,
)

data class PocketNativeGoalHistoryItem(
    val id: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val note: String = "",
    val kind: String = "save",
)

data class PocketNativeGoalItem(
    val id: Int = 0,
    val name: String = "",
    val targetAmount: Double = 0.0,
    val savedAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val progress: Double = 0.0,
    val targetDate: String = "",
    val status: String = "ACTIVE",
    val deadlineState: String = "OPEN",
    val deadlineText: String = "",
    val note: String = "",
    val planFrequency: String = "MONTHLY",
    val planAmount: Double = 0.0,
    val autoReminderOn: Boolean = true,
    val history: List<PocketNativeGoalHistoryItem> = emptyList(),
    val accentStartHex: String = "#7C4DFF",
    val accentEndHex: String = "#3D8DFF",
)

data class PocketNativeGoalsPayload(
    val currencyCode: String = "BDT",
    val currencySymbol: String = "৳",
    val summary: PocketNativeGoalsSummary = PocketNativeGoalsSummary(),
    val goals: List<PocketNativeGoalItem> = emptyList(),
    val quickAddOptions: List<Double> = emptyList(),
    val motivationTitle: String = "",
    val motivationBody: String = "",
)

data class PocketNativeBudgetSaveRequest(
    val budgetName: String,
    val categoryKey: String,
    val amount: Double,
    val periodType: String,
    val startDate: String = "",
    val endDate: String = "",
    val note: String = "",
)

data class PocketNativeGoalSaveRequest(
    val goalName: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val targetDate: String = "",
    val note: String = "",
)

data class PocketNativeGoalSavedRequest(
    val amount: Double,
)

data class PocketNativeGoalsResponse(
    val ok: Boolean = false,
    val message: String = "",
    val user: PocketNativeUser? = null,
    @SerializedName("goals")
    val payload: PocketNativeGoalsPayload? = null,
)

data class PocketNativeTransactionAccountOption(
    val id: Int = 0,
    val name: String = "",
    val type: String = "CASH",
    val typeLabel: String = "",
    val currentBalance: Double = 0.0,
    val isDefault: Boolean = false,
    val accentHex: String = "#7C4DFF",
)

data class PocketNativeTransactionCategoryOption(
    val key: String = "",
    val label: String = "",
    val colorHex: String = "#7C4DFF",
    val iconClass: String = "",
    val iconKey: String = "",
)

data class PocketNativeTransactionPaymentMethod(
    val key: String = "CASH",
    val label: String = "",
)

data class PocketNativeTransactionFormPayload(
    val mode: String = "expense",
    val currencyCode: String = "BDT",
    val currencySymbol: String = "৳",
    val defaultEntryDate: String = "",
    val defaultAccountId: Int = 0,
    val defaultCategoryKey: String = "",
    val defaultPaymentMethod: String = "CASH",
    val title: String = "",
    val subtitle: String = "",
    val partyLabel: String = "",
    val submitLabel: String = "",
    val categorySettingsUrl: String = "",
    val categoryCreateUrl: String = "",
    val categoryEditBaseUrl: String = "",
    val accounts: List<PocketNativeTransactionAccountOption> = emptyList(),
    val categories: List<PocketNativeTransactionCategoryOption> = emptyList(),
    val paymentMethods: List<PocketNativeTransactionPaymentMethod> = emptyList(),
    val isEditMode: Boolean = false,
    val editEntryKind: String = "",
    val editEntryId: Int = 0,
    val initialAmount: Double = 0.0,
    val initialPartyName: String = "",
    val initialNote: String = "",
    val deleteUrl: String = "",
)

data class PocketNativeTransactionFormResponse(
    val ok: Boolean = false,
    val message: String = "",
    val user: PocketNativeUser? = null,
    @SerializedName("form")
    val payload: PocketNativeTransactionFormPayload? = null,
)

data class PocketNativeTransactionSaveRequest(
    val mode: String,
    val entryDate: String,
    val amount: Double,
    val accountId: Int,
    val categoryKey: String,
    val paymentMethod: String,
    val partyName: String,
    val note: String,
    val editKind: String = "",
    val editId: Int = 0,
)

data class PocketNativeTransactionSaveResponse(
    val ok: Boolean = false,
    val message: String = "",
)

data class PocketNativeRecordDeleteRequest(
    val kind: String,
    val id: Int,
)

data class PocketNativeCategoryIconOption(
    val key: String = "",
    val label: String = "",
    val iconClass: String = "",
)

data class PocketNativeCategoryManagerPayload(
    val currentKind: String = "expense",
    val categories: List<PocketNativeTransactionCategoryOption> = emptyList(),
    val iconOptions: List<PocketNativeCategoryIconOption> = emptyList(),
    val colorSwatches: List<String> = emptyList(),
)

data class PocketNativeCategoryManagerResponse(
    val ok: Boolean = false,
    val message: String = "",
    @SerializedName("categories")
    val payload: PocketNativeCategoryManagerPayload? = null,
)

data class PocketNativeCategorySaveRequest(
    val kind: String,
    val originalKey: String = "",
    val label: String,
    val iconKey: String,
    val colorHex: String,
)

data class PocketNativeCategoryDeleteRequest(
    val kind: String,
    val categoryKey: String,
)

data class PocketNativeCategoryMoveRequest(
    val kind: String,
    val categoryKey: String,
    val direction: String,
)
