package com.launchkey.android.authenticator.sdk.ui.internal.util

import com.launchkey.android.authenticator.sdk.ui.TestCoroutineRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


class FlowTimerTest {
    private var fakeNow = 0L
    private val fakeNowProvider = object : TimingCounter.NowProvider {
        override val now: Long
            get() = fakeNow++
    }
    
    @get:Rule
    val coroutineRule = TestCoroutineRule()
    
    @Test
    fun testStateIsUpdated() = coroutineRule.runBlockingTest {
        val stateUpdates = flowTimer(
            fakeNowProvider,
            endsAtInMillis = 10L,
            1L // doesn't matter because delay() calls are automatically advanced with the rule
        ).toList()
        
        stateUpdates.take(10).forEachIndexed { index, timerState ->
            assertTrue(timerState is TimerState.Updated)
            assertEquals((timerState as TimerState.Updated).remainingMillis, 10L - index)
        }
        
        assertTrue(stateUpdates.last() is TimerState.Finished)
    }
    
    @Test
    fun testStateIsFinished() = coroutineRule.runBlockingTest {
        val stateUpdates = flowTimer(
            fakeNowProvider,
            endsAtInMillis = 10L,
            10000L // doesn't matter because delay() calls are automatically advanced with the rule
        ).toList()
        
        assertTrue(stateUpdates.last() is TimerState.Finished)
    }
    
    @Test
    fun testTimerIsStoppedAfterCancelled() = coroutineRule.runBlockingTest {
        launch {
            flowTimer(
                fakeNowProvider,
                endsAtInMillis = 10L,
                10L
            ).collect {
                if (it is TimerState.Updated && it.remainingMillis == 5L) {
                    cancel("Cancelled with ${it.remainingMillis} left")
                }
            }
        }.invokeOnCompletion {
            assertTrue(it is CancellationException)
            assertEquals(5, fakeNowProvider.now - 1)
        }
    }
}