/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.common

import android.content.Context
import com.launchkey.android.authenticator.sdk.ui.R
import java.util.*

class TimeAgo {
    protected var mContext: Context
    private var mCurrentTime: Date? = null

    constructor(context: Context) {
        mContext = context
    }

    constructor(context: Context, currentTime: Date?) {
        mContext = context
        mCurrentTime = currentTime
    }

    private val currentTime: Date?
        private get() = if (mCurrentTime == null) {
            Date()
        } else mCurrentTime

    fun timeAgo(date: Date, longFormat: Boolean): String {
        return timeAgo(date.time, longFormat)
    }

    fun timeAgo(originalSeconds: Long, newSeconds: Long): String {
        val diffSeconds = originalSeconds - newSeconds
        return timeAgoWithDiff(diffSeconds * 1000, false)
    }

    fun timeAgo(millis: Long, longFormat: Boolean): String {
        val diff = currentTime!!.time - millis
        return timeAgoWithDiff(diff, longFormat)
    }

    fun timeAgoWithDiff(longMilliSeconds: Long, longFormat: Boolean): String {
        val r = mContext.resources
        val longSeconds = longMilliSeconds.toDouble() / 1000.0
        if (longSeconds < 1 || longSeconds > Int.MAX_VALUE) {
            return r.getString(R.string.ioa_time_ago_now)
        }
        val minutes = longSeconds / 60.0
        val hours = minutes / 60.0
        val days = hours / 24.0
        val weeks = days / 7.0
        val months = days / 30.0
        val years = days / 365.0
        val words: String
        words = if (longSeconds < 60) {
            if (longFormat) {
                r.getQuantityString(R.plurals.ioa_time_ago_long_seconds, longSeconds.toInt(), longSeconds.toInt())
            } else {
                r.getString(R.string.ioa_time_ago_seconds, longSeconds.toInt())
            }
        } else if (minutes < 60) {
            if (longFormat) {
                r.getQuantityString(R.plurals.ioa_time_ago_long_minutes, minutes.toInt(), minutes.toInt())
            } else {
                r.getString(R.string.ioa_time_ago_minutes, minutes.toInt())
            }
        } else if (hours < 24) {
            if (longFormat) {
                r.getQuantityString(R.plurals.ioa_time_ago_long_hours, hours.toInt(), hours.toInt())
            } else {
                r.getString(R.string.ioa_time_ago_hours, hours.toInt())
            }
        } else if (days < 7) {
            if (longFormat) {
                r.getQuantityString(R.plurals.ioa_time_ago_long_days, days.toInt(), days.toInt())
            } else {
                r.getString(R.string.ioa_time_ago_days, days.toInt())
            }
        } else if (days < 30) {
            if (longFormat) {
                r.getQuantityString(R.plurals.ioa_time_ago_long_weeks, weeks.toInt(), weeks.toInt())
            } else {
                r.getString(R.string.ioa_time_ago_weeks, weeks.toInt())
            }
        } else if (days < 365) {
            if (longFormat) {
                r.getQuantityString(R.plurals.ioa_time_ago_long_months, months.toInt(), months.toInt())
            } else {
                r.getString(R.string.ioa_time_ago_months, months.toInt())
            }
        } else {
            if (longFormat) {
                r.getQuantityString(R.plurals.ioa_time_ago_long_years, years.toInt(), years.toInt())
            } else {
                r.getString(R.string.ioa_time_ago_years, years.toInt())
            }
        }
        return words.trim { it <= ' ' }
    }
}