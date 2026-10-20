package com.softx.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PocketBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            PocketReminderScheduler.ensureDailyReminder(context)
        }
    }
}
