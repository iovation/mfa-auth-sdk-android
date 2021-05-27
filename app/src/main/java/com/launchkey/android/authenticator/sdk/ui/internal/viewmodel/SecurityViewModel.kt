package com.launchkey.android.authenticator.sdk.ui.internal.viewmodel

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.BiometricManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.PINCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.AuthMethodNotAllowedException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.AuthMethodNotSetException
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.internal.common.SecurityItem
import com.launchkey.android.authenticator.sdk.ui.internal.common.SecurityItem.Companion.makeSecurityItem
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SecurityViewModel.AuthMethodState.*
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SecurityViewModel(
    private val defaultDispatcher: CoroutineDispatcher,
    private val authenticatorManager: AuthenticatorManager,
    private val authenticatorUIManager: AuthenticatorUIManager,
    private val pinCodeManager: PINCodeManager,
    private val circleCodeManager: CircleCodeManager,
    private val wearablesManager: WearablesManager,
    private val biometricManager: BiometricManager,
    private val locationsManager: LocationsManager,
    private val nowProvider: TimingCounter.NowProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = savedStateHandle.getLiveData<GetAuthMethodsState>(KEY_STATE)
    val state: LiveData<GetAuthMethodsState>
        get() = _state
    
    private var loadAuthMethodsJob: Job? = null
    
    init {
        getAuthMethods()
    }
    
    override fun onCleared() {
        super.onCleared()
        cancelJobs()
    }
    
    fun cancelJobs() {
        loadAuthMethodsJob?.cancel()
        loadAuthMethodsJob = null
    }
    
    fun getActivationDelay(authMethod: AuthMethod) = when (authMethod) {
        AuthMethod.LOCATIONS -> authenticatorManager.config.activationDelayLocationsSeconds()
        AuthMethod.WEARABLES -> authenticatorManager.config.activationDelayWearablesSeconds()
        else -> 0
    }
    
    fun getAuthMethods() {
        if (!authenticatorManager.isDeviceLinked
            && !authenticatorUIManager.config.areSecurityChangesAllowedWhenUnlinked()
        ) {
            _state.postValue(GetAuthMethodsState.Failed)
        } else {
            if (state.value is GetAuthMethodsState.Loading) return
            _state.postValue(GetAuthMethodsState.Loading)
            loadSetAndAvailableAuthMethods()
        }
    }
    
    fun getAuthenticatorTheme() = authenticatorUIManager.config.themeObj()
    
    private fun loadSetAndAvailableAuthMethods() {
        loadAuthMethodsJob = viewModelScope.launch(defaultDispatcher) {
            val authMethodStates = listOf(
                async { buildPinCodeItem() },
                async { buildCircleCodeItem() },
                async { buildWearablesItem() },
                async { buildLocationsItem() },
                async { buildBiometricsItem() }
            )
            
            try {
                val setSecurityItems = mutableListOf<SecurityItem>()
                val availableSecurityItems = mutableListOf<SecurityItem>()
                authMethodStates.awaitAll().forEach { authMethodState ->
                    when (authMethodState) {
                        is AlreadyAdded -> setSecurityItems.add(authMethodState.securityItem)
                        is Available -> availableSecurityItems.add(authMethodState.securityItem)
                        NotAddable -> Unit
                    }
                }
                _state.postValue(
                    GetAuthMethodsState.Success(
                        availableSecurityItems,
                        setSecurityItems
                    )
                )
            } catch (e: Exception) {
                _state.postValue(GetAuthMethodsState.Failed)
            }
        }
    }
    
    private fun buildBiometricsItem(): AuthMethodState = try {
        val verificationFlag = biometricManager.verificationFlag
        val item = makeSecurityItem(AuthMethod.BIOMETRIC, verificationFlag, nowProvider.now)
        AlreadyAdded(item)
    } catch (e: AuthMethodNotSetException) {
        val item = makeSecurityItem(AuthMethod.BIOMETRIC, null, nowProvider.now)
        Available(item)
    } catch (e: Exception) {
        NotAddable
    }
    
    private suspend fun buildLocationsItem(): AuthMethodState = try {
        val verificationFlag = getLocationsVerificationFlag()
        val item = makeSecurityItem(AuthMethod.LOCATIONS, verificationFlag, nowProvider.now)
        AlreadyAdded(item)
    } catch (e: AuthMethodNotSetException) {
        val item = makeSecurityItem(AuthMethod.LOCATIONS, null, nowProvider.now)
        Available(item)
    } catch (e: Exception) {
        NotAddable
    }
    
    private suspend fun buildWearablesItem(): AuthMethodState = try {
        val verificationFlag = getWearablesVerificationFlag()
        val item = makeSecurityItem(AuthMethod.WEARABLES, verificationFlag, nowProvider.now)
        AlreadyAdded(item)
    } catch (e: AuthMethodNotSetException) {
        val item = makeSecurityItem(AuthMethod.WEARABLES, null, nowProvider.now)
        Available(item)
    } catch (e: Exception) {
        NotAddable
    }
    
    private fun buildPinCodeItem(): AuthMethodState = try {
        val verificationFlag = pinCodeManager.verificationFlag
        val item = makeSecurityItem(AuthMethod.PIN_CODE, verificationFlag, nowProvider.now)
        AlreadyAdded(item)
    } catch (e: AuthMethodNotSetException) {
        val item = makeSecurityItem(AuthMethod.PIN_CODE, null, nowProvider.now)
        Available(item)
    } catch (e: Exception) {
        NotAddable
    }
    
    private fun buildCircleCodeItem(): AuthMethodState = try {
        val verificationFlag = circleCodeManager.verificationFlag
        val item = makeSecurityItem(AuthMethod.CIRCLE_CODE, verificationFlag, nowProvider.now)
        AlreadyAdded(item)
    } catch (e: AuthMethodNotSetException) {
        val item = makeSecurityItem(AuthMethod.CIRCLE_CODE, null, nowProvider.now)
        Available(item)
    } catch (e: Exception) {
        NotAddable
    }
    
    private suspend fun getLocationsVerificationFlag() =
        suspendCancellableCoroutine<VerificationFlag> { continuation ->
            locationsManager.getVerificationFlag(object :
                LocationsManager.GetLocationsVerificationFlagCallback {
                override fun onGetSuccess(verificationFlag: VerificationFlag) {
                    continuation.resume(verificationFlag)
                }
                
                override fun onGetFailure(e: java.lang.Exception) {
                    continuation.resumeWithException(e)
                }
            })
        }
    
    private suspend fun getWearablesVerificationFlag() =
        suspendCancellableCoroutine<VerificationFlag> { continuation ->
            if (!wearablesManager.isSupported)
                continuation.resumeWithException(AuthMethodNotAllowedException())
            else {
                wearablesManager.getVerificationFlag(object :
                    WearablesManager.GetWearablesVerificationFlagCallback {
                    override fun onGetSuccess(verificationFlag: VerificationFlag) {
                        continuation.resume(verificationFlag)
                    }
                    
                    override fun onGetFailure(e: java.lang.Exception) {
                        continuation.resumeWithException(e)
                    }
                })
            }
        }
    
    private sealed class AuthMethodState {
        data class AlreadyAdded(val securityItem: SecurityItem) : AuthMethodState()
        data class Available(val securityItem: SecurityItem) : AuthMethodState()
        object NotAddable : AuthMethodState()
    }
    
    sealed class GetAuthMethodsState : Parcelable {
        @Parcelize
        object Loading : GetAuthMethodsState()
        
        // Implies unlinked
        @Parcelize
        object Failed : GetAuthMethodsState()
        
        @Parcelize
        data class Success(
            val availableSecurityItems: List<SecurityItem>,
            val setSecurityItems: List<SecurityItem>
        ) : GetAuthMethodsState()
    }
    
    companion object {
        private const val KEY_STATE = "state"
    }
}