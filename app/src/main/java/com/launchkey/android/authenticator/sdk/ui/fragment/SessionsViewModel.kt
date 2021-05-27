package com.launchkey.android.authenticator.sdk.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.core.authentication_management.Session
import com.launchkey.android.authenticator.sdk.core.authentication_management.event_callback.EndSessionEventCallback
import com.launchkey.android.authenticator.sdk.core.authentication_management.event_callback.GetSessionsEventCallback
import com.launchkey.android.authenticator.sdk.ui.internal.util.disposeWhenCancelled
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SessionsViewModel(
    private val authenticatorManager: AuthenticatorManager,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
    savedStateHandle: SavedStateHandle?
) : ViewModel() {
    private val _sessionState = MutableLiveData<SessionState>()
    val sessionState: LiveData<SessionState>
        get() = _sessionState

    init {
        refreshSessions()
    }

    fun refreshSessions() = viewModelScope.launch(defaultDispatcher) {
        _sessionState.postValue(SessionState.Loading)
        try {
            val sessions: List<Session> = getAllSessions()
            _sessionState.postValue(SessionState.GetSessionsSuccess(sessions))
        } catch (e: Exception) {
            _sessionState.postValue(SessionState.Failed(e))
        }
    }

    private suspend fun getAllSessions() =
        suspendCancellableCoroutine<List<Session>> { continuation ->
            authenticatorManager.getSessions(object : GetSessionsEventCallback() {
                override fun onSuccess(sessions: MutableList<Session>) {
                    continuation.resume(sessions)
                }

                override fun onFailure(e: Exception) {
                    continuation.resumeWithException(e)
                }
            }).disposeWhenCancelled(continuation)
        }

    fun endSession(session: Session) = viewModelScope.launch(defaultDispatcher) {
        _sessionState.postValue(SessionState.Loading)
        try {
            val endedSession = endSingleSession(session)
            _sessionState.postValue(SessionState.EndSessionSuccess(endedSession))
        } catch (e: Exception) {
            _sessionState.postValue(SessionState.Failed(e))
        }
    }

    private suspend fun endSingleSession(session: Session) =
        suspendCancellableCoroutine<Session> { continuation ->
            authenticatorManager.endSession(session, object : EndSessionEventCallback() {
                override fun onSuccess(session: Session) {
                    continuation.resume(session)
                }

                override fun onFailure(e: Exception) {
                    continuation.resumeWithException(e)
                }
            }).disposeWhenCancelled(continuation)
        }

    sealed class SessionState {
        object Loading : SessionState()
        data class Failed(val failure: Exception) : SessionState()
        data class GetSessionsSuccess(val sessions: List<Session>) : SessionState()
        data class EndSessionSuccess(val session: Session) : SessionState()
    }
}