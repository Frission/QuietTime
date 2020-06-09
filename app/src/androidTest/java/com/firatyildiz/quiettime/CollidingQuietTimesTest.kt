package com.firatyildiz.quiettime

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.firatyildiz.quiettime.model.AppRoomDatabase
import com.firatyildiz.quiettime.model.daos.QuietTimeDao
import com.firatyildiz.quiettime.model.entities.QuietTime
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CollidingQuietTimesTest {
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

    @Test
    @Throws(Exception::class)
    fun onInsertToDatabase_CheckIf_dataIsCorrectlyInserted() {
        // the first quiet time starts at 08:00 and ends at 15:00, every day
        // the second quiet time starts at 07:00 and ends at 14:00, on Monday

        val quietTime = QuietTime("Quiet Time", 127, 8 * 60, 15 * 60)

        quietTimeDao.insertQuietTime(quietTime)


        val collidingQuietTime1 = QuietTime("Colliding Time 1", 1, 7 * 60, 14 * 60)
        val collidingQuietTime2 = QuietTime("Colliding Time 2", 2, 6 * 60, 16 * 60)
        val collidingQuietTime3 = QuietTime("Colliding Time 3", 4, 9 * 60, 13 * 60)
        val collidingQuietTime4 = QuietTime("Colliding Time 4", 8, 9 * 60, 17 * 60)
        val collidingQuietTime5 = QuietTime("Colliding Time 5", 16, 6 * 60, 8 * 60)
        val collidingQuietTime6 = QuietTime("Colliding Time 6", 32, 15 * 60, 19 * 60)
        val collidingQuietTime7 = QuietTime("Colliding Time 7", 64, 8 * 60, 15 * 60)

        val nonCollidingQuietTime =
            QuietTime("Non Colliding Time 1", 64 + 32 + 16, 16 * 60, 20 * 60)
        val nonCollidingQuietTime2 = QuietTime("Non Colliding Time 2", 127, 4 * 60, 7 * 60 + 59)

        val collidingQuietTimes = listOf<QuietTime>(
            collidingQuietTime1,
            collidingQuietTime2,
            collidingQuietTime3,
            collidingQuietTime4,
            collidingQuietTime5,
            collidingQuietTime6,
            collidingQuietTime7
        )


        for (i in 0..6) {

            val collidedQuietTimes = quietTimeDao.getCollidingQuietTimes(
                collidingQuietTimes[i].days,
                collidingQuietTimes[i].startTime,
                collidingQuietTimes[i].endTime
            )

            assertThat(collidedQuietTimes[0].title, equalTo("Quiet Time"))
            assertThat(collidedQuietTimes[0].days, equalTo(127))
            assertThat(collidedQuietTimes[0].startTime, equalTo(8 * 60))
            assertThat(collidedQuietTimes[0].endTime, equalTo(15 * 60))
        }

        var collidedQuietTimes = quietTimeDao.getCollidingQuietTimes(
            nonCollidingQuietTime.days,
            nonCollidingQuietTime.startTime,
            nonCollidingQuietTime.endTime
        )

        assertThat(collidedQuietTimes.size, equalTo(0))

        collidedQuietTimes = quietTimeDao.getCollidingQuietTimes(
            nonCollidingQuietTime2.days,
            nonCollidingQuietTime2.startTime,
            nonCollidingQuietTime2.endTime
        )!!

        assertThat(collidedQuietTimes.size, equalTo(0))
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}