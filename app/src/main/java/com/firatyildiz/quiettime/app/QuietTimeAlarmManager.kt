package com.firatyildiz.quiettime.app

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.firatyildiz.quiettime.helpers.DateTimeLocalizationHelper
import com.firatyildiz.quiettime.model.QuietTimeConstants
import com.firatyildiz.quiettime.model.entities.QuietTime
import timber.log.Timber
import java.util.*

/**
 * @author Fırat Yıldız
 *
 * Created on 14/06/2020
 */
class QuietTimeAlarmManager(val context: Context) {

    fun createAlarmsForQuietTime(
        quietTime: QuietTime,
        currentLocale: Locale,
        updateExistingAlarms: Boolean
    ): Boolean {
        Timber.d("creating alarms for ${quietTime.title}")

        val flag = if (updateExistingAlarms) PendingIntent.FLAG_CANCEL_CURRENT else 0

        if (flag == PendingIntent.FLAG_CANCEL_CURRENT)
            Timber.d("will update existing alarms")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        if (alarmManager == null || notificationManager == null)
            return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted)
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

                    // start alarm, and no need for end alarm
                    setQuietTimeAlarm(
                        true,
                        timeInMillis,
                        calendar,
                        quietTime,
                        i,
                        alarmManager,
                        flag
                    )
                    Timber.d("start request id is ${quietTime.createRequestCode(i, false)}")
                    Timber.d(
                        "new alarm scheduled to ${calendar.get(Calendar.MONTH)}/${calendar.get(
                            Calendar.DAY_OF_MONTH
                        )}"
                    )
                }
            }

        } else if (quietTime.startTime < quietTime.endTime) {
            // a normal alarm where start time is before the end time
            for (i in 0..6) {
                if (quietTime.days and (1 shl i) != 0) {
                    // the day is active, schedule an alarm

                    // start alarm
                    setQuietTimeAlarm(
                        true,
                        timeInMillis,
                        calendar,
                        quietTime,
                        i,
                        alarmManager,
                        flag
                    )
                    Timber.d("start request id is ${quietTime.createRequestCode(i, false)}")
                    Timber.d(
                        "new alarm scheduled to ${calendar.get(Calendar.MONTH)}/${calendar.get(
                            Calendar.DAY_OF_MONTH
                        )}"
                    )

                    // end alarm
                    setQuietTimeAlarm(
                        false,
                        timeInMillis,
                        calendar,
                        quietTime,
                        i,
                        alarmManager,
                        flag
                    )
                    Timber.d("end request id is ${quietTime.createRequestCode(i, true)}")
                    Timber.d(
                        "new alarm scheduled to ${calendar.get(Calendar.MONTH)}/${calendar.get(
                            Calendar.DAY_OF_MONTH
                        )}"
                    )
                }
            }

        } else {
            // an alarm where the end time is in the next day

            for (i in 0..6) {
                if (quietTime.days and (1 shl i) != 0) {
                    // the day is active, schedule an alarm

                    // start alarm
                    setQuietTimeAlarm(
                        true,
                        timeInMillis,
                        calendar,
                        quietTime,
                        i,
                        alarmManager,
                        flag
                    )
                    Timber.d("start request id is ${quietTime.createRequestCode(i, false)}")
                    Timber.d(
                        "new alarm scheduled to ${calendar.get(Calendar.MONTH)}/${calendar.get(
                            Calendar.DAY_OF_MONTH
                        )}"
                    )

                    // the end time is in the next day
                    val dayIndex = i + 1 % 7

                    // end alarm
                    setQuietTimeAlarm(
                        false,
                        timeInMillis,
                        calendar,
                        quietTime,
                        i,
                        alarmManager,
                        flag
                    )
                    Timber.d("end request id is ${quietTime.createRequestCode(i, true)}")
                    Timber.d(
                        "new alarm scheduled to ${calendar.get(Calendar.MONTH)}/${calendar.get(
                            Calendar.DAY_OF_MONTH
                        )}"
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
                    Timber.d("delete start request id is ${quietTime.createRequestCode(i, false)}")

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
                    Timber.d("delete start request id is ${quietTime.createRequestCode(i, false)}")

                    if (pendingIntent != null)
                        alarmManager.cancel(pendingIntent)
                    else
                        Timber.w("the pending intents for ${quietTime.title} are lost!")

                    pendingIntent = getQuietTimePendingIntent(quietTime, i, true)
                    Timber.d("delete end request id is ${quietTime.createRequestCode(i, true)}")

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
                    Timber.d("delete start request id is ${quietTime.createRequestCode(i, false)}")

                    if (pendingIntent != null)
                        alarmManager.cancel(pendingIntent)
                    else
                        Timber.w("the pending intents for ${quietTime.title} are lost!")

                    pendingIntent = getQuietTimePendingIntent(quietTime, i, true)
                    Timber.d("delete end request id is ${quietTime.createRequestCode(i, true)}")

                    if (pendingIntent != null)
                        alarmManager.cancel(pendingIntent)
                    else
                        Timber.w("the pending intents for ${quietTime.title} are lost!")
                }
            }
        }
    }

    fun setQuietTimeAlarm(
        isStartTimeAlarm: Boolean,
        currentTimeInMillis: Long,
        calendar: Calendar,
        quietTime: QuietTime,
        dayIndex: Int,
        alarmManager: AlarmManager,
        flag: Int
    ) {
        calendar.apply { this.timeInMillis = currentTimeInMillis }

        var pendingIntent =
            createQuietTimePendingIntent(quietTime, dayIndex, !isStartTimeAlarm, flag)
        DateTimeLocalizationHelper.setCalendarDateToQuietTime(
            calendar,
            quietTime,
            dayIndex,
            isStartTimeAlarm
        )

        if (Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        else
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
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
        isEndTimeRequest: Boolean,
        flag: Int
    ): PendingIntent {
        val requestCode = quietTime.createRequestCode(dayIndex, isEndTimeRequest)
        val intent = Intent(context, QuietTimeAlarmReceiver::class.java)

        if (!isEndTimeRequest)
            intent.putExtra(QuietTimeConstants.SILENT_MODE, true)
        else
            intent.putExtra(QuietTimeConstants.SILENT_MODE, false)

        intent.putExtra(QuietTimeConstants.ID_COLUMN, quietTime.id)
        intent.putExtra(QuietTimeConstants.TITLE_COLUMN, quietTime.title)
        intent.putExtra(QuietTimeConstants.START_TIME_COLUMN, quietTime.startTime)
        intent.putExtra(QuietTimeConstants.END_TIME_COLUMN, quietTime.endTime)
        intent.putExtra(QuietTimeConstants.DAYS_COLUMN, quietTime.days)
        intent.putExtra(QuietTimeConstants.SILENCE_MODE_COLUMN, quietTime.silenceMode)

        return PendingIntent.getBroadcast(context, requestCode, intent, flag)
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
            intent.putExtra(QuietTimeConstants.SILENT_MODE, true)
        else
            intent.putExtra(QuietTimeConstants.SILENT_MODE, false)

        intent.putExtra(QuietTimeConstants.ID_COLUMN, quietTime.id)
        intent.putExtra(QuietTimeConstants.TITLE_COLUMN, quietTime.title)
        intent.putExtra(QuietTimeConstants.START_TIME_COLUMN, quietTime.startTime)
        intent.putExtra(QuietTimeConstants.END_TIME_COLUMN, quietTime.endTime)
        intent.putExtra(QuietTimeConstants.DAYS_COLUMN, quietTime.days)
        intent.putExtra(QuietTimeConstants.SILENCE_MODE_COLUMN, quietTime.silenceMode)

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }
}