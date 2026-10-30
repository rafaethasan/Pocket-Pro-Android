package com.softx.app.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PocketTransactionParserTest {

    @Test
    fun testBkashPayment() {
        val sms = "Payment Tk 450.00 to Agora Superstore successful. Ref . Fee Tk 0.00. Balance Tk 2,100.00. TrxID 8A29147 at 30/10/2026 14:30"
        val parsed = PocketTransactionParser.parse(sms, "bKash")
        assertNotNull(parsed)
        assertEquals("expense", parsed?.mode)
        assertEquals(450.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("Agora Superstore", parsed?.party)
        assertEquals("groceries", parsed?.categoryKey)
        assertEquals("BKASH", parsed?.paymentMethod)
        assertEquals("8A29147", parsed?.referenceId)
    }

    @Test
    fun testBkashReceive() {
        val sms = "You have received Tk 1,500.00 from 01712345678. Fee Tk 0.00. Balance Tk 4,500.00. TrxID 9K38294 at 30/10/2026 10:15"
        val parsed = PocketTransactionParser.parse(sms, "bKash")
        assertNotNull(parsed)
        assertEquals("income", parsed?.mode)
        assertEquals(1500.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("01712345678", parsed?.party)
        assertEquals("BKASH", parsed?.paymentMethod)
        assertEquals("9K38294", parsed?.referenceId)
    }

    @Test
    fun testNagadMerchantPay() {
        val sms = "TxnID: 71BC430 Merchant Pay Tk 850.00 to Daily Shopping Successful. Balance: Tk 1,200.00."
        val parsed = PocketTransactionParser.parse(sms, "NAGAD")
        assertNotNull(parsed)
        assertEquals("expense", parsed?.mode)
        assertEquals(850.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("Daily Shopping", parsed?.party)
        assertEquals("groceries", parsed?.categoryKey)
        assertEquals("NAGAD", parsed?.paymentMethod)
        assertEquals("71BC430", parsed?.referenceId)
    }

    @Test
    fun testBankDebit() {
        val sms = "Acct *1234 debited for BDT 1,250.00 on 30-Oct-26 at KFC DHANMONDI. Avail Bal BDT 12,340.00. Ref 554321"
        val parsed = PocketTransactionParser.parse(sms, "CityBank")
        assertNotNull(parsed)
        assertEquals("expense", parsed?.mode)
        assertEquals(1250.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("KFC DHANMONDI", parsed?.party)
        assertEquals("food", parsed?.categoryKey)
        assertEquals("554321", parsed?.referenceId)
    }

    @Test
    fun testSalaryCredit() {
        val sms = "Acct *9876 credited with BDT 65,000.00 on 30-Oct-26 by SALARY OCT 2026. Avail Bal BDT 70,000.00"
        val parsed = PocketTransactionParser.parse(sms, "BRAC Bank")
        assertNotNull(parsed)
        assertEquals("income", parsed?.mode)
        assertEquals(65000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("salary", parsed?.categoryKey)
    }
}
