/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import com.launchkey.android.authenticator.sdk.ui.internal.view.ExpirationTimer.Companion.calculateMinSec
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

// Robolectric runner required to
// shadow a static call of Color.rgb() for
// default values in BaseExpirationTimer
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [23])
class ParameterizedBaseExpirationTimerMinSecTest(
    private val mMinutes: Long,
    private val mSeconds: Long,
    private val mExpectedMinutes: Long,
    private val mExpectedSeconds: Long
) {
    @Test
    fun testMillisToMinSecValues() {
        val millis = getMillis(mMinutes, mSeconds)
        val minSec = calculateMinSec(millis)
        Assert.assertEquals(mExpectedMinutes, minSec[0])
        Assert.assertEquals(mExpectedSeconds, minSec[1])
    }
    
    private fun getMillis(minutes: Long, seconds: Long): Long {
        return 1000 * (seconds + minutes * 60)
    }
    
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Minutes={0}, Seconds={1}. Expected Minutes={2}, Seconds={3}")
        fun data(): Collection<Array<Any>> {
            
            // { min, sec, expectedMin, expectedSec }
            return Arrays.asList(
                *arrayOf(
                    arrayOf(0, 0, 0, 0),
                    arrayOf(0, 120, 2, 0),
                    arrayOf(0, 150, 2, 30),
                    arrayOf(0, 181, 3, 1),
                    arrayOf(0, 20, 0, 20),
                    arrayOf(0, 60, 1, 0),
                    arrayOf(2, 0, 2, 0),
                    arrayOf(5, 59, 5, 59),
                    arrayOf(5, 60, 6, 0),
                    arrayOf(5, 61, 6, 1)
                )
            )
        }
    }
}