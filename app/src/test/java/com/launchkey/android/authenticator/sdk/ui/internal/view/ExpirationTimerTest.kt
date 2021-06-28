/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import com.launchkey.android.authenticator.sdk.ui.internal.view.ExpirationTimer.Companion.calculateMinSec
import com.launchkey.android.authenticator.sdk.ui.internal.view.ExpirationTimer.Companion.convertMinSecToFormattedString
import com.launchkey.android.authenticator.sdk.ui.internal.view.ExpirationTimer.Companion.getContentDescription
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric runner required to
// shadow a static call of Color.rgb() for
// default values in BaseExpirationTimer
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
class ExpirationTimerTest {
    @Test
    fun testMillisToMinSecNullCheck() {
        val minSec = calculateMinSec(100)
        Assert.assertNotNull(minSec)
    }
    
    @Test
    fun testMillisToMinSecLengthCheck() {
        val minSec = calculateMinSec(100)
        Assert.assertEquals(2, minSec.size.toLong())
    }
    
    @Test
    fun testMillisToMinSecNegativeMillis() {
        val minSec = calculateMinSec(-100)
        Assert.assertNotNull(minSec)
        Assert.assertEquals(0L, minSec[0])
        Assert.assertEquals(0L, minSec[1])
    }
    
    @Test
    fun testMillisToMinSecBarelyBelowSixtySeconds() {
        val minSec = calculateMinSec(59999)
        Assert.assertNotNull(minSec)
        Assert.assertEquals(0L, minSec[0])
        Assert.assertEquals(59L, minSec[1])
    }
    
    @Test
    fun testMillisToMinSecSixtySeconds() {
        val minSec = calculateMinSec(60000)
        Assert.assertNotNull(minSec)
        Assert.assertEquals(1L, minSec[0])
        Assert.assertEquals(0L, minSec[1])
    }
    
    @Test
    fun testMillisToMinSecBarelyAboveSixtySeconds() {
        val minSec = calculateMinSec(60001)
        Assert.assertNotNull(minSec)
        Assert.assertEquals(1L, minSec[0])
        Assert.assertEquals(0L, minSec[1])
    }
    
    @Test
    fun testConvertMinSecToFormattedStringRegular() {
        val timeFormatted = convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 1, 59)
        Assert.assertNotNull(timeFormatted)
        Assert.assertEquals("01:59", timeFormatted)
    }
    
    @Test
    fun testConvertMinSecToFormattedStringFiftyNineSeconds() {
        val timeFormatted = convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 0, 59)
        Assert.assertNotNull(timeFormatted)
        Assert.assertEquals("00:59", timeFormatted)
    }
    
    @Test
    fun testConvertMinSecToFormattedStringDisallowSixtySeconds() {
        val timeFormatted = convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 0, 60)
        Assert.assertNotNull(timeFormatted)
        Assert.assertEquals("00:00", timeFormatted)
    }
    
    @Test
    fun testConvertMinSecToFormattedStringDisallowOverSixtySeconds() {
        val timeFormatted = convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 0, 61)
        Assert.assertNotNull(timeFormatted)
        Assert.assertEquals("00:00", timeFormatted)
    }
    
    @Test
    fun testConvertMinSecToFormattedStringDisallowUnderZeroSeconds() {
        val timeFormatted = convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 0, -1)
        Assert.assertNotNull(timeFormatted)
        Assert.assertEquals("00:00", timeFormatted)
    }
    
    @Test
    fun testContentDescriptionOneSecond() {
        val expected = "Expires in 1 second"
        val actual = getContentDescription(
            0,
            1,
            TimeAgo(ApplicationProvider.getApplicationContext()),
            ApplicationProvider.getApplicationContext<Context>().resources
        )
        Assert.assertEquals(expected, actual)
    }
    
    @Test
    fun testContentDescriptionFiveSeconds() {
        val expected = "Expires in 5 seconds"
        val actual = getContentDescription(
            0,
            5,
            TimeAgo(ApplicationProvider.getApplicationContext()),
            ApplicationProvider.getApplicationContext<Context>().resources
        )
        Assert.assertEquals(expected, actual)
    }
    
    @Test
    fun testContentDescriptionOneMinute() {
        val expected = "Expires in 1 minute"
        val actual = getContentDescription(
            1,
            0,
            TimeAgo(ApplicationProvider.getApplicationContext()),
            ApplicationProvider.getApplicationContext<Context>().resources
        )
        Assert.assertEquals(expected, actual)
    }
    
    @Test
    fun testContentDescriptionOneMinuteOneSecond() {
        val expected = "Expires in 1 minute and 1 second"
        val actual = getContentDescription(
            1,
            1,
            TimeAgo(ApplicationProvider.getApplicationContext()),
            ApplicationProvider.getApplicationContext<Context>().resources
        )
        Assert.assertEquals(expected, actual)
    }
    
    @Test
    fun testContentDescriptionOneMinuteFortySeconds() {
        val expected = "Expires in 1 minute and 40 seconds"
        val actual = getContentDescription(
            1,
            40,
            TimeAgo(ApplicationProvider.getApplicationContext()),
            ApplicationProvider.getApplicationContext<Context>().resources
        )
        Assert.assertEquals(expected, actual)
    }
    
    @Test
    fun testContentDescriptionTwoMinutes() {
        val expected = "Expires in 2 minutes"
        val actual = getContentDescription(
            2,
            0,
            TimeAgo(ApplicationProvider.getApplicationContext()),
            ApplicationProvider.getApplicationContext<Context>().resources
        )
        Assert.assertEquals(expected, actual)
    }
    
    @Test
    fun testContentDescriptionThreeMinutesOneSecond() {
        val expected = "Expires in 3 minutes and 1 second"
        val actual = getContentDescription(
            3,
            1,
            TimeAgo(ApplicationProvider.getApplicationContext()),
            ApplicationProvider.getApplicationContext<Context>().resources
        )
        Assert.assertEquals(expected, actual)
    }
    
    @Test
    fun testContentDescriptionSixMinutesFiftyNineSeconds() {
        val expected = "Expires in 6 minutes and 59 seconds"
        val actual = getContentDescription(
            6,
            59,
            TimeAgo(ApplicationProvider.getApplicationContext()),
            ApplicationProvider.getApplicationContext<Context>().resources
        )
        Assert.assertEquals(expected, actual)
    }
}