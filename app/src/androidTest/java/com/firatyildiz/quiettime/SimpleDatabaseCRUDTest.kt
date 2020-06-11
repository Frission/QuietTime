package com.firatyildiz.quiettime

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.firatyildiz.quiettime.model.AppRoomDatabase
import com.firatyildiz.quiettime.model.daos.QuietTimeDao
import com.firatyildiz.quiettime.model.entities.QuietTime
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * @author Fırat Yıldız
 */
@RunWith(AndroidJUnit4::class)
class SimpleDatabaseCRUDTest {
    private lateinit var quietTimeDao: QuietTimeDao
    private lateinit var db: AppRoomDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        quietTimeDao = db.quietTimeDao()
    }

    /**
     * This tests read and write together
     */
    @Test
    @Throws(Exception::class)
    fun onInsertToDatabase_CheckIf_dataIsCorrectlyInserted() {
        val quietTime = QuietTime("Hello Test", 127, 8 * 60, 15 * 60)

        quietTimeDao.insertQuietTime(quietTime)

        val quietTimes = quietTimeDao.getAllQuietTimesWithoutLiveData()

        assertThat(quietTimes.size, equalTo(1))
        assertThat(quietTimes[0].title, equalTo("Hello Test"))
        assertThat(quietTimes[0].days, equalTo(127))
        assertThat(quietTimes[0].startTime, equalTo(8 * 60))
        assertThat(quietTimes[0].endTime, equalTo(15 * 60))
    }


    @Test
    @Throws(Exception::class)
    fun onUpdateData_CheckIf_dataIsCorrectlyUpdated() {
        var quietTime = QuietTime("Hello Test", 127, 8 * 60, 15 * 60)

        quietTimeDao.insertQuietTime(quietTime)

        var quietTimes = quietTimeDao.getAllQuietTimesWithoutLiveData()

        assertThat(quietTimes.size, equalTo(1))
        assertThat(quietTimes[0].title, equalTo("Hello Test"))
        assertThat(quietTimes[0].days, equalTo(127))
        assertThat(quietTimes[0].startTime, equalTo(8 * 60))
        assertThat(quietTimes[0].endTime, equalTo(15 * 60))

        quietTime = quietTimes[0]

        quietTime.title = "Goodbye Test 2"
        quietTime.days = 32
        quietTime.startTime = 10 * 60 + 30
        quietTime.endTime = 17 * 60 + 30

        quietTimeDao.updateQuietTime(quietTime)

        quietTimes = quietTimeDao.getAllQuietTimesWithoutLiveData()

        assertThat(quietTimes.size, equalTo(1))
        assertThat(quietTimes[0].title, equalTo("Goodbye Test 2"))
        assertThat(quietTimes[0].days, equalTo(32))
        assertThat(quietTimes[0].startTime, equalTo(10 * 60 + 30))
        assertThat(quietTimes[0].endTime, equalTo(17 * 60 + 30))
    }

    @Test
    @Throws(Exception::class)
    fun onDeleteData_CheckIf_dataIsCorrectlyDeleted() {
        val quietTime = QuietTime("Hello Test", 127, 8 * 60, 15 * 60)
        val quietTime2 = QuietTime("Hello Test", 127, 6 * 60, 7 * 60)

        quietTimeDao.insertQuietTime(quietTime)
        quietTimeDao.insertQuietTime(quietTime2)

        var quietTimes = quietTimeDao.getAllQuietTimesWithoutLiveData()

        assertThat(quietTimes.size, equalTo(2))

        // this doesn't work because the objects need to be exactly the same
        // but the id is assigned automatically
        // quietTimeDao.delete(quietTime2)

        // since we have stated the insert order
        // we know that the second quiet time is equal to quietTime2
        // except for it's id, of course
        quietTimeDao.deleteQuietTimeById(quietTimes[1].id)

        quietTimes = quietTimeDao.getAllQuietTimesWithoutLiveData()

        assertThat(quietTimes.size, equalTo(1))
        assertThat(quietTimes[0].title, equalTo("Hello Test"))
        assertThat(quietTimes[0].days, equalTo(127))
        assertThat(quietTimes[0].startTime, equalTo(8 * 60))
        assertThat(quietTimes[0].endTime, equalTo(15 * 60))
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}