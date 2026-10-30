package com.softx.app.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PocketReceiptParserTest {

    @Test
    fun testParseAgoraSuperstoreReceipt() {
        val rawReceipt = """
            AGORA SUPERSTORE
            Gulshan-2, Dhaka
            BIN: 000123456-0101
            Date: 28/10/2026 14:32
            Mushak-6.3
            Item 1: Milk 1L - 90.00
            Item 2: Eggs 1 Dozen - 150.00
            Item 3: Bread - 60.00
            Item 4: Olive Oil - 1120.00
            Subtotal: 1420.00
            VAT (5%): 71.00
            Total Payable: 1491.00
            Cash Paid: 1500.00
            Change: 9.00
            Thank You For Shopping With Us!
        """.trimIndent()

        val parsed = PocketReceiptParser.parse(rawReceipt)
        assertNotNull(parsed)
        assertEquals("AGORA SUPERSTORE", parsed?.merchant)
        assertEquals(1491.00, parsed?.amount ?: 0.0, 0.01)
        assertEquals("2026-10-28", parsed?.date)
        assertEquals("groceries", parsed?.categoryKey)
    }

    @Test
    fun testParseStarKababReceipt() {
        val rawReceipt = """
            STAR KABAB & RESTAURANT
            Dhanmondi, Dhaka
            Tel: 01711000000
            Cash Memo
            Date: 29-10-2026
            Mutton Kacchi x 2 = 600
            Borhani x 2 = 160
            Mineral Water = 40
            Salad = 50
            Grand Total: Tk 850.00
            Paid by Card
            Please visit again!
        """.trimIndent()

        val parsed = PocketReceiptParser.parse(rawReceipt)
        assertNotNull(parsed)
        assertEquals("STAR KABAB & RESTAURANT", parsed?.merchant)
        assertEquals(850.00, parsed?.amount ?: 0.0, 0.01)
        assertEquals("2026-10-29", parsed?.date)
        assertEquals("food", parsed?.categoryKey)
    }

    @Test
    fun testParseLabaidPharmaReceipt() {
        val rawReceipt = """
            LABAID PHARMA
            Invoice No: LAB-99214
            Date: 2026-10-30
            Doctor: Dr. Rahman
            Napa Extend 1 Box - 120.00
            Sergel 20mg 2 Strips - 200.00
            Monas 10mg - 300.00
            NET TOTAL: 620.00
            Cash: 1000.00
            Change Due: 380.00
        """.trimIndent()

        val parsed = PocketReceiptParser.parse(rawReceipt)
        assertNotNull(parsed)
        assertEquals("LABAID PHARMA", parsed?.merchant)
        assertEquals(620.00, parsed?.amount ?: 0.0, 0.01)
        assertEquals("2026-10-30", parsed?.date)
        assertEquals("health", parsed?.categoryKey)
    }

    @Test
    fun testParseStarbucksReceipt() {
        val rawReceipt = """
            STARBUCKS COFFEE #1042
            Seattle, WA
            10/24/2026 08:15 AM
            Caramel Macchiato  $5.75
            Butter Croissant   $3.95
            Subtotal           $9.70
            Tax                $0.92
            Total              $10.62
            Visa ending in 4124
            Thank you!
        """.trimIndent()

        val parsed = PocketReceiptParser.parse(rawReceipt)
        assertNotNull(parsed)
        assertEquals("STARBUCKS COFFEE 1042", parsed?.merchant)
        assertEquals(10.62, parsed?.amount ?: 0.0, 0.01)
        assertEquals("food", parsed?.categoryKey)
    }
}
