package com.launchkey.android.authenticator.sdk.ui.internal.auth_method

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.util.Disposable
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimerState
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import com.launchkey.android.authenticator.sdk.ui.internal.util.disposeWhenCancelled
import com.launchkey.android.authenticator.sdk.ui.internal.util.flowTimer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class VerificationFlagViewModel(
    private val asyncVerificationFlagManager: AsyncVerificationFlagManager,
    private val nowProvider: TimingCounter.NowProvider,
    private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        const val LOCATIONS = "locations"
        const val WEARABLES = "wearables"
    }

    private val _verificationFlag =
        MutableLiveData<VerificationFlagState>()
    val verificationFlag: LiveData<VerificationFlagState>
        get() = _verificationFlag
    
    private var timerJob: Job? = null
    
    init {
        viewModelScope.launch(defaultDispatcher) {
            _verificationFlag.postValue(VerificationFlagState.FetchingVerificationFlag)
            try {
                val currentVerificationFlag = getVerificationFlag()
                if (currentVerificationFlag.isPendingToggle) {
                    startPendingToggleTimer(currentVerificationFlag)
                } else {
                    _verificationFlag.postValue(
                            VerificationFlagState.GotVerificationFlag(currentVerificationFlag)
                    )
                }
            } catch (e: java.lang.Exception) {
                _verificationFlag.postValue(
                        VerificationFlagState.Failed(e)
                )
            }
        }
    }
    
    private suspend fun getVerificationFlag() =
        suspendCancellableCoroutine<VerificationFlag> { continuation ->
            asyncVerificationFlagManager.getVerificationFlag {
                when (it) {
                    is AsyncVerificationFlagManager.VerificationFlagResult.Success -> {
                        continuation.resume(it.verificationFlag)
                    }
                    is AsyncVerificationFlagManager.VerificationFlagResult.Failure -> {
                        continuation.resumeWithException(it.exception)
                    }
                }
            }.disposeWhenCancelled(continuation)
        }
    
    fun toggleVerificationFlag(newState: VerificationFlag.State) {
        timerJob?.cancel()
        viewModelScope.launch(defaultDispatcher) {
            val newVerificationFlag = changeVerificationFlag(newState)
            if (newVerificationFlag.isPendingToggle) {
                startPendingToggleTimer(newVerificationFlag)
            } else {
                _verificationFlag.postValue(
                        VerificationFlagState.GotVerificationFlag(newVerificationFlag)
                )
            }
        }
    }
    
    private suspend fun changeVerificationFlag(newState: VerificationFlag.State) =
        suspendCancellableCoroutine<VerificationFlag> { continuation ->
            asyncVerificationFlagManager.changeVerificationFlag(newState) {
                when (it) {
                    is AsyncVerificationFlagManager.VerificationFlagResult.Success -> {
                        continuation.resume(it.verificationFlag)
                    }
                    is AsyncVerificationFlagManager.VerificationFlagResult.Failure -> {
                        continuation.resumeWithException(it.exception)
                    }
                }
            }.disposeWhenCancelled(continuation)
        }
    
    private fun startPendingToggleTimer(verificationFlag: VerificationFlag) {
        timerJob = flowTimer(
            nowProvider,
            nowProvider.now + verificationFlag.millisUntilToggled!!,
            100L
        ).conflate()
            .onEach { state ->
                when (state) {
                    TimerState.Finished -> _verificationFlag.postValue(
                            VerificationFlagState.GotVerificationFlag(
                                    verificationFlag
                            )
                    )
                    is TimerState.Updated -> _verificationFlag.postValue(
                            VerificationFlagState.Pending(
                                    state.remainingMillis,
                                    verificationFlag
                            )
                    )
                }
            }.launchIn(viewModelScope)
    }
    
    sealed class VerificationFlagState {
        object FetchingVerificationFlag : VerificationFlagState()
        data class GotVerificationFlag(val verificationFlag: VerificationFlag) :
            VerificationFlagState()
        
        data class Pending(
            val millisUntilToggled: Long,
            val verificationFlag: VerificationFlag
        ) : VerificationFlagState()
        
        data class Failed(val failure: Exception) : VerificationFlagState()
    }

    interface AsyncVerificationFlagManager {
        fun getVerificationFlag(callback: (VerificationFlagResult) -> Unit): Disposable
        fun changeVerificationFlag(state: VerificationFlag.State, callback: (VerificationFlagResult) -> Unit): Disposable
        sealed class VerificationFlagResult {
            data class Success(val verificationFlag: VerificationFlag) : VerificationFlagResult()
            data class Failure(val exception: Exception) : VerificationFlagResult()
        }
    }
}