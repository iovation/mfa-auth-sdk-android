package com.launchkey.android.authenticator.sdk.ui.internal.common

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.launchkey.android.authenticator.sdk.ui.R
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
class TimeAgoTest {
    var mContext: Context? = null
    @Before
    fun setup() {
        mContext = ApplicationProvider.getApplicationContext()
    }
    
    @Test
    fun testTimeAgoNegativeSeconds() {
        val expectedValue = mContext!!.getString(R.string.ioa_time_ago_now)
        testTimeAgoGeneric(negativeMilli, expectedValue, expectedValue)
    }
    
    @Test
    fun testTimeAgoZeroSeconds() {
        val expectedValue = mContext!!.getString(R.string.ioa_time_ago_now)
        testTimeAgoGeneric(zeroMilli, expectedValue, expectedValue)
    }
    
    @Test
    fun testTimeAgoLessThanOneSecond() {
        val expectedValue = mContext!!.getString(R.string.ioa_time_ago_now)
        testTimeAgoGeneric(lessThanOneSecondMilli, expectedValue, expectedValue)
    }
    
    @Test
    fun testTimeAgoOneSecond() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_seconds, 1, 1)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_seconds, 1, 1)
        testTimeAgoGeneric(oneSecondMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoMultipleSeconds() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_seconds, 59, 59)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_seconds, 59)
        testTimeAgoGeneric(lessThanOneMinuteMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoOneMinute() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_minutes, 1, 1)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_minutes, 1)
        testTimeAgoGeneric(oneMinuteMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoMultipleMinutes() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_minutes, 59, 59)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_minutes, 59)
        testTimeAgoGeneric(lessThanOneHourMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoOneHour() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_hours, 1, 1)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_hours, 1)
        testTimeAgoGeneric(oneHourMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoMultipleHours() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_hours, 23, 23)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_hours, 23)
        testTimeAgoGeneric(lessThanOneDayMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoOneDay() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_days, 1, 1)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_days, 1)
        testTimeAgoGeneric(oneDayMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoMultipleDays() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_days, 6, 6)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_days, 6)
        testTimeAgoGeneric(lessThanOneWeekMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoOneWeek() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_weeks, 1, 1)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_weeks, 1)
        testTimeAgoGeneric(oneWeekMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoMultipleWeeks() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_weeks, 4, 4)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_weeks, 4)
        testTimeAgoGeneric(lessThanOneMonthMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoOneMonth() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_months, 1, 1)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_months, 1)
        testTimeAgoGeneric(oneMonthMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoMultipleMonths() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_months, 12, 12)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_months, 12)
        testTimeAgoGeneric(lessThanOneYearMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoOneYear() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_years, 1, 1)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_years, 1)
        testTimeAgoGeneric(oneYearMilli, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoMultipleYears() {
        val expectedLongValue =
            mContext!!.resources.getQuantityString(R.plurals.ioa_time_ago_long_years, 68, 68)
        val expectedShortValue = mContext!!.resources.getString(R.string.ioa_time_ago_years, 68)
        testTimeAgoGeneric(Int.MAX_VALUE * 1000L, expectedLongValue, expectedShortValue)
    }
    
    @Test
    fun testTimeAgoLargerThanMaxInteger() {
        val expectedValue = mContext!!.getString(R.string.ioa_time_ago_now)
        testTimeAgoGeneric(Int.MAX_VALUE * 1000L + 1000L, expectedValue, expectedValue)
    }
    
    private fun testTimeAgoGeneric(
        milliseconds: Long,
        expectedLongValue: String,
        expectedShortValue: String
    ) {
        val seconds = milliseconds / 1000L
        val currentDate = Date()
        val previousDate = Date(currentDate.time - milliseconds)
        val timeAgo = TimeAgo(mContext!!, currentDate)
        
        // Test date long version
        var timeAgoString = timeAgo.timeAgo(previousDate, true)
        Assert.assertEquals(expectedLongValue, timeAgoString)
        
        // Test date short version
        timeAgoString = timeAgo.timeAgo(previousDate, false)
        Assert.assertEquals(expectedShortValue, timeAgoString)
        
        // Test seconds version
        timeAgoString = timeAgo.timeAgo(seconds, 0L)
        Assert.assertEquals(expectedShortValue, timeAgoString)
        
        // Test milliseconds long version
        timeAgoString = timeAgo.timeAgo(currentDate.time - milliseconds, true)
        Assert.assertEquals(expectedLongValue, timeAgoString)
        
        // Test milliseconds short version
        timeAgoString = timeAgo.timeAgo(currentDate.time - milliseconds, false)
        Assert.assertEquals(expectedShortValue, timeAgoString)
    }
    
    companion object {
        // Time constants
        private const val timeOffsetMilli: Long =
            1 // If tests fail it could be because of this variable
        
        // since the function relies on `new Date().getTime()`
        private const val negativeMilli = -1000L
        private const val zeroMilli = 0L
        private const val oneSecondMilli = 1000L
        private const val lessThanOneSecondMilli = oneSecondMilli - timeOffsetMilli
        private const val oneMinuteMilli = 60L * oneSecondMilli
        private const val lessThanOneMinuteMilli = oneMinuteMilli - timeOffsetMilli
        private const val oneHourMilli = 60 * oneMinuteMilli
        private const val lessThanOneHourMilli = oneHourMilli - timeOffsetMilli
        private const val oneDayMilli = 24 * oneHourMilli
        private const val lessThanOneDayMilli = oneDayMilli - timeOffsetMilli
        private const val oneWeekMilli = 7 * oneDayMilli
        private const val lessThanOneWeekMilli = oneWeekMilli - timeOffsetMilli
        private const val oneMonthMilli = 30 * oneDayMilli
        private const val lessThanOneMonthMilli = oneMonthMilli - timeOffsetMilli
        private const val oneYearMilli = 365 * oneDayMilli
        private const val lessThanOneYearMilli = oneYearMilli - timeOffsetMilli
    }
}