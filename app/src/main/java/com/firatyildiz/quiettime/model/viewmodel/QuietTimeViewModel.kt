package com.firatyildiz.quiettime.model.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.firatyildiz.quiettime.app.QuietTimeAlarmManager
import com.firatyildiz.quiettime.model.AppRoomDatabase
import com.firatyildiz.quiettime.model.entities.QuietTime
import com.firatyildiz.quiettime.model.repositories.QuietTimeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * @author Fırat Yıldız
 */
class QuietTimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuietTimeRepository
    private val alarmManager: QuietTimeAlarmManager
    private var firstTimeAccess = true

    var allQuietTimes: LiveData<List<QuietTime>>
        private set

    init {
        val quietTimeDao =
            AppRoomDatabase.getDatabase(application.applicationContext).quietTimeDao()
        repository = QuietTimeRepository(quietTimeDao)
        allQuietTimes = repository.allQuietTimes
        alarmManager = QuietTimeAlarmManager(application)
    }

    /**
     * Will only update the alarms if this is the first time access
     */
    fun updateAlarms(
        quietTimes: List<QuietTime>,
        currentLocale: Locale,
        lifecycleScope: CoroutineScope
    ) = lifecycleScope.launch {
        if (firstTimeAccess) {
            for (quietTime in quietTimes) {
                alarmManager.createAlarmsForQuietTime(quietTime, currentLocale, true)
            }
        }

        firstTimeAccess = false
    }

    fun insertQuietTime(quietTime: QuietTime, currentLocale: Locale) {
        val id = repository.insertQuietTime(quietTime)

        if (id != null) {
            quietTime.id = id.toInt()
            alarmManager.createAlarmsForQuietTime(quietTime, currentLocale, false)
        }
    }

    fun updateQuietTime(quietTime: QuietTime, currentLocale: Locale) {
        val oldQuietTime = repository.getQuietTimeById(quietTime.id)

        oldQuietTime?.let { alarmManager.deleteAlarmsForQuietTime(it) }

        repository.updateQuietTime(quietTime)

        alarmManager.createAlarmsForQuietTime(quietTime, currentLocale, true)
    }

    fun deleteQuietTime(quietTime: QuietTime) {
        alarmManager.deleteAlarmsForQuietTime(quietTime)
        repository.deleteQuietTime(quietTime)
    }

    /**
     * Finds all quiet times that collide with the given time ranges
     */
    fun getCollidingQuietTimes(days: Int, startTime: Int, endTime: Int): List<QuietTime> {
        return repository.getCollidingQuietTimes(days, startTime, endTime)
    }

    /**
     * Same as getCollidingQuietTimes(days, startTime, endTime),
     * the id here is used to prevent collision with self
     */
    fun getCollidingQuietTimes(id: Int, days: Int, startTime: Int, endTime: Int): List<QuietTime> {
        return repository.getCollidingQuietTimes(id, days, startTime, endTime)
    }
}