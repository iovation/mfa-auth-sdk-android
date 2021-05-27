package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.PINCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.AuthMethodAlreadySetException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.pin_code.PINCodeTooLongException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.pin_code.PINCodeTooShortException
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinEmpty
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinInvalid
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinSetFailed
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinSetSuccess
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinValid
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class PinCodeAddViewModel(
    private val pinCodeManager: PINCodeManager,
    val pinCodeRequirements: List<PINCodeRequirement>,
    savedStateHandle: SavedStateHandle?
) : ViewModel() {
    private val _verificationFlagState = MutableLiveData(VerificationFlag.State.ALWAYS)
    private val _pinCodeState: MutableLiveData<PinCodeState> = SingleLiveEvent(PinEmpty)

    val pinCodeState: LiveData<PinCodeState>
        get() = _pinCodeState
    val verificationFlagState: LiveData<VerificationFlag.State>
        get() = _verificationFlagState

    fun setVerificationFlagState(state: VerificationFlag.State) {
        _verificationFlagState.value = state
    }

    fun setPinCode(pinCode: String) {
        try {
            pinCodeManager.setPINCode(pinCode, _verificationFlagState.value!!)
        } catch (e: AuthMethodAlreadySetException) {
            _pinCodeState.postValue(PinSetFailed(e))
        } catch (e: PINCodeTooLongException) {
            _pinCodeState.postValue(PinSetFailed(e))
        } catch (e: PINCodeTooShortException) {
            _pinCodeState.postValue(PinSetFailed(e))
        }

        _pinCodeState.postValue(PinSetSuccess)
    }

    /*
     *  The official requirement is pinCode.length() >= {@link PINCodeManager#PIN_CODE_MIN} but for the
     * UI we require 4 to maintain previous functionality
     */
    fun validatePin(pinCode: String) {
        val requirementsNotMet = checkRequirements(pinCode)
        when {
            pinCode.isEmpty() -> {
                _pinCodeState.value = PinEmpty
            }
            pinCode.length >= maxOf(PINCodeManager.PIN_CODE_MIN, 4) -> {
                _pinCodeState.value =
                    if (requirementsNotMet.isEmpty()) PinValid
                    else PinInvalid(requirementsNotMet)
            }
            else -> {
                _pinCodeState.value = PinInvalid(requirementsNotMet)
            }
        }
    }

    private fun checkRequirements(pinCode: String): List<PINCodeRequirement> {
        val secret = pinCode.map { it.toInt() }

        return pinCodeRequirements.filter { pinCodeRequirement ->
            !pinCodeRequirement.requirement.isValid(secret)
        }
    }

    sealed class PinCodeState {
        object PinSetSuccess : PinCodeState()
        data class PinSetFailed(val exception: Exception) : PinCodeState()
        object PinValid : PinCodeState()
        data class PinInvalid(val requirementsNotMet: List<PINCodeRequirement>) : PinCodeState()
        object PinEmpty : PinCodeState()
    }
}