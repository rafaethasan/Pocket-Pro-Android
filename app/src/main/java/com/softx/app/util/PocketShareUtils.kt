package com.softx.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object PocketShareUtils {

    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.softx.app.native"

    fun buildReminderMessage(
        counterparty: String,
        amount: Double,
        currencySymbol: String = "৳",
        dueDate: String = "",
        note: String = "",
    ): String {
        val name = counterparty.trim().ifBlank { "প্রিয় গ্রাহক/বন্ধু" }
        val formattedAmount = if (amount % 1.0 == 0.0) {
            String.format("%,d", amount.toLong())
        } else {
            String.format("%,.2f", amount)
        }

        val sb = java.lang.StringBuilder()
        sb.append("আসসালামু আলাইকুম $name,\n\n")
        sb.append("পকেট প্রো (Pocket Pro) অ্যাপের হিসাব অনুযায়ী আপনার কাছে $currencySymbol$formattedAmount এর একটি বাকি/পাওনা হিসাব রয়েছে। সুবিধাজনক সময়ে পরিশোধ বা আপডেট করে দিলে বিশেষভাবে উপকৃত হব।\n\n")

        if (dueDate.isNotBlank()) {
            sb.append("🗓️ পরিশোধের তারিখ: $dueDate\n")
        }
        if (note.isNotBlank()) {
            sb.append("📝 বিবরণ: $note\n")
        }

        sb.append("\n✨ ব্যক্তিগত ও ব্যবসায়িক হিসাব রাখুন Pocket Pro অ্যাপে:\n")
        sb.append(PLAY_STORE_URL)

        return sb.toString()
    }

    fun shareLoanReminder(
        context: Context,
        counterparty: String,
        amount: Double,
        currencySymbol: String = "৳",
        dueDate: String = "",
        note: String = "",
    ) {
        val message = buildReminderMessage(
            counterparty = counterparty,
            amount = amount,
            currencySymbol = currencySymbol,
            dueDate = dueDate,
            note = note,
        )

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Send Payment Reminder via")
        context.startActivity(chooser)
    }

    fun shareViaWhatsApp(
        context: Context,
        counterparty: String,
        amount: Double,
        currencySymbol: String = "৳",
        dueDate: String = "",
        note: String = "",
        phoneNumber: String = "",
    ) {
        val message = buildReminderMessage(
            counterparty = counterparty,
            amount = amount,
            currencySymbol = currencySymbol,
            dueDate = dueDate,
            note = note,
        )

        val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
        val intent = if (cleanPhone.isNotBlank()) {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
            }
        } else {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
            }
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            shareLoanReminder(context, counterparty, amount, currencySymbol, dueDate, note)
        }
    }
}
