package com.launchkey.android.authenticator.sdk.ui.internal.common;

import android.content.Context;

import com.launchkey.android.authenticator.sdk.ui.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class TimeAgoTest {

    // Time constants
    private static final long timeOffsetMilli = 1; // If tests fail it could be because of this variable
    // since the function relies on `new Date().getTime()`
    private static final long negativeMilli = -1000L;
    private static final long zeroMilli = 0L;
    private static final long oneSecondMilli = 1000L;
    private static final long lessThanOneSecondMilli = oneSecondMilli - timeOffsetMilli;
    private static final long oneMinuteMilli = 60L * oneSecondMilli;
    private static final long lessThanOneMinuteMilli = oneMinuteMilli - timeOffsetMilli;
    private static final long oneHourMilli = 60 * oneMinuteMilli;
    private static final long lessThanOneHourMilli = oneHourMilli - timeOffsetMilli;
    private static final long oneDayMilli = 24 * oneHourMilli;
    private static final long lessThanOneDayMilli = oneDayMilli - timeOffsetMilli;
    private static final long oneWeekMilli = 7 * oneDayMilli;
    private static final long lessThanOneWeekMilli = oneWeekMilli - timeOffsetMilli;
    private static final long oneMonthMilli = 30 * oneDayMilli;
    private static final long lessThanOneMonthMilli = oneMonthMilli - timeOffsetMilli;
    private static final long oneYearMilli = 365 * oneDayMilli;
    private static final long lessThanOneYearMilli = oneYearMilli - timeOffsetMilli;

    Context mContext;

    @Before
    public void setup() {
        mContext = androidx.test.core.app.ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testTimeAgoNegativeSeconds() {

        String expectedValue = mContext.getString(R.string.ioa_time_ago_now);

        testTimeAgoGeneric(negativeMilli, expectedValue, expectedValue);
    }

    @Test
    public void testTimeAgoZeroSeconds() {

        String expectedValue = mContext.getString(R.string.ioa_time_ago_now);

        testTimeAgoGeneric(zeroMilli, expectedValue, expectedValue);
    }

    @Test
    public void testTimeAgoLessThanOneSecond() {

        String expectedValue = mContext.getString(R.string.ioa_time_ago_now);

        testTimeAgoGeneric(lessThanOneSecondMilli, expectedValue, expectedValue);
    }

    @Test
    public void testTimeAgoOneSecond() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_seconds, 1, 1);
        String expectedShortValue =
                mContext.getResources().getString(R.string.ioa_time_ago_seconds, 1, 1);

        testTimeAgoGeneric(oneSecondMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoMultipleSeconds() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_seconds, 59, 59);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_seconds, 59);

        testTimeAgoGeneric(lessThanOneMinuteMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoOneMinute() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_minutes, 1, 1);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_minutes, 1);

        testTimeAgoGeneric(oneMinuteMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoMultipleMinutes() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_minutes, 59, 59);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_minutes, 59);

        testTimeAgoGeneric(lessThanOneHourMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoOneHour() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_hours, 1, 1);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_hours, 1);

        testTimeAgoGeneric(oneHourMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoMultipleHours() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_hours, 23, 23);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_hours, 23);

        testTimeAgoGeneric(lessThanOneDayMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoOneDay() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_days, 1, 1);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_days, 1);

        testTimeAgoGeneric(oneDayMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoMultipleDays() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_days, 6, 6);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_days, 6);

        testTimeAgoGeneric(lessThanOneWeekMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoOneWeek() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_weeks, 1, 1);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_weeks, 1);

        testTimeAgoGeneric(oneWeekMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoMultipleWeeks() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_weeks, 4, 4);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_weeks, 4);

        testTimeAgoGeneric(lessThanOneMonthMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoOneMonth() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_months, 1, 1);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_months, 1);

        testTimeAgoGeneric(oneMonthMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoMultipleMonths() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_months, 12, 12);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_months, 12);

        testTimeAgoGeneric(lessThanOneYearMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoOneYear() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_years, 1, 1);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_years, 1);

        testTimeAgoGeneric(oneYearMilli, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoMultipleYears() {

        String expectedLongValue = mContext.getResources().getQuantityString(R.plurals.ioa_time_ago_long_years, 68, 68);
        String expectedShortValue = mContext.getResources().getString(R.string.ioa_time_ago_years, 68);

        testTimeAgoGeneric(Integer.MAX_VALUE * 1000L, expectedLongValue, expectedShortValue);
    }

    @Test
    public void testTimeAgoLargerThanMaxInteger() {

        String expectedValue = mContext.getString(R.string.ioa_time_ago_now);

        testTimeAgoGeneric(Integer.MAX_VALUE * 1000L + 1000L, expectedValue, expectedValue);
    }

    private void testTimeAgoGeneric(long milliseconds, String expectedLongValue, String expectedShortValue) {

        long seconds = milliseconds / 1000L;

        Date currentDate = new Date();
        Date previousDate = new Date(currentDate.getTime() - milliseconds);
        TimeAgo timeAgo = new TimeAgo(mContext, currentDate);

        // Test date long version
        String timeAgoString = timeAgo.timeAgo(previousDate, true);
        assertEquals(expectedLongValue, timeAgoString);

        // Test date short version
        timeAgoString = timeAgo.timeAgo(previousDate, false);
        assertEquals(expectedShortValue, timeAgoString);

        // Test seconds version
        timeAgoString = timeAgo.timeAgo(seconds, 0L);
        assertEquals(expectedShortValue, timeAgoString);

        // Test milliseconds long version
        timeAgoString = timeAgo.timeAgo(currentDate.getTime() - milliseconds, true);
        assertEquals(expectedLongValue, timeAgoString);

        // Test milliseconds short version
        timeAgoString = timeAgo.timeAgo(currentDate.getTime() - milliseconds, false);
        assertEquals(expectedShortValue, timeAgoString);
    }
}
