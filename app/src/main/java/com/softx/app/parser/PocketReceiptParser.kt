package com.softx.app.parser

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

data class ParsedReceipt(
    val merchant: String,
    val amount: Double,
    val date: String,
    val categoryKey: String,
    val rawText: String,
    val summary: String,
)

object PocketReceiptParser {

    private val IGNORE_MERCHANT_KEYWORDS = listOf(
        "tax invoice", "cash memo", "invoice", "receipt", "bill", "mushak", "vat", "bin",
        "tin", "welcome", "thank you", "phone", "tel", "cell", "email", "address", "road",
        "dhaka", "bangladesh", "counter", "pos", "terminal", "order", "table", "token", "date",
    )

    private val TOTAL_LINE_PATTERN = Pattern.compile(
        """(?:total\s*payable|grand\s*total|net\s*total|bill\s*total|amount\s*payable|amount\s*due|সর্বমোট|মোট|total|payable|subtotal)[\s:=-]+(?:tk|bdt|৳|\$|usd|eur|€|inr|rs)?\.?\s*([0-9]{1,3}(?:,[0-9]{3})+(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
        Pattern.CASE_INSENSITIVE
    )

    private val GENERIC_AMOUNT_PATTERN = Pattern.compile(
        """(?:tk|bdt|৳|\$|usd|eur|€|inr|rs)?\.?\s*([0-9]{1,3}(?:,[0-9]{3})+(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
        Pattern.CASE_INSENSITIVE
    )

    private val DATE_PATTERNS = listOf(
        // 28/10/2026 or 28-10-2026
        Pattern.compile("""\b([0-3]?[0-9])[-/.]([0-1]?[0-9])[-/.](20[2-3][0-9])\b"""),
        // 2026-10-28 or 2026/10/28
        Pattern.compile("""\b(20[2-3][0-9])[-/.]([0-1]?[0-9])[-/.]([0-3]?[0-9])\b"""),
    )

    fun parse(rawText: String): ParsedReceipt? {
        val text = rawText.trim()
        if (text.isBlank()) return null

        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return null

        val merchant = extractMerchant(lines)
        val amount = extractTotalAmount(lines, text) ?: return null
        val date = extractDate(text) ?: LocalDate.now().toString()
        val categoryKey = PocketTransactionParser.resolveCategory("$merchant $text")

        return ParsedReceipt(
            merchant = merchant.ifBlank { "Receipt Purchase" },
            amount = amount,
            date = date,
            categoryKey = categoryKey,
            rawText = text,
            summary = "Scanned receipt: $merchant (৳$amount)",
        )
    }

    private fun extractMerchant(lines: List<String>): String {
        for (line in lines.take(5)) {
            val clean = line.replace(Regex("""[^a-zA-Z0-9\s&'-]"""), "").trim()
            if (clean.length < 3) continue
            val lower = clean.lowercase(Locale.ROOT)
            val isIgnored = IGNORE_MERCHANT_KEYWORDS.any { lower.contains(it) }
            if (!isIgnored && !clean.all { it.isDigit() || it.isWhitespace() }) {
                return clean
            }
        }
        return lines.firstOrNull()?.take(30).orEmpty()
    }

    private fun extractTotalAmount(lines: List<String>, fullText: String): Double? {
        // Priority 1: Search from bottom to top for explicit "Total / Grand Total / Net Total" lines
        for (line in lines.reversed()) {
            val matcher = TOTAL_LINE_PATTERN.matcher(line)
            if (matcher.find()) {
                val amtStr = matcher.group(1)?.replace(",", "") ?: continue
                val amt = amtStr.toDoubleOrNull()
                if (amt != null && amt > 0.0) {
                    return amt
                }
            }
        }

        // Priority 2: Look for line containing "Total" with any amount
        for (line in lines.reversed()) {
            val lower = line.lowercase(Locale.ROOT)
            if (lower.contains("total") || lower.contains("payable") || lower.contains("সর্বমোট")) {
                val matcher = GENERIC_AMOUNT_PATTERN.matcher(line)
                var lastAmount: Double? = null
                while (matcher.find()) {
                    val candidate = matcher.group(1)?.replace(",", "")?.toDoubleOrNull()
                    if (candidate != null && candidate > 0.0) {
                        lastAmount = candidate
                    }
                }
                if (lastAmount != null) return lastAmount
            }
        }

        // Priority 3: Fallback to the largest monetary amount found on the receipt (excluding years/dates)
        val allAmounts = mutableListOf<Double>()
        val genericMatcher = Pattern.compile("""\b([0-9]{1,3}(?:,[0-9]{3})*\.[0-9]{2}|[0-9]{2,6}\.[0-9]{2})\b""").matcher(fullText)
        while (genericMatcher.find()) {
            val amt = genericMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
            if (amt != null && amt in 1.0..1000000.0) {
                allAmounts.add(amt)
            }
        }
        return allAmounts.maxOrNull()
    }

    private fun extractDate(text: String): String? {
        for (pattern in DATE_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                try {
                    val g1 = matcher.group(1) ?: continue
                    val g2 = matcher.group(2) ?: continue
                    val g3 = matcher.group(3) ?: continue

                    return if (g1.length == 4) {
                        // yyyy-MM-dd
                        val year = g1.toInt()
                        val month = g2.toInt().coerceIn(1, 12)
                        val day = g3.toInt().coerceIn(1, 31)
                        LocalDate.of(year, month, day).toString()
                    } else {
                        // dd-MM-yyyy
                        val day = g1.toInt().coerceIn(1, 31)
                        val month = g2.toInt().coerceIn(1, 12)
                        val year = g3.toInt()
                        LocalDate.of(year, month, day).toString()
                    }
                } catch (_: Exception) {
                }
            }
        }
        return null
    }
}
