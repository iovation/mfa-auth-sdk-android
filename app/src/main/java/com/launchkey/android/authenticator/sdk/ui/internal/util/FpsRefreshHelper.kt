/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.view.View

class FpsRefreshHelper internal constructor(view: View?, fps: Int, nowProvider: TimingCounter.NowProvider) {
    private val mView: View?
    private val mUpdateRate: Long
    private val nowProvider: TimingCounter.NowProvider
    private var mLastUpdate = 0L

    @JvmOverloads
    constructor(view: View?, fps: Int = FPS_DEFAULT) : this(view, fps, TimingCounter.DefaultTimeProvider()) {
    }

    fun invalidate(): Boolean {
        return invalidate(false)
    }

    fun forceInvalidate(): Boolean {
        return invalidate(true)
    }

    private fun invalidate(forced: Boolean): Boolean {
        var mustUpdate = forced
        val now = nowProvider.now
        if (!forced) {
            mustUpdate = now >= mLastUpdate + mUpdateRate
        }
        if (mustUpdate && mView != null) {
            mLastUpdate = now
            mView.invalidate()
        }
        return mustUpdate
    }

    companion object {
        const val FPS_MIN = 1
        const val FPS_DEFAULT = 20
        const val FPS_MAX = 60
    }

    init {
        var fps = fps
        if (fps > FPS_MAX) {
            fps = FPS_MAX
        } else if (fps < FPS_MIN) {
            fps = FPS_MIN
        }
        mView = view
        mUpdateRate = 1000L / fps.toLong()
        this.nowProvider = nowProvider
    }
}