package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.core.auth_method_management.PINCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodVerificationCallback
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent

class PinCodeCheckViewModel(
    private val pinCodeManager: PINCodeManager,
    private val thresholdAutoUnlink: Int,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val VERIFICATION_STATE = "verification_state"
        private const val VERIFICATION_ALWAYS = "verification_always"
        private const val VERIFICATION_WHEN_REQUIRED = "verification_when_required"

        private const val REQUEST_STATE = "request_state"
        private const val REMOVE_REQUESTED = "remove_requested"
        private const val REMOVE_REQUEST_SUCCESS = "remove_request_success"
        private const val REMOVE_REQUEST_FAILED = "remove_request_failed"
        private const val CHANGE_REQUESTED = "change_requested"
        private const val CHANGE_REQUEST_SUCCESS = "change_request_success"
        private const val CHANGE_REQUEST_FAILED = "change_request_failed"

        private const val UNLINK_STATE = "unlink_state"
        private const val UNLINK_WARNING_TRIGGERED = "unlink_warning_triggered"
        private const val UNLINK_WARNING_TRIGGERED_COUNT = "unlink_warning_triggered_count"
        private const val UNLINK_TRIGGERED = "unlink_triggered"
        private const val UNLINK_TRIGGERED_COUNT = "unlink_triggered_count"
    }

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
        if (savedStateHandle.contains(VERIFICATION_STATE)) {
            _verificationFlagState.value = when (savedStateHandle.get<String>(VERIFICATION_STATE)) {
                VERIFICATION_ALWAYS -> VerificationFlag.State.ALWAYS
                VERIFICATION_WHEN_REQUIRED -> VerificationFlag.State.WHEN_REQUIRED
                else -> throw IllegalStateException("Invalid Verification State")
            }
        } else {
            val state = pinCodeManager.verificationFlag.state
            savedStateHandle.set(VERIFICATION_STATE, when (state) {
                VerificationFlag.State.ALWAYS -> VERIFICATION_ALWAYS
                VerificationFlag.State.WHEN_REQUIRED -> VERIFICATION_WHEN_REQUIRED
            })
            _verificationFlagState.value = state
        }
        if (savedStateHandle.contains(REQUEST_STATE)) _requestState.value = when (savedStateHandle.get<String>(REQUEST_STATE)) {
            REMOVE_REQUESTED -> RequestState.RemoveRequested
            REMOVE_REQUEST_SUCCESS -> RequestState.RemoveRequestSuccess
            REMOVE_REQUEST_FAILED -> RequestState.RemoveRequestFailed
            CHANGE_REQUESTED -> RequestState.ChangeRequested
            CHANGE_REQUEST_SUCCESS -> RequestState.ChangeRequestSuccess
            CHANGE_REQUEST_FAILED -> RequestState.ChangeRequestFailed
            else -> throw IllegalStateException("Invalid Request State")
        }
        if (savedStateHandle.contains(UNLINK_STATE)) _unlinkState.value = when (savedStateHandle.get<String>(UNLINK_STATE)) {
            UNLINK_WARNING_TRIGGERED -> UnlinkState.UnlinkWarningTriggered(savedStateHandle.get<Int>(UNLINK_WARNING_TRIGGERED_COUNT)!!)
            UNLINK_TRIGGERED -> UnlinkState.UnlinkTriggered(savedStateHandle.get<Int>(UNLINK_TRIGGERED_COUNT)!!)
            else -> throw IllegalStateException("Invalid Unlink State")
        }
    }

    fun verifyPinCode(pinCode: String) {
        when (_requestState.value) {
            is RequestState.RemoveRequested, is RequestState.RemoveRequestFailed -> {
                removePinCode(pinCode)
            }
            is RequestState.ChangeRequested, is RequestState.ChangeRequestFailed -> {
                changeVerificationFlag(
                    pinCode,
                    if (_verificationFlagState.value == VerificationFlag.State.ALWAYS)
                        VerificationFlag.State.WHEN_REQUIRED
                    else VerificationFlag.State.ALWAYS
                )
            }
            else -> Unit
        }
    }

    fun requestRemovePinCode() {
        _requestState.value =
            RequestState.RemoveRequested
    }

    fun requestChangeVerificationFlag() {
        _requestState.value =
            RequestState.ChangeRequested
    }

    private fun removePinCode(pinCode: String) {
        pinCodeManager.removePINCode(pinCode, object : AuthMethodVerificationCallback {
            override fun onVerificationSuccess() {
                savedStateHandle.set(REQUEST_STATE, REMOVE_REQUEST_SUCCESS)
                _requestState.value = RequestState.RemoveRequestSuccess
            }

            override fun onVerificationFailure(
                failure: AuthMethodFailure,
                unlinkTriggered: Boolean,
                unlinkWarningTriggered: Boolean,
                attemptsRemaining: Int?
            ) {
                savedStateHandle.set(REQUEST_STATE, REMOVE_REQUEST_FAILED)
                _requestState.value = RequestState.RemoveRequestFailed
                if (unlinkTriggered) {
                    savedStateHandle.set(UNLINK_STATE, UNLINK_TRIGGERED)
                    savedStateHandle.set(UNLINK_TRIGGERED_COUNT, thresholdAutoUnlink)
                    _unlinkState.value = UnlinkState.UnlinkTriggered(thresholdAutoUnlink)
                } else if (unlinkWarningTriggered) {
                    savedStateHandle.set(UNLINK_STATE, UNLINK_WARNING_TRIGGERED)
                    savedStateHandle.set(UNLINK_WARNING_TRIGGERED_COUNT, attemptsRemaining!!)
                    _unlinkState.value = UnlinkState.UnlinkWarningTriggered(attemptsRemaining)
                }
            }
        })
    }


    private fun changeVerificationFlag(pinCode: String, newState: VerificationFlag.State) {
        pinCodeManager.changeVerificationFlag(
            pinCode,
            newState,
            object : AuthMethodVerificationCallback {
                override fun onVerificationSuccess() {
                    savedStateHandle.set(REQUEST_STATE, CHANGE_REQUEST_SUCCESS)
                    _requestState.value = RequestState.ChangeRequestSuccess
                    savedStateHandle.set(VERIFICATION_STATE, when (newState) {
                        VerificationFlag.State.ALWAYS -> VERIFICATION_ALWAYS
                        VerificationFlag.State.WHEN_REQUIRED -> VERIFICATION_WHEN_REQUIRED
                    })
                    _verificationFlagState.value = newState
                }

                override fun onVerificationFailure(
                    failure: AuthMethodFailure,
                    unlinkTriggered: Boolean,
                    unlinkWarningTriggered: Boolean,
                    attemptsRemaining: Int?
                ) {
                    savedStateHandle.set(REQUEST_STATE, CHANGE_REQUEST_FAILED)
                    _requestState.value = RequestState.ChangeRequestFailed
                    if (unlinkTriggered) {
                        savedStateHandle.set(UNLINK_STATE, UNLINK_TRIGGERED)
                        savedStateHandle.set(UNLINK_TRIGGERED_COUNT, thresholdAutoUnlink)
                        _unlinkState.value = UnlinkState.UnlinkTriggered(thresholdAutoUnlink)
                    } else if (unlinkWarningTriggered) {
                        savedStateHandle.set(UNLINK_STATE, UNLINK_WARNING_TRIGGERED)
                        savedStateHandle.set(UNLINK_WARNING_TRIGGERED_COUNT, attemptsRemaining!!)
                        _unlinkState.value = UnlinkState.UnlinkWarningTriggered(attemptsRemaining)
                    }
                }
            })
    }

    sealed class RequestState {
        object RemoveRequested : RequestState()
        object RemoveRequestSuccess : RequestState()
        object RemoveRequestFailed : RequestState()
        object ChangeRequested : RequestState()
        object ChangeRequestSuccess : RequestState()
        object ChangeRequestFailed : RequestState()
    }

    sealed class UnlinkState {
        data class UnlinkWarningTriggered(val attemptsRemaining: Int) : UnlinkState()
        data class UnlinkTriggered(val thresholdAutoUnlink: Int) : UnlinkState()
    }

}