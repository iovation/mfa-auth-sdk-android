package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.biometric

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.core.auth_method_management.BiometricManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.core.util.Disposable
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher

class BiometricAddViewModel(
    private val biometricManager: BiometricManager,
    private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var scanJob: Disposable? = null
    private val _verificationFlagState = MutableLiveData(VerificationFlag.State.ALWAYS)
    private val _biometricState: MutableLiveData<BiometricState> = SingleLiveEvent()

    val verificationFlagState: LiveData<VerificationFlag.State>
        get() = _verificationFlagState

    val biometricState: LiveData<BiometricState>
        get() = _biometricState

    override fun onCleared() {
        cancelScan()
    }

    fun setVerificationFlagState(state: VerificationFlag.State) {
        _verificationFlagState.value = state
    }

    fun needsUiToCancel() = biometricManager.needsUiToCancelBiometric()

    fun scanBiometric() {
        cancelScan()
        _biometricState.postValue(BiometricState.Scanning)
        scanJob = biometricManager.setBiometric(
            _verificationFlagState.value!!,
            object : BiometricManager.SetBiometricCallback {
                override fun onSetSuccess() {
                    _biometricState.postValue(BiometricState.Set)
                }

                override fun onSetFailure(authMethodFailure: AuthMethodFailure) {
                    _biometricState.postValue(BiometricState.Failed(authMethodFailure))
                }
            })
    }

    fun cancelScan() {
        scanJob?.dispose()
        scanJob = null
    }

    sealed class BiometricState {
        object Scanning : BiometricState()
        object Set : BiometricState()
        data class Failed(val failure: AuthMethodFailure) : BiometricState()
    }
}