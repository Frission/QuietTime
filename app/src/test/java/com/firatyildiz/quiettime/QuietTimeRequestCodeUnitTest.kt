package com.firatyildiz.quiettime

import com.firatyildiz.quiettime.model.entities.QuietTime
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Fırat Yıldız
 *
 * Created on 14/06/2020
 */
class QuietTimeRequestCodeUnitTest {

    @Test
    fun quietTimeRequestCode_returnsAsExpected() {
        val quietTime = QuietTime("Test", 127, 12 * 60, 15 * 60)
        quietTime.id = 1

        var requestCode = quietTime.createRequestCode(0, false)
        assertThat(requestCode, equalTo(1000001))

        requestCode = quietTime.createRequestCode(1, false)
        assertThat(requestCode, equalTo(1000002))

        requestCode = quietTime.createRequestCode(2, false)
        assertThat(requestCode, equalTo(1000004))

        requestCode = quietTime.createRequestCode(3, false)
        assertThat(requestCode, equalTo(1000008))

        requestCode = quietTime.createRequestCode(4, false)
        assertThat(requestCode, equalTo(1000016))

        requestCode = quietTime.createRequestCode(5, false)
        assertThat(requestCode, equalTo(1000032))

        requestCode = quietTime.createRequestCode(6, false)
        assertThat(requestCode, equalTo(1000064))

        quietTime.id = 26

        requestCode = quietTime.createRequestCode(0, true)
        assertThat(requestCode, equalTo(2600001 + 1))

        requestCode = quietTime.createRequestCode(1, true)
        assertThat(requestCode, equalTo(2600002 + 1))

        requestCode = quietTime.createRequestCode(2, true)
        assertThat(requestCode, equalTo(2600004 + 1))

        requestCode = quietTime.createRequestCode(6, true)
        assertThat(requestCode, equalTo(2600064 + 1))
    }
}