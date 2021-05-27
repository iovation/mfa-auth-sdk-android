/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.os.Handler
import androidx.lifecycle.*
import com.launchkey.android.authenticator.sdk.ui.internal.util.ExpirationTimerTracker.State.Expired
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent

class ExpirationTimerTracker @JvmOverloads constructor(otherNow: Long,
                                                       otherStartsAt: Long,
                                                       private val mOtherEndsAt: Long,
                                                       private val mNowProvider: TimingCounter.NowProvider,
                                                       handler: Handler,
                                                       lifecycleOwner: LifecycleOwner,
                                                       private val mRefreshRate: Long = defaultRefreshRate) {
    private val mNowOffset: Long
    private val mDuration: Long
    private val mHandler: Handler
    private val mUpdates: Runnable
    private val _state: MutableLiveData<State> = SingleLiveEvent()
    val state: LiveData<State>
        get() = _state
    private fun start() {
        mUpdates.run()
    }

    fun stop() {
        // For whatever reason, cancelling a runnable (even on the same thread?) doesn't remove it
        // from the Looper queue. Posting then cancelling does...whatever.
        mHandler.post { mHandler.removeCallbacks(mUpdates) }
    }

    private fun calculateOtherNow(): Long {
        return now + mNowOffset
    }

    private val now: Long
        private get() = mNowProvider.now

    open class State {
        class Update(val remainingMillis: Long, val progress: Float) : State()
        class Expired : State()
    }

    companion object {
        private val defaultRefreshRate: Long
            private get() {
                val fpsScale = 1.0f
                var fps = FpsRefreshHelper.FPS_DEFAULT * fpsScale
                if (fps > FpsRefreshHelper.FPS_MAX) {
                    fps = FpsRefreshHelper.FPS_MAX.toFloat()
                } else if (fps < FpsRefreshHelper.FPS_MIN) {
                    fps = FpsRefreshHelper.FPS_MIN.toFloat()
                }
                return (1000f / fps).toLong()
            }
    }

    init {
        mDuration = mOtherEndsAt - otherStartsAt
        mNowOffset = otherNow - now
        mHandler = handler
        mUpdates = object : Runnable {
            override fun run() {
                val otherNow = calculateOtherNow()
                val otherRemaining = mOtherEndsAt - otherNow
                val progress = 1.0f - otherRemaining / mDuration.toFloat()
                if (otherRemaining < 0) {
                    _state.value = Expired()
                    return
                }
                _state.value = State.Update(otherRemaining, progress)
                mHandler.postDelayed(this, mRefreshRate)
            }
        }
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    lifecycleOwner.lifecycle.removeObserver(this)
                    return
                }
                if (event == Lifecycle.Event.ON_RESUME) {
                    start()
                } else if (event == Lifecycle.Event.ON_PAUSE) {
                    stop()
                }
            }
        })
    }
}