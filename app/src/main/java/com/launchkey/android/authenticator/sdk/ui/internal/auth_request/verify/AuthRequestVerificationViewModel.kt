package com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.BiometricManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager.CircleCodeTick
import com.launchkey.android.authenticator.sdk.core.auth_method_management.GeofencesManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.PINCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequestManager
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.core.util.CompositeDisposable
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.FactorsTracker
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify.AuthRequestVerificationViewModel.VerificationState.*
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class AuthRequestVerificationViewModel(
    private val authRequestManager: AuthRequestManager,
    private val authenticatorManager: AuthenticatorManager,
    private val pinCodeManager: PINCodeManager,
    private val circleCodeManager: CircleCodeManager,
    private val wearablesManager: WearablesManager,
    private val biometricManager: BiometricManager,
    private val locationsManager: LocationsManager,
    private val geofencesManager: GeofencesManager,
    private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle?) : ViewModel() {
    private lateinit var factorsTracker: FactorsTracker
    private val compositeDisposable = CompositeDisposable()
    private var authRequest: AuthRequest? = null
    private val _verificationState = SingleLiveEvent<VerificationState>()
    private val _currentStep = MutableLiveData(0)
    
    val verificationState: LiveData<VerificationState>
        get() = _verificationState
    
    val currentStep: LiveData<Int>
        get() = _currentStep
    
    
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
    
    fun stopVerifyingPassiveAuthMethods() {
        compositeDisposable.clear()
    }
    
    fun amountToVerify() = factorsTracker.countForUi
    
    fun verifyPinCode(pinCode: String, verificationCallbackFromView: AuthMethodAuthRequestVerificationCallback?) = viewModelScope.launch(defaultDispatcher) {
        pinCodeManager.verifyPINCodeForAuthRequest(pinCode, authRequest!!, object : AuthMethodAuthRequestVerificationCallback {
            override fun onVerificationSuccess(authRequestWasSent: Boolean) {
                if (!authRequestWasSent) {
                    setAuthMethodVerified(AuthMethod.PIN_CODE)
                } else {
                    _verificationState.postValue(AutoSent)
                }
                verificationCallbackFromView?.onVerificationSuccess(authRequestWasSent)
            }
    
            override fun onVerificationFailure(authRequestWasSent: Boolean, failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                when {
                    unlinkTriggered -> _verificationState.postValue(UnlinkTriggered(AuthMethod.PIN_CODE, authenticatorManager.config.thresholdAutoUnlink()))
                    unlinkWarningTriggered -> _verificationState.postValue(UnlinkWarningTriggered(AuthMethod.PIN_CODE, attemptsRemaining!!))
                    authRequestWasSent -> _verificationState.postValue(AutoFailed(AuthMethod.PIN_CODE))
                }
                verificationCallbackFromView?.onVerificationFailure(authRequestWasSent, failure, unlinkTriggered, unlinkWarningTriggered, attemptsRemaining)
            }
        })
    }
    
    fun verifyCircleCode(circleCode: List<CircleCodeTick>, verificationCallbackFromView: AuthMethodAuthRequestVerificationCallback?) = viewModelScope.launch(defaultDispatcher) {
        circleCodeManager.verifyCircleCodeForAuthRequest(circleCode, authRequest!!, object : AuthMethodAuthRequestVerificationCallback {
            override fun onVerificationSuccess(authRequestWasSent: Boolean) {
                if (!authRequestWasSent) {
                    setAuthMethodVerified(AuthMethod.CIRCLE_CODE)
                } else {
                    _verificationState.postValue(AutoSent)
                }
                verificationCallbackFromView?.onVerificationSuccess(authRequestWasSent)
            }
    
            override fun onVerificationFailure(authRequestWasSent: Boolean, failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                when {
                    unlinkTriggered -> _verificationState.postValue(UnlinkTriggered(AuthMethod.CIRCLE_CODE, authenticatorManager.config.thresholdAutoUnlink()))
                    unlinkWarningTriggered -> _verificationState.postValue(UnlinkWarningTriggered(AuthMethod.CIRCLE_CODE, attemptsRemaining!!))
                    authRequestWasSent -> _verificationState.postValue(AutoFailed(AuthMethod.CIRCLE_CODE))
                }
                verificationCallbackFromView?.onVerificationFailure(authRequestWasSent, failure, unlinkTriggered, unlinkWarningTriggered, attemptsRemaining)
            }
        })
    }
    
    fun verifyBiometric(verificationCallbackFromView: AuthMethodAuthRequestVerificationCallback?) = viewModelScope.launch(defaultDispatcher) {
        compositeDisposable.add(
            biometricManager.verifyBiometricForAuthRequest(authRequest!!, object : AuthMethodAuthRequestVerificationCallback {
                override fun onVerificationSuccess(authRequestWasSent: Boolean) {
                    if (!authRequestWasSent) {
                        setAuthMethodVerified(AuthMethod.BIOMETRIC)
                    } else {
                        _verificationState.postValue(AutoSent)
                    }
                    verificationCallbackFromView?.onVerificationSuccess(authRequestWasSent)
                }
        
                override fun onVerificationFailure(authRequestWasSent: Boolean, failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                    when {
                        unlinkTriggered -> _verificationState.postValue(UnlinkTriggered(AuthMethod.BIOMETRIC, authenticatorManager.config.thresholdAutoUnlink()))
                        unlinkWarningTriggered -> _verificationState.postValue(UnlinkWarningTriggered(AuthMethod.BIOMETRIC, attemptsRemaining!!))
                        authRequestWasSent -> _verificationState.postValue(AutoFailed(AuthMethod.BIOMETRIC))
                    }
                    verificationCallbackFromView?.onVerificationFailure(authRequestWasSent, failure, unlinkTriggered, unlinkWarningTriggered, attemptsRemaining)
                }
            })
        )
    }
    
    fun verifyLocations(verificationCallbackFromView: AuthMethodAuthRequestVerificationCallback?) = viewModelScope.launch(defaultDispatcher) {
        compositeDisposable.add(
            locationsManager.verifyLocationsForAuthRequest(authRequest!!, object : AuthMethodAuthRequestVerificationCallback {
                override fun onVerificationSuccess(authRequestWasSent: Boolean) {
                    if (!authRequestWasSent) {
                        setAuthMethodVerified(AuthMethod.LOCATIONS)
                    } else {
                        _verificationState.postValue(AutoSent)
                    }
                    verificationCallbackFromView?.onVerificationSuccess(authRequestWasSent)
                }
        
                override fun onVerificationFailure(authRequestWasSent: Boolean, failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                    when {
                        unlinkTriggered -> _verificationState.postValue(UnlinkTriggered(AuthMethod.LOCATIONS, authenticatorManager.config.thresholdAutoUnlink()))
                        unlinkWarningTriggered -> _verificationState.postValue(UnlinkWarningTriggered(AuthMethod.LOCATIONS, attemptsRemaining!!))
                        authRequestWasSent -> _verificationState.postValue(AutoFailed(AuthMethod.LOCATIONS))
                    }
                    verificationCallbackFromView?.onVerificationFailure(authRequestWasSent, failure, unlinkTriggered, unlinkWarningTriggered, attemptsRemaining)
                }
            })
        )
    }
    
    fun verifyGeofences(verificationCallbackFromView: AuthMethodAuthRequestVerificationCallback?) = viewModelScope.launch(defaultDispatcher) {
        compositeDisposable.add(
            geofencesManager.verifyGeofencesForAuthRequest(authRequest!!, object : AuthMethodAuthRequestVerificationCallback {
                override fun onVerificationSuccess(authRequestWasSent: Boolean) {
                    if (!authRequestWasSent) {
                        setAuthMethodVerified(AuthMethod.GEOFENCING)
                    } else {
                        _verificationState.postValue(AutoSent)
                    }
                    verificationCallbackFromView?.onVerificationSuccess(authRequestWasSent)
                }
        
                override fun onVerificationFailure(authRequestWasSent: Boolean, failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                    if (unlinkTriggered) {
                        _verificationState.postValue(UnlinkTriggered(AuthMethod.GEOFENCING, authenticatorManager.config.thresholdAutoUnlink()))
                    } else if (unlinkWarningTriggered) {
                        _verificationState.postValue(UnlinkWarningTriggered(AuthMethod.GEOFENCING, attemptsRemaining!!))
                    } else if (authRequestWasSent) {
                        _verificationState.postValue(AutoFailed(AuthMethod.GEOFENCING))
                    } else if (!authRequestWasSent) { // outside the conditional geofence
                        setAuthMethodVerified(AuthMethod.GEOFENCING)
                    }
                    verificationCallbackFromView?.onVerificationFailure(authRequestWasSent, failure, unlinkTriggered, unlinkWarningTriggered, attemptsRemaining)
                }
            })
        )
    }
    
    fun verifyWearables(verificationCallbackFromView: AuthMethodAuthRequestVerificationCallback?) = viewModelScope.launch(defaultDispatcher) {
        compositeDisposable.add(
            wearablesManager.verifyWearablesForAuthRequest(authRequest!!, object : AuthMethodAuthRequestVerificationCallback {
                override fun onVerificationSuccess(authRequestWasSent: Boolean) {
                    if (!authRequestWasSent) {
                        setAuthMethodVerified(AuthMethod.WEARABLES)
                    } else {
                        _verificationState.postValue(AutoSent)
                    }
                    verificationCallbackFromView?.onVerificationSuccess(authRequestWasSent)
                }
        
                override fun onVerificationFailure(authRequestWasSent: Boolean, failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                    when {
                        unlinkTriggered -> _verificationState.postValue(UnlinkTriggered(AuthMethod.WEARABLES, authenticatorManager.config.thresholdAutoUnlink()))
                        unlinkWarningTriggered -> _verificationState.postValue(UnlinkWarningTriggered(AuthMethod.WEARABLES, attemptsRemaining!!))
                        authRequestWasSent -> _verificationState.postValue(AutoFailed(AuthMethod.WEARABLES))
                    }
                    verificationCallbackFromView?.onVerificationFailure(authRequestWasSent, failure, unlinkTriggered, unlinkWarningTriggered, attemptsRemaining)
                }
            })
        )
    }
    
    private fun setAuthMethodVerified(authMethodVerified: AuthMethod) {
        factorsTracker.setVerified(authMethodVerified)
        var nextAuthMethod = factorsTracker.currentId
        if (nextAuthMethod == null) { // could be null from conditional geo-fence so check again
            val authMethodsToVerify = authRequestManager.getAuthMethodsToVerify(authRequest!!)
            factorsTracker = FactorsTracker(authMethodsToVerify)
            nextAuthMethod = factorsTracker.currentId
            if (nextAuthMethod == null) {
                _verificationState.postValue(VerifiedAllAuthMethods)
                return
            }
        }
        setCurrentAuthMethod(nextAuthMethod)
    }
    
    private fun setCurrentAuthMethod(authMethod: AuthMethod) {
        _verificationState.postValue(VerifyingAuthMethod(authMethod))
        _currentStep.postValue(factorsTracker.currentForUi)
    }
    
    fun setAuthRequestToBeVerified(authRequestToBeVerified: AuthRequest) {
        if (authRequest == authRequestToBeVerified) {
            return
        }
        authRequest = authRequestToBeVerified
        factorsTracker = FactorsTracker(authRequestManager.getAuthMethodsToVerify(authRequest!!))
        setCurrentAuthMethod(factorsTracker.currentId!!)
    }
    
    sealed class VerificationState {
        object AutoSent : VerificationState()
        data class AutoFailed(val failedAuthMethod: AuthMethod) : VerificationState()
        data class VerifyingAuthMethod(val authMethod: AuthMethod) : VerificationState()
        data class UnlinkTriggered(val failedAuthMethod: AuthMethod, val unlinkThreshold: Int) : VerificationState()
        data class UnlinkWarningTriggered(val failedAuthMethod: AuthMethod, val attemptsRemaining: Int) : VerificationState()
        object VerifiedAllAuthMethods : VerificationState()
    }
}