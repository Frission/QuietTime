package com.firatyildiz.quiettime.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.firatyildiz.quiettime.helpers.DateTimeLocalizationHelper
import com.firatyildiz.quiettime.model.entities.QuietTime
import timber.log.Timber
import java.util.*

/**
 * @author Fırat Yıldız
 *
 * Created on 14/06/2020
 */
class QuietTimeAlarmManager(val context: Context) {


    companion object {
        const val SILENT_MODE = "silentMode"
    }

    fun createAlarmsForQuietTime(quietTime: QuietTime, currentLocale: Locale): Boolean {
        Timber.d("creating alarms for ${quietTime.title}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (alarmManager == null)
            return false

        val calendar = GregorianCalendar.getInstance(currentLocale)
        val timeInMillis = System.currentTimeMillis()

        if (quietTime.startTime == quietTime.endTime) {
            // if this condition is true, the phone should be silent for the entire day
            // thus we only need to schedule 1 alarm for each day

            // since this alarm will fire every day it doesn't matter which day we start scheduling from
            for (i in 0..6) {
                if (quietTime.days and (1 shl i) != 0) {
                    // the day is active, schedule an alarm

                    calendar.apply { this.timeInMillis = timeInMillis }

                    val pendingIntent = createQuietTimePendingIntent(quietTime, i, false)
                    DateTimeLocalizationHelper.setCalendarDateToQuietTime(
                        calendar,
                        quietTime,
                        i,
                        true
                    )
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                    )
                }
            }

        } else if (quietTime.startTime < quietTime.endTime) {
            // a normal alarm where start time is before the end time

            for (i in 0..6) {
                if (quietTime.days and (1 shl i) != 0) {
                    // the day is active, schedule an alarm

                    calendar.apply { this.timeInMillis = timeInMillis }

                    var pendingIntent = createQuietTimePendingIntent(quietTime, i, false)
                    DateTimeLocalizationHelper.setCalendarDateToQuietTime(
                        calendar,
                        quietTime,
                        i,
                        true
                    )
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                    )

                    calendar.apply { this.timeInMillis = timeInMillis }

                    pendingIntent = createQuietTimePendingIntent(quietTime, i, true)
                    DateTimeLocalizationHelper.setCalendarDateToQuietTime(
                        calendar,
                        quietTime,
                        i,
                        false
                    )
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                    )
                }
            }

        } else {
            // an alarm where the end time is in the next day

            for (i in 0..6) {
                if (quietTime.days and (1 shl i) != 0) {
                    // the day is active, schedule an alarm

                    calendar.apply { this.timeInMillis = timeInMillis }

                    var pendingIntent = createQuietTimePendingIntent(quietTime, i, false)
                    DateTimeLocalizationHelper.setCalendarDateToQuietTime(
                        calendar,
                        quietTime,
                        i,
                        true
                    )
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                    )

                    calendar.apply { this.timeInMillis = timeInMillis }

                    // the end time is in the next day
                    val dayIndex = i + 1 % 7

                    pendingIntent = createQuietTimePendingIntent(quietTime, dayIndex, true)
                    DateTimeLocalizationHelper.setCalendarDateToQuietTime(
                        calendar,
                        quietTime,
                        dayIndex,
                        false
                    )

                    // calendar was set to the start of the week, add 7 days
                    calendar.add(Calendar.DAY_OF_MONTH, 7)

                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                    )
                }
            }
        }

        return true
    }

    fun deleteAlarmsForQuietTime(quietTime: QuietTime) {
        Timber.d("deleting alarms for ${quietTime.title}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (alarmManager == null)
            return

        if (quietTime.startTime == quietTime.endTime) {
            // if this condition is true, the phone should be silent for the entire day
            // thus we only need to schedule 1 alarm for each day

            // since this alarm will fire every day it doesn't matter which day we start scheduling from
            for (i in 0..6) {
                if (quietTime.days and (1 shl i) != 0) {
                    // the day is active, schedule an alarm


                    val pendingIntent = getQuietTimePendingIntent(quietTime, i, false)

                    if (pendingIntent != null)
                        alarmManager.cancel(pendingIntent)
                    else
                        Timber.w("the pending intents for ${quietTime.title} are lost!")
                }
            }

        } else if (quietTime.startTime < quietTime.endTime) {
            // a normal alarm where start time is before the end time

            for (i in 0..6) {
                if (quietTime.days and (1 shl i) != 0) {
                    // the day is active, schedule an alarm

                    var pendingIntent = getQuietTimePendingIntent(quietTime, i, false)

                    if (pendingIntent != null)
                        alarmManager.cancel(pendingIntent)
                    else
                        Timber.w("the pending intents for ${quietTime.title} are lost!")

                    pendingIntent = getQuietTimePendingIntent(quietTime, i, true)

                    if (pendingIntent != null)
                        alarmManager.cancel(pendingIntent)
                    else
                        Timber.w("the pending intents for ${quietTime.title} are lost!")
                }
            }

        } else {
            // an alarm where the end time is in the next day

            for (i in 0..6) {
                if (quietTime.days and (1 shl i) != 0) {
                    // the day is active, schedule an alarm

                    var pendingIntent = getQuietTimePendingIntent(quietTime, i, false)

                    if (pendingIntent != null)
                        alarmManager.cancel(pendingIntent)
                    else
                        Timber.w("the pending intents for ${quietTime.title} are lost!")

                    pendingIntent = getQuietTimePendingIntent(quietTime, i, true)

                    if (pendingIntent != null)
                        alarmManager.cancel(pendingIntent)
                    else
                        Timber.w("the pending intents for ${quietTime.title} are lost!")
                }
            }
        }
    }

    /**
     * Creates a pending intent ready to be scheduled as an alarm for that day of the week.
     *
     * @param scheduleForNextWeek If this is true, the alarm will start firing from the next week, on the set day of week
     * @param dayIndex The index of the day of the week. First day of the week is 0.
     */
    fun createQuietTimePendingIntent(
        quietTime: QuietTime,
        dayIndex: Int,
        isEndTimeRequest: Boolean
    ): PendingIntent {
        val requestCode = quietTime.createRequestCode(dayIndex, isEndTimeRequest)
        val intent = Intent(context, QuietTimeAlarmReceiver::class.java)

        if (!isEndTimeRequest)
            intent.putExtra(SILENT_MODE, true)
        else
            intent.putExtra(SILENT_MODE, false)

        return PendingIntent.getBroadcast(context, requestCode, intent, 0)
    }

    /**
     * Finds and returns a currently scheduled pending intent.
     */
    fun getQuietTimePendingIntent(
        quietTime: QuietTime,
        dayIndex: Int,
        isEndTimeRequest: Boolean
    ): PendingIntent? {
        val requestCode = quietTime.createRequestCode(dayIndex, isEndTimeRequest)
        val intent = Intent(context, QuietTimeAlarmReceiver::class.java)

        if (!isEndTimeRequest)
            intent.putExtra(SILENT_MODE, true)
        else
            intent.putExtra(SILENT_MODE, false)

        return PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE)
    }
}