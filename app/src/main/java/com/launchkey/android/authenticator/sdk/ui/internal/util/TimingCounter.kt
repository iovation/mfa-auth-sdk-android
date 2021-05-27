/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.os.SystemClock

class TimingCounter @JvmOverloads constructor(private val mProvider: NowProvider = DefaultTimeProvider()) {
    private var mStartedAt: Long = 0
    private var mTiming: Long = 0
    fun start(): TimingCounter {
        mStartedAt = mProvider.now
        return this
    }

    fun end(): Long {
        val mEndedAt = mProvider.now
        mTiming = mEndedAt - mStartedAt
        return mTiming
    }

    fun total(): Long {
        return mTiming
    }

    interface NowProvider {
        val now: Long
    }

    class DefaultTimeProvider : NowProvider {
        override val now: Long
            get() = SystemClock.elapsedRealtime()
    }
}