package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.launchkey.android.authenticator.sdk.ui.TestCoroutineRule
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.TimerViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TimerViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private var fakeNow = 0L
    private val fakeNowProvider = object : TimingCounter.NowProvider {
        override val now: Long
            get() = fakeNow++
    }
    private val viewModel = TimerViewModel(
        fakeNowProvider,
        testCoroutineRule.testCoroutineDispatcher,
        SavedStateHandle()
    )
    
    @Test
    fun testSingleTimerShouldComplete() = testCoroutineRule.runBlockingTest {
        val timerItems = listOf(
            TimerViewModel.TimerItem(0, 10L)
        )
        
        pauseDispatcher { // manual control
            viewModel.startTimers(timerItems, 1L)
            
            repeat(9) {
                testCoroutineRule.testCoroutineDispatcher.advanceTimeBy(1L)
            }
            
            assertThat(
                viewModel.state.value,
                instanceOf(TimerViewModel.State.ItemUpdated::class.java)
            )
            assertEquals(
                1,
                (viewModel.state.value as TimerViewModel.State.ItemUpdated<*>).remainingMillis
            )
            
            testCoroutineRule.testCoroutineDispatcher.advanceTimeBy(1L)
            assertThat(
                viewModel.state.value,
                instanceOf(TimerViewModel.State.AllItemsFinished::class.java)
            )
        }
    }
    
    @Test
    fun testMultipleTimersShouldComplete() = testCoroutineRule.runBlockingTest {
        val itemCount = 5
        val endsAt = (10 * itemCount).toLong()
        val timerItems = (1..itemCount).map {
            TimerViewModel.TimerItem(it, endsAt)
        }
        
        pauseDispatcher { // manual control
            viewModel.startTimers(timerItems, 1L)
            
            repeat(9) {
                testCoroutineRule.testCoroutineDispatcher.advanceTimeBy(1L)
            }
            
            assertThat(
                viewModel.state.value,
                instanceOf(TimerViewModel.State.ItemUpdated::class.java)
            )
            assertEquals(
                1,
                (viewModel.state.value as TimerViewModel.State.ItemUpdated<*>).remainingMillis
            )
            
            testCoroutineRule.testCoroutineDispatcher.advanceTimeBy(1L)
            assertThat(
                viewModel.state.value,
                instanceOf(TimerViewModel.State.AllItemsFinished::class.java)
            )
        }
    }
}