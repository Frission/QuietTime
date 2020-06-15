package com.firatyildiz.quiettime

import com.firatyildiz.quiettime.helpers.DateTimeLocalizationHelper
import com.firatyildiz.quiettime.model.QuietTimeConstants
import com.firatyildiz.quiettime.model.entities.QuietTime
import junit.framework.Assert.assertEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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

    @Test
    fun getDayIndexOfCalendar_returnsAsExpected() {

        var calendar = GregorianCalendar.getInstance(Locale.UK)

        // apparently month number is 0 based
        // 1st of June, 2020, is a Monday, so we should receive a 0 in Locale.UK
        calendar.set(2020, 5, 1, 12, 0, 0)

        assertEquals(0, DateTimeLocalizationHelper.getDayOfWeekAsDayIndex(calendar))

        calendar = GregorianCalendar.getInstance(Locale.US)

        // apparently month number is 0 based
        // 1st of June, 2020, is a Monday, so we should receive a 1 in Locale.US
        calendar.set(2020, 5, 1, 12, 0, 0)

        assertEquals(1, DateTimeLocalizationHelper.getDayOfWeekAsDayIndex(calendar))
    }

    @Test
    fun setCalendarToQuietTime_worksAsExpected_inLocaleUS() {
        val calendar = GregorianCalendar.getInstance(Locale.US)
        val expectedCalendar = GregorianCalendar.getInstance(Locale.US)

        // date is monday
        // schedule to monday
        calendar.set(2020, 5, 1, 12, 0, 0)
        var quietTime = QuietTime("Test", 127, 12 * 60 + 30, 15 * 60, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 1, 12, 30, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 1, true)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is monday
        // schedule to tuesday
        calendar.set(2020, 5, 1, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 2, 15, 0, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 2, true)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is monday
        // schedule to tuesday
        calendar.set(2020, 5, 1, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 2, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 2, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is monday
        // schedule to saturday
        calendar.set(2020, 5, 1, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 6, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 6, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is saturday
        // schedule to saturday
        calendar.set(2020, 5, 6, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 6, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 6, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is sunday
        // schedule to saturday
        calendar.set(2020, 5, 7, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 13, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 6, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is sunday
        // schedule to monday
        calendar.set(2020, 5, 7, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 8, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 1, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is sunday
        // schedule to wednesday
        calendar.set(2020, 5, 7, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 10, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 3, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))
    }

    @Test
    fun setCalendarToQuietTime_worksAsExpected_inLocaleUK() {
        val calendar = GregorianCalendar.getInstance(Locale.UK)
        val expectedCalendar = GregorianCalendar.getInstance(Locale.UK)

        // date is monday
        // schedule to monday
        calendar.set(2020, 5, 1, 12, 0, 0)
        var quietTime =
            QuietTime("UK First", 127, 12 * 60 + 30, 15 * 60, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 1, 12, 30, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 0, true)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is monday
        // schedule to wednesday
        calendar.set(2020, 5, 1, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 3, 15, 0, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 2, true)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))


        // date is sunday
        // schedule to sunday
        calendar.set(2020, 5, 7, 12, 0, 0)
        quietTime =
            QuietTime("Sunday Test 2 UK", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 4, 7, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 6, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is sunday
        // schedule to saturday
        calendar.set(2020, 5, 7, 12, 0, 0)
        quietTime =
            QuietTime("Sunday Test 3 UK", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 13, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 5, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is sunday
        // schedule to monday
        calendar.set(2020, 5, 7, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 8, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 0, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))

        // date is sunday
        // schedule to wednesday
        calendar.set(2020, 5, 7, 12, 0, 0)
        quietTime = QuietTime("Test", 127, 15 * 60, 17 * 60 + 47, QuietTimeConstants.VIBRATE)
        expectedCalendar.set(2020, 5, 10, 17, 47, 0)

        DateTimeLocalizationHelper.setCalendarDateToQuietTime(calendar, quietTime, 2, false)

        assertThat(
            calendar.get(Calendar.DAY_OF_MONTH),
            equalTo(expectedCalendar.get(Calendar.DAY_OF_MONTH))
        )
        assertThat(calendar.get(Calendar.HOUR), equalTo(expectedCalendar.get(Calendar.HOUR)))
        assertThat(calendar.get(Calendar.MINUTE), equalTo(expectedCalendar.get(Calendar.MINUTE)))
    }
}