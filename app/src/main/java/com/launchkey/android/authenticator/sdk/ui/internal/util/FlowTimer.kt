package com.launchkey.android.authenticator.sdk.ui.internal.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

fun flowTimer(
    nowProvider: TimingCounter.NowProvider,
    endsAtInMillis: Long,
    refreshRateInMillis: Long,
) = flow {
    var remainingMillis = endsAtInMillis - nowProvider.now
    
    while (remainingMillis > 0) {
        emit(TimerState.Updated(remainingMillis))
        delay(refreshRateInMillis)
        remainingMillis = endsAtInMillis - nowProvider.now
    }
    
    emit(TimerState.Finished)
}

sealed class TimerState {
    data class Updated(val remainingMillis: Long) :
        TimerState()
    
    object Finished : TimerState()
}
