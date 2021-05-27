package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager.CircleCodeTick
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodVerificationCallback
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent

class CircleCodeCheckViewModel(
    private val circleCodeManager: CircleCodeManager,
    private val thresholdAutoUnlink: Int,
    savedStateHandle: SavedStateHandle?) : ViewModel() {
    private val _verificationFlagState: MutableLiveData<VerificationFlag.State> = MutableLiveData()
    private val _requestState: MutableLiveData<RequestState> = SingleLiveEvent()
    private val _unlinkState: MutableLiveData<UnlinkState> = SingleLiveEvent()
    
    val verificationFlagState: LiveData<VerificationFlag.State>
        get() = _verificationFlagState
    
    val requestState: LiveData<RequestState>
        get() = _requestState
    
    val unlinkState: LiveData<UnlinkState>
        get() = _unlinkState
    
    init {
        if (circleCodeManager.isCircleCodeSet) {
            _verificationFlagState.value = circleCodeManager.verificationFlag.state
        }
    }
    
    fun verifyCircleCode(circleCode: List<CircleCodeTick>) {
        val changeOrRemoveState = _requestState.value
        if (changeOrRemoveState is RequestState.RemoveRequested || changeOrRemoveState is RequestState.RemoveRequestFailed) {
            removeCircleCode(circleCode)
        } else if (changeOrRemoveState is RequestState.ChangeRequested || changeOrRemoveState is RequestState.ChangeRequestFailed) {
            toggleVerificationFlag(
                circleCode,
                if (_verificationFlagState.value == VerificationFlag.State.ALWAYS) VerificationFlag.State.WHEN_REQUIRED else VerificationFlag.State.ALWAYS)
        }
    }
    
    fun requestRemoveCircleCode() {
        _requestState.value = RequestState.RemoveRequested
    }
    
    private fun removeCircleCode(circleCode: List<CircleCodeTick>) {
        circleCodeManager.removeCircleCode(circleCode, object : AuthMethodVerificationCallback {
            override fun onVerificationSuccess() {
                _requestState.value = RequestState.RemoveRequestSuccess
            }
    
            override fun onVerificationFailure(failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                _requestState.value = RequestState.RemoveRequestFailed(unlinkTriggered)
                if (unlinkTriggered) {
                    _unlinkState.setValue(UnlinkState.UnlinkTriggered(thresholdAutoUnlink))
                } else if (unlinkWarningTriggered) {
                    _unlinkState.value = UnlinkState.UnlinkWarningTriggered(attemptsRemaining!!)
                }
            }
        })
    }
    
    fun requestToggleVerificationFlag() {
        _requestState.value = RequestState.ChangeRequested
    }
    
    private fun toggleVerificationFlag(circleCode: List<CircleCodeTick>, newState: VerificationFlag.State) {
        circleCodeManager.changeVerificationFlag(circleCode, newState, object : AuthMethodVerificationCallback {
            override fun onVerificationSuccess() {
                _requestState.value = RequestState.ChangeRequestSuccess
                _verificationFlagState.value = newState
            }
    
            override fun onVerificationFailure(failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                _requestState.value = RequestState.ChangeRequestFailed(unlinkTriggered)
                if (unlinkTriggered) {
                    _unlinkState.setValue(UnlinkState.UnlinkTriggered(thresholdAutoUnlink))
                } else if (unlinkWarningTriggered) {
                    _unlinkState.value = UnlinkState.UnlinkWarningTriggered(attemptsRemaining!!)
                }
            }
        })
    }
    
    sealed class RequestState {
        object ChangeRequested : RequestState()
        object RemoveRequested : RequestState()
        object ChangeRequestSuccess : RequestState()
        object RemoveRequestSuccess : RequestState()
        abstract class RequestFailed(val unlinked: Boolean) : RequestState() {}
        class RemoveRequestFailed(unlinked: Boolean) : RequestFailed(unlinked)
        class ChangeRequestFailed(unlinked: Boolean) : RequestFailed(unlinked)
    }
    
    sealed class UnlinkState {
        data class UnlinkWarningTriggered(val attemptsRemaining: Int) : UnlinkState()
        data class UnlinkTriggered(val thresholdAutoUnlink: Int) : UnlinkState()
    }
}