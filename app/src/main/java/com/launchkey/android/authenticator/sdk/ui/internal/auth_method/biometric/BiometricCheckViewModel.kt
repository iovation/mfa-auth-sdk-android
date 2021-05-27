package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.biometric

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.core.auth_method_management.BiometricManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodVerificationCallback
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.core.util.Disposable
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent

class BiometricCheckViewModel(
    private val biometricManager: BiometricManager,
    private val thresholdAutoUnlink: Int,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val VERIFICATION_STATE = "verification_state"
        private const val VERIFICATION_ALWAYS = "verification_always"
        private const val VERIFICATION_WHEN_REQUIRED = "verification_when_required"

        private const val BIOMETRIC_STATE = "biometric_state"
        private const val SCANNING_REMOVE = "scanning_remove"
        private const val SCANNING_TOGGLE = "scanning_toggle"
        private const val REMOVED = "removed"
        private const val TOGGLED = "toggled"
        private const val FAILED = "failed"

        private const val UNLINK_STATE = "unlink_state"
        private const val UNLINK_WARNING_TRIGGERED = "unlink_warning_triggered"
        private const val UNLINK_WARNING_TRIGGERED_COUNT = "unlink_warning_triggered_count"
        private const val UNLINK_TRIGGERED = "unlink_triggered"
        private const val UNLINK_TRIGGERED_COUNT = "unlink_triggered_count"
    }
    
    private var scanJob: Disposable? = null
    private val _biometricState: MutableLiveData<BiometricState> = SingleLiveEvent()
    private val _unlinkState: MutableLiveData<UnlinkState> = SingleLiveEvent()
    private val _verificationFlagState = MutableLiveData<VerificationFlag.State>()

    val biometricState: LiveData<BiometricState>
        get() = _biometricState

    val verificationFlagState: LiveData<VerificationFlag.State>
        get() = _verificationFlagState

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
            val state = biometricManager.verificationFlag.state
            savedStateHandle.set(VERIFICATION_STATE, when (state) {
                VerificationFlag.State.ALWAYS -> VERIFICATION_ALWAYS
                VerificationFlag.State.WHEN_REQUIRED -> VERIFICATION_WHEN_REQUIRED
            })
            _verificationFlagState.value = state
        }
        if (savedStateHandle.contains(BIOMETRIC_STATE)) _biometricState.value = when (savedStateHandle.get<String>(BIOMETRIC_STATE)) {
            SCANNING_REMOVE -> BiometricState.ScanningRemove
            SCANNING_TOGGLE -> BiometricState.ScanningToggle
            REMOVED -> BiometricState.Removed
            TOGGLED -> BiometricState.Toggled
            // TODO: Make AuthMethodFailure and sub-interfaces parcelable?
            FAILED -> BiometricState.Failed(object : AuthMethodFailure {})
            else -> throw IllegalStateException("Invalid Request State")
        }
        if (savedStateHandle.contains(UNLINK_STATE)) _unlinkState.value = when (savedStateHandle.get<String>(UNLINK_STATE)) {
            UNLINK_WARNING_TRIGGERED -> UnlinkState.UnlinkWarningTriggered(savedStateHandle.get<Int>(UNLINK_WARNING_TRIGGERED_COUNT)!!)
            UNLINK_TRIGGERED -> UnlinkState.UnlinkTriggered(savedStateHandle.get<Int>(UNLINK_TRIGGERED_COUNT)!!)
            else -> throw IllegalStateException("Invalid Unlink State")
        }

        if (_biometricState.value is BiometricState.ScanningRemove) {
            scanBiometricNoPost(true)
        } else if (_biometricState.value is BiometricState.ScanningRemove) {
            scanBiometricNoPost(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelScan()
    }

    fun needsUiToCancel() = biometricManager.needsUiToCancelBiometric()

    fun scanBiometric(removing: Boolean) {
        val state = if (removing) BiometricState.ScanningRemove else BiometricState.ScanningToggle
        savedStateHandle.set(BIOMETRIC_STATE, when (state) {
            BiometricState.ScanningRemove -> SCANNING_REMOVE
            BiometricState.ScanningToggle -> SCANNING_TOGGLE
            else -> throw IllegalStateException("Invalid scanning state")
        })
        _biometricState.postValue(state)
        scanBiometricNoPost(removing)
    }

    private fun scanBiometricNoPost(removing: Boolean) {
        cancelScan()
        scanJob = if (removing) removeBiometric()
        else toggleVerificationFlag()
    }

    private fun removeBiometric() =
        biometricManager.removeBiometric(object : AuthMethodVerificationCallback {
            override fun onVerificationSuccess() {
                savedStateHandle.set(BIOMETRIC_STATE, REMOVED)
                _biometricState.postValue(BiometricState.Removed)
            }

            override fun onVerificationFailure(
                failure: AuthMethodFailure,
                unlinkTriggered: Boolean,
                unlinkWarningTriggered: Boolean,
                attemptsRemaining: Int?
            ) {
                savedStateHandle.set(BIOMETRIC_STATE, FAILED)
                _biometricState.postValue(BiometricState.Failed(failure))
                if (unlinkTriggered) {
                    savedStateHandle.set(UNLINK_STATE, UNLINK_TRIGGERED)
                    savedStateHandle.set(UNLINK_TRIGGERED_COUNT, thresholdAutoUnlink)
                    _unlinkState.postValue(UnlinkState.UnlinkTriggered(thresholdAutoUnlink))
                } else if (unlinkWarningTriggered) {
                    savedStateHandle.set(UNLINK_STATE, UNLINK_WARNING_TRIGGERED)
                    savedStateHandle.set(UNLINK_WARNING_TRIGGERED_COUNT, attemptsRemaining!!)
                    _unlinkState.postValue(UnlinkState.UnlinkWarningTriggered(attemptsRemaining))
                }
            }
        })


    private fun toggleVerificationFlag(): Disposable {
        val newState = if (verificationFlagState.value == VerificationFlag.State.ALWAYS)
            VerificationFlag.State.WHEN_REQUIRED
        else
            VerificationFlag.State.ALWAYS

        return biometricManager.changeVerificationFlag(
            newState,
            object : AuthMethodVerificationCallback {
                override fun onVerificationSuccess() {
                    savedStateHandle.set(VERIFICATION_STATE, when (newState) {
                        VerificationFlag.State.ALWAYS -> VERIFICATION_ALWAYS
                        VerificationFlag.State.WHEN_REQUIRED -> VERIFICATION_WHEN_REQUIRED
                    })
                    _verificationFlagState.postValue(newState)
                    savedStateHandle.set(BIOMETRIC_STATE, TOGGLED)
                    _biometricState.postValue(BiometricState.Toggled)
                }

                override fun onVerificationFailure(
                    failure: AuthMethodFailure,
                    unlinkTriggered: Boolean,
                    unlinkWarningTriggered: Boolean,
                    attemptsRemaining: Int?
                ) {
                    savedStateHandle.set(BIOMETRIC_STATE, FAILED)
                    _biometricState.postValue(BiometricState.Failed(failure))
                    if (unlinkTriggered) {
                        savedStateHandle.set(UNLINK_STATE, UNLINK_TRIGGERED)
                        savedStateHandle.set(UNLINK_TRIGGERED_COUNT, thresholdAutoUnlink)
                        _unlinkState.postValue(UnlinkState.UnlinkTriggered(thresholdAutoUnlink))
                    } else if (unlinkWarningTriggered) {
                        savedStateHandle.set(UNLINK_STATE, UNLINK_WARNING_TRIGGERED)
                        savedStateHandle.set(UNLINK_WARNING_TRIGGERED_COUNT, attemptsRemaining!!)
                        _unlinkState.postValue(UnlinkState.UnlinkWarningTriggered(attemptsRemaining))
                    }
                }

            })
    }

    fun cancelScan() {
        scanJob?.dispose()
        scanJob = null
    }

    sealed class BiometricState {
        object ScanningRemove : BiometricState()
        object ScanningToggle : BiometricState()
        object Removed : BiometricState()
        object Toggled : BiometricState()
        data class Failed(val failure: AuthMethodFailure) : BiometricState()
    }

    sealed class UnlinkState {
        data class UnlinkWarningTriggered(val attemptsRemaining: Int) : UnlinkState()
        data class UnlinkTriggered(val thresholdAutoUnlink: Int) : UnlinkState()
    }
}