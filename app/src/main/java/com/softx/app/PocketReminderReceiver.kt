package com.softx.app

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.softx.app.data.PocketNativePreferencesStore
import com.softx.app.data.PocketNativeCollectionPlan
import com.softx.app.data.PocketNativeMonthlyReminder
import com.softx.app.data.pocketInstallmentReminderEnabled
import com.softx.app.data.pocketLoanReminderRemainingAmount
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class PocketReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        PocketReminderScheduler.createChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            2201,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val preferencesStore = PocketNativePreferencesStore(context)
        if (!preferencesStore.nightlyReminderEnabled()) {
            return
        }
        val reminders = preferencesStore.monthlyReminders()
        val loanReminders = preferencesStore.loanDueReminders()
        val installmentPlans = preferencesStore.installmentPlans().filter { it.status == "active" && pocketInstallmentReminderEnabled(it) }
        val collectionPlans = preferencesStore.normalizeCollectionPlansForToday().filter { it.status == "active" && it.autoReminderEnabled }
        val today = LocalDate.now()
        val todayDate = today.toString()
        val tomorrowDate = today.plusDays(1).toString()
        val loggedToday = preferencesStore.lastMoneyEntryLoggedOn() == todayDate
        val todayEntryCount = preferencesStore.todayMoneyEntryCount()
        val reminderLeadDays = listOf(0L, 1L, 3L, 5L, 10L)
        val monthlyReminderGroups = reminderLeadDays.associateWith { leadDays ->
            reminders.filter { daysUntilMonthlyReminder(it, today) == leadDays }
        }
        val collectionReminderGroups = reminderLeadDays.associateWith { leadDays ->
            collectionPlans.filter { daysUntilCollectionReminder(it, today) == leadDays }
        }
        val dueToday = monthlyReminderGroups[0L].orEmpty()
        val collectDueToday = collectionReminderGroups[0L].orEmpty()
        val dueTomorrow = monthlyReminderGroups[1L].orEmpty()
        val collectDueTomorrow = collectionReminderGroups[1L].orEmpty()
        val dueInThreeDays = monthlyReminderGroups[3L].orEmpty()
        val collectInThreeDays = collectionReminderGroups[3L].orEmpty()
        val dueInFiveDays = monthlyReminderGroups[5L].orEmpty()
        val collectInFiveDays = collectionReminderGroups[5L].orEmpty()
        val dueInTenDays = monthlyReminderGroups[10L].orEmpty()
        val collectInTenDays = collectionReminderGroups[10L].orEmpty()
        val loanDueToday = loanReminders.filter { it.dueDate == todayDate }
        val loanDueTomorrow = loanReminders.filter { it.dueDate == tomorrowDate }
        val overdueLoans = loanReminders.filter { it.dueDate < todayDate }
        val installmentDueToday = installmentPlans.filter { it.nextDueDate == todayDate }
        val installmentDueTomorrow = installmentPlans.filter { it.nextDueDate == tomorrowDate }
        val overdueInstallments = installmentPlans.filter { it.nextDueDate < todayDate }
        val dueSignalCount = overdueInstallments.size +
            overdueLoans.size +
            installmentDueToday.size +
            installmentDueTomorrow.size +
            loanDueToday.size +
            loanDueTomorrow.size +
            dueToday.size +
            collectDueToday.size +
            dueTomorrow.size +
            collectDueTomorrow.size +
            dueInThreeDays.size +
            collectInThreeDays.size +
            dueInFiveDays.size +
            collectInFiveDays.size +
            dueInTenDays.size +
            collectInTenDays.size

        val contentText = when {
            !loggedToday && dueSignalCount > 0 -> "Add today's money entries. You also have $dueSignalCount reminder signals to review."
            !loggedToday -> "No money entry logged today. Add your expense, income, or loan update before sleep."
            loggedToday && dueSignalCount > 0 -> "Good job. $todayEntryCount entries logged today. Review your upcoming dues for tomorrow."
            overdueInstallments.isNotEmpty() -> "Overdue installment: ${overdueInstallments.joinToString(", ") { it.title }}"
            overdueLoans.isNotEmpty() -> "Overdue follow-up: ${overdueLoans.joinToString(", ") { loanReminderSummary(it) }}"
            installmentDueToday.isNotEmpty() -> "Installment due today: ${installmentDueToday.joinToString(", ") { it.title }}"
            loanDueToday.isNotEmpty() -> "Loan follow-up today: ${loanDueToday.joinToString(", ") { loanReminderSummary(it) }}"
            dueToday.isNotEmpty() -> "Today: ${dueToday.joinToString(", ") { it.title }}"
            collectDueToday.isNotEmpty() -> "Collect today: ${collectDueToday.joinToString(", ") { it.title }}"
            installmentDueTomorrow.isNotEmpty() -> "Installment tomorrow: ${installmentDueTomorrow.joinToString(", ") { it.title }}"
            loanDueTomorrow.isNotEmpty() -> "Loan follow-up tomorrow: ${loanDueTomorrow.joinToString(", ") { loanReminderSummary(it) }}"
            dueTomorrow.isNotEmpty() -> "Tomorrow: ${dueTomorrow.joinToString(", ") { it.title }}"
            collectDueTomorrow.isNotEmpty() -> "Collect tomorrow: ${collectDueTomorrow.joinToString(", ") { it.title }}"
            dueInThreeDays.isNotEmpty() -> "In 3 days: ${dueInThreeDays.joinToString(", ") { it.title }}"
            collectInThreeDays.isNotEmpty() -> "Collect in 3 days: ${collectInThreeDays.joinToString(", ") { it.title }}"
            dueInFiveDays.isNotEmpty() -> "In 5 days: ${dueInFiveDays.joinToString(", ") { it.title }}"
            collectInFiveDays.isNotEmpty() -> "Collect in 5 days: ${collectInFiveDays.joinToString(", ") { it.title }}"
            dueInTenDays.isNotEmpty() -> "In 10 days: ${dueInTenDays.joinToString(", ") { it.title }}"
            collectInTenDays.isNotEmpty() -> "Collect in 10 days: ${collectInTenDays.joinToString(", ") { it.title }}"
            else -> "Take one minute tonight to review your income, expenses, and goals."
        }
        val bigText = buildString {
            if (!loggedToday) {
                append("No money entry logged today. Add your expense, income, loan, or payback before the day ends.")
            } else {
                append("You logged $todayEntryCount money ${if (todayEntryCount == 1) "entry" else "entries"} today. Great job staying updated.")
            }
            if (overdueInstallments.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Overdue installment: ")
                append(overdueInstallments.joinToString(" • ") { "${it.title} ${it.nextDueDate}" })
            }
            if (overdueLoans.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Overdue follow-up: ")
                append(overdueLoans.joinToString(" • ") { loanReminderLine(it) })
            }
            if (installmentDueToday.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Installment due today: ")
                append(installmentDueToday.joinToString(" • ") { "${it.title} ${it.installmentAmount.toInt()}" })
            }
            if (loanDueToday.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Loan due today: ")
                append(loanDueToday.joinToString(" • ") { loanReminderLine(it) })
            }
            if (dueToday.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Due today: ")
                append(dueToday.joinToString(" • ") { "${it.dayOfMonth} ${it.title}" })
            }
            if (collectDueToday.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Collect today: ")
                append(collectDueToday.joinToString(" • ") { collectionReminderLine(it, 0) })
            }
            if (loanDueTomorrow.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Loan tomorrow: ")
                append(loanDueTomorrow.joinToString(" • ") { loanReminderLine(it) })
            }
            if (installmentDueTomorrow.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Installment tomorrow: ")
                append(installmentDueTomorrow.joinToString(" • ") { "${it.title} ${it.installmentAmount.toInt()}" })
            }
            if (dueTomorrow.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Coming tomorrow: ")
                append(dueTomorrow.joinToString(" • ") { monthlyReminderLine(it, 1) })
            }
            if (collectDueTomorrow.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Collect tomorrow: ")
                append(collectDueTomorrow.joinToString(" • ") { collectionReminderLine(it, 1) })
            }
            if (dueInThreeDays.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Coming in 3 days: ")
                append(dueInThreeDays.joinToString(" • ") { monthlyReminderLine(it, 3) })
            }
            if (collectInThreeDays.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Collect in 3 days: ")
                append(collectInThreeDays.joinToString(" • ") { collectionReminderLine(it, 3) })
            }
            if (dueInFiveDays.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Coming in 5 days: ")
                append(dueInFiveDays.joinToString(" • ") { monthlyReminderLine(it, 5) })
            }
            if (collectInFiveDays.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Collect in 5 days: ")
                append(collectInFiveDays.joinToString(" • ") { collectionReminderLine(it, 5) })
            }
            if (dueInTenDays.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Coming in 10 days: ")
                append(dueInTenDays.joinToString(" • ") { monthlyReminderLine(it, 10) })
            }
            if (collectInTenDays.isNotEmpty()) {
                if (isNotBlank()) append("\n")
                append("Collect in 10 days: ")
                append(collectInTenDays.joinToString(" • ") { collectionReminderLine(it, 10) })
            }
            if (isNotBlank()) {
                append("\n")
            }
            append("Take one minute tonight to keep Pocket Pro complete for today.")
        }

        val notificationBuilder = NotificationCompat.Builder(context, PocketReminderScheduler.reminderChannelId())
            .setSmallIcon(R.drawable.ic_pocket_launcher)
            .setContentTitle(if (!loggedToday) "Pocket Pro nightly check-in" else "Pocket Pro smart reminder")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
        val soundEnabled = preferencesStore.notificationSoundEnabled()
        val vibrationEnabled = preferencesStore.notificationVibrationEnabled()
        when {
            soundEnabled && vibrationEnabled -> notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
            soundEnabled -> notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            vibrationEnabled -> notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            else -> notificationBuilder.setSilent(true)
        }
        val notification = notificationBuilder.build()

        NotificationManagerCompat.from(context).notify(2200, notification)
    }

    private fun loanReminderSummary(reminder: com.softx.app.data.PocketNativeLoanDueReminder): String {
        val counterparty = reminder.counterparty.ifBlank { "someone" }
        return if (reminder.kind == "lent") {
            "collect from $counterparty"
        } else {
            "pay $counterparty"
        }
    }

    private fun loanReminderLine(reminder: com.softx.app.data.PocketNativeLoanDueReminder): String {
        val counterparty = reminder.counterparty.ifBlank { "someone" }
        val remainingAmount = pocketLoanReminderRemainingAmount(reminder).toInt()
        return if (reminder.kind == "lent") {
            "${reminder.dueDate} collect $remainingAmount from $counterparty"
        } else {
            "${reminder.dueDate} pay $remainingAmount to $counterparty"
        }
    }

    private fun daysUntilMonthlyReminder(
        reminder: PocketNativeMonthlyReminder,
        today: LocalDate,
    ): Long {
        val dueDate = nextMonthlyReminderDate(reminder, today)
        return ChronoUnit.DAYS.between(today, dueDate)
    }

    private fun nextMonthlyReminderDate(
        reminder: PocketNativeMonthlyReminder,
        today: LocalDate,
    ): LocalDate {
        val safeDay = reminder.dayOfMonth.coerceIn(1, 28)
        val currentMonth = YearMonth.from(today)
        val currentDue = currentMonth.atDay(safeDay)
        return if (!currentDue.isBefore(today)) {
            currentDue
        } else {
            currentMonth.plusMonths(1).atDay(safeDay)
        }
    }

    private fun monthlyReminderLine(
        reminder: PocketNativeMonthlyReminder,
        daysLeft: Int,
    ): String {
        val suffix = when (daysLeft) {
            0 -> "today"
            1 -> "tomorrow"
            else -> "in $daysLeft days"
        }
        return "${reminder.title} on day ${reminder.dayOfMonth} ($suffix)"
    }

    private fun daysUntilCollectionReminder(
        plan: PocketNativeCollectionPlan,
        today: LocalDate,
    ): Long {
        val dueDate = runCatching { LocalDate.parse(plan.nextCollectionDate) }.getOrElse { today }
        return ChronoUnit.DAYS.between(today, dueDate)
    }

    private fun collectionReminderLine(
        plan: PocketNativeCollectionPlan,
        daysLeft: Int,
    ): String {
        val suffix = when (daysLeft) {
            0 -> "today"
            1 -> "tomorrow"
            else -> "in $daysLeft days"
        }
        return "${plan.title} ${plan.currentDueAmount.toInt()} from ${plan.counterparty} ($suffix)"
    }
}
