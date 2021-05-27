package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager.CircleCodeTick
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.circle_code.CircleCodeTooLongException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.circle_code.CircleCodeTooShortException
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code.CircleCodeAddViewModel.State.*
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent

class CircleCodeAddViewModel(
    private val circleCodeManager: CircleCodeManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val ticks = mutableListOf<CircleCodeTick>()
    private val _state: MutableLiveData<State> = SingleLiveEvent()
    
    val state: LiveData<State>
        get() = _state
    
    fun addCircleCode(ticks: List<CircleCodeTick>, flag: VerificationFlag.State) {
        if (_state.value is CircleCodeAwaitingVerification) {
            try {
                if (this.ticks == ticks) {
                    circleCodeManager.setCircleCode(ticks, flag)
                    _state.setValue(CircleCodeSetSuccess)
                } else {
                    _state.setValue(CircleCodeSetFailed(CircleCodesDoNotMatchException()))
                }
            } catch (e: Exception) {
                _state.setValue(CircleCodeSetFailed(e))
            }
        } else {
            if (ticks.size < CircleCodeManager.CIRCLE_CODE_MIN) {
                _state.value = CircleCodeSetFailed(CircleCodeTooShortException(""))
                return
            } else if (ticks.size > CircleCodeManager.CIRCLE_CODE_MAX) {
                _state.value = CircleCodeSetFailed(CircleCodeTooLongException(""))
                return
            }
            this.ticks.clear()
            this.ticks.addAll(ticks)
            _state.setValue(CircleCodeAwaitingVerification)
        }
    }
    
    sealed class State {
        object CircleCodeAwaitingVerification : State()
        object CircleCodeSetSuccess : State()
        data class CircleCodeSetFailed(val exception: Exception) : State()
    }
}