package com.firatyildiz.quiettime.app

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.ConfigurationCompat
import com.firatyildiz.quiettime.R
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
class QuietTimeAlarmReceiver : BroadcastReceiver() {

    var quietTimeName = "Quiet Time"
    var quietTimeText = "Quite time has been triggered."
    var quietTime: QuietTime? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        // TODO send a notification

        val isSilenceRequest = intent?.extras?.getBoolean(QuietTimeConstants.SILENT_MODE)
        val silenceMode = intent?.extras?.getInt(QuietTimeConstants.SILENCE_MODE_COLUMN)

        val quietTimeId = intent?.extras?.getInt(QuietTimeConstants.ID_COLUMN)
        val quietTimeTitle = intent?.extras?.getString(QuietTimeConstants.TITLE_COLUMN)
        val quietTimeDays = intent?.extras?.getInt(QuietTimeConstants.DAYS_COLUMN)
        val quietTimeStartTime = intent?.extras?.getInt(QuietTimeConstants.START_TIME_COLUMN)
        val quietTimeEndTime = intent?.extras?.getInt(QuietTimeConstants.END_TIME_COLUMN)
        val quietTimeSilenceMode = intent?.extras?.getInt(QuietTimeConstants.SILENCE_MODE_COLUMN)

        if (quietTimeId != null && quietTimeTitle != null && quietTimeDays != null &&
            quietTimeStartTime != null && quietTimeEndTime != null && quietTimeSilenceMode != null
        ) {
            quietTime = QuietTime(
                quietTimeTitle,
                quietTimeDays,
                quietTimeStartTime,
                quietTimeEndTime,
                quietTimeSilenceMode
            )
            quietTime?.id = quietTimeId
        }

        if (quietTimeTitle != null)
            this.quietTimeName = quietTimeTitle

        if (context != null && isSilenceRequest != null)
            quietTimeText = if (isSilenceRequest)
                context.getString(R.string.notification_text_silent)
            else
                context.getString(R.string.notification_text_not_silent)

        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        if (audioManager != null && isSilenceRequest != null && silenceMode != null) {
            if (isSilenceRequest) {
                turnSilentModeOn(context, audioManager, silenceMode)
            }

            if (!isSilenceRequest) {
                turnSilentModeOff(context, audioManager)
            }
        }
    }

    fun turnSilentModeOn(context: Context?, audioManager: AudioManager, silenceMode: Int) {
        Timber.d("turning silent mode on")
        if (context != null) {
            val builder =
                NotificationCompat.Builder(context, QuietTimeConstants.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_silent_mode)
                    .setContentTitle(context.getString(R.string.notification_title, quietTimeName))
                    .setContentText(quietTimeText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, builder.build())

            audioManager.ringerMode = silenceMode
//            if(Build.VERSION.SDK_INT >= 23)
//                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)

            quietTime?.let {
                scheduleNewAlarm(context, it, false)
            }
        }
    }

    fun turnSilentModeOff(context: Context?, audioManager: AudioManager) {
        Timber.d("turning silent mode off")
        if (context != null) {
            var builder =
                NotificationCompat.Builder(context, QuietTimeConstants.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_silent_mode_off)
                    .setContentTitle(context.getString(R.string.notification_title, quietTimeName))
                    .setContentText(quietTimeText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                notificationManager.notify(1, builder.build())
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                notificationManager.notify(1, builder.build())
            }

            quietTime?.let {
                scheduleNewAlarm(context, it, true)
            }
        }
    }

    fun scheduleNewAlarm(context: Context, quietTime: QuietTime, isEndTimeRequest: Boolean) {
        val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]

        val quietTimeAlarmManager = QuietTimeAlarmManager(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val calendar = GregorianCalendar.getInstance(currentLocale)
        val dayIndex = DateTimeLocalizationHelper.getDayOfWeekAsDayIndex(calendar)

        val pendingIntent = quietTimeAlarmManager.createQuietTimePendingIntent(
            quietTime,
            dayIndex,
            isEndTimeRequest,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        calendar.timeInMillis = System.currentTimeMillis()
        calendar.timeInMillis += 1000
        DateTimeLocalizationHelper.setCalendarDateToQuietTime(
            calendar,
            quietTime,
            dayIndex,
            !isEndTimeRequest
        )

        if (Build.VERSION.SDK_INT >= 23)
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        else
            alarmManager?.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
    }
}