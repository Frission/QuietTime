package com.firatyildiz.quiettime

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.firatyildiz.quiettime.app.QuietTimeAlarmManager
import com.firatyildiz.quiettime.app.QuietTimeAlarmReceiver
import com.firatyildiz.quiettime.model.entities.QuietTime
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author Fırat Yıldız
 *
 * Created on 14/06/2020
 */
@RunWith(AndroidJUnit4::class)
class QuietTimeAlarmManagerTest {

    @Test
    fun quietTimePendingIntents_areCreatedAsExpected() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val quietTimeAlarmManager = QuietTimeAlarmManager(appContext)

        val quietTime = QuietTime("Test", 127, 15 * 60, 15 * 60)
        quietTime.id = 35

        val intent = Intent(appContext, QuietTimeAlarmReceiver::class.java)

        for (dayIndex in 0..6) {
            // create an intent and confirm that it is correct for each day

            val expectedPendingIntent = PendingIntent.getBroadcast(
                appContext,
                quietTime.createRequestCode(dayIndex, false),
                intent,
                0
            )

            assertThat(
                expectedPendingIntent,
                equalTo(quietTimeAlarmManager.createQuietTimePendingIntent(quietTime, dayIndex))
            )
        }
    }
}