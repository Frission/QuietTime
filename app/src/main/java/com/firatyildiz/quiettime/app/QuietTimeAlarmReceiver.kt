package com.firatyildiz.quiettime.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import timber.log.Timber

/**
 * @author Fırat Yıldız
 *
 * Created on 14/06/2020
 */
class QuietTimeAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // TODO send a notification

        val turnSilentModeOn = intent?.extras?.getBoolean(QuietTimeAlarmManager.SILENT_MODE)

        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        if (audioManager != null && turnSilentModeOn != null) {
            if (turnSilentModeOn && audioManager.ringerMode != AudioManager.RINGER_MODE_VIBRATE) {
                Timber.d("turning silent mode on")
                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            } else if (!turnSilentModeOn && audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                Timber.d("turning silent mode off")
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        }
    }
}