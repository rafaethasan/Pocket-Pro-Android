package com.softx.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.softx.app.data.PocketNativePreferencesStore
import java.util.Calendar

private const val POCKET_REMINDER_CHANNEL_ID = "pocket_daily_reminder"
private const val POCKET_REMINDER_CHANNEL_NAME = "Pocket Pro Reminders"
private const val POCKET_REMINDER_REQUEST_CODE = 2200

object PocketReminderScheduler {
    fun ensureDailyReminder(context: Context) {
        createChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildReminderPendingIntent(context)
        alarmManager.cancel(pendingIntent)
        val preferencesStore = PocketNativePreferencesStore(context)
        if (!preferencesStore.nightlyReminderEnabled()) {
            return
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextReminderAtMillis(preferencesStore.nightlyReminderHour()),
            AlarmManager.INTERVAL_DAY,
            pendingIntent,
        )
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(buildReminderPendingIntent(context))
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channel = NotificationChannel(
            POCKET_REMINDER_CHANNEL_ID,
            POCKET_REMINDER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Pocket Pro smart nightly money reminder"
        }
        manager.createNotificationChannel(channel)
    }

    internal fun reminderChannelId(): String = POCKET_REMINDER_CHANNEL_ID

    internal fun buildReminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, PocketReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            POCKET_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextReminderAtMillis(hourOfDay: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay.coerceIn(6, 23))
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
