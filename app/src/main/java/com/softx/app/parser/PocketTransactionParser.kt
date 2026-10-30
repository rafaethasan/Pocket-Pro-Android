package com.softx.app.parser

import java.util.Locale
import java.util.regex.Pattern

data class ParsedTransaction(
    val mode: String, // "income" or "expense"
    val amount: Double,
    val party: String,
    val categoryKey: String,
    val paymentMethod: String,
    val referenceId: String = "",
    val rawText: String = "",
    val source: String = "",
    val summary: String = "",
)

object PocketTransactionParser {

    private val AMOUNT_PATTERN = Pattern.compile(
        """(?:Tk|BDT|৳|USD|\$|INR|Rs|EUR|€|GBP|£)\.?\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
        Pattern.CASE_INSENSITIVE
    )

    private val TRX_ID_PATTERN = Pattern.compile(
        """(?:TrxID|TxnID|Txn Id|Trx ID|Ref|Reference)[:\s]+([A-Za-z0-9]+)""",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(rawText: String, sender: String? = null): ParsedTransaction? {
        val text = rawText.trim()
        if (text.isBlank()) return null

        val cleanSender = sender.orEmpty().trim().lowercase(Locale.ROOT)
        val lowerText = text.lowercase(Locale.ROOT)

        // 1. Detect source provider
        val source = when {
            cleanSender.contains("bkash") || lowerText.contains("bkash") -> "bKash"
            cleanSender.contains("nagad") || lowerText.contains("nagad") -> "Nagad"
            cleanSender.contains("upay") || lowerText.contains("upay") -> "Upay"
            cleanSender.contains("rocket") || lowerText.contains("rocket") || cleanSender.contains("16216") -> "Rocket"
            cleanSender.contains("city") || cleanSender.contains("brac") || cleanSender.contains("dbbl") ||
                cleanSender.contains("ebl") || cleanSender.contains("scb") || cleanSender.contains("bank") ||
                lowerText.contains("acct") || lowerText.contains("account") || lowerText.contains("card") -> "Bank"
            else -> "Digital"
        }

        // 2. Parse bKash formats
        if (source == "bKash" || lowerText.contains("bkash")) {
            return parseBkash(text, source)
        }

        // 3. Parse Nagad formats
        if (source == "Nagad" || lowerText.contains("nagad")) {
            return parseNagad(text, source)
        }

        // 4. Parse Bank or General Card / POS formats
        return parseBankOrGeneral(text, source)
    }

    private fun parseBkash(text: String, source: String): ParsedTransaction? {
        val lower = text.lowercase(Locale.ROOT)
        val trxId = extractTrxId(text)

        // bKash Received Money / Cash In
        if (lower.contains("you have received") || lower.contains("cash in")) {
            val amount = extractAmount(text) ?: return null
            val fromMatcher = Pattern.compile("""from\s+([0-9A-Za-z+\s]+?)(?:\.|\s+fee|\s+ref|\s+balance|\s+trxid|$)""", Pattern.CASE_INSENSITIVE).matcher(text)
            val party = if (fromMatcher.find()) fromMatcher.group(1)?.trim().orEmpty() else "bKash Sender"
            return ParsedTransaction(
                mode = "income",
                amount = amount,
                party = party.ifBlank { "bKash Received" },
                categoryKey = "other_income",
                paymentMethod = "BKASH",
                referenceId = trxId,
                rawText = text,
                source = source,
                summary = "Received ৳$amount via bKash from $party"
            )
        }

        // bKash Merchant Payment
        if (lower.contains("payment") || lower.contains("merchant pay")) {
            val amount = extractAmount(text) ?: return null
            val toMatcher = Pattern.compile("""to\s+([^.]+?)(?:\s+successful|\.|\s+fee|\s+ref|\s+balance|\s+trxid|$)""", Pattern.CASE_INSENSITIVE).matcher(text)
            val party = if (toMatcher.find()) toMatcher.group(1)?.trim().orEmpty() else "Merchant"
            val category = resolveCategory(party)
            return ParsedTransaction(
                mode = "expense",
                amount = amount,
                party = party.ifBlank { "Merchant Payment" },
                categoryKey = category,
                paymentMethod = "BKASH",
                referenceId = trxId,
                rawText = text,
                source = source,
                summary = "Paid ৳$amount via bKash to $party"
            )
        }

        // bKash Send Money
        if (lower.contains("send money")) {
            val amount = extractAmount(text) ?: return null
            val toMatcher = Pattern.compile("""to\s+([0-9A-Za-z+\s]+?)(?:\s+successful|\.|\s+fee|\s+ref|\s+balance|\s+trxid|$)""", Pattern.CASE_INSENSITIVE).matcher(text)
            val party = if (toMatcher.find()) toMatcher.group(1)?.trim().orEmpty() else "Receiver"
            return ParsedTransaction(
                mode = "expense",
                amount = amount,
                party = party.ifBlank { "Sent Money" },
                categoryKey = "transfer",
                paymentMethod = "BKASH",
                referenceId = trxId,
                rawText = text,
                source = source,
                summary = "Sent ৳$amount via bKash to $party"
            )
        }

        // bKash Cash Out
        if (lower.contains("cash out")) {
            val amount = extractAmount(text) ?: return null
            val toMatcher = Pattern.compile("""to\s+([0-9A-Za-z+\s]+?)(?:\s+successful|\.|\s+fee|\s+ref|\s+balance|\s+trxid|$)""", Pattern.CASE_INSENSITIVE).matcher(text)
            val party = if (toMatcher.find()) toMatcher.group(1)?.trim().orEmpty() else "Agent"
            return ParsedTransaction(
                mode = "expense",
                amount = amount,
                party = "Cash Out ($party)",
                categoryKey = "cash_out",
                paymentMethod = "BKASH",
                referenceId = trxId,
                rawText = text,
                source = source,
                summary = "Cash out ৳$amount via bKash agent $party"
            )
        }

        return null
    }

    private fun parseNagad(text: String, source: String): ParsedTransaction? {
        val lower = text.lowercase(Locale.ROOT)
        val trxId = extractTrxId(text)

        // Nagad Cash In / Received
        if (lower.contains("cash in") || lower.contains("received")) {
            val amount = extractAmount(text) ?: return null
            val fromMatcher = Pattern.compile("""from\s+([^.]+?)(?:\s+successful|\.|\s+balance|\s+fee|$)""", Pattern.CASE_INSENSITIVE).matcher(text)
            val party = if (fromMatcher.find()) fromMatcher.group(1)?.trim().orEmpty() else "Nagad Sender"
            return ParsedTransaction(
                mode = "income",
                amount = amount,
                party = party.ifBlank { "Nagad Received" },
                categoryKey = "other_income",
                paymentMethod = "NAGAD",
                referenceId = trxId,
                rawText = text,
                source = source,
                summary = "Received ৳$amount via Nagad from $party"
            )
        }

        // Nagad Merchant Pay
        if (lower.contains("merchant pay") || lower.contains("payment")) {
            val amount = extractAmount(text) ?: return null
            val toMatcher = Pattern.compile("""to\s+([^.]+?)(?:\s+successful|\.|\s+balance|\s+fee|$)""", Pattern.CASE_INSENSITIVE).matcher(text)
            val party = if (toMatcher.find()) toMatcher.group(1)?.trim().orEmpty() else "Merchant"
            val category = resolveCategory(party)
            return ParsedTransaction(
                mode = "expense",
                amount = amount,
                party = party.ifBlank { "Merchant Payment" },
                categoryKey = category,
                paymentMethod = "NAGAD",
                referenceId = trxId,
                rawText = text,
                source = source,
                summary = "Paid ৳$amount via Nagad to $party"
            )
        }

        // Nagad Send Money
        if (lower.contains("send money")) {
            val amount = extractAmount(text) ?: return null
            val toMatcher = Pattern.compile("""to\s+([^.]+?)(?:\s+successful|\.|\s+balance|\s+fee|$)""", Pattern.CASE_INSENSITIVE).matcher(text)
            val party = if (toMatcher.find()) toMatcher.group(1)?.trim().orEmpty() else "Receiver"
            return ParsedTransaction(
                mode = "expense",
                amount = amount,
                party = party.ifBlank { "Sent Money" },
                categoryKey = "transfer",
                paymentMethod = "NAGAD",
                referenceId = trxId,
                rawText = text,
                source = source,
                summary = "Sent ৳$amount via Nagad to $party"
            )
        }

        // Nagad Cash Out
        if (lower.contains("cash out")) {
            val amount = extractAmount(text) ?: return null
            val toMatcher = Pattern.compile("""to\s+([^.]+?)(?:\s+successful|\.|\s+balance|\s+fee|$)""", Pattern.CASE_INSENSITIVE).matcher(text)
            val party = if (toMatcher.find()) toMatcher.group(1)?.trim().orEmpty() else "Agent"
            return ParsedTransaction(
                mode = "expense",
                amount = amount,
                party = "Cash Out ($party)",
                categoryKey = "cash_out",
                paymentMethod = "NAGAD",
                referenceId = trxId,
                rawText = text,
                source = source,
                summary = "Cash out ৳$amount via Nagad agent $party"
            )
        }

        return null
    }

    private fun parseBankOrGeneral(text: String, source: String): ParsedTransaction? {
        val lower = text.lowercase(Locale.ROOT)
        val amount = extractAmount(text) ?: return null
        val trxId = extractTrxId(text)

        val isDebit = lower.contains("debited") || lower.contains("spent") || lower.contains("paid") ||
            lower.contains("purchase") || lower.contains("withdrawn") || lower.contains("charged")

        val isCredit = lower.contains("credited") || lower.contains("deposited") ||
            lower.contains("received") || lower.contains("salary")

        val mode = if (isCredit && !isDebit) "income" else "expense"

        // Extract merchant / party
        val partyMatcher = Pattern.compile("""(?:at|to|by|for)\s+([A-Za-z0-9\s._&-]+?)(?:\.|\s+on|\s+avail|\s+bal|\s+ref|\s+txn|$)""", Pattern.CASE_INSENSITIVE).matcher(text)
        val rawParty = if (partyMatcher.find()) partyMatcher.group(1)?.trim().orEmpty() else ""
        val party = rawParty.ifBlank {
            if (mode == "income") "Deposit / Bank Credit" else "Bank / Card Expense"
        }

        val category = resolveCategory(party + " " + text)

        val paymentMethod = when {
            lower.contains("card") || lower.contains("visa") || lower.contains("mastercard") -> "CARD"
            lower.contains("acct") || lower.contains("account") -> "BANK"
            else -> "CARD"
        }

        return ParsedTransaction(
            mode = mode,
            amount = amount,
            party = party,
            categoryKey = category,
            paymentMethod = paymentMethod,
            referenceId = trxId,
            rawText = text,
            source = source,
            summary = "${if (mode == "income") "Credited" else "Debited"} ৳$amount at $party"
        )
    }

    private fun extractAmount(text: String): Double? {
        val matcher = AMOUNT_PATTERN.matcher(text)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "") ?: return null
            return amountStr.toDoubleOrNull()
        }
        return null
    }

    private fun extractTrxId(text: String): String {
        val matcher = TRX_ID_PATTERN.matcher(text)
        return if (matcher.find()) matcher.group(1)?.trim().orEmpty() else ""
    }

    fun resolveCategory(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            // Food & Dining
            listOf("kfc", "pizza", "burger", "star kabab", "restaurant", "cafe", "foodpanda", "pathao food", "coffee", "baker", "dine", "kitchen", "biryani", "khana", "shawarma", "tea", "starbucks", "mcdonalds", "subway").any { lower.contains(it) } -> "food"

            // Groceries & Supermarkets
            listOf("agora", "shwapno", "meenabazar", "grocer", "superstore", "chaldal", "bazaar", "bazar", "unimart", "daily shopping", "almas", "walmart", "target", "tesco", "costco", "carrefour", "whole foods").any { lower.contains(it) } -> "groceries"

            // Transport & Travel
            listOf("uber", "pathao", "cng", "petrol", "fuel", "gas refill", "railway", "shohoz", "bus", "flyer", "air", "biman", "rickshaw", "metro", "toll", "padma bridge", "lyft", "grab", "airline", "train").any { lower.contains(it) } -> "transport"

            // Shopping & Apparel
            listOf("daraz", "aarong", "yellow", "artisan", "apex", "bata", "cloth", "fashion", "mall", "gadget", "star tech", "ryans", "pickaboo", "aliexpress", "amazon", "ebay", "apple", "nike", "zara", "h&m").any { lower.contains(it) } -> "shopping"

            // Health & Medical
            listOf("pharmacy", "pharma", "hospital", "clinic", "diagnostic", "doctor", "labaid", "square hospital", "evercare", "medicine", "ibn sina", "popular").any { lower.contains(it) } -> "health"

            // Utilities & Bills
            listOf("desco", "dpdc", "wasa", "titas", "nesco", "electricity", "water", "gas", "internet", "link3", "carnival", "amber", "bill", "recharge", "airtel", "grameenphone", "banglalink", "robi").any { lower.contains(it) } -> "bills"

            // Entertainment & Subscriptions
            listOf("netflix", "spotify", "cineplex", "blockbuster", "movie", "steam", "playstation", "game", "youtube").any { lower.contains(it) } -> "entertainment"

            // Salary & Career
            listOf("salary", "payroll", "stipend", "bonus", "freelance", "upwork", "fiverr").any { lower.contains(it) } -> "salary"

            // Education
            listOf("school", "college", "university", "tuition", "brac", "nsu", "aiub", "coursera", "udemy").any { lower.contains(it) } -> "education"

            else -> "other"
        }
    }
}
