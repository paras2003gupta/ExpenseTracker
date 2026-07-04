package com.expenseit.core.util

import kotlin.math.abs

/**
 * Indian number format for INR currency — port of money.go/format.js.
 * Formats integer paise into ₹XX,XX,XXX.XX (Indian grouping: last 3, then 2s).
 */
object MoneyFormatter {

    /**
     * Format minor units (paise) to display string.
     * e.g., 125050 → "₹1,250.50"
     */
    fun formatINR(minor: Long): String {
        val sign = if (minor < 0) "-" else ""
        val absMinor = abs(minor)
        val rupees = absMinor / 100
        val paise = absMinor % 100
        
        val rupeesStr = formatIndianGrouping(rupees)
        val paiseStr = String.format("%02d", paise)
        return "${sign}₹$rupeesStr.$paiseStr"
    }

    private fun formatIndianGrouping(amount: Long): String {
        val s = amount.toString()
        if (s.length <= 3) return s
        val last3 = s.substring(s.length - 3)
        var remaining = s.substring(0, s.length - 3)
        val sb = java.lang.StringBuilder()
        while (remaining.length > 2) {
            sb.insert(0, "," + remaining.substring(remaining.length - 2))
            remaining = remaining.substring(0, remaining.length - 2)
        }
        return remaining + sb.toString() + "," + last3
    }

    /**
     * Convert rupees (Double) to paise (Long).
     * e.g., 1250.50 → 125050
     */
    fun rupeesToMinor(rupees: Double): Long {
        return Math.round(rupees * 100)
    }

    /**
     * Convert rupees string to paise.
     */
    fun rupeesToMinor(rupees: String): Long {
        val value = rupees.toDoubleOrNull() ?: 0.0
        return rupeesToMinor(value)
    }

    /**
     * Format with sign for balance display.
     * Positive = "you are owed", negative = "you owe".
     */
    fun formatBalance(minor: Long): String {
        return if (minor >= 0) {
            "+${formatINR(minor)}"
        } else {
            "-${formatINR(-minor)}"
        }
    }
}
