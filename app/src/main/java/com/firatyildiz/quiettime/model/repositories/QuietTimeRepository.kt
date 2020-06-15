package com.firatyildiz.quiettime.model.repositories

import androidx.lifecycle.LiveData
import com.firatyildiz.quiettime.model.daos.QuietTimeDao
import com.firatyildiz.quiettime.model.entities.QuietTime

/**
 * @author Fırat Yıldız
 */
class QuietTimeRepository(private val quietTimeDao: QuietTimeDao) {

    val allQuietTimes: LiveData<List<QuietTime>> = quietTimeDao.getAllQuietTimes()

    fun insertQuietTime(quietTime: QuietTime): Long? {
        return quietTimeDao.insertQuietTime(quietTime)
    }

    fun updateQuietTime(quietTime: QuietTime) {
        quietTimeDao.updateQuietTime(quietTime)
    }

    fun deleteQuietTime(quietTime: QuietTime) {
        quietTimeDao.deleteQuietTime(quietTime)
    }

    fun getQuietTimeById(id: Int): QuietTime? {
        return quietTimeDao.getQuietTimeById(id)
    }

    fun getCollidingQuietTimes(days: Int, startTime: Int, endTime: Int): List<QuietTime> {
        return quietTimeDao.getCollidingQuietTimes(days, startTime, endTime)
    }

    fun getCollidingQuietTimes(id: Int, days: Int, startTime: Int, endTime: Int): List<QuietTime> {
        return quietTimeDao.getCollidingQuietTimes(id, days, startTime, endTime)
    }
}