package com.launchkey.android.authenticator.sdk.ui.internal.auth_method

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimerState
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import com.launchkey.android.authenticator.sdk.ui.internal.util.flowTimer
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class TimerViewModel(
    private val nowProvider: TimingCounter.NowProvider,
    private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = SingleLiveEvent<State>()
    val state: LiveData<State>
        get() = _state
    
    private val timerJobs = mutableMapOf<Any, Job>()
    
    override fun onCleared() {
        stopTimers()
        super.onCleared()
    }
    
    fun <T : Any> startTimers(timerItems: List<TimerItem<T>>, refreshRate: Long = 100L) {
        timerItems.forEach { timerItem ->
            timerJobs[timerItem.item] = viewModelScope.launch(defaultDispatcher) {
                flowTimer(
                    nowProvider,
                    timerItem.endsAt,
                    refreshRate
                ).conflate()
                    .collect { timerState ->
                        when (timerState) {
                            TimerState.Finished -> {
                                _state.postValue(State.ItemFinished(timerItem))
                                timerJobs[timerItem.item]?.cancel()
                                timerJobs.remove(timerItem.item)
                                if (timerJobs.isEmpty()) _state.postValue(State.AllItemsFinished)
                            }
                            is TimerState.Updated -> {
                                _state.postValue(
                                        State.ItemUpdated(
                                                timerItem,
                                                timerState.remainingMillis
                                        )
                                )
                            }
                        }
                    }
            }
        }
    }
    
    fun <T : Any> cancelTimerForItem(item: T) {
        timerJobs[item]?.cancel("Timer Stopped for item: $item", null)
        timerJobs.remove(item)
    }
    
    fun stopTimers() {
        timerJobs.values.forEach {
            it.cancel("All timers stopped", null)
        }
        timerJobs.clear()
    }
    
    sealed class State {
        data class ItemUpdated<T>(val timerItem: TimerItem<T>, val remainingMillis: Long) : State()
        data class ItemFinished<T>(val timerItem: TimerItem<T>) : State()
        object AllItemsFinished : State()
    }
    
    data class TimerItem<out T>(val item: T, val endsAt: Long)
}