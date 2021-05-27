/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

interface TimerDisplay {
    fun onTimerUpdate(remainingMillis: Long, progress: Float)
}