package com.firatyildiz.quiettime

import com.firatyildiz.quiettime.helpers.DateTimeLocalizationHelper
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * @author Fırat Yıldız
 */
class DateTimeLocalizationHelperUnitTests {

    val weekDays: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    @Test
    fun weekStartDay_IsMonday_InLocaleUK() {
        val startDay = DateTimeLocalizationHelper.getStartDayOfWeekId(Locale.UK)

        assertEquals(startDay, R.string.monday)
    }

    @Test
    fun weekStartDay_IsSunday_InLocaleUS() {
        val startDay = DateTimeLocalizationHelper.getStartDayOfWeekId(Locale.US)

        assertEquals(startDay, R.string.sunday)
    }

    @Test
    fun getActiveWeekDays_returnsAsExpected() {
        var expected = "Mon, Tue, Wed"
        var result = DateTimeLocalizationHelper.getActiveWeekDaysAsString(weekDays, 7)

        assertEquals(expected, result)

        expected = "Mon, Tue, Fri, Sat"
        result = DateTimeLocalizationHelper.getActiveWeekDaysAsString(weekDays, 1 + 2 + 16 + 32)

        assertEquals(expected, result)

        expected = "Mon"
        result = DateTimeLocalizationHelper.getActiveWeekDaysAsString(weekDays, 1)

        assertEquals(expected, result)

        expected = "Tue"
        result = DateTimeLocalizationHelper.getActiveWeekDaysAsString(weekDays, 2)

        assertEquals(expected, result)

        expected = "Mon, Tue, Wed, Thu, Fri, Sat, Sun"
        result = DateTimeLocalizationHelper.getActiveWeekDaysAsString(weekDays, 127)

        assertEquals(expected, result)
    }

    @Test
    fun getTimeRangeAsReadableString_returnsAsExpected() {
        var expected = "10:00 - 15:00"
        var result = DateTimeLocalizationHelper.getTimeRangeAsReadableString(600, 900)

        assertEquals(expected, result)

        expected = "12:00 - 18:00"
        result = DateTimeLocalizationHelper.getTimeRangeAsReadableString(12 * 60, 18 * 60)

        assertEquals(expected, result)

        expected = "14:47 - 16:36"
        result = DateTimeLocalizationHelper.getTimeRangeAsReadableString(14 * 60 + 47, 16 * 60 + 36)

        assertEquals(expected, result)

        expected = "22:00 - 00:00"
        result = DateTimeLocalizationHelper.getTimeRangeAsReadableString(22 * 60, 24 * 60)

        assertEquals(expected, result)

        expected = "22:00 - 04:00"
        result = DateTimeLocalizationHelper.getTimeRangeAsReadableString(22 * 60, 4 * 60)

        assertEquals(expected, result)
    }
}