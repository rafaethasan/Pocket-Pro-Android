package com.softx.app.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PocketNativeMonthlyReminder(
    val id: String,
    val title: String,
    val dayOfMonth: Int,
    val note: String = "",
    val kind: String = "general",
    val amount: Double = 0.0,
    val categoryKey: String = "",
    val linkedBudgetId: Int = 0,
)

data class PocketNativeLoanDueReminder(
    val id: String,
    val kind: String,
    val counterparty: String,
    val amount: Double,
    val originalAmount: Double = 0.0,
    val settledAmount: Double = 0.0,
    val dueDate: String,
    val note: String = "",
    val paymentHistory: List<PocketNativeLoanDuePayment>? = emptyList(),
    val status: String = "pending",
    val createdAt: String = "",
)

data class PocketNativeLoanDuePayment(
    val id: String,
    val amount: Double,
    val paidDate: String,
    val note: String = "",
)

data class PocketNativeCollectionPayment(
    val id: String,
    val amount: Double,
    val collectedDate: String,
    val coveredDueDate: String,
    val note: String = "",
)

data class PocketNativeCollectionPlan(
    val id: String,
    val title: String,
    val counterparty: String,
    val baseAmount: Double,
    val currentDueAmount: Double,
    val frequency: String = "MONTHLY",
    val nextCollectionDate: String,
    val note: String = "",
    val autoReminderEnabled: Boolean = true,
    val carryOverEnabled: Boolean = true,
    val status: String = "active",
    val paymentHistory: List<PocketNativeCollectionPayment> = emptyList(),
    val createdAt: String = "",
)

data class PocketNativeInstallmentPayment(
    val id: String,
    val installmentNumber: Int,
    val amount: Double,
    val paidDate: String,
    val coveredDueDate: String,
    val note: String = "",
)

data class PocketNativeInstallmentPlan(
    val id: String,
    val title: String,
    val lender: String,
    val categoryKey: String,
    val categoryLabel: String,
    val loanAmount: Double,
    val installmentAmount: Double,
    val frequency: String,
    val firstDueDate: String,
    val nextDueDate: String,
    val totalInstallments: Int,
    val paidInstallments: Int = 0,
    val paidAmountTotal: Double = 0.0,
    val note: String = "",
    val paymentHistory: List<PocketNativeInstallmentPayment>? = emptyList(),
    val autoReminderEnabled: Boolean? = true,
    val status: String = "active",
    val createdAt: String = "",
)

data class PocketNativeLanguageOption(
    val code: String,
    val label: String,
    val nativeLabel: String,
    val localeTag: String,
    val relatedCurrencies: List<String>,
)

data class PocketNativeCurrencyOption(
    val code: String,
    val label: String,
    val symbol: String,
)

data class PocketNativePreferenceSnapshot(
    val languageCode: String,
    val currencyCode: String,
    val hasSeenOnboarding: Boolean,
    val hasCompletedInitialSetup: Boolean,
    val hasSeenHomeTour: Boolean = false,
    val nightlyReminderEnabled: Boolean = true,
    val nightlyReminderHour: Int = 22,
    val notificationSoundEnabled: Boolean = true,
    val notificationVibrationEnabled: Boolean = true,
    val monthlyReminders: List<PocketNativeMonthlyReminder>,
    val loanDueReminders: List<PocketNativeLoanDueReminder>? = emptyList(),
    val installmentPlans: List<PocketNativeInstallmentPlan>? = emptyList(),
    val collectionPlans: List<PocketNativeCollectionPlan>? = emptyList(),
    val signedInUser: PocketNativeUser? = null,
    val billingEntitlement: PocketNativeBillingEntitlement? = null,
)

data class PocketNativeBillingEntitlement(
    val productId: String = "",
    val planCode: String = "",
    val planLabel: String = "",
    val purchaseToken: String = "",
    val purchaseTime: Long = 0L,
    val purchaseType: String = "",
)

val PocketNativePaidPlanCodes = setOf(
    "POCKET_PRO",
    "POCKET_PREMIUM",
    "POCKET_LIFETIME",
    "POCKET_PREMIUM_MONTHLY",
    "POCKET_PREMIUM_YEARLY",
    "POCKET_PREMIUM_LIFETIME",
)

fun pocketNormalizeInstallmentTitle(
    rawTitle: String,
    categoryLabel: String = "",
): String {
    val normalized = rawTitle
        .trim()
        .replace(Regex("(?i)\\b(emi|kisti)\\b"), "Installment")
        .replace(Regex("\\s+"), " ")
        .trim()
    return when {
        normalized.isNotBlank() -> normalized
        categoryLabel.isNotBlank() -> "$categoryLabel Installment"
        else -> "Installment"
    }
}

fun pocketInstallmentReminderEnabled(plan: PocketNativeInstallmentPlan): Boolean = plan.autoReminderEnabled != false

fun pocketInstallmentPaidAmount(plan: PocketNativeInstallmentPlan): Double {
    val storedAmount = plan.paidAmountTotal
    return when {
        storedAmount > 0.0 -> storedAmount.coerceAtMost(plan.loanAmount)
        plan.paidInstallments > 0 -> (plan.paidInstallments * plan.installmentAmount).coerceAtMost(plan.loanAmount)
        else -> 0.0
    }
}

fun pocketInstallmentCompletedCount(plan: PocketNativeInstallmentPlan): Int {
    if (plan.installmentAmount <= 0.0) return plan.paidInstallments.coerceAtLeast(0)
    val byAmount = kotlin.math.floor((pocketInstallmentPaidAmount(plan) / plan.installmentAmount) + 0.0001).toInt()
    return byAmount.coerceAtLeast(plan.paidInstallments).coerceAtMost(plan.totalInstallments)
}

fun pocketLoanReminderOriginalAmount(reminder: PocketNativeLoanDueReminder): Double {
    val storedOriginal = reminder.originalAmount
    return when {
        storedOriginal > 0.0 -> storedOriginal.coerceAtLeast(reminder.amount)
        reminder.amount > 0.0 -> (reminder.amount + reminder.settledAmount).coerceAtLeast(reminder.amount)
        else -> 0.0
    }
}

fun pocketLoanReminderSettledAmount(reminder: PocketNativeLoanDueReminder): Double {
    val originalAmount = pocketLoanReminderOriginalAmount(reminder)
    if (originalAmount <= 0.0) return 0.0
    val historyAmount = reminder.paymentHistory.orEmpty().sumOf { it.amount.coerceAtLeast(0.0) }
    return when {
        reminder.settledAmount > 0.0 -> reminder.settledAmount.coerceAtMost(originalAmount)
        historyAmount > 0.0 -> historyAmount.coerceAtMost(originalAmount)
        else -> (originalAmount - reminder.amount).coerceAtLeast(0.0).coerceAtMost(originalAmount)
    }
}

fun pocketLoanReminderRemainingAmount(reminder: PocketNativeLoanDueReminder): Double {
    val originalAmount = pocketLoanReminderOriginalAmount(reminder)
    return when {
        reminder.amount > 0.0 -> reminder.amount.coerceAtMost(originalAmount.coerceAtLeast(reminder.amount))
        originalAmount > 0.0 -> (originalAmount - pocketLoanReminderSettledAmount(reminder)).coerceAtLeast(0.0)
        else -> 0.0
    }
}

class PocketNativePreferencesStore(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun languageCode(): String = prefs.getString(KEY_LANGUAGE_CODE, DEFAULT_LANGUAGE_CODE)?.ifBlank {
        DEFAULT_LANGUAGE_CODE
    } ?: DEFAULT_LANGUAGE_CODE

    fun hasSeenOnboarding(): Boolean = prefs.getBoolean(KEY_ONBOARDING_SEEN, false)
    fun hasSeenHomeTour(): Boolean = prefs.getBoolean(KEY_HOME_TOUR_SEEN, false)
    fun hasCompletedInitialSetup(): Boolean = prefs.getBoolean(KEY_INITIAL_SETUP_DONE, false)

    fun currencyCode(): String = prefs.getString(KEY_CURRENCY_CODE, DEFAULT_CURRENCY_CODE)?.ifBlank {
        DEFAULT_CURRENCY_CODE
    } ?: DEFAULT_CURRENCY_CODE

    fun appLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)

    fun appLockPin(): String = prefs.getString(KEY_APP_LOCK_PIN, "")?.trim().orEmpty()

    fun nightlyReminderEnabled(): Boolean = prefs.getBoolean(KEY_NIGHTLY_REMINDER_ENABLED, true)

    fun nightlyReminderHour(): Int = prefs.getInt(KEY_NIGHTLY_REMINDER_HOUR, 22).coerceIn(6, 23)

    fun notificationSoundEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATION_SOUND_ENABLED, true)

    fun notificationVibrationEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATION_VIBRATION_ENABLED, true)

    fun lastMoneyEntryLoggedOn(): String = prefs.getString(KEY_LAST_MONEY_ENTRY_DATE, "") ?: ""

    fun todayMoneyEntryCount(): Int {
        val todayIso = java.time.LocalDate.now().toString()
        return if (lastMoneyEntryLoggedOn() == todayIso) {
            prefs.getInt(KEY_TODAY_MONEY_ENTRY_COUNT, 0)
        } else {
            0
        }
    }

    fun monthlyReminders(): List<PocketNativeMonthlyReminder> {
        val raw = prefs.getString(KEY_MONTHLY_REMINDERS, "[]") ?: "[]"
        return runCatching {
            val type = object : TypeToken<List<PocketNativeMonthlyReminder>>() {}.type
            gson.fromJson<List<PocketNativeMonthlyReminder>>(raw, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun loanDueReminders(): List<PocketNativeLoanDueReminder> {
        val raw = prefs.getString(KEY_LOAN_DUE_REMINDERS, "[]") ?: "[]"
        return runCatching {
            val type = object : TypeToken<List<PocketNativeLoanDueReminder>>() {}.type
            gson.fromJson<List<PocketNativeLoanDueReminder>>(raw, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun installmentPlans(): List<PocketNativeInstallmentPlan> {
        val raw = prefs.getString(KEY_INSTALLMENT_PLANS, "[]") ?: "[]"
        return runCatching {
            val type = object : TypeToken<List<PocketNativeInstallmentPlan>>() {}.type
            gson.fromJson<List<PocketNativeInstallmentPlan>>(raw, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun collectionPlans(): List<PocketNativeCollectionPlan> {
        val raw = prefs.getString(KEY_COLLECTION_PLANS, "[]") ?: "[]"
        return runCatching {
            val type = object : TypeToken<List<PocketNativeCollectionPlan>>() {}.type
            gson.fromJson<List<PocketNativeCollectionPlan>>(raw, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun billingEntitlement(): PocketNativeBillingEntitlement? {
        val raw = prefs.getString(KEY_BILLING_ENTITLEMENT, "") ?: ""
        if (raw.isBlank()) return null
        return runCatching { gson.fromJson(raw, PocketNativeBillingEntitlement::class.java) }.getOrNull()
    }

    fun setHasSeenOnboarding(seen: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_SEEN, seen).apply()
    }

    fun setHasSeenHomeTour(seen: Boolean) {
        prefs.edit().putBoolean(KEY_HOME_TOUR_SEEN, seen).apply()
    }

    fun setHasCompletedInitialSetup(done: Boolean) {
        prefs.edit().putBoolean(KEY_INITIAL_SETUP_DONE, done).apply()
    }

    fun setLanguageCode(code: String) {
        val cleanCode = if (SUPPORTED_LANGUAGE_CODES.contains(code)) code else DEFAULT_LANGUAGE_CODE
        prefs.edit().putString(KEY_LANGUAGE_CODE, cleanCode).apply()
        applyAppLanguage(cleanCode)
    }

    fun setCurrencyCode(code: String) {
        val cleanCode = if (SUPPORTED_CURRENCY_CODES.contains(code)) code else DEFAULT_CURRENCY_CODE
        prefs.edit().putString(KEY_CURRENCY_CODE, cleanCode).apply()
    }

    fun saveMonthlyReminders(reminders: List<PocketNativeMonthlyReminder>) {
        prefs.edit().putString(KEY_MONTHLY_REMINDERS, gson.toJson(reminders)).apply()
    }

    fun saveLoanDueReminders(reminders: List<PocketNativeLoanDueReminder>) {
        prefs.edit().putString(KEY_LOAN_DUE_REMINDERS, gson.toJson(reminders)).apply()
    }

    fun saveInstallmentPlans(plans: List<PocketNativeInstallmentPlan>) {
        prefs.edit().putString(KEY_INSTALLMENT_PLANS, gson.toJson(plans)).apply()
    }

    fun saveCollectionPlans(plans: List<PocketNativeCollectionPlan>) {
        prefs.edit().putString(KEY_COLLECTION_PLANS, gson.toJson(plans)).apply()
    }

    fun addLoanDueReminder(
        kind: String,
        counterparty: String,
        amount: Double,
        dueDate: String,
        note: String = "",
    ) {
        val cleanKind = kind.trim().lowercase()
        if (cleanKind !in setOf("lent", "borrow")) return
        val cleanDate = dueDate.trim()
        if (cleanDate.isBlank() || amount <= 0.0) return
        val cleanParty = counterparty.trim()
        val cleanNote = note.trim()
        val existing = loanDueReminders().toMutableList()
        existing.add(
            0,
            PocketNativeLoanDueReminder(
                id = "loan-${System.currentTimeMillis()}-${existing.size}",
                kind = cleanKind,
                counterparty = cleanParty,
                amount = amount,
                originalAmount = amount,
                settledAmount = 0.0,
                dueDate = cleanDate,
                note = cleanNote,
                paymentHistory = emptyList(),
                status = "pending",
                createdAt = java.time.OffsetDateTime.now().toString(),
            ),
        )
        saveLoanDueReminders(existing)
    }

    fun resolveLoanDueReminder(
        reminderId: String = "",
        settlementMode: String,
        counterparty: String,
        amount: Double,
        note: String = "",
    ): PocketNativeLoanDueReminder? {
        val targetKind = when (settlementMode.trim().lowercase()) {
            "receive" -> "lent"
            "repay" -> "borrow"
            else -> ""
        }
        if (targetKind.isBlank() || amount <= 0.0) return null
        val cleanParty = counterparty.trim().lowercase()
        val pending = loanDueReminders().filter { it.status == "pending" && it.kind == targetKind }
        if (pending.isEmpty()) return null
        val matchingById = pending.firstOrNull {
            reminderId.isNotBlank() && it.id == reminderId
        }
        val matchingByPartyAndAmount = pending.firstOrNull {
            cleanParty.isNotBlank() &&
                it.counterparty.trim().lowercase() == cleanParty &&
                kotlin.math.abs(pocketLoanReminderRemainingAmount(it) - amount) < 0.01
        }
        val matchingByAmount = pending.firstOrNull { kotlin.math.abs(pocketLoanReminderRemainingAmount(it) - amount) < 0.01 }
        val matchingByParty = pending.firstOrNull {
            cleanParty.isNotBlank() && it.counterparty.trim().lowercase() == cleanParty
        }
        val matched = matchingById ?: matchingByPartyAndAmount ?: matchingByAmount ?: matchingByParty ?: pending.firstOrNull()
        if (matched != null) {
            val originalAmount = pocketLoanReminderOriginalAmount(matched)
            val currentRemaining = pocketLoanReminderRemainingAmount(matched)
            val currentSettled = pocketLoanReminderSettledAmount(matched)
            val paymentAmount = amount.coerceAtMost(currentRemaining.coerceAtLeast(0.0))
            if (paymentAmount <= 0.0) return matched
            val nextRemaining = (currentRemaining - paymentAmount).coerceAtLeast(0.0)
            val paymentEntry = PocketNativeLoanDuePayment(
                id = "loan-pay-${System.currentTimeMillis()}-${matched.id}",
                amount = paymentAmount,
                paidDate = java.time.LocalDate.now().toString(),
                note = note.trim().ifBlank { matched.note },
            )
            val updated = if (nextRemaining <= 0.009) {
                loanDueReminders().filterNot { it.id == matched.id }
            } else {
                loanDueReminders().map { reminder ->
                    if (reminder.id == matched.id) {
                        reminder.copy(
                            amount = nextRemaining,
                            originalAmount = originalAmount.coerceAtLeast(currentRemaining),
                            settledAmount = (currentSettled + paymentAmount).coerceAtMost(originalAmount.coerceAtLeast(currentRemaining)),
                            paymentHistory = (reminder.paymentHistory.orEmpty() + paymentEntry),
                        )
                    } else {
                        reminder
                    }
                }
            }
            saveLoanDueReminders(updated)
        }
        return matched
    }

    fun deleteLoanDueReminder(reminderId: String) {
        saveLoanDueReminders(loanDueReminders().filterNot { it.id == reminderId })
    }

    fun updateLoanDueReminderDate(
        reminderId: String,
        dueDate: String,
    ) {
        val cleanDate = dueDate.trim()
        if (cleanDate.isBlank()) return
        val updated = loanDueReminders().map { reminder ->
            if (reminder.id == reminderId) {
                reminder.copy(dueDate = cleanDate)
            } else {
                reminder
            }
        }
        saveLoanDueReminders(updated)
    }

    fun updateLoanDueReminder(
        reminderId: String,
        kind: String,
        counterparty: String,
        originalAmount: Double,
        remainingAmount: Double,
        dueDate: String,
        note: String = "",
    ) {
        val cleanKind = kind.trim().lowercase()
        val cleanParty = counterparty.trim()
        val cleanDate = dueDate.trim()
        val cleanOriginal = originalAmount.coerceAtLeast(0.0)
        val cleanRemaining = remainingAmount.coerceAtLeast(0.0).coerceAtMost(cleanOriginal)
        if (cleanKind !in setOf("lent", "borrow")) return
        if (cleanParty.isBlank() || cleanDate.isBlank() || cleanOriginal <= 0.0 || cleanRemaining <= 0.0) return
        val updated = loanDueReminders().map { reminder ->
            if (reminder.id == reminderId) {
                reminder.copy(
                    kind = cleanKind,
                    counterparty = cleanParty,
                    amount = cleanRemaining,
                    originalAmount = cleanOriginal,
                    settledAmount = (cleanOriginal - cleanRemaining).coerceAtLeast(0.0),
                    dueDate = cleanDate,
                    note = note.trim(),
                )
            } else {
                reminder
            }
        }
        saveLoanDueReminders(updated)
    }

    fun addInstallmentPlan(
        title: String,
        lender: String,
        categoryKey: String,
        categoryLabel: String,
        loanAmount: Double,
        installmentAmount: Double,
        frequency: String,
        firstDueDate: String,
        totalInstallments: Int,
        autoReminderEnabled: Boolean = true,
        note: String = "",
    ) {
        val cleanTitle = pocketNormalizeInstallmentTitle(title, categoryLabel)
        val cleanDate = firstDueDate.trim()
        val cleanFrequency = frequency.trim().uppercase().ifBlank { "MONTHLY" }
        if (cleanTitle.isBlank() || cleanDate.isBlank() || loanAmount <= 0.0 || installmentAmount <= 0.0 || totalInstallments <= 0) {
            return
        }
        val existing = installmentPlans().toMutableList()
        existing.add(
            0,
            PocketNativeInstallmentPlan(
                id = "plan-${System.currentTimeMillis()}-${existing.size}",
                title = cleanTitle,
                lender = lender.trim(),
                categoryKey = categoryKey.trim(),
                categoryLabel = categoryLabel.trim().ifBlank { "Other" },
                loanAmount = loanAmount,
                installmentAmount = installmentAmount,
                frequency = cleanFrequency,
                firstDueDate = cleanDate,
                nextDueDate = cleanDate,
                totalInstallments = totalInstallments,
                note = note.trim(),
                autoReminderEnabled = autoReminderEnabled,
                createdAt = java.time.OffsetDateTime.now().toString(),
            ),
        )
        saveInstallmentPlans(existing)
    }

    fun updateInstallmentPlan(
        planId: String,
        title: String,
        lender: String,
        categoryKey: String,
        categoryLabel: String,
        loanAmount: Double,
        installmentAmount: Double,
        frequency: String,
        nextDueDate: String,
        totalInstallments: Int,
        autoReminderEnabled: Boolean = true,
        note: String = "",
    ) {
        val existingPlans = installmentPlans()
        val currentPlan = existingPlans.firstOrNull { it.id == planId } ?: return
        val cleanTitle = pocketNormalizeInstallmentTitle(title, categoryLabel)
        val cleanDate = nextDueDate.trim()
        val cleanFrequency = frequency.trim().uppercase().ifBlank { "MONTHLY" }
        val completedCount = pocketInstallmentCompletedCount(currentPlan)
        val paidAmount = pocketInstallmentPaidAmount(currentPlan)
        val safeLoanAmount = loanAmount.coerceAtLeast(paidAmount.coerceAtLeast(0.0))
        val safeTotalInstallments = totalInstallments.coerceAtLeast(completedCount.coerceAtLeast(1))
        if (
            cleanTitle.isBlank() ||
            cleanDate.isBlank() ||
            safeLoanAmount <= 0.0 ||
            installmentAmount <= 0.0 ||
            safeTotalInstallments <= 0
        ) {
            return
        }
        saveInstallmentPlans(
            existingPlans.map { plan ->
                if (plan.id == planId) {
                    plan.copy(
                        title = cleanTitle,
                        lender = lender.trim(),
                        categoryKey = categoryKey.trim(),
                        categoryLabel = categoryLabel.trim().ifBlank { "Other" },
                        loanAmount = safeLoanAmount,
                        installmentAmount = installmentAmount,
                        frequency = cleanFrequency,
                        firstDueDate = if ((plan.paymentHistory ?: emptyList()).isEmpty()) cleanDate else plan.firstDueDate,
                        nextDueDate = cleanDate,
                        totalInstallments = safeTotalInstallments,
                        autoReminderEnabled = autoReminderEnabled,
                        note = note.trim(),
                        paidInstallments = completedCount.coerceAtMost(safeTotalInstallments),
                        paidAmountTotal = paidAmount.coerceAtMost(safeLoanAmount),
                    )
                } else {
                    plan
                }
            },
        )
    }

    fun updateInstallmentPlanDate(
        planId: String,
        nextDueDate: String,
    ) {
        val cleanDate = nextDueDate.trim()
        if (cleanDate.isBlank()) return
        saveInstallmentPlans(
            installmentPlans().map { plan ->
                if (plan.id == planId) plan.copy(nextDueDate = cleanDate) else plan
            },
        )
    }

    fun markInstallmentPaid(planId: String) {
        addInstallmentPayment(planId = planId, amount = null)
    }

    fun addInstallmentPayment(
        planId: String,
        amount: Double?,
        noteOverride: String = "",
    ) {
        val updated = installmentPlans().mapNotNull { plan ->
            if (plan.id != planId) {
                plan
            } else {
                val currentPaidAmount = pocketInstallmentPaidAmount(plan)
                val currentCompleted = pocketInstallmentCompletedCount(plan)
                val paymentAmount = (amount ?: plan.installmentAmount)
                    .coerceAtLeast(0.0)
                    .coerceAtMost((plan.loanAmount - currentPaidAmount).coerceAtLeast(0.0))
                if (paymentAmount <= 0.0) {
                    return@mapNotNull plan
                }
                val nextPaidAmount = (currentPaidAmount + paymentAmount).coerceAtMost(plan.loanAmount)
                val nextCompleted = if (plan.installmentAmount <= 0.0) {
                    currentCompleted
                } else {
                    kotlin.math.floor((nextPaidAmount / plan.installmentAmount) + 0.0001).toInt().coerceAtMost(plan.totalInstallments)
                }
                val completedDelta = (nextCompleted - currentCompleted).coerceAtLeast(0)
                val paymentEntry = PocketNativeInstallmentPayment(
                    id = "pay-${System.currentTimeMillis()}-${currentCompleted + 1}",
                    installmentNumber = (currentCompleted + 1).coerceAtMost(plan.totalInstallments),
                    amount = paymentAmount,
                    paidDate = java.time.LocalDate.now().toString(),
                    coveredDueDate = plan.nextDueDate,
                    note = noteOverride.trim().ifBlank { plan.note },
                )
                val history = (plan.paymentHistory ?: emptyList()) + paymentEntry
                val done = nextPaidAmount >= plan.loanAmount || nextCompleted >= plan.totalInstallments
                if (done) {
                    plan.copy(
                        paidInstallments = plan.totalInstallments,
                        paidAmountTotal = plan.loanAmount,
                        paymentHistory = history,
                        status = "completed",
                    )
                } else {
                    plan.copy(
                        paidInstallments = nextCompleted,
                        paidAmountTotal = nextPaidAmount,
                        paymentHistory = history,
                        nextDueDate = if (completedDelta > 0) {
                            generateSequence(plan.nextDueDate) { previous ->
                                shiftInstallmentDate(previous, plan.frequency)
                            }.drop(completedDelta).first()
                        } else {
                            plan.nextDueDate
                        },
                    )
                }
            }
        }
        saveInstallmentPlans(updated)
    }

    fun setInstallmentReminderEnabled(
        planId: String,
        enabled: Boolean,
    ) {
        saveInstallmentPlans(
            installmentPlans().map { plan ->
                if (plan.id == planId) {
                    plan.copy(autoReminderEnabled = enabled)
                } else {
                    plan
                }
            },
        )
    }

    fun deleteInstallmentPlan(planId: String) {
        saveInstallmentPlans(installmentPlans().filterNot { it.id == planId })
    }

    fun addCollectionPlan(
        title: String,
        counterparty: String,
        baseAmount: Double,
        nextCollectionDate: String,
        frequency: String = "MONTHLY",
        note: String = "",
        autoReminderEnabled: Boolean = true,
        carryOverEnabled: Boolean = true,
    ) {
        val cleanTitle = title.trim()
        val cleanCounterparty = counterparty.trim()
        val cleanDate = nextCollectionDate.trim()
        if (cleanTitle.isBlank() || cleanCounterparty.isBlank() || cleanDate.isBlank() || baseAmount <= 0.0) return
        val existing = collectionPlans().toMutableList()
        existing.add(
            0,
            PocketNativeCollectionPlan(
                id = "collect-${System.currentTimeMillis()}-${existing.size}",
                title = cleanTitle,
                counterparty = cleanCounterparty,
                baseAmount = baseAmount,
                currentDueAmount = baseAmount,
                frequency = frequency.trim().uppercase().ifBlank { "MONTHLY" },
                nextCollectionDate = cleanDate,
                note = note.trim(),
                autoReminderEnabled = autoReminderEnabled,
                carryOverEnabled = carryOverEnabled,
                createdAt = java.time.OffsetDateTime.now().toString(),
            ),
        )
        saveCollectionPlans(existing)
    }

    fun updateCollectionPlan(
        planId: String,
        title: String,
        counterparty: String,
        baseAmount: Double,
        nextCollectionDate: String,
        frequency: String = "MONTHLY",
        note: String = "",
        autoReminderEnabled: Boolean = true,
        carryOverEnabled: Boolean = true,
    ) {
        val cleanTitle = title.trim()
        val cleanCounterparty = counterparty.trim()
        val cleanDate = nextCollectionDate.trim()
        if (cleanTitle.isBlank() || cleanCounterparty.isBlank() || cleanDate.isBlank() || baseAmount <= 0.0) return
        saveCollectionPlans(
            collectionPlans().map { plan ->
                if (plan.id == planId) {
                    val currentPaidAhead = (plan.baseAmount - plan.currentDueAmount).coerceAtMost(plan.baseAmount)
                    val recalculatedDue = if (plan.currentDueAmount <= 0.0) {
                        (baseAmount - currentPaidAhead).coerceAtLeast(0.0)
                    } else {
                        plan.currentDueAmount
                    }
                    plan.copy(
                        title = cleanTitle,
                        counterparty = cleanCounterparty,
                        baseAmount = baseAmount,
                        currentDueAmount = recalculatedDue,
                        nextCollectionDate = cleanDate,
                        frequency = frequency.trim().uppercase().ifBlank { "MONTHLY" },
                        note = note.trim(),
                        autoReminderEnabled = autoReminderEnabled,
                        carryOverEnabled = carryOverEnabled,
                    )
                } else {
                    plan
                }
            },
        )
    }

    fun updateCollectionPlanDate(
        planId: String,
        nextCollectionDate: String,
    ) {
        val cleanDate = nextCollectionDate.trim()
        if (cleanDate.isBlank()) return
        saveCollectionPlans(
            collectionPlans().map { plan ->
                if (plan.id == planId) plan.copy(nextCollectionDate = cleanDate) else plan
            },
        )
    }

    fun setCollectionReminderEnabled(
        planId: String,
        enabled: Boolean,
    ) {
        saveCollectionPlans(
            collectionPlans().map { plan ->
                if (plan.id == planId) plan.copy(autoReminderEnabled = enabled) else plan
            },
        )
    }

    fun deleteCollectionPlan(planId: String) {
        saveCollectionPlans(collectionPlans().filterNot { it.id == planId })
    }

    fun normalizeCollectionPlansForToday(): List<PocketNativeCollectionPlan> {
        val today = java.time.LocalDate.now()
        var changed = false
        val updated = collectionPlans().map { plan ->
            if (plan.status != "active") return@map plan
            var dueDate = runCatching { java.time.LocalDate.parse(plan.nextCollectionDate) }.getOrElse { today }
            var dueAmount = plan.currentDueAmount.coerceAtLeast(0.0)
            val normalizedFrequency = plan.frequency.trim().uppercase().ifBlank { "MONTHLY" }
            while (java.time.YearMonth.from(dueDate).isBefore(java.time.YearMonth.from(today))) {
                dueDate = when (normalizedFrequency) {
                    "YEARLY" -> dueDate.plusYears(1)
                    else -> dueDate.plusMonths(1)
                }
                if (plan.carryOverEnabled) {
                    dueAmount += plan.baseAmount
                } else {
                    dueAmount = plan.baseAmount
                }
                changed = true
            }
            if (dueDate.toString() != plan.nextCollectionDate || kotlin.math.abs(dueAmount - plan.currentDueAmount) > 0.009) {
                changed = true
                plan.copy(nextCollectionDate = dueDate.toString(), currentDueAmount = dueAmount)
            } else {
                plan
            }
        }
        if (changed) {
            saveCollectionPlans(updated)
        }
        return updated
    }

    fun collectFromPlan(
        planId: String,
        amount: Double,
        note: String = "",
    ) {
        if (amount <= 0.0) return
        val normalized = normalizeCollectionPlansForToday()
        val updated = normalized.map { plan ->
            if (plan.id != planId) return@map plan
            val safeAmount = amount.coerceAtLeast(0.0)
            val paymentEntry = PocketNativeCollectionPayment(
                id = "collect-pay-${System.currentTimeMillis()}-${plan.id}",
                amount = safeAmount,
                collectedDate = java.time.LocalDate.now().toString(),
                coveredDueDate = plan.nextCollectionDate,
                note = note.trim().ifBlank { plan.note },
            )
            if (safeAmount + 0.009 < plan.currentDueAmount) {
                plan.copy(
                    currentDueAmount = (plan.currentDueAmount - safeAmount).coerceAtLeast(0.0),
                    paymentHistory = plan.paymentHistory + paymentEntry,
                )
            } else {
                val extraAmount = (safeAmount - plan.currentDueAmount).coerceAtLeast(0.0)
                val nextBaseDue = if (extraAmount > 0.0) {
                    (plan.baseAmount - extraAmount).coerceAtLeast(0.0)
                } else {
                    plan.baseAmount
                }
                plan.copy(
                    currentDueAmount = nextBaseDue,
                    nextCollectionDate = shiftInstallmentDate(plan.nextCollectionDate, plan.frequency),
                    paymentHistory = plan.paymentHistory + paymentEntry,
                )
            }
        }
        saveCollectionPlans(updated)
    }

    fun saveSignedInUser(user: PocketNativeUser?) {
        prefs.edit().putString(KEY_SIGNED_IN_USER, if (user == null) "" else gson.toJson(user)).apply()
    }

    fun signedInUser(): PocketNativeUser? {
        val raw = prefs.getString(KEY_SIGNED_IN_USER, "") ?: ""
        val user = if (raw.isBlank()) null else runCatching { gson.fromJson(raw, PocketNativeUser::class.java) }.getOrNull()
        return applyBillingEntitlement(user)
    }

    fun clearSignedInUser() {
        prefs.edit().remove(KEY_SIGNED_IN_USER).apply()
    }

    fun saveBillingEntitlement(entitlement: PocketNativeBillingEntitlement?) {
        prefs.edit().putString(KEY_BILLING_ENTITLEMENT, if (entitlement == null) "" else gson.toJson(entitlement)).apply()
    }

    fun clearBillingEntitlement() {
        prefs.edit().remove(KEY_BILLING_ENTITLEMENT).apply()
    }

    fun applyBillingEntitlement(user: PocketNativeUser?): PocketNativeUser? {
        val entitlement = billingEntitlement() ?: return user
        val current = user ?: PocketNativeUser(
            fullName = "Pocket Pro User",
            avatarInitials = "PP",
        )
        return current.copy(
            planCode = entitlement.planCode.ifBlank { current.planCode },
            planLabel = entitlement.planLabel.ifBlank { current.planLabel },
        )
    }

    fun saveAppLock(pin: String) {
        val cleanPin = pin.filter { it.isDigit() }.take(6)
        prefs.edit()
            .putBoolean(KEY_APP_LOCK_ENABLED, cleanPin.isNotBlank())
            .putString(KEY_APP_LOCK_PIN, cleanPin)
            .apply()
    }

    fun disableAppLock() {
        prefs.edit()
            .putBoolean(KEY_APP_LOCK_ENABLED, false)
            .putString(KEY_APP_LOCK_PIN, "")
            .apply()
    }

    fun setNightlyReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NIGHTLY_REMINDER_ENABLED, enabled).apply()
    }

    fun setNightlyReminderHour(hour: Int) {
        prefs.edit().putInt(KEY_NIGHTLY_REMINDER_HOUR, hour.coerceIn(6, 23)).apply()
    }

    fun setNotificationSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_SOUND_ENABLED, enabled).apply()
    }

    fun setNotificationVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_VIBRATION_ENABLED, enabled).apply()
    }

    fun validateAppLockPin(pin: String): Boolean {
        val savedPin = appLockPin()
        return savedPin.isNotBlank() && savedPin == pin.filter { it.isDigit() }
    }

    fun markMoneyEntryLogged() {
        val todayIso = java.time.LocalDate.now().toString()
        val nextCount = if (lastMoneyEntryLoggedOn() == todayIso) {
            prefs.getInt(KEY_TODAY_MONEY_ENTRY_COUNT, 0) + 1
        } else {
            1
        }
        prefs.edit()
            .putString(KEY_LAST_MONEY_ENTRY_DATE, todayIso)
            .putInt(KEY_TODAY_MONEY_ENTRY_COUNT, nextCount)
            .apply()
    }

    fun exportSnapshot(): PocketNativePreferenceSnapshot = PocketNativePreferenceSnapshot(
        languageCode = languageCode(),
        currencyCode = currencyCode(),
        hasSeenOnboarding = hasSeenOnboarding(),
        hasCompletedInitialSetup = hasCompletedInitialSetup(),
        hasSeenHomeTour = hasSeenHomeTour(),
        nightlyReminderEnabled = nightlyReminderEnabled(),
        nightlyReminderHour = nightlyReminderHour(),
        notificationSoundEnabled = notificationSoundEnabled(),
        notificationVibrationEnabled = notificationVibrationEnabled(),
        monthlyReminders = monthlyReminders(),
        loanDueReminders = loanDueReminders(),
        installmentPlans = installmentPlans(),
        collectionPlans = collectionPlans(),
        signedInUser = signedInUser(),
        billingEntitlement = billingEntitlement(),
    )

    fun importSnapshot(snapshot: PocketNativePreferenceSnapshot) {
        setHasSeenOnboarding(snapshot.hasSeenOnboarding)
        setHasCompletedInitialSetup(snapshot.hasCompletedInitialSetup)
        setHasSeenHomeTour(snapshot.hasSeenHomeTour)
        setLanguageCode(snapshot.languageCode)
        setCurrencyCode(snapshot.currencyCode)
        setNightlyReminderEnabled(snapshot.nightlyReminderEnabled)
        setNightlyReminderHour(snapshot.nightlyReminderHour)
        setNotificationSoundEnabled(snapshot.notificationSoundEnabled)
        setNotificationVibrationEnabled(snapshot.notificationVibrationEnabled)
        saveMonthlyReminders(snapshot.monthlyReminders)
        saveLoanDueReminders(snapshot.loanDueReminders.orEmpty())
        saveInstallmentPlans(snapshot.installmentPlans.orEmpty())
        saveCollectionPlans(snapshot.collectionPlans.orEmpty())
        saveSignedInUser(snapshot.signedInUser)
        saveBillingEntitlement(snapshot.billingEntitlement)
    }

    fun applySavedLanguage() {
        val current = languageCode()
        applyAppLanguage(current)
    }

    private fun applyAppLanguage(code: String) {
        val option = languageOptionFor(code)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(option.localeTag),
        )
    }

    companion object {
        private const val PREFS_NAME = "pocket_native_preferences"
        private const val KEY_LANGUAGE_CODE = "language_code"
        private const val KEY_CURRENCY_CODE = "currency_code"
        private const val KEY_ONBOARDING_SEEN = "onboarding_seen"
        private const val KEY_HOME_TOUR_SEEN = "home_tour_seen"
        private const val KEY_INITIAL_SETUP_DONE = "initial_setup_done"
        private const val KEY_MONTHLY_REMINDERS = "monthly_reminders"
        private const val KEY_LOAN_DUE_REMINDERS = "loan_due_reminders"
        private const val KEY_INSTALLMENT_PLANS = "installment_plans"
        private const val KEY_COLLECTION_PLANS = "collection_plans"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_APP_LOCK_PIN = "app_lock_pin"
        private const val KEY_NIGHTLY_REMINDER_ENABLED = "nightly_reminder_enabled"
        private const val KEY_NIGHTLY_REMINDER_HOUR = "nightly_reminder_hour"
        private const val KEY_NOTIFICATION_SOUND_ENABLED = "notification_sound_enabled"
        private const val KEY_NOTIFICATION_VIBRATION_ENABLED = "notification_vibration_enabled"
        private const val KEY_SIGNED_IN_USER = "signed_in_user"
        private const val KEY_BILLING_ENTITLEMENT = "billing_entitlement"
        private const val KEY_LAST_MONEY_ENTRY_DATE = "last_money_entry_date"
        private const val KEY_TODAY_MONEY_ENTRY_COUNT = "today_money_entry_count"

        const val DEFAULT_LANGUAGE_CODE = "en"
        const val DEFAULT_CURRENCY_CODE = "BDT"

        val LANGUAGE_OPTIONS: List<PocketNativeLanguageOption> = listOf(
            PocketNativeLanguageOption("en", "English", "English", "en", listOf("USD", "GBP", "AUD", "CAD", "SGD")),
            PocketNativeLanguageOption("bn", "Bangla", "বাংলা", "bn", listOf("BDT")),
            PocketNativeLanguageOption("hi", "Hindi", "हिन्दी", "hi", listOf("INR")),
            PocketNativeLanguageOption("ur", "Urdu", "اردو", "ur", listOf("PKR")),
            PocketNativeLanguageOption("ar", "Arabic", "العربية", "ar", listOf("AED", "SAR", "QAR")),
            PocketNativeLanguageOption("es", "Spanish", "Español", "es", listOf("EUR", "USD")),
            PocketNativeLanguageOption("fr", "French", "Français", "fr", listOf("EUR", "CHF")),
            PocketNativeLanguageOption("de", "German", "Deutsch", "de", listOf("EUR", "CHF")),
            PocketNativeLanguageOption("pt", "Portuguese", "Português", "pt", listOf("BRL", "EUR")),
            PocketNativeLanguageOption("it", "Italian", "Italiano", "it", listOf("EUR")),
            PocketNativeLanguageOption("tr", "Turkish", "Türkçe", "tr", listOf("TRY")),
            PocketNativeLanguageOption("ru", "Russian", "Русский", "ru", listOf("RUB")),
            PocketNativeLanguageOption("zh", "Chinese", "中文", "zh-CN", listOf("CNY")),
            PocketNativeLanguageOption("ja", "Japanese", "日本語", "ja", listOf("JPY")),
            PocketNativeLanguageOption("ko", "Korean", "한국어", "ko", listOf("KRW")),
            PocketNativeLanguageOption("id", "Indonesian", "Bahasa Indonesia", "id", listOf("IDR")),
        )

        val CURRENCY_OPTIONS: List<PocketNativeCurrencyOption> = listOf(
            PocketNativeCurrencyOption("BDT", "Bangladeshi Taka", "৳"),
            PocketNativeCurrencyOption("USD", "US Dollar", "$"),
            PocketNativeCurrencyOption("EUR", "Euro", "€"),
            PocketNativeCurrencyOption("GBP", "British Pound", "£"),
            PocketNativeCurrencyOption("INR", "Indian Rupee", "₹"),
            PocketNativeCurrencyOption("AED", "UAE Dirham", "د.إ"),
            PocketNativeCurrencyOption("SAR", "Saudi Riyal", "﷼"),
            PocketNativeCurrencyOption("QAR", "Qatari Riyal", "﷼"),
            PocketNativeCurrencyOption("PKR", "Pakistani Rupee", "₨"),
            PocketNativeCurrencyOption("JPY", "Japanese Yen", "¥"),
            PocketNativeCurrencyOption("CNY", "Chinese Yuan", "¥"),
            PocketNativeCurrencyOption("KRW", "South Korean Won", "₩"),
            PocketNativeCurrencyOption("SGD", "Singapore Dollar", "S$"),
            PocketNativeCurrencyOption("AUD", "Australian Dollar", "A$"),
            PocketNativeCurrencyOption("CAD", "Canadian Dollar", "C$"),
            PocketNativeCurrencyOption("TRY", "Turkish Lira", "₺"),
            PocketNativeCurrencyOption("RUB", "Russian Ruble", "₽"),
            PocketNativeCurrencyOption("BRL", "Brazilian Real", "R$"),
            PocketNativeCurrencyOption("IDR", "Indonesian Rupiah", "Rp"),
            PocketNativeCurrencyOption("CHF", "Swiss Franc", "CHF"),
        )

        private val SUPPORTED_LANGUAGE_CODES = LANGUAGE_OPTIONS.map { it.code }.toSet()
        private val SUPPORTED_CURRENCY_CODES = CURRENCY_OPTIONS.map { it.code }.toSet()

        fun languageOptionFor(code: String): PocketNativeLanguageOption {
            return LANGUAGE_OPTIONS.firstOrNull { it.code == code } ?: LANGUAGE_OPTIONS.first()
        }

        fun currencyOptionFor(code: String): PocketNativeCurrencyOption {
            return CURRENCY_OPTIONS.firstOrNull { it.code == code } ?: CURRENCY_OPTIONS.first()
        }

        private fun shiftInstallmentDate(currentDate: String, frequency: String): String {
            val base = runCatching { java.time.LocalDate.parse(currentDate) }.getOrElse { java.time.LocalDate.now() }
            return when (frequency.trim().uppercase()) {
                "YEARLY" -> base.plusYears(1)
                else -> base.plusMonths(1)
            }.toString()
        }
    }
}
