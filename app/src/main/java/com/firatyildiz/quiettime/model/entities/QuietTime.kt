package com.firatyildiz.quiettime.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.firatyildiz.quiettime.model.QuietTimeConstants
import java.io.Serializable

/**
 * @author Fırat Yıldız
 *
 * Start time and end time are stored as minutes that have passed since midnight,
 * ex. 240 would be 04:00
 *
 * Days are stored as a integer and treated as a byte, a '1' for each day that the phone will be silenced
 * ex. '1000001' -> the auto silence will work every first and last day of a week, this would also equal 65
 *
 * id will also be the requestCode for the AlarmManager intent.
 */
@Entity
data class QuietTime(
    @ColumnInfo(name = QuietTimeConstants.TITLE_COLUMN) var title: String,
    @ColumnInfo(name = QuietTimeConstants.DAYS_COLUMN) var days: Int,
    @ColumnInfo(name = QuietTimeConstants.START_TIME_COLUMN) var startTime: Int,
    @ColumnInfo(name = QuietTimeConstants.END_TIME_COLUMN) var endTime: Int,
    @ColumnInfo(name = QuietTimeConstants.SILENCE_MODE_COLUMN) var silenceMode: Int
) : Serializable {
    @ColumnInfo(name = QuietTimeConstants.ID_COLUMN)
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    companion object {
        @JvmStatic
        private val serialVersionUID: Long = 12062020L
    }

    /**
     * Returns a unique request code for the pending intent up to 999 alarms.
     *
     * @param dayIndex The index of the day in a week. First day of week is 0
     * @param isEndTimeRequest We need a way to differentiate between a quiet time's start and end requests, this boolean will add
     * 1 to the request code to make it unique again
     */
    fun createRequestCode(dayIndex: Int, isEndTimeRequest: Boolean): Int {
        val requestCodeStart = id.toString().padEnd(7, '0').toInt()
        var requestCode = (requestCodeStart + (days and (1 shl dayIndex)))

        if (isEndTimeRequest)
            requestCode += 1

        return requestCode
    }
}