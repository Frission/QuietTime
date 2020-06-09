package com.firatyildiz.quiettime.model.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.firatyildiz.quiettime.model.AppRoomDatabase
import com.firatyildiz.quiettime.model.entities.QuietTime
import com.firatyildiz.quiettime.model.repositories.QuietTimeRepository

class QuietTimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuietTimeRepository

    var allQuietTimes: LiveData<List<QuietTime>>
        private set

    init {
        val quietTimeDao =
            AppRoomDatabase.getDatabase(application.applicationContext).quietTimeDao()
        repository = QuietTimeRepository(quietTimeDao)
        allQuietTimes = repository.allQuietTimes
    }

    fun insertQuietTime(quietTime: QuietTime) {
        repository.insert(quietTime)
    }

    fun getCollidingQuietTimes(days: Int, startTime: Int, endTime: Int): List<QuietTime>? {
        return getCollidingQuietTimes(days, startTime, endTime)
    }
}