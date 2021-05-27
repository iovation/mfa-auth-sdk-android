/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */

package com.launchkey.android.authenticator.sdk.ui.internal.view;

import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// Robolectric runner required to
// shadow a static call of Color.rgb() for
// default values in BaseExpirationTimer
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class ExpirationTimerTest {

    @Test
    public void testMillisToMinSecNullCheck() {

        final long[] minSec = ExpirationTimer.calculateMinSec(100);
        assertNotNull(minSec);
    }

    @Test
    public void testMillisToMinSecLengthCheck() {

        final long[] minSec = ExpirationTimer.calculateMinSec(100);
        assertEquals(2, minSec.length);
    }

    @Test
    public void testMillisToMinSecNegativeMillis() {

        final long[] minSec = ExpirationTimer.calculateMinSec(-100);
        assertNotNull(minSec);
        assertEquals(0L, minSec[0]);
        assertEquals(0L, minSec[1]);
    }

    @Test
    public void testMillisToMinSecBarelyBelowSixtySeconds() {

        final long[] minSec = ExpirationTimer.calculateMinSec(59999);
        assertNotNull(minSec);
        assertEquals(0L, minSec[0]);
        assertEquals(59L, minSec[1]);
    }

    @Test
    public void testMillisToMinSecSixtySeconds() {

        final long[] minSec = ExpirationTimer.calculateMinSec(60000);
        assertNotNull(minSec);
        assertEquals(1L, minSec[0]);
        assertEquals(0L, minSec[1]);
    }

    @Test
    public void testMillisToMinSecBarelyAboveSixtySeconds() {

        final long[] minSec = ExpirationTimer.calculateMinSec(60001);
        assertNotNull(minSec);
        assertEquals(1L, minSec[0]);
        assertEquals(0L, minSec[1]);
    }

    @Test
    public void testConvertMinSecToFormattedStringRegular() {

        final String timeFormatted = ExpirationTimer.convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 1, 59);
        assertNotNull(timeFormatted);
        assertEquals("01:59", timeFormatted);
    }

    @Test
    public void testConvertMinSecToFormattedStringFiftyNineSeconds() {

        final String timeFormatted = ExpirationTimer.convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 0, 59);
        assertNotNull(timeFormatted);
        assertEquals("00:59", timeFormatted);
    }

    @Test
    public void testConvertMinSecToFormattedStringDisallowSixtySeconds() {

        final String timeFormatted = ExpirationTimer.convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 0, 60);
        assertNotNull(timeFormatted);
        assertEquals("00:00", timeFormatted);
    }

    @Test
    public void testConvertMinSecToFormattedStringDisallowOverSixtySeconds() {

        final String timeFormatted = ExpirationTimer.convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 0, 61);
        assertNotNull(timeFormatted);
        assertEquals("00:00", timeFormatted);
    }

    @Test
    public void testConvertMinSecToFormattedStringDisallowUnderZeroSeconds() {

        final String timeFormatted = ExpirationTimer.convertMinSecToFormattedString(ExpirationTimer.FORMAT_DEFAULT, 0, -1);
        assertNotNull(timeFormatted);
        assertEquals("00:00", timeFormatted);
    }

    @Test
    public void testContentDescriptionOneSecond() {
        final String expected = "Expires in 1 second";
        final String actual = ExpirationTimer.getContentDescription(
                0,
                1,
                new TimeAgo(androidx.test.core.app.ApplicationProvider.getApplicationContext()),
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getResources());
        assertEquals(expected, actual);
    }

    @Test
    public void testContentDescriptionFiveSeconds() {
        final String expected = "Expires in 5 seconds";
        final String actual = ExpirationTimer.getContentDescription(
                0,
                5,
                new TimeAgo(androidx.test.core.app.ApplicationProvider.getApplicationContext()),
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getResources());
        assertEquals(expected, actual);
    }

    @Test
    public void testContentDescriptionOneMinute() {
        final String expected = "Expires in 1 minute";
        final String actual = ExpirationTimer.getContentDescription(
                1,
                0,
                new TimeAgo(androidx.test.core.app.ApplicationProvider.getApplicationContext()),
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getResources());
        assertEquals(expected, actual);
    }

    @Test
    public void testContentDescriptionOneMinuteOneSecond() {
        final String expected = "Expires in 1 minute and 1 second";
        final String actual = ExpirationTimer.getContentDescription(
                1,
                1,
                new TimeAgo(androidx.test.core.app.ApplicationProvider.getApplicationContext()),
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getResources());
        assertEquals(expected, actual);
    }

    @Test
    public void testContentDescriptionOneMinuteFortySeconds() {
        final String expected = "Expires in 1 minute and 40 seconds";
        final String actual = ExpirationTimer.getContentDescription(
                1,
                40,
                new TimeAgo(androidx.test.core.app.ApplicationProvider.getApplicationContext()),
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getResources());
        assertEquals(expected, actual);
    }

    @Test
    public void testContentDescriptionTwoMinutes() {
        final String expected = "Expires in 2 minutes";
        final String actual = ExpirationTimer.getContentDescription(
                2,
                0,
                new TimeAgo(androidx.test.core.app.ApplicationProvider.getApplicationContext()),
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getResources());
        assertEquals(expected, actual);
    }

    @Test
    public void testContentDescriptionThreeMinutesOneSecond() {
        final String expected = "Expires in 3 minutes and 1 second";
        final String actual = ExpirationTimer.getContentDescription(
                3,
                1,
                new TimeAgo(androidx.test.core.app.ApplicationProvider.getApplicationContext()),
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getResources());
        assertEquals(expected, actual);
    }

    @Test
    public void testContentDescriptionSixMinutesFiftyNineSeconds() {
        final String expected = "Expires in 6 minutes and 59 seconds";
        final String actual = ExpirationTimer.getContentDescription(
                6,
                59,
                new TimeAgo(androidx.test.core.app.ApplicationProvider.getApplicationContext()),
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getResources());
        assertEquals(expected, actual);
    }
}
