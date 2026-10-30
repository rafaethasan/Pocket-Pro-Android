package com.softx.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.softx.app.MainActivity
import com.softx.app.parser.PocketTransactionParser

class PocketSmsReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "pocket_transactions"
        const val CHANNEL_NAME = "Smart Transaction Alerts"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Automatic transaction alerts from bKash, Nagad, and Banks"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        ensureChannel(context)

        for (sms in messages) {
            val sender = sms.displayOriginatingAddress ?: ""
            val body = sms.displayMessageBody ?: continue

            val parsed = PocketTransactionParser.parse(body, sender) ?: continue

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("action", "smart_sms_entry")
                putExtra("mode", parsed.mode)
                putExtra("amount", parsed.amount)
                putExtra("party", parsed.party)
                putExtra("category", parsed.categoryKey)
                putExtra("paymentMethod", parsed.paymentMethod)
                putExtra("referenceId", parsed.referenceId)
                putExtra("rawText", parsed.rawText)
            }

            val requestCode = (System.currentTimeMillis() % 10000).toInt()
            val pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (parsed.mode == "income") {
                "⚡ Money Received: ৳${parsed.amount}"
            } else {
                "⚡ Expense Detected: ৳${parsed.amount}"
            }

            val contentText = "${parsed.summary}. Tap to review and confirm."

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText("$contentText\n\nSource: ${parsed.source}\nRef: ${parsed.referenceId.ifBlank { "N/A" }}"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            try {
                NotificationManagerCompat.from(context).notify(requestCode, notification)
            } catch (_: SecurityException) {
                // Permission not granted
            }
        }
    }
}
