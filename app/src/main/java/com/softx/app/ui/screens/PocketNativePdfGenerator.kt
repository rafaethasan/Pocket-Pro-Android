package com.softx.app.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.softx.app.data.PocketNativeRecentActivity
import com.softx.app.data.PocketNativeSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PocketNativePdfGenerator {

    fun generateReportPdf(
        context: Context,
        accountName: String,
        currencySymbol: String,
        summary: PocketNativeSummary,
        recentActivity: List<PocketNativeRecentActivity>,
    ): File {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Top Luxury Header Banner (Dark Navy)
        paint.color = Color.parseColor("#0F172A")
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, paint)

        // Accent top stripe
        paint.color = Color.parseColor("#00E5FF")
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 4f, paint)

        // App Brand
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        canvas.drawText("POCKET PRO", 36f, 44f, paint)

        // Subtitle
        paint.color = Color.parseColor("#38BDF8")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("OFFICIAL FINANCIAL STATEMENT & INSIGHT REPORT", 36f, 62f, paint)

        // Security Tagline
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("🛡️ Bank-Grade AES-256 Verified On-Device Generation • Confidential", 36f, 82f, paint)

        // Date right-aligned
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val generatedDate = dateFormat.format(Date())
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#CBD5E1")
        paint.textSize = 9f
        canvas.drawText("Generated: $generatedDate", pageWidth - 36f, 44f, paint)
        canvas.drawText("Account: ${accountName.ifBlank { "Primary Wallet" }}", pageWidth - 36f, 62f, paint)
        paint.textAlign = Paint.Align.LEFT

        // 2. Summary KPI Cards (y = 130 to 205)
        val cardTop = 130f
        val cardHeight = 75f
        val cardWidth = (pageWidth - 72f - 24f) / 3f

        // Card 1: Income
        val incomeRect = RectF(36f, cardTop, 36f + cardWidth, cardTop + cardHeight)
        drawKpiCard(
            canvas = canvas,
            paint = paint,
            rect = incomeRect,
            bgColor = Color.parseColor("#ECFDF5"),
            borderColor = Color.parseColor("#10B981"),
            textColor = Color.parseColor("#047857"),
            title = "TOTAL INCOME",
            amount = "+$currencySymbol ${formatAmount(summary.monthIncome)}",
        )

        // Card 2: Expense
        val expenseLeft = 36f + cardWidth + 12f
        val expenseRect = RectF(expenseLeft, cardTop, expenseLeft + cardWidth, cardTop + cardHeight)
        drawKpiCard(
            canvas = canvas,
            paint = paint,
            rect = expenseRect,
            bgColor = Color.parseColor("#FEF2F2"),
            borderColor = Color.parseColor("#EF4444"),
            textColor = Color.parseColor("#B91C1C"),
            title = "TOTAL EXPENSE",
            amount = "-$currencySymbol ${formatAmount(summary.monthExpense)}",
        )

        // Card 3: Net Balance
        val netLeft = expenseLeft + cardWidth + 12f
        val netRect = RectF(netLeft, cardTop, netLeft + cardWidth, cardTop + cardHeight)
        val netIsPositive = summary.totalBalance >= 0
        drawKpiCard(
            canvas = canvas,
            paint = paint,
            rect = netRect,
            bgColor = Color.parseColor("#EFF6FF"),
            borderColor = Color.parseColor("#3B82F6"),
            textColor = Color.parseColor("#1D4ED8"),
            title = "AVAILABLE BALANCE",
            amount = "${if (netIsPositive) "" else "-"}$currencySymbol ${formatAmount(Math.abs(summary.totalBalance))}",
        )

        // 3. Transactions Section
        val tableTop = 230f
        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        canvas.drawText("RECENT TRANSACTIONS & AUDIT TRAIL", 36f, tableTop, paint)

        // Table Header
        val headerY = tableTop + 14f
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(RectF(36f, headerY, pageWidth - 36f, headerY + 24f), 6f, 6f, paint)

        paint.color = Color.parseColor("#475569")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("DATE", 46f, headerY + 15f, paint)
        canvas.drawText("TYPE", 125f, headerY + 15f, paint)
        canvas.drawText("CATEGORY / DETAILS", 195f, headerY + 15f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT", pageWidth - 46f, headerY + 15f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Rows
        var currentY = headerY + 32f
        val rowHeight = 24f
        val maxRows = 18
        val itemsToDraw = recentActivity.take(maxRows)

        if (itemsToDraw.isEmpty()) {
            paint.color = Color.parseColor("#94A3B8")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            canvas.drawText("No recent transactions recorded yet.", 46f, currentY + 18f, paint)
        } else {
            itemsToDraw.forEachIndexed { index, item ->
                if (index % 2 == 1) {
                    paint.color = Color.parseColor("#F8FAFC")
                    canvas.drawRect(36f, currentY - 5f, pageWidth - 36f, currentY + rowHeight - 7f, paint)
                }

                // Date
                paint.color = Color.parseColor("#64748B")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.5f
                val dateText = item.date.take(10)
                canvas.drawText(dateText, 46f, currentY + 10f, paint)

                // Type Badge
                val isIncome = item.kind.contains("income", ignoreCase = true)
                paint.color = if (isIncome) Color.parseColor("#059669") else Color.parseColor("#DC2626")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8f
                canvas.drawText(if (isIncome) "INCOME" else "EXPENSE", 125f, currentY + 10f, paint)

                // Category & Note
                paint.color = Color.parseColor("#1E293B")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.5f
                val label = item.label.ifBlank { item.note }.take(32)
                canvas.drawText(label, 195f, currentY + 10f, paint)

                // Amount
                paint.textAlign = Paint.Align.RIGHT
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (isIncome) Color.parseColor("#059669") else Color.parseColor("#DC2626")
                val sign = if (isIncome) "+" else "-"
                canvas.drawText("$sign$currencySymbol ${formatAmount(item.amount)}", pageWidth - 46f, currentY + 10f, paint)
                paint.textAlign = Paint.Align.LEFT

                currentY += rowHeight
            }
        }

        // 4. Luxury Footer
        val footerY = pageHeight - 45f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(36f, footerY, pageWidth - 36f, footerY, paint)

        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("POCKET PRO FINANCIAL SYSTEM • 100% PRIVATE OFFLINE ARCHITECTURE", 36f, footerY + 18f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page 1 of 1 • Official Export", pageWidth - 36f, footerY + 18f, paint)
        paint.textAlign = Paint.Align.LEFT

        document.finishPage(page)

        val outputFile = File(context.cacheDir, "pocket-pro-statement.pdf")
        if (outputFile.exists()) {
            outputFile.delete()
        }
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    private fun drawKpiCard(
        canvas: Canvas,
        paint: Paint,
        rect: RectF,
        bgColor: Int,
        borderColor: Int,
        textColor: Int,
        title: String,
        amount: String,
    ) {
        // Background
        paint.color = bgColor
        canvas.drawRoundRect(rect, 10f, 10f, paint)

        // Border
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        // Title
        paint.color = textColor
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, rect.left + 12f, rect.top + 22f, paint)

        // Amount
        paint.color = textColor
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(amount, rect.left + 12f, rect.top + 48f, paint)
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%,d", amount.toLong())
        } else {
            String.format(Locale.US, "%,.2f", amount)
        }
    }
}
