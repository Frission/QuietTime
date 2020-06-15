package com.firatyildiz.quiettime.app

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.firatyildiz.quiettime.R
import com.firatyildiz.quiettime.model.QuietTimeConstants
import timber.log.Timber

/**
 * @author Fırat Yıldız
 *
 * Created on 14/06/2020
 */
class QuietTimeAlarmReceiver : BroadcastReceiver() {

    var quietTimeName = "Quiet Time"
    var quietTimeText = "Quite time has been triggered."

    override fun onReceive(context: Context?, intent: Intent?) {
        // TODO send a notification

        val isSilenceRequest = intent?.extras?.getBoolean(QuietTimeConstants.SILENT_MODE)
        val silenceMode = intent?.extras?.getInt(QuietTimeConstants.SILENCE_MODE_COLUMN)
        val quietTimeName = intent?.extras?.getString(QuietTimeConstants.TITLE_COLUMN)

        if (quietTimeName != null)
            this.quietTimeName = quietTimeName

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
            var builder =
                NotificationCompat.Builder(context, QuietTimeConstants.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_silent_mode)
                    .setContentTitle(context.getString(R.string.notification_title, quietTimeName))
                    .setContentText(quietTimeText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, builder.build())
            audioManager.ringerMode = silenceMode
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
            } else {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                notificationManager.notify(1, builder.build())
            }
        }
    }
}