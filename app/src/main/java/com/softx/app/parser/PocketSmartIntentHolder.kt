package com.softx.app.parser

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object PocketSmartIntentHolder {
    var pendingTransaction by mutableStateOf<ParsedTransaction?>(null)
    var pendingQuickAction by mutableStateOf<String?>(null)

    fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.getStringExtra("action")
        if (action == "open_add") {
            pendingQuickAction = "open_add"
        } else if (action == "smart_sms_entry") {
            val mode = intent.getStringExtra("mode") ?: "expense"
            val amount = intent.getDoubleExtra("amount", 0.0)
            val party = intent.getStringExtra("party") ?: ""
            val category = intent.getStringExtra("category") ?: "other"
            val paymentMethod = intent.getStringExtra("paymentMethod") ?: "CASH"
            val referenceId = intent.getStringExtra("referenceId") ?: ""
            val rawText = intent.getStringExtra("rawText") ?: ""

            pendingTransaction = ParsedTransaction(
                mode = mode,
                amount = amount,
                party = party,
                categoryKey = category,
                paymentMethod = paymentMethod,
                referenceId = referenceId,
                rawText = rawText,
                source = "SMS",
                summary = "Detected from SMS: $amount at $party",
            )
        }
    }

    fun consumeQuickAction(): String? {
        val action = pendingQuickAction
        pendingQuickAction = null
        return action
    }

    fun consumePending(): ParsedTransaction? {
        val current = pendingTransaction
        pendingTransaction = null
        return current
    }
}
