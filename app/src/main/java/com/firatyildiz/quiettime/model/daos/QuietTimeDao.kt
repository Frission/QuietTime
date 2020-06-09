package com.firatyildiz.quiettime.model.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.firatyildiz.quiettime.model.QuietTimeConstants
import com.firatyildiz.quiettime.model.entities.QuietTime
import org.jetbrains.annotations.TestOnly

@Dao
interface QuietTimeDao {

    @Query("SELECT * FROM ${QuietTimeConstants.TABLE_NAME}")
    fun getAllQuietTimes(): LiveData<List<QuietTime>>

    /**
     * First, check if any days in the week collide by bitwise AND, if the result is not 0, then some days are colliding.
     * On the colliding days, check if the given start time or end time collides with this quiet time.
     *
     * If given start time is earlier than the colliding start time, the given end time must be before the colliding end time
     * If given end time is later than the colliding end time, the start time must be after the colliding end time
     *
     * For example if the given start time is before the end time of the matching quiet time,
     * then this quiet time will silence the phone again when it is silent, but when the matching quiet time ends,
     * the phone will be unsilenced, leading to unexpected behavior
     */
    @Query(
        "SELECT * FROM ${QuietTimeConstants.TABLE_NAME} " +
                "WHERE ${QuietTimeConstants.DAYS_COLUMN} & (:days) > 0 AND (" +
                "((:startTime) <= ${QuietTimeConstants.START_TIME_COLUMN} AND (:endTime) >= ${QuietTimeConstants.START_TIME_COLUMN}) " +
                "OR ((:endTime) >= ${QuietTimeConstants.START_TIME_COLUMN} AND (:startTime) <= ${QuietTimeConstants.END_TIME_COLUMN}))"
    )
    fun getCollidingQuietTimes(days: Int, startTime: Int, endTime: Int): List<QuietTime>

    @Query("DELETE FROM ${QuietTimeConstants.TABLE_NAME} WHERE ${QuietTimeConstants.ID_COLUMN} == (:id)")
    fun deleteQuietTimeById(id: Int)

    @Insert
    fun insertQuietTime(quietTime: QuietTime)

    @Delete
    fun deleteQuietTime(quietTime: QuietTime)

    @Update
    fun updateQuietTime(quietTime: QuietTime)

    @TestOnly
    @Query("SELECT * FROM ${QuietTimeConstants.TABLE_NAME}")
    fun getAllQuietTimesWithoutLiveData(): List<QuietTime>
}