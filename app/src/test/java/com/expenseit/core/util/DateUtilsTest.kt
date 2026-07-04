package com.expenseit.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    @Test
    fun testNewId() {
        val id1 = DateUtils.newId()
        val id2 = DateUtils.newId()
        assertNotNull(id1)
        assertNotNull(id2)
        org.junit.Assert.assertNotEquals(id1, id2)
        assertEquals(36, id1.length) // Standard UUID length
    }

    @Test
    fun testFormatDate() {
        // Test with a known epoch millis (1717180800000 = June 1, 2024 12:00:00 AM UTC in standard time,
        // formatted according to IST local representation in the app display format)
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.JUNE, 1, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dateStr = DateUtils.formatDate(cal.timeInMillis)
        assertEquals("01 Jun 2024", dateStr)
    }

    @Test
    fun testIsoDateConversion() {
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.JUNE, 1, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val epoch = cal.timeInMillis
        val iso = DateUtils.toIsoDate(epoch)
        assertEquals("2024-06-01", iso)

        val parsedEpoch = DateUtils.fromIsoDate("2024-06-01")
        val parsedCal = Calendar.getInstance()
        parsedCal.timeInMillis = parsedEpoch
        assertEquals(2024, parsedCal.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, parsedCal.get(Calendar.MONTH))
        assertEquals(1, parsedCal.get(Calendar.DAY_OF_MONTH))
    }
}
