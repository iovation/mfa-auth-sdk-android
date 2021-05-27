package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequestManager
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequestResponse
import com.launchkey.android.authenticator.sdk.core.auth_request_management.DenialReason
import com.launchkey.android.authenticator.sdk.core.auth_request_management.event_callback.AuthRequestPushReceivedEvent
import com.launchkey.android.authenticator.sdk.core.auth_request_management.event_callback.AuthRequestResponseEvent
import com.launchkey.android.authenticator.sdk.core.auth_request_management.event_callback.GetAuthRequestEventCallback
import com.launchkey.android.authenticator.sdk.core.exception.AuthRequestCancelledException
import com.launchkey.android.authenticator.sdk.core.util.CompositeDisposable
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestFragmentViewModel.AuthRequestState.*
import com.launchkey.android.authenticator.sdk.ui.internal.util.FailureDetails
import com.launchkey.android.authenticator.sdk.ui.internal.util.FailureUtils
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class AuthRequestFragmentViewModel(
    private val authRequestManager: AuthRequestManager,
    private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle?) : ViewModel() {
    private val _authRequestState = SingleLiveEvent<AuthRequestState>()
    private val _fetchState = SingleLiveEvent<FetchState>()
    private val compositeDisposable = CompositeDisposable()
    
    val authRequestState: LiveData<AuthRequestState>
        get() = _authRequestState
    val fetchState: LiveData<FetchState>
        get() = _fetchState
    var currentAuthRequest: AuthRequest? = null
        private set
    
    private val authRequestPushReceivedEvent: AuthRequestPushReceivedEvent = object : AuthRequestPushReceivedEvent() {
        override fun onSuccess(result: Void?) = _fetchState.postValue(FetchState.PushReceived)
        override fun onFailure(e: Exception) = _fetchState.postValue(FetchState.Failed(e))
    }
    
    private val getAuthRequestEventCallback: GetAuthRequestEventCallback = object : GetAuthRequestEventCallback() {
        override fun onSuccess(latestAuthRequest: AuthRequest?) {
            when {
                latestAuthRequest == null -> {
                    currentAuthRequest = latestAuthRequest
                    _fetchState.postValue(FetchState.FetchedEmptyAuthRequest)
                }
                currentAuthRequest == null -> {
                    currentAuthRequest = latestAuthRequest
                    _fetchState.postValue(FetchState.FetchedAuthRequest(latestAuthRequest))
                }
                latestAuthRequest.id != currentAuthRequest!!.id -> {
                    currentAuthRequest = latestAuthRequest
                    _fetchState.postValue(FetchState.FetchedNewerAuthRequest(latestAuthRequest))
                }
            }
        }
        
        override fun onFailure(e: Exception) {
            _fetchState.postValue(FetchState.Failed(e))
        }
    }
    
    private val authRequestResponseEvent: AuthRequestResponseEvent = object : AuthRequestResponseEvent() {
        override fun onSuccess(result: AuthRequestResponse) {
            when (result.result) {
                AuthRequestResponse.Result.APPROVED -> setAuthRequestStateToResponded(true)
                AuthRequestResponse.Result.DENIED -> setAuthRequestStateToResponded(false)
                AuthRequestResponse.Result.FAILED -> _authRequestState.postValue(AuthRequestFailed(FailureUtils.getMessageForFailure(result.failure!!)))
            }
        }
        
        override fun onFailure(e: Exception) {
            when (e) {
                is AuthRequestCancelledException -> {
                    _authRequestState.postValue(
                        AuthRequestFailed(FailureDetails(
                            R.string.ioa_ar_error_result_canc_title,
                            R.string.ioa_ar_error_result_canc_message
                        ))
                    )
                }
                else -> {
                    currentAuthRequest = null
                    _authRequestState.postValue(Failed(e))
                }
            }
        }
    }
    
    public override fun onCleared() {
        compositeDisposable.clear()
        authRequestManager.unregisterForEvents(authRequestPushReceivedEvent, getAuthRequestEventCallback, authRequestResponseEvent)
    }
    
    fun setAuthRequestStateToResponded(authorized: Boolean?) {
        _authRequestState.postValue(Responded(authorized))
        currentAuthRequest = null
    }
    
    fun setAuthRequestExpired() {
        _authRequestState.postValue(
            AuthRequestFailed(FailureDetails(
                R.string.ioa_ar_error_result_expi_title,
                R.string.ioa_ar_error_result_expi_message))
        )
    }
    
    fun checkForAuthRequest() = viewModelScope.launch(defaultDispatcher) {
        _fetchState.postValue(FetchState.Fetching)
        authRequestManager.checkForAuthRequest(null)
    }
    
    fun acceptAuthRequest() = viewModelScope.launch(defaultDispatcher) {
        val authRequestSent = authRequestManager.acceptAndSendIfFailed(currentAuthRequest!!)
        _authRequestState.postValue(
            if (authRequestSent) Sending
            else Accepted
        )
    }
    
    fun denyAuthRequest() = viewModelScope.launch(defaultDispatcher) {
        val denialReasons = currentAuthRequest!!.denialReasons
        if (denialReasons.isEmpty()) {
            authRequestManager.denyAndSend(currentAuthRequest!!, null)
        } else {
            _authRequestState.postValue(Denying)
        }
    }
    
    fun denyAuthRequestWithDenialReason(authRequest: AuthRequest, denialReason: DenialReason) = viewModelScope.launch(defaultDispatcher) {
        _authRequestState.postValue(Sending)
        authRequestManager.denyAndSend(authRequest, denialReason)
    }
    
    fun sendAuthRequest() = viewModelScope.launch(defaultDispatcher) {
        _authRequestState.postValue(Sending)
        authRequestManager.send(currentAuthRequest!!)
    }
    
    fun startVerifyingAuthMethods() = viewModelScope.launch(defaultDispatcher) {
        if (FactorsTracker(authRequestManager.getAuthMethodsToVerify(currentAuthRequest!!)).currentId == null) {
            sendAuthRequest()
        } else {
            _authRequestState.postValue(Verifying)
        }
    }
    
    fun getFailureDetails(): FailureDetails = when(_authRequestState.value) {
        is AuthRequestFailed -> (_authRequestState.value as AuthRequestFailed).failureDetails
        else -> throw IllegalStateException("Auth Request has not yet Failed.")
    }
    
    init {
        authRequestManager.registerForEvents(authRequestPushReceivedEvent, getAuthRequestEventCallback, authRequestResponseEvent)
        checkForAuthRequest()
    }
    
    sealed class AuthRequestState {
        object Accepted : AuthRequestState()
        object Denying : AuthRequestState()
        object Verifying : AuthRequestState()
        class Responded(val isAuthorized: Boolean?) : AuthRequestState()
        object Sending : AuthRequestState()
        data class AuthRequestFailed(val failureDetails: FailureDetails) : AuthRequestState()
        data class Failed(val failure: Exception) : AuthRequestState()
    }
    
    sealed class FetchState {
        object PushReceived : FetchState()
        object Fetching : FetchState()
        object FetchedEmptyAuthRequest : FetchState()
        data class FetchedAuthRequest(val authRequest: AuthRequest?) : FetchState()
        data class FetchedNewerAuthRequest(val authRequest: AuthRequest) : FetchState()
        data class Failed(val failure: Exception) : FetchState()
    }
}