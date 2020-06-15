package com.firatyildiz.quiettime.helpers

import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.model.entities.QuietTime
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
                R.string.monday_short,
                R.string.tuesday_short,
                R.string.wednesday_short,
                R.string.thursday_short,
                R.string.friday_short,
                R.string.saturday_short,
                R.string.sunday_short
            )
        else
            listOf(
                R.string.sunday_short,
                R.string.monday_short,
                R.string.tuesday_short,
                R.string.wednesday_short,
                R.string.thursday_short,
                R.string.friday_short,
                R.string.saturday_short
            )
    }

    /**
     * Creates a string that only has the active days of the week
     * The days will be separated with ", ". ex. Mon, Tue, Wed
     *
     * @param dayNames Names of the days of the week, preferably short ones, please make sure there are 7 elements in this list
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

    /**
     * Will set the date of the calendar according to the current local time.
     * If the provided day index of the week is already past, the calendar will be set
     * to the same day of the week, next week
     *
     * @param calendar Calendar that has been set to the desired Locale.
     * @param dayIndex Index of the day of the week. First day of the week is 0.
     */
    fun setCalendarToValidDayOfWeek(calendar: Calendar, dayIndex: Int) {
        calendar.timeInMillis = System.currentTimeMillis()


    }

    /**
     * Returns the index of the current date of this calendar.
     *
     * NOTE: According to the GregorianCalendar, first day of the week is SUNDAY, which also equals to 1.
     * In this app, first day of the week will equal to 0
     *
     * @param calendar Calendar that has been set to the correct locale
     */
    fun getDayOfWeekAsDayIndex(calendar: Calendar): Int {
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return dayOfWeek - calendar.firstDayOfWeek
    }

    /**
     * Sets the calendar to the start or end time of this quiet time inside the current week of the calendar.
     * The date will change depending on locale. The date will be set to the next week if dayIndex is in the past.
     * ex. setting date to Wednesday when today is Saturday
     *
     * @param calendar Calendar that is set to today
     * @param dayIndex Index of the day of the week. First day of the weeks is 0.
     */
    fun setCalendarDateToQuietTime(
        calendar: Calendar,
        quietTime: QuietTime,
        dayIndex: Int,
        setToStartTime: Boolean
    ) {

        val todayTimeInMillis = calendar.timeInMillis

        calendar.apply {
            if (setToStartTime) {
                set(Calendar.HOUR_OF_DAY, quietTime.startTime / 60)
                set(Calendar.MINUTE, quietTime.startTime % 60)
            } else {
                set(Calendar.HOUR_OF_DAY, quietTime.endTime / 60)
                set(Calendar.MINUTE, quietTime.endTime % 60)
            }
            set(Calendar.SECOND, 0)
        }

        calendar.set(Calendar.DAY_OF_WEEK, dayIndex + calendar.firstDayOfWeek)

        if (calendar.timeInMillis < todayTimeInMillis)
            calendar.add(Calendar.DAY_OF_MONTH, 7)
    }
}

//val usCalendar = GregorianCalendar.getInstance(Locale.US)
//
//usCalendar.set(
//calendar.get(Calendar.YEAR),
//calendar.get(Calendar.MONTH),
//calendar.get(Calendar.DAY_OF_MONTH)
//)
//
//usCalendar.apply {
//    if(setToStartTime) {
//        set(Calendar.HOUR_OF_DAY, quietTime.startTime / 60)
//        set(Calendar.MINUTE, quietTime.startTime % 60)
//    } else {
//        set(Calendar.HOUR_OF_DAY, quietTime.endTime / 60)
//        set(Calendar.MINUTE, quietTime.endTime % 60)
//    }
//    set(Calendar.SECOND, 0)
//}
//
//val currentWeekDay = usCalendar.get(Calendar.DAY_OF_WEEK)
//var weekDayToScheduleTo = 1 + dayIndex
//
//
//// if the day we are setting this alarm to is already past, schedule it to the next week
//if(currentWeekDay > weekDayToScheduleTo) {
//    usCalendar.set(Calendar.DAY_OF_WEEK, weekDayToScheduleTo)
//    usCalendar.add(Calendar.DAY_OF_MONTH, 7)
//} else {
//    if(calendar.firstDayOfWeek == Calendar.MONDAY && weekDayToScheduleTo == 8) {
//        usCalendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
//        if(currentWeekDay != Calendar.SUNDAY)
//            usCalendar.add(Calendar.DAY_OF_MONTH, 7)
//    } else {
//        usCalendar.set(Calendar.DAY_OF_WEEK, weekDayToScheduleTo)
//    }
//}
//
//calendar.set(
//usCalendar.get(Calendar.YEAR),
//usCalendar.get(Calendar.MONTH),
//usCalendar.get(Calendar.DAY_OF_MONTH),
//usCalendar.get(Calendar.HOUR_OF_DAY),
//usCalendar.get(Calendar.MINUTE),
//usCalendar.get(Calendar.SECOND)
//)