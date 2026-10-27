package com.softx.app.data

import android.content.Context
import com.google.gson.Gson
import java.io.IOException
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import retrofit2.HttpException

data class PocketNativeBackupStats(
    val cacheFileCount: Int,
    val queuedTransactions: Int,
    val queuedBudgets: Int,
    val queuedGoals: Int,
    val reminderCount: Int,
)

data class PocketNativeBackupSnapshot(
    val version: Int = 1,
    val exportedAt: String,
    val preferences: PocketNativePreferenceSnapshot,
    val cacheFiles: Map<String, String>,
)

class PocketNativeRepository(
    private val context: Context,
    private val api: PocketNativeApiService = PocketNativeApiFactory.service(context),
) {
    private val gson = Gson()
    private val preferencesStore by lazy { PocketNativePreferencesStore(context) }
    private val cacheDir: File by lazy {
        File(context.filesDir, "pocket-cache").apply { mkdirs() }
    }

    private data class QueuedTransaction(
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
        val clientTransactionId: String = "",
        val dueDate: String = "",
    )

    private data class QueuedBudgetSave(
        val budgetName: String,
        val categoryKey: String,
        val amount: Double,
        val periodType: String,
        val startDate: String,
        val endDate: String,
        val note: String,
    )

    private data class QueuedGoalSave(
        val goalName: String,
        val targetAmount: Double,
        val savedAmount: Double,
        val targetDate: String,
        val note: String,
        val planFrequency: String = "MONTHLY",
        val planAmount: Double = 0.0,
        val autoReminderOn: Boolean = true,
    )

    private data class QueuedCategoryAction(
        val action: String,
        val kind: String,
        val originalKey: String = "",
        val label: String = "",
        val iconKey: String = "",
        val colorHex: String = "",
        val categoryKey: String = "",
        val direction: String = "",
    )

    private fun deriveEditUrl(
        mode: String,
        editKind: String,
        editId: Int,
    ): String {
        if (editId <= 0 || editKind.isBlank()) return ""
        val safeEditKind = normalizeEditKindForSave(editKind, mode)
        val safeMode = webCompatibleModeFor(mode)
        return "/pocket/transaction?mode=$safeMode&edit_kind=$safeEditKind&edit_id=$editId&next=%2Fpocket%2Frecords"
    }

    private fun normalizeEditKindForSave(
        editKind: String,
        mode: String,
    ): String {
        val cleanKind = editKind.trim().lowercase()
        if (cleanKind == "income" || cleanKind == "expense") {
            return cleanKind
        }
        return when (mode.trim().lowercase()) {
            "income", "borrow", "receive" -> "income"
            else -> "expense"
        }
    }

    private fun webCompatibleModeFor(mode: String): String = when (mode.trim().lowercase()) {
        "borrow", "receive" -> "income"
        "lent" -> "lent"
        else -> "expense"
    }

    private fun backendCategoryFor(
        mode: String,
        categoryKey: String,
    ): String = when (mode.trim().lowercase()) {
        "borrow" -> "loan_received"
        "receive" -> "due_collection"
        "lent" -> "loan_given"
        "repay" -> "loan_payment"
        else -> categoryKey.trim()
    }

    private fun userHasFullAccess(user: PocketNativeUser?): Boolean {
        val plan = user?.planCode?.trim().orEmpty()
        return user?.demoAccessEnabled == true || PocketNativePaidPlanCodes.contains(plan)
    }

    private fun preferredUser(responseUser: PocketNativeUser?): PocketNativeUser? {
        val cachedUser = preferencesStore.signedInUser()
        val preferred = when {
            userHasFullAccess(responseUser) -> responseUser
            userHasFullAccess(cachedUser) -> cachedUser
            responseUser != null -> responseUser
            else -> cachedUser
        }
        val localProfile = if (preferred === responseUser) cachedUser else responseUser
        return pocketMergeLocalProfile(preferred, localProfile)
    }

    private fun harmonizeDashboardUser(response: PocketNativeDashboardResponse): PocketNativeDashboardResponse {
        return response.copy(user = preferredUser(response.user))
    }

    private fun harmonizeAccountsUser(response: PocketNativeAccountsResponse): PocketNativeAccountsResponse {
        return response.copy(user = preferredUser(response.user))
    }

    private fun harmonizeRecurringUser(response: PocketNativeRecurringResponse): PocketNativeRecurringResponse {
        return response.copy(user = preferredUser(response.user))
    }

    private fun friendlyApiError(error: Throwable, fallback: String): String {
        if (error is HttpException) {
            val serverMessage = runCatching {
                val body = error.response()?.errorBody()?.string().orEmpty()
                val parsed = gson.fromJson(body, PocketNativeAuthResponse::class.java)
                parsed?.message.orEmpty()
            }.getOrDefault("")
            return serverMessage.ifBlank {
                when (error.code()) {
                    401 -> "Login failed. Username/Email/Password check করুন।"
                    404 -> "Pocket Pro service path was not found. Please update the app."
                    in 500..599 -> "Pocket Pro server is busy. Please try again."
                    else -> fallback
                }
            }
        }
        return error.message?.takeIf { it.isNotBlank() } ?: fallback
    }

    suspend fun login(loginIdentifier: String, password: String): Result<PocketNativeUser> = runCatching {
        try {
            val response = api.login(
                PocketNativeLoginRequest(
                    login_identifier = loginIdentifier.trim(),
                    password = password,
                )
            )
            if (!response.ok || response.user == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro login failed." })
            }
            response.user
        } catch (error: Exception) {
            throw IllegalStateException(friendlyApiError(error, "We could not sign you in. Please try again."))
        }
    }

    suspend fun register(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        uiLanguage: String,
        currencyCode: String,
    ): Result<PocketNativeUser> = runCatching {
        val cleanEmail = email.trim()
        val cleanUsername = username.trim()
        try {
            val response = api.register(
                PocketNativeRegisterRequest(
                    email = cleanEmail,
                    username = cleanUsername,
                    password = password,
                    confirm_password = confirmPassword,
                    ui_language = uiLanguage,
                    currency_code = currencyCode,
                )
            )
            if (!response.ok || response.user == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro account could not be created." })
            }
            response.user
        } catch (error: Exception) {
            if (error is UnknownHostException || error is SocketTimeoutException || error is ConnectException) {
                pocketLocalRegisteredUser(
                    email = cleanEmail,
                    username = cleanUsername,
                )
            } else {
                throw error
            }
        }
    }

    suspend fun quickStart(
        uiLanguage: String,
        currencyCode: String,
    ): Result<PocketNativeUser> = runCatching {
        val response = api.quickStart(
            PocketNativeGuestRequest(
                ui_language = uiLanguage,
                currency_code = currencyCode,
            )
        )
        if (!response.ok || response.user == null) {
            throw IllegalStateException(response.message.ifBlank { "Guest mode could not be started." })
        }
        response.user
    }

    suspend fun me(): Result<PocketNativeUser> = runCatching {
        val response = api.me()
        if (!response.ok || response.user == null) {
            throw IllegalStateException(response.message.ifBlank { "Not signed in." })
        }
        response.user
    }

    suspend fun dashboard(): Result<PocketNativeDashboardResponse> = runCatching {
        syncPendingTransactions()
        syncPendingBudgetSaves()
        syncPendingGoalSaves()
        try {
            val response = harmonizeDashboardUser(api.dashboard())
            if (!response.ok || response.dashboard == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro dashboard could not be loaded." })
            }
            writeCache("dashboard.json", response)
            response
        } catch (error: Exception) {
            readCache<PocketNativeDashboardResponse>("dashboard.json")?.let(::harmonizeDashboardUser)?.takeIf { it.ok && it.dashboard != null }
                ?: throw error
        }
    }

    fun cachedDashboard(): PocketNativeDashboardResponse? {
        return readCacheSafe<PocketNativeDashboardResponse>("dashboard.json")
            ?.let(::harmonizeDashboardUser)
            ?.takeIf { it.ok && it.dashboard != null }
    }

    suspend fun records(monthValue: String): Result<PocketNativeRecordsResponse> = runCatching {
        syncPendingTransactions()
        try {
            val response = api.records(monthValue)
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro records could not be loaded." })
            }
            writeCache("records-${monthValue.ifBlank { "current" }}.json", response)
            response
        } catch (error: Exception) {
            readCache<PocketNativeRecordsResponse>("records-${monthValue.ifBlank { "current" }}.json")?.takeIf { it.ok && it.payload != null }
                ?: throw error
        }
    }

    fun cachedRecords(monthValue: String): PocketNativeRecordsResponse? {
        val cacheName = "records-${monthValue.ifBlank { "current" }}.json"
        return readCacheSafe<PocketNativeRecordsResponse>(cacheName)
            ?.takeIf { it.ok && it.payload != null }
    }

    suspend fun accounts(): Result<PocketNativeAccountsResponse> = runCatching {
        syncPendingTransactions()
        try {
            val response = harmonizeAccountsUser(api.accounts())
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro accounts could not be loaded." })
            }
            writeCache("accounts.json", response)
            response
        } catch (error: Exception) {
            readCache<PocketNativeAccountsResponse>("accounts.json")?.let(::harmonizeAccountsUser)?.takeIf { it.ok && it.payload != null }
                ?: throw error
        }
    }

    fun cachedAccounts(): PocketNativeAccountsResponse? {
        return readCacheSafe<PocketNativeAccountsResponse>("accounts.json")
            ?.let(::harmonizeAccountsUser)
            ?.takeIf { it.ok && it.payload != null }
    }

    suspend fun recurring(): Result<PocketNativeRecurringResponse> = runCatching {
        syncPendingTransactions()
        try {
            val response = harmonizeRecurringUser(api.recurring())
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro recurring templates could not be loaded." })
            }
            writeCache("recurring.json", response)
            response
        } catch (error: Exception) {
            readCache<PocketNativeRecurringResponse>("recurring.json")?.let(::harmonizeRecurringUser)?.takeIf { it.ok && it.payload != null }
                ?: buildOfflineBootstrapRecurringResponse()
        }
    }

    suspend fun budget(): Result<PocketNativeBudgetResponse> = runCatching {
        syncPendingBudgetSaves()
        try {
            val response = api.budget()
            if (response.ok && response.payload != null) {
                val normalized = normalizeBudgetResponse(response)
                writeCache("budget.json", normalized)
                syncDashboardBudgetSummaryFromBudgetCache()
                return@runCatching normalized
            }
            readCache<PocketNativeBudgetResponse>("budget.json")
                ?.takeIf { it.ok && it.payload != null }
                ?.let(::normalizeBudgetResponse)
                ?: buildOfflineBootstrapBudgetResponse().copy(
                    message = response.message.ifBlank { "Pocket Pro loaded a local budget fallback." },
                )
        } catch (error: Exception) {
            readCache<PocketNativeBudgetResponse>("budget.json")
                ?.takeIf { it.ok && it.payload != null }
                ?.let(::normalizeBudgetResponse)
                ?: buildOfflineBootstrapBudgetResponse()
        }
    }

    fun cachedBudget(): PocketNativeBudgetResponse? {
        val cached = readCacheSafe<PocketNativeBudgetResponse>("budget.json")
            ?.takeIf { it.ok && it.payload != null }
            ?: return null
        val normalized = normalizeBudgetResponse(cached)
        if (normalized != cached) {
            writeCache("budget.json", normalized)
            syncDashboardBudgetSummaryFromBudgetCache()
        }
        return normalized
    }

    suspend fun saveBudget(
        budgetName: String,
        categoryKey: String,
        amount: Double,
        periodType: String,
        startDate: String,
        endDate: String,
        note: String,
    ): Result<PocketNativeBudgetResponse> = runCatching {
        val normalizedCategoryKey = categoryKey.trim()
        val existingBudgetForCategory = cachedBudget()
            ?.payload
            ?.budgets
            ?.firstOrNull { it.categoryKey.equals(normalizedCategoryKey, ignoreCase = true) }
        if (existingBudgetForCategory != null) {
            throw IllegalStateException("${existingBudgetForCategory.categoryLabel} budget already exists. Edit the existing one instead.")
        }
        val request = PocketNativeBudgetSaveRequest(
            budgetName = budgetName.trim(),
            categoryKey = normalizedCategoryKey,
            amount = amount,
            periodType = periodType,
            startDate = startDate,
            endDate = endDate,
            note = note.trim(),
        )
        try {
            val response = api.saveBudget(request)
            if (response.ok && response.payload != null) {
                writeCache("budget.json", response)
                syncDashboardBudgetSummaryFromBudgetCache()
                return@runCatching response
            }
            applyOfflineBudgetToCache(request)
            readCache<PocketNativeBudgetResponse>("budget.json")
                ?.copy(message = response.message.ifBlank { "Budget saved and synced into your local budget view." })
                ?: buildOfflineBootstrapBudgetResponse().copy(
                    message = response.message.ifBlank { "Budget saved and added to your local budget view." },
                )
        } catch (error: Exception) {
            queuePendingBudgetSave(
                QueuedBudgetSave(
                    budgetName = request.budgetName,
                    categoryKey = request.categoryKey,
                    amount = request.amount,
                    periodType = request.periodType,
                    startDate = request.startDate,
                    endDate = request.endDate,
                    note = request.note,
                )
            )
            applyOfflineBudgetToCache(request)
            readCache<PocketNativeBudgetResponse>("budget.json")
                ?.copy(message = "Saved locally. Pocket Pro will sync this budget when the server is ready.")
                ?: PocketNativeBudgetResponse(
                    ok = true,
                    message = "Saved locally. Pocket Pro will sync this budget when the server is ready.",
                )
        }
    }

    suspend fun goals(): Result<PocketNativeGoalsResponse> = runCatching {
        syncPendingGoalSaves()
        val cachedResponse = readCacheSafe<PocketNativeGoalsResponse>("goals.json")
        try {
            val response = api.goals()
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro goals could not be loaded." })
            }
            val mergedResponse = mergeGoalResponseWithCache(response, cachedResponse)
            writeCache("goals.json", mergedResponse)
            mergedResponse
        } catch (error: Exception) {
            readCache<PocketNativeGoalsResponse>("goals.json")?.takeIf { it.ok && it.payload != null }
                ?: buildOfflineBootstrapGoalsResponse()
        }
    }

    fun cachedGoals(): PocketNativeGoalsResponse? {
        return readCacheSafe<PocketNativeGoalsResponse>("goals.json")
            ?.takeIf { it.ok && it.payload != null }
    }

    suspend fun saveGoal(
        goalName: String,
        targetAmount: Double,
        savedAmount: Double,
        targetDate: String,
        note: String,
        planFrequency: String = "MONTHLY",
        planAmount: Double = 0.0,
        autoReminderOn: Boolean = true,
    ): Result<PocketNativeGoalsResponse> = runCatching {
        val request = PocketNativeGoalSaveRequest(
            goalName = goalName.trim(),
            targetAmount = targetAmount,
            savedAmount = savedAmount,
            targetDate = targetDate,
            note = note.trim(),
        )
        try {
            val response = api.saveGoal(request)
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro goal could not be saved." })
            }
            val merged = mergeGoalPlannerState(
                response = response,
                goalName = request.goalName,
                targetAmount = request.targetAmount,
                savedAmount = request.savedAmount,
                targetDate = request.targetDate,
                note = request.note,
                planFrequency = planFrequency,
                planAmount = planAmount,
                autoReminderOn = autoReminderOn,
            )
            writeCache("goals.json", merged)
            merged
        } catch (error: Exception) {
            if (error is IOException || error.cause is IOException) {
                queuePendingGoalSave(
                    QueuedGoalSave(
                        goalName = request.goalName,
                        targetAmount = request.targetAmount,
                        savedAmount = request.savedAmount,
                        targetDate = request.targetDate,
                        note = request.note,
                        planFrequency = planFrequency,
                        planAmount = planAmount,
                        autoReminderOn = autoReminderOn,
                    )
                )
                applyOfflineGoalToCache(request, planFrequency, planAmount, autoReminderOn)
                readCache<PocketNativeGoalsResponse>("goals.json")
                    ?: PocketNativeGoalsResponse(
                        ok = true,
                        message = "Saved offline. Pocket Pro will sync this goal when internet is back.",
                    )
            } else {
                throw error
            }
        }
    }

    suspend fun categories(kind: String): Result<PocketNativeCategoryManagerResponse> = runCatching {
        syncPendingCategoryActions()
        val normalizedKind = categoryManagerKind(kind)
        try {
            val response = api.categories(normalizedKind)
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro categories could not be loaded." })
            }
            writeCache("categories-$normalizedKind.json", response)
            response
        } catch (error: Exception) {
            readCache<PocketNativeCategoryManagerResponse>("categories-$normalizedKind.json")?.takeIf { it.ok && it.payload != null }
                ?: buildOfflineCategoryManagerResponse(normalizedKind)
        }
    }

    suspend fun saveCategory(
        kind: String,
        originalKey: String,
        label: String,
        iconKey: String,
        colorHex: String,
    ): Result<PocketNativeCategoryManagerResponse> = runCatching {
        val normalizedKind = categoryManagerKind(kind)
        val request = PocketNativeCategorySaveRequest(
            kind = normalizedKind,
            originalKey = originalKey,
            label = label.trim(),
            iconKey = iconKey,
            colorHex = colorHex,
        )
        try {
            val response = api.saveCategory(request)
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro category could not be saved." })
            }
            writeCache("categories-$normalizedKind.json", response)
            syncCategoryOptionsIntoTransactionForms(normalizedKind, response.payload.categories)
            response
        } catch (error: Exception) {
            queuePendingCategoryAction(
                QueuedCategoryAction(
                    action = "save",
                    kind = normalizedKind,
                    originalKey = originalKey,
                    label = request.label,
                    iconKey = request.iconKey,
                    colorHex = request.colorHex,
                ),
            )
            ensureCategoryCache(normalizedKind)
            applyOfflineCategorySaveToCache(
                kind = normalizedKind,
                originalKey = originalKey,
                label = request.label,
                iconKey = request.iconKey,
                colorHex = request.colorHex,
            )
            readCache<PocketNativeCategoryManagerResponse>("categories-$normalizedKind.json")
                ?: PocketNativeCategoryManagerResponse(
                    ok = true,
                    message = "Saved locally. Pocket Pro will sync this category when the server is ready.",
                    payload = PocketNativeCategoryManagerPayload(
                        currentKind = normalizedKind,
                        categories = listOf(
                            PocketNativeTransactionCategoryOption(
                                key = normalizedOfflineCategoryKey(originalKey, request.label),
                                label = request.label,
                                colorHex = request.colorHex,
                                iconClass = "",
                                iconKey = request.iconKey,
                            )
                        ),
                    ),
                )
        }
    }

    suspend fun deleteCategory(kind: String, categoryKey: String): Result<PocketNativeCategoryManagerResponse> = runCatching {
        val normalizedKind = categoryManagerKind(kind)
        try {
            val response = api.deleteCategory(
                PocketNativeCategoryDeleteRequest(
                    kind = normalizedKind,
                    categoryKey = categoryKey,
                )
            )
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro category could not be deleted." })
            }
            writeCache("categories-$normalizedKind.json", response)
            syncCategoryOptionsIntoTransactionForms(normalizedKind, response.payload.categories)
            response
        } catch (error: Exception) {
            queuePendingCategoryAction(
                QueuedCategoryAction(
                    action = "delete",
                    kind = normalizedKind,
                    categoryKey = categoryKey,
                ),
            )
            ensureCategoryCache(normalizedKind)
            applyOfflineCategoryDeleteToCache(normalizedKind, categoryKey)
            readCache<PocketNativeCategoryManagerResponse>("categories-$normalizedKind.json")
                ?: PocketNativeCategoryManagerResponse(
                    ok = true,
                    message = "Saved offline. Pocket Pro will sync this category change when internet is back.",
                    payload = PocketNativeCategoryManagerPayload(currentKind = normalizedKind),
                )
        }
    }

    suspend fun moveCategory(
        kind: String,
        categoryKey: String,
        direction: String,
    ): Result<PocketNativeCategoryManagerResponse> = runCatching {
        val normalizedKind = categoryManagerKind(kind)
        try {
            val response = api.moveCategory(
                PocketNativeCategoryMoveRequest(
                    kind = normalizedKind,
                    categoryKey = categoryKey,
                    direction = direction,
                )
            )
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro category order could not be updated." })
            }
            writeCache("categories-$normalizedKind.json", response)
            syncCategoryOptionsIntoTransactionForms(normalizedKind, response.payload.categories)
            response
        } catch (error: Exception) {
            queuePendingCategoryAction(
                QueuedCategoryAction(
                    action = "move",
                    kind = normalizedKind,
                    categoryKey = categoryKey,
                    direction = direction,
                ),
            )
            ensureCategoryCache(normalizedKind)
            applyOfflineCategoryMoveToCache(normalizedKind, categoryKey, direction)
            readCache<PocketNativeCategoryManagerResponse>("categories-$normalizedKind.json")
                ?: PocketNativeCategoryManagerResponse(
                    ok = true,
                    message = "Saved offline. Pocket Pro will sync this category order when internet is back.",
                    payload = PocketNativeCategoryManagerPayload(currentKind = normalizedKind),
                )
        }
    }

    suspend fun addGoalSaved(goalId: Int, amount: Double, note: String = ""): Result<PocketNativeGoalsResponse> = runCatching {
        try {
            val response = api.addGoalSaved(
                goalId = goalId,
                request = PocketNativeGoalSavedRequest(amount = amount),
            )
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Goal progress could not be updated." })
            }
            val merged = applyGoalSavedToCache(response, goalId, amount, note)
            writeCache("goals.json", merged)
            merged
        } catch (error: Exception) {
            if (error is IOException || error.cause is IOException) {
                val current = readCacheSafe<PocketNativeGoalsResponse>("goals.json") ?: buildOfflineBootstrapGoalsResponse()
                val merged = applyGoalSavedToCache(current, goalId, amount, note)
                writeCache("goals.json", merged)
                merged
            } else {
                throw error
            }
        }
    }

    fun updateGoalLocally(
        goalId: Int,
        goalName: String,
        targetAmount: Double,
        savedAmount: Double,
        targetDate: String,
        note: String,
        planFrequency: String = "MONTHLY",
        planAmount: Double = 0.0,
        autoReminderOn: Boolean = true,
    ): Result<PocketNativeGoalsResponse> = runCatching {
        val current = readCacheSafe<PocketNativeGoalsResponse>("goals.json") ?: buildOfflineBootstrapGoalsResponse()
        val payload = current.payload ?: buildOfflineBootstrapGoalsResponse().payload ?: PocketNativeGoalsPayload()
        val updatedGoals = payload.goals.map { item ->
            if (item.id == goalId) {
                val remainingAmount = (targetAmount - savedAmount).coerceAtLeast(0.0)
                val progress = if (targetAmount > 0.0) ((savedAmount / targetAmount) * 100.0).coerceIn(0.0, 100.0) else 0.0
                item.copy(
                    name = goalName.trim(),
                    targetAmount = targetAmount,
                    savedAmount = savedAmount.coerceAtLeast(0.0),
                    remainingAmount = remainingAmount,
                    progress = progress,
                    targetDate = targetDate.trim(),
                    deadlineText = targetDate.trim().ifBlank { item.deadlineText },
                    note = note.trim(),
                    planFrequency = planFrequency,
                    planAmount = planAmount.coerceAtLeast(0.0),
                    autoReminderOn = autoReminderOn,
                )
            } else {
                item
            }
        }
        val updatedSummary = goalsSummaryFromItems(updatedGoals)
        val updated = current.copy(
            ok = true,
            message = "Goal updated locally.",
            payload = payload.copy(
                summary = updatedSummary,
                goals = updatedGoals,
            ),
        )
        writeCache("goals.json", updated)
        updated
    }

    fun deleteGoalLocally(goalId: Int): Result<PocketNativeGoalsResponse> = runCatching {
        val current = readCacheSafe<PocketNativeGoalsResponse>("goals.json") ?: buildOfflineBootstrapGoalsResponse()
        val payload = current.payload ?: buildOfflineBootstrapGoalsResponse().payload ?: PocketNativeGoalsPayload()
        val updatedGoals = payload.goals.filterNot { it.id == goalId }
        val updated = current.copy(
            ok = true,
            message = "Goal deleted locally.",
            payload = payload.copy(
                summary = goalsSummaryFromItems(updatedGoals),
                goals = updatedGoals,
            ),
        )
        writeCache("goals.json", updated)
        updated
    }

    suspend fun transactionForm(
        mode: String,
        editKind: String = "",
        editId: Int = 0,
    ): Result<PocketNativeTransactionFormResponse> = runCatching {
        val normalizedMode = mode.trim().lowercase()
        try {
            val response = api.transactionForm(normalizedMode, editKind, editId)
            if (!response.ok || response.payload == null) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro transaction form could not be loaded." })
            }
            writeCache("transaction-form-$normalizedMode.json", response)
            response
        } catch (error: Exception) {
            if (editId > 0) {
                throw error
            }
            readCache<PocketNativeTransactionFormResponse>("transaction-form-$normalizedMode.json")?.takeIf { it.ok && it.payload != null }
                ?: buildOfflineBootstrapTransactionForm(normalizedMode)
        }
    }

    suspend fun saveTransaction(
        mode: String,
        entryDate: String,
        amount: Double,
        accountId: Int,
        categoryKey: String,
        paymentMethod: String,
        partyName: String,
        note: String,
        editKind: String = "",
        editId: Int = 0,
        editUrl: String = "",
        dueDate: String = "",
    ): Result<PocketNativeTransactionSaveResponse> = runCatching {
        val normalizedMode = mode.trim()
        val normalizedAmount = BigDecimal.valueOf(amount)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
        val request = PocketNativeTransactionSaveRequest(
            mode = normalizedMode,
            entryDate = entryDate,
            amount = normalizedAmount,
            accountId = accountId,
            categoryKey = backendCategoryFor(normalizedMode, categoryKey),
            paymentMethod = paymentMethod,
            partyName = partyName.trim(),
            note = note.trim(),
            editKind = normalizeEditKindForSave(editKind, normalizedMode),
            editId = editId,
            clientTransactionId = UUID.randomUUID().toString(),
            dueDate = dueDate.trim(),
        )
        try {
            val displayMode = request.mode.trim().lowercase()
            val resolvedEditUrl = editUrl.ifBlank {
                deriveEditUrl(
                    mode = displayMode,
                    editKind = request.editKind,
                    editId = request.editId,
                )
            }
            val canUseWebEditFallback = request.editId > 0 &&
                resolvedEditUrl.isNotBlank() &&
                displayMode in setOf("income", "expense", "lent", "borrow", "receive", "repay")
            if (canUseWebEditFallback) {
                val fields = linkedMapOf(
                    "mode" to webCompatibleModeFor(displayMode),
                    "entry_date" to request.entryDate,
                    "amount" to request.amount.toString(),
                    "account_id" to request.accountId.toString(),
                    "category" to request.categoryKey,
                    "payment_method" to request.paymentMethod,
                    "source_name" to request.partyName,
                    "note" to request.note,
                    "edit_kind" to request.editKind,
                    "edit_id" to request.editId.toString(),
                    "next" to "/pocket/records",
                )
                val fallbackResponse = api.saveTransactionByUrl(resolvedEditUrl, fields)
                if (!fallbackResponse.isSuccessful) {
                    throw IllegalStateException("Pocket Pro transaction could not be updated.")
                }
                return@runCatching PocketNativeTransactionSaveResponse(
                    ok = true,
                    message = "Record updated.",
                )
            }
            val response = api.saveTransaction(
                request.copy(
                    mode = displayMode,
                )
            )
            if (!response.ok) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro transaction could not be saved." })
            }
            if (request.editId > 0) {
                val updateMessage = when (displayMode) {
                    "income" -> "Income updated."
                    "expense" -> "Expense updated."
                    "lent" -> "Loan updated."
                    "borrow" -> "Loan updated."
                    "receive" -> "Received money updated."
                    "repay" -> "Payment updated."
                    else -> "Record updated."
                }
                response.copy(
                    message = updateMessage,
                )
            } else {
                response.copy(
                    message = when (displayMode) {
                        "borrow" -> "Loan added."
                        "receive" -> "Money received."
                        "lent" -> "Loan given."
                        "repay" -> "Payment saved."
                        else -> response.message
                    },
                ).also {
                    applyOfflineTransactionToCaches(request)
                    preferencesStore.markMoneyEntryLogged()
                }
            }
        } catch (error: Exception) {
            if (error is IOException || error.cause is IOException) {
                queuePendingTransaction(
                    QueuedTransaction(
                        mode = request.mode,
                        entryDate = request.entryDate,
                        amount = request.amount,
                        accountId = request.accountId,
                        categoryKey = request.categoryKey,
                        paymentMethod = request.paymentMethod,
                        partyName = request.partyName,
                        note = request.note,
                        editKind = request.editKind,
                        editId = request.editId,
                        clientTransactionId = request.clientTransactionId,
                        dueDate = request.dueDate,
                    )
                )
                applyOfflineTransactionToCaches(request)
                PocketNativeTransactionSaveResponse(
                    ok = true,
                    message = "Saved offline. Pocket Pro will sync this entry when internet is back.",
                ).also {
                    if (request.editId <= 0) {
                        preferencesStore.markMoneyEntryLogged()
                    }
                }
            } else {
                throw error
            }
        }
    }

    suspend fun deleteRecord(
        kind: String,
        id: Int,
        deleteUrl: String = "",
    ): Result<PocketNativeTransactionSaveResponse> = runCatching {
        try {
            val response = api.deleteRecord(
                PocketNativeRecordDeleteRequest(
                    kind = kind.trim(),
                    id = id,
                ),
            )
            if (!response.ok) {
                throw IllegalStateException(response.message.ifBlank { "Pocket Pro record could not be deleted." })
            }
            response
        } catch (error: Exception) {
            val normalizedKind = kind.trim().lowercase()
            val fallbackDeleteUrl = when {
                deleteUrl.isNotBlank() -> deleteUrl
                id > 0 && normalizedKind == "income" -> "/incomes/$id/delete"
                id > 0 && normalizedKind == "expense" -> "/expenses/$id/delete"
                else -> ""
            }
            if (fallbackDeleteUrl.isBlank()) {
                throw error
            }
            val fallback = api.deleteRecordByUrl(fallbackDeleteUrl)
            if (!fallback.isSuccessful) {
                throw IllegalStateException("Pocket Pro record could not be deleted.")
            }
            PocketNativeTransactionSaveResponse(
                ok = true,
                message = "Record deleted.",
            )
        }
    }

    suspend fun downloadPdfToCache(
        url: String,
        fileName: String,
    ): Result<File> = runCatching {
        val responseBody = api.downloadPdf(url)
        val exportDir = File(context.filesDir, "pocket-cache/exports").apply { mkdirs() }
        val targetFile = File(exportDir, fileName)
        targetFile.outputStream().use { output ->
            responseBody.byteStream().use { input ->
                input.copyTo(output)
            }
        }
        targetFile
    }

    suspend fun logout(): Result<Unit> = runCatching {
        api.logout()
        PocketNativeApiFactory.clearSession()
    }

    fun clearLocalSession() {
        PocketNativeApiFactory.clearSession()
    }

    fun clearLocalCache(): Result<String> = runCatching {
        var removed = 0
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "json" && file.delete()) {
                removed += 1
            }
        }
        "Cleared $removed cached Pocket Pro files."
    }

    fun clearOfflineQueues(): Result<String> = runCatching {
        val queueFiles = listOf(
            pendingTransactionsFile(),
            pendingBudgetFile(),
            pendingGoalsFile(),
            pendingCategoryActionsFile(),
        )
        var removed = 0
        queueFiles.forEach { file ->
            if (file.exists() && file.delete()) {
                removed += 1
            }
        }
        "Cleared $removed offline queue files."
    }

    private fun pendingTransactionsFile(): File = File(cacheDir, "pending-transactions.json")
    private fun pendingBudgetFile(): File = File(cacheDir, "pending-budgets.json")
    private fun pendingGoalsFile(): File = File(cacheDir, "pending-goals.json")
    private fun pendingCategoryActionsFile(): File = File(cacheDir, "pending-category-actions.json")

    private fun queuePendingTransaction(item: QueuedTransaction) {
        val current = readPendingTransactions().toMutableList()
        current.add(item)
        pendingTransactionsFile().writeText(gson.toJson(current))
    }

    private fun readPendingTransactions(): List<QueuedTransaction> {
        return runCatching {
            val file = pendingTransactionsFile()
            if (!file.exists()) {
                emptyList()
            } else {
                gson.fromJson(file.readText(), Array<QueuedTransaction>::class.java)?.toList() ?: emptyList()
            }
        }.getOrElse { emptyList() }
    }

    private fun queuePendingBudgetSave(item: QueuedBudgetSave) {
        val current = readPendingBudgetSaves().toMutableList()
        current.add(item)
        pendingBudgetFile().writeText(gson.toJson(current))
    }

    private fun readPendingBudgetSaves(): List<QueuedBudgetSave> {
        return runCatching {
            val file = pendingBudgetFile()
            if (!file.exists()) {
                emptyList()
            } else {
                gson.fromJson(file.readText(), Array<QueuedBudgetSave>::class.java)?.toList() ?: emptyList()
            }
        }.getOrElse { emptyList() }
    }

    private fun queuePendingGoalSave(item: QueuedGoalSave) {
        val current = readPendingGoalSaves().toMutableList()
        current.add(item)
        pendingGoalsFile().writeText(gson.toJson(current))
    }

    private fun readPendingGoalSaves(): List<QueuedGoalSave> {
        return runCatching {
            val file = pendingGoalsFile()
            if (!file.exists()) {
                emptyList()
            } else {
                gson.fromJson(file.readText(), Array<QueuedGoalSave>::class.java)?.toList() ?: emptyList()
            }
        }.getOrElse { emptyList() }
    }

    private fun queuePendingCategoryAction(item: QueuedCategoryAction) {
        val current = readPendingCategoryActions().toMutableList()
        current.add(item)
        pendingCategoryActionsFile().writeText(gson.toJson(current))
    }

    private fun readPendingCategoryActions(): List<QueuedCategoryAction> {
        return runCatching {
            val file = pendingCategoryActionsFile()
            if (!file.exists()) {
                emptyList()
            } else {
                gson.fromJson(file.readText(), Array<QueuedCategoryAction>::class.java)?.toList() ?: emptyList()
            }
        }.getOrElse { emptyList() }
    }

    private fun isIncomeLikeMode(mode: String): Boolean {
        return mode.trim().lowercase() in setOf("income", "borrow", "receive")
    }

    private fun modeLabel(mode: String): String = when (mode.trim().lowercase()) {
        "income" -> "Income"
        "expense" -> "Expense"
        "lent" -> "Give Loan"
        "borrow" -> "Take Loan"
        "receive" -> "Get Back"
        "repay" -> "Pay Back"
        else -> "Record"
    }

    private data class OfflineCategoryMeta(
        val label: String,
        val colorHex: String,
        val iconClass: String,
    )

    private inline fun <reified T> readCacheSafe(name: String): T? = readCache(name)

    private fun offlineCategoryMeta(mode: String, categoryKey: String): OfflineCategoryMeta {
        val normalizedMode = mode.trim().lowercase()
        val kind = if (isIncomeLikeMode(normalizedMode)) "income" else "expense"
        val specialLoanMeta = when (normalizedMode) {
            "lent" -> OfflineCategoryMeta("Give Loan", "#f3aa3c", "fa-solid fa-hand-holding-dollar")
            "borrow" -> OfflineCategoryMeta("Take Loan", "#22c55e", "fa-solid fa-money-bill-transfer")
            "receive" -> OfflineCategoryMeta("Get Back", "#14b8a6", "fa-solid fa-money-bill-trend-up")
            "repay" -> OfflineCategoryMeta("Pay Back", "#ff6a59", "fa-solid fa-money-bill-wave")
            else -> null
        }
        if (specialLoanMeta != null) return specialLoanMeta

        val categoryCache = readCacheSafe<PocketNativeCategoryManagerResponse>("categories-$kind.json")?.payload?.categories.orEmpty()
        val formCache = readCacheSafe<PocketNativeTransactionFormResponse>("transaction-form-$normalizedMode.json")?.payload?.categories.orEmpty()
        val option = (categoryCache + formCache).firstOrNull { it.key.equals(categoryKey, ignoreCase = true) }
        return OfflineCategoryMeta(
            label = option?.label?.takeIf { it.isNotBlank() } ?: modeLabel(normalizedMode),
            colorHex = option?.colorHex?.takeIf { it.isNotBlank() } ?: if (isIncomeLikeMode(normalizedMode)) "#4d8eff" else "#ff6a59",
            iconClass = option?.iconClass?.takeIf { it.isNotBlank() } ?: if (isIncomeLikeMode(normalizedMode)) "fa-solid fa-arrow-trend-up" else "fa-solid fa-arrow-trend-down",
        )
    }

    private fun buildOfflineBootstrapTransactionForm(mode: String): PocketNativeTransactionFormResponse {
        val today = LocalDate.now().toString()
        val normalizedMode = mode.trim().lowercase()
        val accounts = offlineBootstrapAccounts()
        val categories = offlineBootstrapCategories(normalizedMode)
        val defaultCategory = when (normalizedMode) {
            "income" -> "salary"
            "expense" -> "food"
            "lent" -> "loan_given"
            "borrow" -> "loan_received"
            "receive" -> "due_collection"
            "repay" -> "loan_payment"
            else -> categories.firstOrNull()?.key.orEmpty()
        }
        val defaultPaymentMethod = "CASH"
        val payload = PocketNativeTransactionFormPayload(
            mode = normalizedMode,
            currencyCode = preferencesStore.currencyCode(),
            currencySymbol = offlineCurrencySymbol(preferencesStore.currencyCode()),
            defaultEntryDate = today,
            defaultAccountId = accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id ?: 0,
            defaultCategoryKey = defaultCategory,
            defaultPaymentMethod = defaultPaymentMethod,
            title = offlineFormTitle(normalizedMode),
            subtitle = offlineFormSubtitle(normalizedMode),
            partyLabel = offlinePartyLabel(normalizedMode),
            submitLabel = offlineSubmitLabel(normalizedMode),
            categorySettingsUrl = "",
            categoryCreateUrl = "",
            categoryEditBaseUrl = "",
            accounts = accounts,
            categories = categories,
            paymentMethods = offlineBootstrapPaymentMethods(),
            isEditMode = false,
            editEntryKind = "",
            editEntryId = 0,
            initialAmount = 0.0,
            initialPartyName = "",
            initialNote = "",
            deleteUrl = "",
        )
        val response = PocketNativeTransactionFormResponse(
            ok = true,
            message = "Offline form ready.",
            user = preferredUser(null),
            payload = payload,
        )
        writeCache("transaction-form-$normalizedMode.json", response)
        return response
    }

    private fun offlineBootstrapAccounts(): List<PocketNativeTransactionAccountOption> {
        val formAccounts = listOf(
            "income",
            "expense",
            "lent",
            "borrow",
            "receive",
            "repay",
        ).flatMap { cacheName ->
            readCacheSafe<PocketNativeTransactionFormResponse>("transaction-form-$cacheName.json")
                ?.payload
                ?.accounts
                .orEmpty()
        }
        val accountCards = readCacheSafe<PocketNativeAccountsResponse>("accounts.json")
            ?.payload
            ?.accounts
            .orEmpty()
            .map {
                PocketNativeTransactionAccountOption(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    typeLabel = it.typeLabel,
                    currentBalance = it.currentBalance,
                    isDefault = it.isDefault,
                    accentHex = it.accentHex,
                )
            }
        val dashboardAccounts = readCacheSafe<PocketNativeDashboardResponse>("dashboard.json")
            ?.dashboard
            ?.accounts
            .orEmpty()
            .map {
                PocketNativeTransactionAccountOption(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    typeLabel = it.type.replaceFirstChar { char -> char.uppercase() },
                    currentBalance = it.currentBalance,
                    isDefault = it.isDefault,
                    accentHex = "#7C4DFF",
                )
            }
        val combined = (formAccounts + accountCards + dashboardAccounts)
            .filter { it.id > 0 && it.name.isNotBlank() }
            .distinctBy { it.id }
        if (combined.isNotEmpty()) {
            return combined
        }
        return listOf(
            PocketNativeTransactionAccountOption(
                id = -1,
                name = "Cash Wallet",
                type = "CASH",
                typeLabel = "Cash",
                currentBalance = 0.0,
                isDefault = true,
                accentHex = "#7C4DFF",
            )
        )
    }

    private fun offlineBootstrapCategories(mode: String): List<PocketNativeTransactionCategoryOption> {
        val cached = readCacheSafe<PocketNativeTransactionFormResponse>("transaction-form-$mode.json")
            ?.payload
            ?.categories
            .orEmpty()
        if (cached.isNotEmpty()) {
            return cached
        }
        return when (mode) {
            "income" -> listOf(
                offlineCategory("salary", "Salary", "#4d8eff", "wallet"),
                offlineCategory("business_sales", "Business Sales", "#22c55e", "briefcase"),
                offlineCategory("service_income", "Service Income", "#06b6d4", "badge"),
                offlineCategory("other_income", "Other Income", "#8b5cf6", "sparkles"),
            )
            "expense" -> listOf(
                offlineCategory("food", "Food", "#ff8a4c", "utensils"),
                offlineCategory("transport", "Transport", "#38bdf8", "car"),
                offlineCategory("shopping", "Shopping", "#f472b6", "shopping_bag"),
                offlineCategory("bills", "Bills", "#f59e0b", "receipt"),
            )
            "lent" -> listOf(offlineCategory("loan_given", "Loan Given", "#f3aa3c", "hand_coins"))
            "borrow" -> listOf(offlineCategory("loan_received", "Loan Received", "#22c55e", "handshake"))
            "receive" -> listOf(offlineCategory("due_collection", "Due Collection", "#14b8a6", "coins"))
            "repay" -> listOf(offlineCategory("loan_payment", "Loan Payment", "#ff6a59", "banknote_arrow_down"))
            else -> listOf(offlineCategory("other", "Other", "#7C4DFF", "shapes"))
        }
    }

    private fun offlineCategory(
        key: String,
        label: String,
        colorHex: String,
        iconKey: String,
    ): PocketNativeTransactionCategoryOption = PocketNativeTransactionCategoryOption(
        key = key,
        label = label,
        colorHex = colorHex,
        iconClass = "",
        iconKey = iconKey,
    )

    private fun categoryManagerKind(kind: String): String {
        return when (kind.trim().lowercase()) {
            "income" -> "income"
            else -> "expense"
        }
    }

    private fun buildOfflineCategoryManagerResponse(kind: String): PocketNativeCategoryManagerResponse {
        val normalizedKind = categoryManagerKind(kind)
        val response = PocketNativeCategoryManagerResponse(
            ok = true,
            message = "Pocket Pro loaded local categories.",
            payload = PocketNativeCategoryManagerPayload(
                currentKind = normalizedKind,
                categories = offlineBootstrapCategories(normalizedKind),
                iconOptions = emptyList(),
                colorSwatches = listOf(
                    "#58A6FF",
                    "#22c55e",
                    "#06b6d4",
                    "#f59e0b",
                    "#f472b6",
                    "#ff6a59",
                    "#8b5cf6",
                ),
            ),
        )
        writeCache("categories-$normalizedKind.json", response)
        return response
    }

    private fun ensureCategoryCache(kind: String) {
        val normalizedKind = categoryManagerKind(kind)
        val cached = readCacheSafe<PocketNativeCategoryManagerResponse>("categories-$normalizedKind.json")
        if (cached?.payload != null) return
        buildOfflineCategoryManagerResponse(normalizedKind)
    }

    private fun offlineBootstrapPaymentMethods(): List<PocketNativeTransactionPaymentMethod> = listOf(
        PocketNativeTransactionPaymentMethod(key = "CASH", label = "Cash"),
        PocketNativeTransactionPaymentMethod(key = "BANK", label = "Bank"),
        PocketNativeTransactionPaymentMethod(key = "MOBILE", label = "Mobile Banking"),
    )

    private fun offlineFormTitle(mode: String): String = when (mode) {
        "income" -> "Income"
        "expense" -> "Expense"
        "lent" -> "Give Loan"
        "borrow" -> "Take Loan"
        "receive" -> "Get Back"
        "repay" -> "Pay Back"
        else -> "Add Record"
    }

    private fun offlineFormSubtitle(mode: String): String = when (mode) {
        "income" -> "Add money you received."
        "expense" -> "Add money you spent."
        "lent" -> "Track money you gave to someone."
        "borrow" -> "Track money you took from someone."
        "receive" -> "Track money you received back."
        "repay" -> "Track money you paid back."
        else -> "Add a finance record."
    }

    private fun offlinePartyLabel(mode: String): String = when (mode) {
        "lent" -> "Given to"
        "borrow" -> "Taken from"
        "receive" -> "Received from"
        "repay" -> "Paid to"
        else -> "Person"
    }

    private fun offlineSubmitLabel(mode: String): String = when (mode) {
        "income" -> "Save Income"
        "expense" -> "Save Expense"
        "lent" -> "Save Loan"
        "borrow" -> "Save Loan"
        "receive" -> "Save Get Back"
        "repay" -> "Save Pay Back"
        else -> "Save"
    }

    private fun offlineCurrencySymbol(currencyCode: String): String = when (currencyCode.trim().uppercase()) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "INR" -> "₹"
        "AED" -> "د.إ"
        else -> "৳"
    }

    private fun buildOfflineBootstrapRecurringResponse(): PocketNativeRecurringResponse {
        val currencyCode = preferencesStore.currencyCode()
        val currencySymbol = offlineCurrencySymbol(currencyCode)
        val reminderTemplates = preferencesStore.monthlyReminders().mapIndexed { index, reminder ->
            PocketNativeRecurringItem(
                id = -(index + 1),
                kind = "expense",
                title = reminder.title.ifBlank { "Monthly Reminder" },
                category = "bills",
                amount = 0.0,
                paymentMethod = "CASH",
                dayOfMonth = reminder.dayOfMonth,
                note = reminder.note,
                isActive = true,
                sourceName = "local",
                lastGeneratedMonth = "",
                nextDueDate = nextRecurringDueDate(reminder.dayOfMonth),
                dueInDays = recurringDueInDays(nextRecurringDueDate(reminder.dayOfMonth)),
                iconClass = "",
                label = "Expense",
            )
        }
        val fallbackTemplates = if (reminderTemplates.isNotEmpty()) {
            reminderTemplates
        } else {
            listOf(
                PocketNativeRecurringItem(
                    id = -101,
                    kind = "income",
                    title = "Salary Preview",
                    category = "salary",
                    amount = 0.0,
                    paymentMethod = "BANK",
                    dayOfMonth = 1,
                    note = "Recurring salary example",
                    isActive = true,
                    sourceName = "local",
                    lastGeneratedMonth = "",
                    nextDueDate = nextRecurringDueDate(1),
                    dueInDays = recurringDueInDays(nextRecurringDueDate(1)),
                    iconClass = "",
                    label = "Income",
                ),
                PocketNativeRecurringItem(
                    id = -102,
                    kind = "expense",
                    title = "Utility Bill Preview",
                    category = "bills",
                    amount = 0.0,
                    paymentMethod = "CASH",
                    dayOfMonth = 5,
                    note = "Recurring bill example",
                    isActive = true,
                    sourceName = "local",
                    lastGeneratedMonth = "",
                    nextDueDate = nextRecurringDueDate(5),
                    dueInDays = recurringDueInDays(nextRecurringDueDate(5)),
                    iconClass = "",
                    label = "Expense",
                ),
            )
        }
        val nextTemplate = fallbackTemplates.minByOrNull { it.nextDueDate.ifBlank { "9999-99-99" } }
        val payload = PocketNativeRecurringPayload(
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            summary = PocketNativeRecurringSummary(
                totalTemplates = fallbackTemplates.size,
                activeTemplates = fallbackTemplates.count { it.isActive },
                incomeTemplates = fallbackTemplates.count { it.kind == "income" },
                expenseTemplates = fallbackTemplates.count { it.kind == "expense" },
                nextDueTitle = nextTemplate?.title.orEmpty(),
                nextDueDate = nextTemplate?.nextDueDate.orEmpty(),
            ),
            templates = fallbackTemplates,
        )
        val response = PocketNativeRecurringResponse(
            ok = true,
            message = "Offline recurring preview ready.",
            user = preferredUser(null),
            payload = payload,
        )
        writeCache("recurring.json", response)
        return response
    }

    private fun buildOfflineBootstrapGoalsResponse(): PocketNativeGoalsResponse {
        val currencyCode = preferencesStore.currencyCode()
        val currencySymbol = offlineCurrencySymbol(currencyCode)
        val payload = PocketNativeGoalsPayload(
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            summary = PocketNativeGoalsSummary(),
            goals = emptyList(),
            quickAddOptions = listOf(1000.0, 5000.0, 10000.0),
            motivationTitle = "Goals are ready",
            motivationBody = "Pocket Pro could not load live goals right now, but you can still create and edit goals locally until the server responds again.",
        )
        return PocketNativeGoalsResponse(
            ok = true,
            message = "Pocket Pro loaded a local goals fallback.",
            payload = payload,
        )
    }

    private fun goalsSummaryFromItems(items: List<PocketNativeGoalItem>): PocketNativeGoalsSummary {
        val savedAmount = items.sumOf { it.savedAmount }
        val targetAmount = items.sumOf { it.targetAmount }
        val remainingAmount = items.sumOf { it.remainingAmount }
        val totalProgressPercent = if (targetAmount > 0.0) {
            ((savedAmount / targetAmount) * 100.0).toInt().coerceIn(0, 100)
        } else {
            0
        }
        return PocketNativeGoalsSummary(
            totalProgressPercent = totalProgressPercent,
            savedAmount = savedAmount,
            targetAmount = targetAmount,
            remainingAmount = remainingAmount,
            goalsCount = items.size,
        )
    }

    private fun buildOfflineBootstrapBudgetResponse(): PocketNativeBudgetResponse {
        val currencyCode = preferencesStore.currencyCode()
        val currencySymbol = offlineCurrencySymbol(currencyCode)
        val fallbackCategories = offlineBootstrapCategories("expense")
        val payload = PocketNativeBudgetPayload(
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            summary = PocketNativeBudgetSummary(
                totalBudget = 0.0,
                spent = 0.0,
                remaining = 0.0,
                usedPercent = 0,
                activeBudgets = 0,
            ),
            budgets = emptyList(),
            categoryOptions = fallbackCategories,
            tipTitle = "Budget planner is ready",
            tipBody = "Pocket Pro could not load live budget data right now, but you can still create budgets, reminders, installments, and loan follow-up items locally.",
        )
        return PocketNativeBudgetResponse(
            ok = true,
            message = "Pocket Pro loaded a local budget fallback.",
            payload = payload,
        )
    }

    private fun nextRecurringDueDate(dayOfMonth: Int): String {
        val today = LocalDate.now()
        val safeDay = dayOfMonth.coerceIn(1, 28)
        val thisMonthCandidate = today.withDayOfMonth(safeDay)
        return if (!thisMonthCandidate.isBefore(today)) {
            thisMonthCandidate.toString()
        } else {
            today.plusMonths(1).withDayOfMonth(safeDay).toString()
        }
    }

    private fun recurringDueInDays(dateIso: String): Int {
        val parsed = runCatching { LocalDate.parse(dateIso) }.getOrNull() ?: return 0
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), parsed).toInt().coerceAtLeast(0)
    }

    private fun applyOfflineTransactionToCaches(request: PocketNativeTransactionSaveRequest) {
        val mode = request.mode.trim().lowercase()
        val signedAmount = if (isIncomeLikeMode(mode)) request.amount else -request.amount
        val entryKind = if (isIncomeLikeMode(mode)) "income" else "expense"
        val meta = offlineCategoryMeta(mode, request.categoryKey)
        val displayNote = request.note.ifBlank { request.partyName.ifBlank { modeLabel(mode) } }
        val syntheticId = -((System.currentTimeMillis() % Int.MAX_VALUE).toInt().coerceAtLeast(1))
        val recordItem = PocketNativeRecordItem(
            id = syntheticId,
            kind = entryKind,
            mode = mode,
            label = meta.label,
            note = displayNote,
            date = request.entryDate,
            amount = request.amount,
            signedAmount = signedAmount,
            colorHex = meta.colorHex,
            iconClass = meta.iconClass,
            canDelete = false,
            detailUrl = "",
            editUrl = "",
            deleteUrl = "",
        )
        applyOfflineRecordCache(recordItem)
        applyOfflineDashboardCache(recordItem)
        applyOfflineAccountsCache(recordItem, request.accountId)
        applyBudgetExpenseToCache(request)
    }

    private fun applyBudgetExpenseToCache(request: PocketNativeTransactionSaveRequest) {
        if (!request.mode.equals("expense", ignoreCase = true)) return
        val response = normalizeBudgetResponse(readCacheSafe<PocketNativeBudgetResponse>("budget.json") ?: return)
        val payload = response.payload ?: return
        var touched = false
        val updatedBudgets = payload.budgets.map { item ->
            if (!item.categoryKey.equals(request.categoryKey, ignoreCase = true)) {
                item
            } else {
                touched = true
                val nextSpent = item.spent + request.amount
                val nextRemaining = (item.amount - nextSpent).coerceAtLeast(0.0)
                item.copy(
                    spent = nextSpent,
                    remaining = nextRemaining,
                    progress = if (item.amount > 0.0) ((nextSpent / item.amount) * 100.0).coerceIn(0.0, 100.0) else 0.0,
                )
            }
        }
        if (!touched) return
        val totalBudget = updatedBudgets.sumOf { it.amount }
        val spent = updatedBudgets.sumOf { it.spent }
        val remaining = (totalBudget - spent).coerceAtLeast(0.0)
        writeCache(
            "budget.json",
            response.copy(
                payload = payload.copy(
                    summary = payload.summary.copy(
                        totalBudget = totalBudget,
                        spent = spent,
                        remaining = remaining,
                        usedPercent = if (totalBudget > 0.0) ((spent / totalBudget) * 100.0).toInt().coerceIn(0, 100) else 0,
                        activeBudgets = updatedBudgets.size,
                    ),
                    budgets = updatedBudgets,
                ),
            ),
        )
        syncDashboardBudgetSummaryFromBudgetCache()
    }

    private fun applyOfflineRecordCache(item: PocketNativeRecordItem) {
        val monthValue = item.date.take(7).ifBlank { LocalDate.now().toString().take(7) }
        val cacheName = "records-$monthValue.json"
        val response = readCacheSafe<PocketNativeRecordsResponse>(cacheName) ?: return
        val payload = response.payload ?: return
        val existingGroup = payload.groups.firstOrNull { it.dateIso == item.date }
        val updatedGroups = if (existingGroup != null) {
            payload.groups.map { group ->
                if (group.dateIso != item.date) {
                    group
                } else {
                    group.copy(
                        incomeTotal = if (item.signedAmount >= 0) group.incomeTotal + item.amount else group.incomeTotal,
                        expenseTotal = if (item.signedAmount < 0) group.expenseTotal + kotlin.math.abs(item.signedAmount) else group.expenseTotal,
                        rows = listOf(item) + group.rows,
                    )
                }
            }
        } else {
            listOf(
                PocketNativeRecordGroup(
                    dateIso = item.date,
                    dateLabel = item.date,
                    weekdayLabel = "",
                    incomeTotal = if (item.signedAmount >= 0) item.amount else 0.0,
                    expenseTotal = if (item.signedAmount < 0) kotlin.math.abs(item.signedAmount) else 0.0,
                    rows = listOf(item),
                )
            ) + payload.groups
        }
        val updatedSummary = payload.summary.copy(
            income = payload.summary.income + if (item.signedAmount >= 0) item.amount else 0.0,
            expense = payload.summary.expense + if (item.signedAmount < 0) kotlin.math.abs(item.signedAmount) else 0.0,
            lent = payload.summary.lent + if (item.mode == "lent") kotlin.math.abs(item.signedAmount) else 0.0,
            balance = payload.summary.balance + item.signedAmount,
            records = payload.summary.records + 1,
        )
        writeCache(
            cacheName,
            response.copy(
                payload = payload.copy(
                    summary = updatedSummary,
                    groups = updatedGroups,
                ),
            ),
        )
    }

    private fun applyOfflineDashboardCache(item: PocketNativeRecordItem) {
        val response = readCacheSafe<PocketNativeDashboardResponse>("dashboard.json") ?: return
        val dashboard = response.dashboard ?: return
        val todayIso = LocalDate.now().toString()
        val summary = dashboard.summary
        val updatedSummary = summary.copy(
            totalBalance = summary.totalBalance + item.signedAmount,
            monthIncome = summary.monthIncome + if (item.signedAmount >= 0) item.amount else 0.0,
            monthExpense = summary.monthExpense + if (item.signedAmount < 0) kotlin.math.abs(item.signedAmount) else 0.0,
            todayIncome = summary.todayIncome + if (item.date == todayIso && item.signedAmount >= 0) item.amount else 0.0,
            todayExpense = summary.todayExpense + if (item.date == todayIso && item.signedAmount < 0) kotlin.math.abs(item.signedAmount) else 0.0,
        )
        val recent = listOf(
            PocketNativeRecentActivity(
                id = item.id,
                kind = item.kind,
                label = item.label,
                note = item.note,
                date = item.date,
                amount = item.signedAmount,
                colorHex = item.colorHex,
            )
        ) + dashboard.recentActivity
        writeCache(
            "dashboard.json",
            response.copy(
                dashboard = dashboard.copy(
                    summary = updatedSummary,
                    recentActivity = recent.take(8),
                    accounts = dashboard.accounts.map { account ->
                        account.copy(
                            currentBalance = account.currentBalance,
                        )
                    },
                ),
            ),
        )
    }

    private fun resolvedSyncAccountId(accountId: Int): Int {
        if (accountId > 0) return accountId
        return readCacheSafe<PocketNativeAccountsResponse>("accounts.json")
            ?.payload
            ?.accounts
            ?.firstOrNull()
            ?.id
            ?: readCacheSafe<PocketNativeDashboardResponse>("dashboard.json")
                ?.dashboard
                ?.accounts
                ?.firstOrNull()
                ?.id
            ?: listOf("income", "expense", "lent", "borrow", "receive", "repay")
                .firstNotNullOfOrNull { cacheName ->
                    readCacheSafe<PocketNativeTransactionFormResponse>("transaction-form-$cacheName.json")
                        ?.payload
                        ?.accounts
                        ?.firstOrNull()
                        ?.id
                        ?.takeIf { it > 0 }
                }
            ?: accountId
    }

    private fun applyOfflineAccountsCache(item: PocketNativeRecordItem, accountId: Int) {
        val response = readCacheSafe<PocketNativeAccountsResponse>("accounts.json") ?: return
        val payload = response.payload ?: return
        val updatedAccounts = payload.accounts.map { account ->
            if (account.id != accountId) {
                account
            } else {
                account.copy(
                    currentBalance = account.currentBalance + item.signedAmount,
                    incomeTotal = account.incomeTotal + if (item.signedAmount >= 0) item.amount else 0.0,
                    expenseTotal = account.expenseTotal + if (item.signedAmount < 0) kotlin.math.abs(item.signedAmount) else 0.0,
                    netChange = account.netChange + item.signedAmount,
                )
            }
        }
        val updatedSummary = payload.summary.copy(
            totalBalance = payload.summary.totalBalance + item.signedAmount,
            totalIncome = payload.summary.totalIncome + if (item.signedAmount >= 0) item.amount else 0.0,
            totalExpense = payload.summary.totalExpense + if (item.signedAmount < 0) kotlin.math.abs(item.signedAmount) else 0.0,
            netChange = payload.summary.netChange + item.signedAmount,
        )
        val recent = listOf(
            PocketNativeRecentActivity(
                id = item.id,
                kind = item.kind,
                label = item.label,
                note = item.note,
                date = item.date,
                amount = item.signedAmount,
                colorHex = item.colorHex,
            )
        ) + payload.recentActivity
        writeCache(
            "accounts.json",
            response.copy(
                payload = payload.copy(
                    summary = updatedSummary,
                    accounts = updatedAccounts,
                    recentActivity = recent.take(10),
                ),
            ),
        )
    }

    private fun applyOfflineBudgetToCache(request: PocketNativeBudgetSaveRequest) {
        val response = normalizeBudgetResponse(
            readCacheSafe<PocketNativeBudgetResponse>("budget.json") ?: buildOfflineBootstrapBudgetResponse()
        )
        val payload = response.payload ?: buildOfflineBootstrapBudgetResponse().payload ?: return
        val syntheticId = -((System.currentTimeMillis() % Int.MAX_VALUE).toInt().coerceAtLeast(1))
        val categoryLabel = payload.categoryOptions.firstOrNull { it.key.equals(request.categoryKey, ignoreCase = true) }?.label
            ?: request.categoryKey.replace("_", " ").replaceFirstChar { it.uppercase() }
        val newItem = PocketNativeBudgetItem(
            id = syntheticId,
            name = request.budgetName,
            categoryKey = request.categoryKey,
            categoryLabel = categoryLabel,
            amount = request.amount,
            spent = 0.0,
            remaining = request.amount,
            progress = 0.0,
            periodType = request.periodType,
            note = request.note,
            accentHex = "#7C4DFF",
            isActive = true,
            dueDayOfMonth = request.startDate.substringAfterLast("-", "").toIntOrNull()?.coerceIn(1, 28) ?: 1,
        )
        val updatedSummary = payload.summary.copy(
            totalBudget = payload.summary.totalBudget + request.amount,
            remaining = payload.summary.remaining + request.amount,
            activeBudgets = payload.summary.activeBudgets + 1,
        )
        writeCache(
            "budget.json",
            response.copy(
                payload = payload.copy(
                    summary = updatedSummary,
                    budgets = listOf(newItem) + payload.budgets,
                ),
            ),
        )
        syncDashboardBudgetSummaryFromBudgetCache()
    }

    fun updateBudgetItemLocal(
        budgetId: Int,
        budgetName: String,
        categoryKey: String,
        amount: Double,
        note: String,
        dueDayOfMonth: Int,
    ): PocketNativeBudgetResponse? {
        val response = readCacheSafe<PocketNativeBudgetResponse>("budget.json") ?: return null
        val payload = response.payload ?: return null
        val duplicateCategory = payload.budgets.any {
            it.id != budgetId && it.categoryKey.equals(categoryKey, ignoreCase = true)
        }
        if (duplicateCategory) return null
        val updatedBudgets = payload.budgets.map { item ->
            if (item.id == budgetId) {
                item.copy(
                    name = budgetName.trim(),
                    categoryKey = categoryKey,
                    categoryLabel = payload.categoryOptions.firstOrNull { it.key.equals(categoryKey, ignoreCase = true) }?.label
                        ?: item.categoryLabel,
                    amount = amount,
                    remaining = (amount - item.spent).coerceAtLeast(0.0),
                    progress = if (amount > 0.0) ((item.spent / amount) * 100.0).coerceIn(0.0, 100.0) else 0.0,
                    note = note.trim(),
                    dueDayOfMonth = dueDayOfMonth.coerceIn(1, 28),
                )
            } else {
                item
            }
        }
        val totalBudget = updatedBudgets.sumOf { it.amount }
        val spent = updatedBudgets.sumOf { it.spent }
        val remaining = (totalBudget - spent).coerceAtLeast(0.0)
        val updated = response.copy(
            payload = payload.copy(
                summary = payload.summary.copy(
                    totalBudget = totalBudget,
                    spent = spent,
                    remaining = remaining,
                    usedPercent = if (totalBudget > 0.0) ((spent / totalBudget) * 100.0).toInt().coerceIn(0, 100) else 0,
                    activeBudgets = updatedBudgets.size,
                ),
                budgets = updatedBudgets,
            ),
        )
        writeCache("budget.json", updated)
        syncDashboardBudgetSummaryFromBudgetCache()
        return updated
    }

    fun deleteBudgetItemLocal(
        budgetId: Int,
    ): PocketNativeBudgetResponse? {
        val response = readCacheSafe<PocketNativeBudgetResponse>("budget.json") ?: return null
        val payload = response.payload ?: return null
        val updatedBudgets = payload.budgets.filterNot { it.id == budgetId }
        val totalBudget = updatedBudgets.sumOf { it.amount }
        val spent = updatedBudgets.sumOf { it.spent }
        val remaining = (totalBudget - spent).coerceAtLeast(0.0)
        val updated = response.copy(
            payload = payload.copy(
                summary = payload.summary.copy(
                    totalBudget = totalBudget,
                    spent = spent,
                    remaining = remaining,
                    usedPercent = if (totalBudget > 0.0) ((spent / totalBudget) * 100.0).toInt().coerceIn(0, 100) else 0,
                    activeBudgets = updatedBudgets.size,
                ),
                budgets = updatedBudgets,
            ),
        )
        writeCache("budget.json", updated)
        syncDashboardBudgetSummaryFromBudgetCache()
        return updated
    }

    private fun syncDashboardBudgetSummaryFromBudgetCache() {
        val budgetResponse = normalizeBudgetResponse(readCacheSafe<PocketNativeBudgetResponse>("budget.json") ?: return)
        val budgetPayload = budgetResponse.payload ?: return
        val dashboardResponse = readCacheSafe<PocketNativeDashboardResponse>("dashboard.json") ?: return
        val dashboard = dashboardResponse.dashboard ?: return

        val totalBudget = budgetPayload.summary.totalBudget
        val spent = budgetPayload.summary.spent
        val remaining = budgetPayload.summary.remaining
        val usedPercent = budgetPayload.summary.usedPercent
        writeCache(
            "dashboard.json",
            dashboardResponse.copy(
                dashboard = dashboard.copy(
                    summary = dashboard.summary.copy(
                        budgetAmount = totalBudget,
                        budgetSpent = spent,
                        budgetRemaining = remaining,
                        budgetUsedPercent = usedPercent,
                    ),
                ),
            ),
        )
    }

    private fun normalizeBudgetResponse(response: PocketNativeBudgetResponse): PocketNativeBudgetResponse {
        val payload = response.payload ?: return response
        if (payload.budgets.isEmpty()) return response

        val analyticsByCategory = readCacheSafe<PocketNativeDashboardResponse>("dashboard.json")
            ?.dashboard
            ?.analytics
            ?.categories
            ?.groupBy { it.key.trim().lowercase() }
            ?.mapValues { (_, rows) -> rows.sumOf { it.amount } }
            .orEmpty()

        val orderedCategoryKeys = payload.budgets
            .map { it.categoryKey.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()

        val normalizedBudgets = orderedCategoryKeys.mapNotNull { normalizedKey ->
            val items = payload.budgets.filter { it.categoryKey.trim().lowercase() == normalizedKey }
            val canonical = items.maxWithOrNull(
                compareBy<PocketNativeBudgetItem> { it.amount }
                    .thenByDescending { kotlin.math.abs(it.id) }
            ) ?: return@mapNotNull null

            val analyticsSpent = analyticsByCategory[normalizedKey] ?: 0.0
            val existingSpent = items.maxOfOrNull { it.spent } ?: 0.0
            val mergedSpent = maxOf(analyticsSpent, existingSpent)
            val remaining = (canonical.amount - mergedSpent).coerceAtLeast(0.0)
            val progress = if (canonical.amount > 0.0) {
                ((mergedSpent / canonical.amount) * 100.0).coerceIn(0.0, 100.0)
            } else {
                0.0
            }

            canonical.copy(
                spent = mergedSpent,
                remaining = remaining,
                progress = progress,
            )
        }

        val totalBudget = normalizedBudgets.sumOf { it.amount }
        val totalSpent = normalizedBudgets.sumOf { it.spent }
        val totalRemaining = (totalBudget - totalSpent).coerceAtLeast(0.0)

        return response.copy(
            payload = payload.copy(
                summary = payload.summary.copy(
                    totalBudget = totalBudget,
                    spent = totalSpent,
                    remaining = totalRemaining,
                    usedPercent = if (totalBudget > 0.0) {
                        ((totalSpent / totalBudget) * 100.0).toInt().coerceIn(0, 100)
                    } else {
                        0
                    },
                    activeBudgets = normalizedBudgets.size,
                ),
                budgets = normalizedBudgets,
            ),
        )
    }

    private fun applyOfflineGoalToCache(
        request: PocketNativeGoalSaveRequest,
        planFrequency: String,
        planAmount: Double,
        autoReminderOn: Boolean,
    ) {
        val response = readCacheSafe<PocketNativeGoalsResponse>("goals.json") ?: return
        val payload = response.payload ?: return
        val syntheticId = -((System.currentTimeMillis() % Int.MAX_VALUE).toInt().coerceAtLeast(1))
        val remainingAmount = (request.targetAmount - request.savedAmount).coerceAtLeast(0.0)
        val progress = if (request.targetAmount > 0) (request.savedAmount / request.targetAmount).coerceIn(0.0, 1.0) else 0.0
        val newItem = PocketNativeGoalItem(
            id = syntheticId,
            name = request.goalName,
            targetAmount = request.targetAmount,
            savedAmount = request.savedAmount,
            remainingAmount = remainingAmount,
            progress = progress,
            targetDate = request.targetDate,
            status = "ACTIVE",
            deadlineState = "OPEN",
            deadlineText = request.targetDate,
            note = request.note,
            planFrequency = planFrequency,
            planAmount = planAmount.coerceAtLeast(0.0),
            autoReminderOn = autoReminderOn,
            history = if (request.savedAmount > 0.0) {
                listOf(
                    PocketNativeGoalHistoryItem(
                        id = "goal-${syntheticId}-${System.currentTimeMillis()}",
                        amount = request.savedAmount,
                        date = LocalDate.now().toString(),
                        note = "Initial saved amount",
                        kind = "initial",
                    ),
                )
            } else {
                emptyList()
            },
        )
        val totalSaved = payload.summary.savedAmount + request.savedAmount
        val totalTarget = payload.summary.targetAmount + request.targetAmount
        val totalRemaining = payload.summary.remainingAmount + remainingAmount
        val totalProgressPercent = if (totalTarget > 0) ((totalSaved / totalTarget) * 100).toInt().coerceIn(0, 100) else 0
        writeCache(
            "goals.json",
            response.copy(
                payload = payload.copy(
                    summary = payload.summary.copy(
                        totalProgressPercent = totalProgressPercent,
                        savedAmount = totalSaved,
                        targetAmount = totalTarget,
                        remainingAmount = totalRemaining,
                        goalsCount = payload.summary.goalsCount + 1,
                    ),
                    goals = listOf(newItem) + payload.goals,
                ),
            ),
        )
    }

    private fun mergeGoalPlannerState(
        response: PocketNativeGoalsResponse,
        goalName: String,
        targetAmount: Double,
        savedAmount: Double,
        targetDate: String,
        note: String,
        planFrequency: String,
        planAmount: Double,
        autoReminderOn: Boolean,
    ): PocketNativeGoalsResponse {
        val payload = response.payload ?: return response
        var matched = false
        val updatedGoals = payload.goals.map { item ->
            val isMatch = !matched && (
                item.name.equals(goalName, ignoreCase = true) &&
                    item.targetDate.equals(targetDate, ignoreCase = true)
                ) || (!matched &&
                item.name.equals(goalName, ignoreCase = true) &&
                item.targetAmount == targetAmount)
            if (isMatch) {
                matched = true
                item.copy(
                    note = note,
                    planFrequency = planFrequency,
                    planAmount = planAmount.coerceAtLeast(0.0),
                    autoReminderOn = autoReminderOn,
                    history = if (savedAmount > 0.0 && item.history.isEmpty()) {
                        listOf(
                            PocketNativeGoalHistoryItem(
                                id = "goal-${item.id}-${System.currentTimeMillis()}",
                                amount = savedAmount,
                                date = LocalDate.now().toString(),
                                note = "Initial saved amount",
                                kind = "initial",
                            ),
                        )
                    } else {
                        item.history
                    },
                )
            } else {
                item
            }
        }
        return response.copy(
            payload = payload.copy(
                goals = updatedGoals,
                summary = goalsSummaryFromItems(updatedGoals),
            ),
        )
    }

    private fun mergeGoalResponseWithCache(
        response: PocketNativeGoalsResponse,
        cachedResponse: PocketNativeGoalsResponse?,
    ): PocketNativeGoalsResponse {
        val responsePayload = response.payload ?: return response
        val cachedPayload = cachedResponse?.payload ?: return response
        if (responsePayload.goals.isEmpty() && cachedPayload.goals.isNotEmpty()) {
            return cachedResponse
        }
        val cachedGoalsByKey = cachedPayload.goals.associateBy { "${it.id}:${it.name.lowercase()}" }
        val mergedGoals = responsePayload.goals.map { liveGoal ->
            val cachedGoal = cachedGoalsByKey["${liveGoal.id}:${liveGoal.name.lowercase()}"]
                ?: cachedPayload.goals.firstOrNull { it.name.equals(liveGoal.name, ignoreCase = true) }
            if (cachedGoal == null) {
                liveGoal
            } else {
                liveGoal.copy(
                    planFrequency = cachedGoal.planFrequency,
                    planAmount = cachedGoal.planAmount.takeIf { it > 0.0 } ?: liveGoal.planAmount,
                    autoReminderOn = cachedGoal.autoReminderOn,
                    history = if (cachedGoal.history.isNotEmpty()) cachedGoal.history else liveGoal.history,
                    note = cachedGoal.note.ifBlank { liveGoal.note },
                )
            }
        }
        return response.copy(
            payload = responsePayload.copy(
                goals = mergedGoals,
                summary = goalsSummaryFromItems(mergedGoals),
            ),
        )
    }

    private fun applyGoalSavedToCache(
        response: PocketNativeGoalsResponse,
        goalId: Int,
        amount: Double,
        note: String,
    ): PocketNativeGoalsResponse {
        val payload = response.payload ?: return response
        val updatedGoals = payload.goals.map { item ->
            if (item.id == goalId) {
                val updatedSaved = (item.savedAmount + amount).coerceAtMost(item.targetAmount.coerceAtLeast(item.savedAmount + amount))
                val remaining = (item.targetAmount - updatedSaved).coerceAtLeast(0.0)
                val progress = if (item.targetAmount > 0.0) ((updatedSaved / item.targetAmount) * 100.0).coerceIn(0.0, 100.0) else 0.0
                item.copy(
                    savedAmount = updatedSaved,
                    remainingAmount = remaining,
                    progress = progress,
                    history = listOf(
                        PocketNativeGoalHistoryItem(
                            id = "goal-${item.id}-${System.currentTimeMillis()}",
                            amount = amount,
                            date = LocalDate.now().toString(),
                            note = note.trim(),
                            kind = "save",
                        ),
                    ) + item.history,
                )
            } else {
                item
            }
        }
        return response.copy(
            payload = payload.copy(
                goals = updatedGoals,
                summary = goalsSummaryFromItems(updatedGoals),
            ),
        )
    }

    private fun normalizedOfflineCategoryKey(originalKey: String, label: String): String {
        if (originalKey.isNotBlank()) return originalKey.trim().lowercase()
        return label.trim().lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "custom_category" }
    }

    private fun applyOfflineCategorySaveToCache(
        kind: String,
        originalKey: String,
        label: String,
        iconKey: String,
        colorHex: String,
    ) {
        val cacheName = "categories-${kind.lowercase()}.json"
        val response = readCacheSafe<PocketNativeCategoryManagerResponse>(cacheName) ?: return
        val payload = response.payload ?: return
        val categoryKey = normalizedOfflineCategoryKey(originalKey, label)
        val nextItem = PocketNativeTransactionCategoryOption(
            key = categoryKey,
            label = label,
            colorHex = colorHex,
            iconClass = "",
            iconKey = iconKey,
        )
        val existing = payload.categories.toMutableList()
        val index = existing.indexOfFirst { it.key.equals(categoryKey, ignoreCase = true) }
        if (index >= 0) {
            existing[index] = nextItem
        } else {
            existing.add(0, nextItem)
        }
        val updatedPayload = payload.copy(categories = existing)
        writeCache(cacheName, response.copy(payload = updatedPayload))
        syncCategoryOptionsIntoTransactionForms(kind, existing)
    }

    private fun applyOfflineCategoryDeleteToCache(kind: String, categoryKey: String) {
        val cacheName = "categories-${kind.lowercase()}.json"
        val response = readCacheSafe<PocketNativeCategoryManagerResponse>(cacheName) ?: return
        val payload = response.payload ?: return
        val existing = payload.categories.filterNot { it.key.equals(categoryKey, ignoreCase = true) }
        val updatedPayload = payload.copy(categories = existing)
        writeCache(cacheName, response.copy(payload = updatedPayload))
        syncCategoryOptionsIntoTransactionForms(kind, existing)
    }

    private fun applyOfflineCategoryMoveToCache(kind: String, categoryKey: String, direction: String) {
        val cacheName = "categories-${kind.lowercase()}.json"
        val response = readCacheSafe<PocketNativeCategoryManagerResponse>(cacheName) ?: return
        val payload = response.payload ?: return
        val existing = payload.categories.toMutableList()
        val index = existing.indexOfFirst { it.key.equals(categoryKey, ignoreCase = true) }
        if (index < 0) return
        val swapIndex = when (direction.trim().lowercase()) {
            "up" -> index - 1
            "down" -> index + 1
            else -> index
        }
        if (swapIndex !in existing.indices) return
        val current = existing[index]
        existing[index] = existing[swapIndex]
        existing[swapIndex] = current
        val updatedPayload = payload.copy(categories = existing)
        writeCache(cacheName, response.copy(payload = updatedPayload))
        syncCategoryOptionsIntoTransactionForms(kind, existing)
    }

    private fun syncCategoryOptionsIntoTransactionForms(
        kind: String,
        categories: List<PocketNativeTransactionCategoryOption>,
    ) {
        val affectedModes = when (kind.trim().lowercase()) {
            "income" -> listOf("income", "borrow", "receive")
            else -> listOf("expense", "lent", "repay")
        }
        affectedModes.forEach { mode ->
            val cacheName = "transaction-form-$mode.json"
            val response = readCacheSafe<PocketNativeTransactionFormResponse>(cacheName) ?: return@forEach
            val payload = response.payload ?: return@forEach
            writeCache(cacheName, response.copy(payload = payload.copy(categories = categories)))
        }
    }

    private suspend fun syncPendingTransactions() {
        val pending = readPendingTransactions()
        if (pending.isEmpty()) {
            return
        }
        val remaining = mutableListOf<QueuedTransaction>()
        for (item in pending) {
            try {
                val response = api.saveTransaction(
                    PocketNativeTransactionSaveRequest(
                        mode = item.mode,
                        entryDate = item.entryDate,
                        amount = item.amount,
                        accountId = resolvedSyncAccountId(item.accountId),
                        categoryKey = backendCategoryFor(item.mode, item.categoryKey),
                        paymentMethod = item.paymentMethod,
                        partyName = item.partyName,
                        note = item.note,
                        editKind = item.editKind,
                        editId = item.editId,
                        clientTransactionId = item.clientTransactionId,
                        dueDate = item.dueDate,
                    ),
                )
                if (!response.ok) {
                    remaining.add(item)
                }
            } catch (_: Exception) {
                remaining.add(item)
            }
        }
        val file = pendingTransactionsFile()
        if (remaining.isEmpty()) {
            if (file.exists()) {
                file.delete()
            }
        } else {
            file.writeText(gson.toJson(remaining))
        }
    }

    private suspend fun syncPendingBudgetSaves() {
        val pending = readPendingBudgetSaves()
        if (pending.isEmpty()) {
            return
        }
        val remaining = mutableListOf<QueuedBudgetSave>()
        for (item in pending) {
            try {
                val response = api.saveBudget(
                    PocketNativeBudgetSaveRequest(
                        budgetName = item.budgetName,
                        categoryKey = item.categoryKey,
                        amount = item.amount,
                        periodType = item.periodType,
                        startDate = item.startDate,
                        endDate = item.endDate,
                        note = item.note,
                    )
                )
                if (!response.ok || response.payload == null) {
                    remaining.add(item)
                } else {
                    writeCache("budget.json", response)
                }
            } catch (_: Exception) {
                remaining.add(item)
            }
        }
        val file = pendingBudgetFile()
        if (remaining.isEmpty()) {
            if (file.exists()) {
                file.delete()
            }
        } else {
            file.writeText(gson.toJson(remaining))
        }
    }

    private suspend fun syncPendingGoalSaves() {
        val pending = readPendingGoalSaves()
        if (pending.isEmpty()) {
            return
        }
        val remaining = mutableListOf<QueuedGoalSave>()
        for (item in pending) {
            try {
                val response = api.saveGoal(
                    PocketNativeGoalSaveRequest(
                        goalName = item.goalName,
                        targetAmount = item.targetAmount,
                        savedAmount = item.savedAmount,
                        targetDate = item.targetDate,
                        note = item.note,
                    )
                )
                if (!response.ok || response.payload == null) {
                    remaining.add(item)
                } else {
                    writeCache("goals.json", response)
                }
            } catch (_: Exception) {
                remaining.add(item)
            }
        }
        val file = pendingGoalsFile()
        if (remaining.isEmpty()) {
            if (file.exists()) {
                file.delete()
            }
        } else {
            file.writeText(gson.toJson(remaining))
        }
    }

    private suspend fun syncPendingCategoryActions() {
        val pending = readPendingCategoryActions()
        if (pending.isEmpty()) {
            return
        }
        val remaining = mutableListOf<QueuedCategoryAction>()
        for (item in pending) {
            try {
                val response = when (item.action) {
                    "save" -> api.saveCategory(
                        PocketNativeCategorySaveRequest(
                            kind = item.kind,
                            originalKey = item.originalKey,
                            label = item.label,
                            iconKey = item.iconKey,
                            colorHex = item.colorHex,
                        ),
                    )
                    "delete" -> api.deleteCategory(
                        PocketNativeCategoryDeleteRequest(
                            kind = item.kind,
                            categoryKey = item.categoryKey,
                        ),
                    )
                    "move" -> api.moveCategory(
                        PocketNativeCategoryMoveRequest(
                            kind = item.kind,
                            categoryKey = item.categoryKey,
                            direction = item.direction,
                        ),
                    )
                    else -> null
                }
                if (response == null || !response.ok || response.payload == null) {
                    remaining.add(item)
                } else {
                    writeCache("categories-${item.kind.lowercase()}.json", response)
                    syncCategoryOptionsIntoTransactionForms(item.kind, response.payload.categories)
                }
            } catch (_: Exception) {
                remaining.add(item)
            }
        }
        val file = pendingCategoryActionsFile()
        if (remaining.isEmpty()) {
            if (file.exists()) {
                file.delete()
            }
        } else {
            file.writeText(gson.toJson(remaining))
        }
    }

    private fun writeCache(name: String, data: Any) {
        File(cacheDir, name).writeText(gson.toJson(data))
    }

    fun backupStats(preferencesStore: PocketNativePreferencesStore): PocketNativeBackupStats {
        val cacheFiles = cacheDir.listFiles()?.count { it.isFile && it.extension == "json" } ?: 0
        return PocketNativeBackupStats(
            cacheFileCount = cacheFiles,
            queuedTransactions = readPendingTransactions().size,
            queuedBudgets = readPendingBudgetSaves().size,
            queuedGoals = readPendingGoalSaves().size,
            reminderCount = preferencesStore.monthlyReminders().size + preferencesStore.loanDueReminders().size + preferencesStore.installmentPlans().size,
        )
    }

    fun buildLocalBackupJson(preferencesStore: PocketNativePreferencesStore): Result<String> = runCatching {
        val cacheSnapshot = linkedMapOf<String, String>()
        cacheDir.listFiles()
            ?.filter { it.isFile && it.extension == "json" }
            ?.sortedBy { it.name }
            ?.forEach { file ->
                cacheSnapshot[file.name] = file.readText()
            }
        val backup = PocketNativeBackupSnapshot(
            exportedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            preferences = preferencesStore.exportSnapshot(),
            cacheFiles = cacheSnapshot,
        )
        gson.toJson(backup)
    }

    fun restoreLocalBackupJson(
        json: String,
        preferencesStore: PocketNativePreferencesStore,
    ): Result<String> = runCatching {
        val snapshot = gson.fromJson(json, PocketNativeBackupSnapshot::class.java)
            ?: throw IllegalStateException("Pocket Pro backup file is empty.")
        if (snapshot.version <= 0) {
            throw IllegalStateException("Pocket Pro backup version is not supported.")
        }
        snapshot.cacheFiles.forEach { (name, content) ->
            if (name.endsWith(".json")) {
                File(cacheDir, name).writeText(content)
            }
        }
        preferencesStore.importSnapshot(snapshot.preferences)
        "Backup restored."
    }

    private inline fun <reified T> readCache(name: String): T? {
        val file = File(cacheDir, name)
        if (!file.exists()) {
            return null
        }
        return runCatching { gson.fromJson(file.readText(), T::class.java) }.getOrNull()
    }
}

private fun pocketLocalRegisteredUser(
    email: String,
    username: String,
): PocketNativeUser {
    val cleanUsername = username.ifBlank { "pocketuser" }
    val normalizedName = cleanUsername
        .replace("_", " ")
        .replace("-", " ")
        .trim()
        .ifBlank { "Pocket Pro User" }
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.lowercase(Locale.US).replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
            }
        }
        .ifBlank { "Pocket Pro User" }

    return PocketNativeUser(
        tenantId = 0,
        userId = -abs(cleanUsername.lowercase(Locale.US).hashCode().takeIf { it != Int.MIN_VALUE } ?: 1),
        username = cleanUsername,
        fullName = normalizedName,
        email = email,
        role = "USER",
        avatarInitials = pocketAvatarInitialsFor(
            fullName = normalizedName,
            fallback = cleanUsername.take(2).uppercase(Locale.US),
        ),
    )
}
