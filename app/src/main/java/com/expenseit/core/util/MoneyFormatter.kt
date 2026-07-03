package com.expenseit.core.util

import java.text.DecimalFormat

/**
 * Indian number format for INR currency — port of money.go/format.js.
 * Formats integer paise into ₹XX,XX,XXX.XX (Indian grouping: last 3, then 2s).
 */
object MoneyFormatter {

    private val inrFormat = DecimalFormat("##,##,##,##,##,##,##0.00")

    /**
     * Format minor units (paise) to display string.
     * e.g., 125050 → "₹1,250.50"
     */
    fun formatINR(minor: Long): String {
        val rupees = minor / 100.0
        return "₹${inrFormat.format(rupees)}"
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
