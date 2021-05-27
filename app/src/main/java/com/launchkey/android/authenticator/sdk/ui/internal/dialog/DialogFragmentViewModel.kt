package com.launchkey.android.authenticator.sdk.ui.internal.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.lang.IllegalStateException

class DialogFragmentViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    companion object {
        private const val STATE = "state"
        private const val NEEDS_TO_BE_SHOWN = 1
        private const val SHOWN = 2
        private const val GONE = 3
    }
    private val _state: MutableLiveData<State> = MutableLiveData()
    val state: LiveData<State>
        get() = _state
    init {
        val state: Int? = savedStateHandle.get(STATE)
        if (state != null) {
            this._state.value = when (state) {
                NEEDS_TO_BE_SHOWN -> State.NeedsToBeShown
                SHOWN -> State.Shown
                GONE -> State.Gone
                else -> throw IllegalStateException("Unknown state")
            }
        }
    }

    fun changeState(state: State) {
        savedStateHandle.set(STATE, when (state) {
            is State.NeedsToBeShown -> NEEDS_TO_BE_SHOWN
            is State.Shown -> SHOWN
            is State.Gone -> GONE
        })
        _state.postValue(state)
    }

    sealed class State {
        object NeedsToBeShown : State()
        object Shown : State()
        object Gone : State()
    }
}