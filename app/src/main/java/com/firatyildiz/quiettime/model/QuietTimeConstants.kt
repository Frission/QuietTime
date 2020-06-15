package com.firatyildiz.quiettime.model

import android.media.AudioManager

/**
 * @author Fırat Yıldız
 */
object QuietTimeConstants {
    const val DATABASE_NAME = "quiet_time"
    const val TABLE_NAME = "QuietTime"
    const val ID_COLUMN = "_id"
    const val TITLE_COLUMN = "Title"
    const val DAYS_COLUMN = "Days"
    const val START_TIME_COLUMN = "StartTime"
    const val END_TIME_COLUMN = "EndTime"
    const val SILENCE_MODE_COLUMN = "SilenceMode"

    const val SILENT_MODE = "silentMode"
    const val VIBRATE = AudioManager.RINGER_MODE_VIBRATE
    const val MUTE = AudioManager.RINGER_MODE_SILENT

    const val NOTIFICATION_CHANNEL_ID = "quiet_time_notifications_firatyildiz"
}