package com.expenseit.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Date utilities and ID generation.
 */
object DateUtils {

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale("en", "IN"))
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("en", "IN"))
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun newId(): String = UUID.randomUUID().toString()

    fun now(): Long = System.currentTimeMillis()

    fun formatDate(epochMillis: Long): String {
        return displayFormat.format(Date(epochMillis))
    }

    fun formatMonthYear(epochMillis: Long): String {
        return monthYearFormat.format(Date(epochMillis))
    }

    fun toIsoDate(epochMillis: Long): String {
        return isoFormat.format(Date(epochMillis))
    }

    fun fromIsoDate(iso: String): Long {
        return try {
            isoFormat.parse(iso)?.time ?: now()
        } catch (_: Exception) {
            now()
        }
    }

    /** Returns epoch millis for the first day of the given month offset (0 = this month, -1 = last month). */
    fun monthStart(offset: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Returns epoch millis for the last moment of the last day of the given month offset. */
    fun monthEnd(offset: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset + 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis - 1
    }

    /** Creates epoch millis for a specific day of the current month. */
    fun thisMonth(day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, day.coerceIn(1, cal.getActualMaximum(Calendar.DAY_OF_MONTH)))
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** Creates epoch millis for a specific day of the previous month. */
    fun lastMonth(day: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, day.coerceIn(1, cal.getActualMaximum(Calendar.DAY_OF_MONTH)))
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
