/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */

package com.launchkey.android.authenticator.sdk.ui.internal.view;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

// Robolectric runner required to
// shadow a static call of Color.rgb() for
// default values in BaseExpirationTimer
@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(sdk = 23)
public class ParameterizedBaseExpirationTimerMinSecTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "Minutes={0}, Seconds={1}. Expected Minutes={2}, Seconds={3}")
    public static Collection<Object[]>  data() {

        // { min, sec, expectedMin, expectedSec }
        return Arrays.asList(new Object[][] {
                { 0, 0, 0, 0 },
                { 0, 120, 2, 0 },
                { 0, 150, 2, 30 },
                { 0, 181, 3, 1 },
                { 0, 20, 0, 20 },
                { 0, 60, 1, 0 },
                { 2, 0, 2, 0 },
                { 5, 59, 5, 59 },
                { 5, 60, 6, 0 },
                { 5, 61, 6, 1 }
        });
    }

    private long mMinutes;
    private long mSeconds;
    private long mExpectedMinutes;
    private long mExpectedSeconds;

    public ParameterizedBaseExpirationTimerMinSecTest(long minutes, long seconds, long expectedMinutes, long expectedSeconds) {
        mMinutes = minutes;
        mSeconds = seconds;
        mExpectedMinutes = expectedMinutes;
        mExpectedSeconds = expectedSeconds;
    }

    @Test
    public void testMillisToMinSecValues() {

        final long millis = getMillis(mMinutes, mSeconds);
        final long[] minSec = ExpirationTimer.calculateMinSec(millis);
        assertEquals(mExpectedMinutes, minSec[0]);
        assertEquals(mExpectedSeconds, minSec[1]);
    }

    private long getMillis(long minutes, long seconds) {
        return 1000 * (seconds + (minutes * 60));
    }
}
