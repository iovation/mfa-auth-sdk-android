/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import com.launchkey.android.authenticator.sdk.ui.internal.view.ExpirationTimer.Companion.convertMinSecToFormattedString
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
class ParameterizedBaseExpirationTimerFormatTest(
    private val mMinutes: Long,
    private val mSeconds: Long,
    private val mExpectedFormattedString: String
) {
    @Test
    fun testMillisToMinSecValues() {
        val formattedString = convertMinSecToFormattedString(FORMAT, mMinutes, mSeconds)
        Assert.assertEquals(mExpectedFormattedString, formattedString)
    }
    
    companion object {
        private const val FORMAT = ExpirationTimer.FORMAT_DEFAULT
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Minutes={0}, Seconds={1}. Expected={2} (" + FORMAT + ")")
        fun data(): Collection<Array<Any>> {
            
            // { min, sec, expectedFormattedString }
            return Arrays.asList(
                arrayOf(0, 0, "00:00"),
                arrayOf(0, 1, "00:01"),
                arrayOf(0, 24, "00:24"),
                arrayOf(1, 0, "01:00"),
                arrayOf(11, 11, "11:11"),
                arrayOf(14, 59, "14:59"),
                arrayOf(14, 0, "14:00"),
                arrayOf(14, 9, "14:09")
            )
        }
    }
}