package com.firatyildiz.quiettime.helpers

import com.firatyildiz.quiettime.R
import java.util.*
import kotlin.math.abs

/**
 * @author Fırat Yıldız
 *
 * In this application, there will be alarms set on specific days of the week.
 * To tell which days have alarms, I am using an integer.
 * The first 7 bits of the integer are treated as days.
 * ex. Int = 16 means that there's an alarm set on Friday, since 16 = 0000100b
 * ex. Int = 80 means there's an alarm set on Friday and Sunday, since 80 = 0000101b
 */
object DateTimeLocalizationHelper {

    /**
     * Will return the resource Id for the week day's name
     */
    fun getStartDayOfWeekId(locale: Locale): Int {
        val calendar = GregorianCalendar.getInstance(locale)

        return when (calendar.firstDayOfWeek) {
            Calendar.MONDAY -> R.string.monday
            Calendar.SUNDAY -> R.string.sunday
            else -> R.string.no_date
        }
    }

    /**
     * Returns the week day names in a list, will take locale into account
     *
     * ex. If Locale.US, Sunday will be first, if Locale.UK, Monday will be first
     */
    fun getWeekStringIds(locale: Locale): List<Int> {
        val startDay = getStartDayOfWeekId(locale)

        return if (startDay == R.string.monday)
            listOf(
                R.string.monday,
                R.string.tuesday,
                R.string.wednesday,
                R.string.thursday,
                R.string.friday,
                R.string.saturday,
                R.string.sunday
            )
        else
            listOf(
                R.string.sunday,
                R.string.monday,
                R.string.tuesday,
                R.string.wednesday,
                R.string.thursday,
                R.string.friday,
                R.string.saturday
            )
    }

    /**
     * Creates a string that only has the active days of the week
     * The days will be separated with ", ". ex. Mon, Tue, Wed
     *
     * @param dayNames Names of the days of the week, preferably short ones, please makes sure there are 7 elements in this list
     * @param days Integer for the active days, ex. 7 means Mon, Tue, Wed are active (1110000b)
     * @return A string with active days . ex. Mon, Tue, Wed for days = 7
     */
    fun getActiveWeekDaysAsString(dayNames: List<String>, days: Int): String {
        if (dayNames.size != 7)
            throw Exception("There needs to be 7 elements inside the dayNames list")

        var currentDayBit = 1
        var index = 0
        val activeDays: StringBuilder = StringBuilder()

        while (index < 7) {
            if (days and currentDayBit != 0) {
                if (activeDays.isNotEmpty())
                    activeDays.append(", ")

                activeDays.append(dayNames[index])
            }

            index++
            currentDayBit = currentDayBit shl 1
        }

        return activeDays.toString()
    }

    /**
     * Returns the given minutes as a readable digital time.
     *
     * ex. 600 will return as 10:00
     */
    fun getMinutesAsReadableString(minutes: Int): String {
        val time = StringBuilder()
        val min = abs(minutes) % 1440

        time.append((min / 60 % 1440).toString().padStart(2, '0'))
        time.append(':')
        time.append((min % 60).toString().padStart(2, '0'))
        return time.toString()
    }

    /**
     * Returns the given time range as a readable string
     *
     * ex. 600, 900 will return as 10:00 - 15:00
     */
    fun getTimeRangeAsReadableString(startTimeAsMinutes: Int, endTimeAsMinutes: Int): String {

        val time = StringBuilder()

        time.append(getMinutesAsReadableString(startTimeAsMinutes))
        time.append(" - ")
        time.append(getMinutesAsReadableString(endTimeAsMinutes))

        return time.toString()
    }
}