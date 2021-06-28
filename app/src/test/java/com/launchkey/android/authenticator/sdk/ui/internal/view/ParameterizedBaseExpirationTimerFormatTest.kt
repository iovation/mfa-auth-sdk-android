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
public class ParameterizedBaseExpirationTimerFormatTest {

    private static final String FORMAT = ExpirationTimer.FORMAT_DEFAULT;

    @ParameterizedRobolectricTestRunner.Parameters(name = "Minutes={0}, Seconds={1}. Expected={2} (" + FORMAT + ")")
    public static Collection<Object[]>  data() {

        // { min, sec, expectedFormattedString }
        return Arrays.asList(new Object[][] {
                { 0, 0, "00:00" },
                { 0, 1, "00:01" },
                { 0, 24, "00:24" },
                { 1, 0, "01:00" },
                { 11, 11, "11:11" },
                { 14, 59, "14:59" },
                { 14, 0, "14:00" },
                { 14, 9, "14:09" }
        });
    }

    private long mMinutes;
    private long mSeconds;
    private String mExpectedFormattedString;

    public ParameterizedBaseExpirationTimerFormatTest(long minutes, long seconds, String expectedFormattedString) {

        mMinutes = minutes;
        mSeconds = seconds;
        mExpectedFormattedString = expectedFormattedString;
    }

    @Test
    public void testMillisToMinSecValues() {

        final String formattedString = ExpirationTimer.convertMinSecToFormattedString(FORMAT, mMinutes, mSeconds);
        assertEquals(mExpectedFormattedString, formattedString);
    }
}
