package com.expenseit.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun testFormatINR() {
        assertEquals("₹1,250.50", MoneyFormatter.formatINR(125050))
        assertEquals("₹0.00", MoneyFormatter.formatINR(0))
        assertEquals("₹10,00,000.00", MoneyFormatter.formatINR(100000000))
    }

    @Test
    fun testRupeesToMinor() {
        assertEquals(125050L, MoneyFormatter.rupeesToMinor(1250.50))
        assertEquals(0L, MoneyFormatter.rupeesToMinor(0.0))
        assertEquals(100000L, MoneyFormatter.rupeesToMinor(1000.0))
    }

    @Test
    fun testRupeesToMinorString() {
        assertEquals(125050L, MoneyFormatter.rupeesToMinor("1250.50"))
        assertEquals(0L, MoneyFormatter.rupeesToMinor("invalid"))
        assertEquals(100000L, MoneyFormatter.rupeesToMinor("1000"))
    }

    @Test
    fun testFormatBalance() {
        assertEquals("+₹125.00", MoneyFormatter.formatBalance(12500))
        assertEquals("-₹125.00", MoneyFormatter.formatBalance(-12500))
        assertEquals("+₹0.00", MoneyFormatter.formatBalance(0))
    }
}
