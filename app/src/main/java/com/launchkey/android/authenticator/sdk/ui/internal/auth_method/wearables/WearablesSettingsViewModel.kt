package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import androidx.lifecycle.*
import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import com.launchkey.android.authenticator.sdk.ui.internal.util.disposeWhenCancelled
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WearablesSettingsViewModel(private val wearablesManager: WearablesManager,
                                 private val nowProvider: TimingCounter.NowProvider,
                                 private val defaultDispatcher: CoroutineDispatcher,
                                 savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _getStoredWearablesState: MutableLiveData<GetStoredWearablesState> = MutableLiveData()
    val getStoredWearablesState: LiveData<GetStoredWearablesState> = _getStoredWearablesState

    private val _cancelRemoveState: MutableLiveData<CancelRemoveState> = SingleLiveEvent()
    val cancelRemoveState: LiveData<CancelRemoveState> = _cancelRemoveState

    private val _removeState: MutableLiveData<RemoveState> = SingleLiveEvent()
    val removeState: LiveData<RemoveState> = _removeState

    private val _removeAllState: MutableLiveData<RemoveAllState> = SingleLiveEvent()
    val removeAllState: LiveData<RemoveAllState> = _removeAllState

    init {
        getStoredWearables()
    }

    private suspend fun getStoredWearablesAsync() = suspendCancellableCoroutine<List<WearablesManager.Wearable>> { continuation ->
        wearablesManager.getStoredWearables(object : WearablesManager.GetStoredWearablesCallback {
            override fun onGetSuccess(wearables: MutableList<WearablesManager.Wearable>) {
                continuation.resume(wearables)
            }

            override fun onGetFailure(e: Exception) {
                continuation.resumeWithException(e)
            }
        }).disposeWhenCancelled(continuation)
    }

    fun getStoredWearables() = viewModelScope.launch(defaultDispatcher) {
        try {
            _getStoredWearablesState.postValue(GetStoredWearablesState.Success(getStoredWearablesAsync().map { WearableItem(it, nowProvider.now) }))
        } catch (exception: Exception) {
            _getStoredWearablesState.postValue(GetStoredWearablesState.Failure(exception))
        }
    }

    fun cancelRemoveWearable(wearable: WearablesManager.Wearable) = viewModelScope.launch(defaultDispatcher) {
        wearablesManager.cancelRemoveWearable(wearable, object : WearablesManager.CancelRemoveWearableCallback {
            override fun onCancelRemoveSuccess() {
                _cancelRemoveState.postValue(CancelRemoveState.Success(WearableItem(wearable, nowProvider.now)))
                getStoredWearables()
            }
            override fun onCancelRemoveFailure(e: Exception) {
                _cancelRemoveState.postValue(CancelRemoveState.Failure(e))
            }
        })
    }

    fun removeWearable(wearable: WearablesManager.Wearable) {
        wearablesManager.removeWearable(wearable, object : WearablesManager.RemoveWearableCallback {
            override fun onRemoveSuccess() {
                _removeState.postValue(RemoveState.Success(WearableItem(wearable, nowProvider.now)))
                getStoredWearables()
            }
            override fun onRemoveFailure(e: Exception) {
                _removeState.postValue(RemoveState.Failure(e))
            }
        })
    }

    fun removeAllWearables() = viewModelScope.launch(defaultDispatcher) {
        try {
            getStoredWearablesAsync().forEach {
                try {
                    if (!it.isPendingRemoval) {
                        removeWearable(it)
                    }
                } catch (exception: Exception) {
                    _removeAllState.postValue(RemoveAllState.Failure(exception))
                    cancel()
                    return@launch
                }
            }
            _removeAllState.postValue(RemoveAllState.Success())
        } catch (exception: Exception) {
            _removeAllState.postValue(RemoveAllState.Failure(exception))
        }
    }

    sealed class GetStoredWearablesState {
        data class Success(val wearables: List<WearableItem>) : GetStoredWearablesState()
        data class Failure(val exception: Exception) : GetStoredWearablesState()
    }

    sealed class RemoveState {
        data class Success(val wearable: WearableItem) : RemoveState()
        data class Failure(val exception: Exception) : RemoveState()
    }

    sealed class CancelRemoveState {
        data class Success(val wearable: WearableItem) : CancelRemoveState()
        data class Failure(val exception: Exception) : CancelRemoveState()
    }

    sealed class RemoveAllState {
        class Success : RemoveAllState()
        data class Failure(val exception: Exception) : RemoveAllState()
    }
}