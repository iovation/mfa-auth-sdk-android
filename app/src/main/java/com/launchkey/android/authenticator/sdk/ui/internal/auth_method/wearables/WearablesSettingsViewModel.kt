package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import androidx.lifecycle.*
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import com.launchkey.android.authenticator.sdk.ui.internal.util.disposeWhenCancelled
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WearablesSettingsViewModel(
    private val wearablesManager: WearablesManager,
    private val nowProvider: TimingCounter.NowProvider,
    private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _newWearableState = SingleLiveEvent<NewWearableState>()
    val newWearableState: LiveData<NewWearableState>
        get() = _newWearableState

    private val _getStoredWearablesState = MutableLiveData<GetStoredWearablesState>()
    val getStoredWearablesState: LiveData<GetStoredWearablesState>
        get() = _getStoredWearablesState

    private val _removeSingleWearableState = SingleLiveEvent<RemoveSingleWearableState>()
    val removeSingleWearableState: LiveData<RemoveSingleWearableState>
        get() = _removeSingleWearableState

    private val _removeAllWearablesState = SingleLiveEvent<RemoveAllWearablesState>()
    val removeAllWearablesState: LiveData<RemoveAllWearablesState>
        get() = _removeAllWearablesState

    private lateinit var wearableToRemove: WearablesManager.Wearable
    private var getStoredWearablesJob: Job? = null

    init {
        fetchWearables()
    }

    private suspend fun getAllWearables() =
        suspendCancellableCoroutine<List<WearablesManager.Wearable>> { continuation ->
            wearablesManager.getStoredWearables(object :
                WearablesManager.GetStoredWearablesCallback {
                override fun onGetSuccess(wearables: MutableList<WearablesManager.Wearable>) {
                    continuation.resume(wearables)
                }

                override fun onGetFailure(e: Exception) {
                    continuation.resumeWithException(e)
                }
            }).disposeWhenCancelled(continuation)
        }

    fun fetchWearables() {
        getStoredWearablesJob?.let {
            if (it.isActive) {
                return
            }
        }

        getStoredWearablesJob = viewModelScope.launch(defaultDispatcher) {
            _getStoredWearablesState.postValue(GetStoredWearablesState.GettingStoredWearables)
            try {
                val wearables = getAllWearables().map {
                    WearableItem(it, nowProvider.now)
                }
                _getStoredWearablesState.postValue(
                    GetStoredWearablesState.GotStoredWearables(wearables)
                )
            } catch (e: Exception) {
                _getStoredWearablesState.postValue(GetStoredWearablesState.Failed(e))
            }
        }
    }

    fun requestNewWearable() {
        _newWearableState.postValue(NewWearableState.AddingNewWearable)
    }

    fun addedNewWearable() {
        if (_newWearableState.value!! !is NewWearableState.AddedNewWearable) {
            _newWearableState.postValue(NewWearableState.AddedNewWearable)
        }
    }

    private fun removeWearable(wearable: WearablesManager.Wearable) {
        viewModelScope.launch {
            try {
                if (wearable.isPendingRemoval) {
                    cancelRemoveWearable(wearable)
                    _removeSingleWearableState.postValue(
                        RemoveSingleWearableState.CancelledWearableRemoval(wearable)
                    )
                } else {
                    removeSingleWearable(wearable)
                    _removeSingleWearableState.postValue(
                        RemoveSingleWearableState.PendingWearableRemoval(wearable)
                    )
                }
                fetchWearables()
            } catch (e: Exception) {
                _removeSingleWearableState.postValue(RemoveSingleWearableState.Failed(e))
            }
        }
    }

    fun removeSelectedWearable() {
        removeWearable(wearableToRemove)
    }

    fun setWearableToRemove(wearable: WearablesManager.Wearable) {
        wearableToRemove = wearable
        _removeSingleWearableState.postValue(
            RemoveSingleWearableState.RemovingWearable(wearable)
        )
    }

    private suspend fun removeSingleWearable(wearable: WearablesManager.Wearable) =
        suspendCancellableCoroutine<Exception?> { continuation ->
            wearablesManager.removeWearable(wearable,
                object : WearablesManager.RemoveWearableCallback {
                    override fun onRemoveSuccess() {
                        continuation.resume(null)
                    }

                    override fun onRemoveFailure(e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }).disposeWhenCancelled(continuation)
        }

    private suspend fun cancelRemoveWearable(wearable: WearablesManager.Wearable) =
        suspendCancellableCoroutine<Exception?> { continuation ->
            wearablesManager.cancelRemoveWearable(wearable,
                object : WearablesManager.CancelRemoveWearableCallback {
                    override fun onCancelRemoveSuccess() {
                        continuation.resume(null)
                    }

                    override fun onCancelRemoveFailure(e: Exception) {
                        continuation.resumeWithException(e)
                    }
                })
        }

    fun requestRemoveAllWearables() {
        _removeAllWearablesState.postValue(RemoveAllWearablesState.RemovingAllWearables)
    }

    fun removeAllWearables() {
        viewModelScope.launch(defaultDispatcher) {
            val wearables: List<WearablesManager.Wearable>
            try {
                wearables = getAllWearables()
            } catch (exception: Exception) {
                _removeAllWearablesState.postValue(RemoveAllWearablesState.Failed(exception))
                return@launch
            }

            launch {
                wearables.forEach { wearable ->
                    try {
                        if (!wearable.isPendingRemoval) {
                            removeSingleWearable(wearable)
                        }
                    } catch (e: Exception) {
                        _removeAllWearablesState.postValue(RemoveAllWearablesState.Failed(e))
                        cancel()
                    }
                }
            }.invokeOnCompletion {
                if (it == null) {
                    _removeAllWearablesState.postValue(RemoveAllWearablesState.PendingRemovalForAllWearables)
                }
            }
        }
    }

    sealed class GetStoredWearablesState {
        object GettingStoredWearables : GetStoredWearablesState()
        data class GotStoredWearables(val wearables: List<WearableItem>) : GetStoredWearablesState()
        data class Failed(val failure: Exception) : GetStoredWearablesState()
    }

    sealed class RemoveAllWearablesState {
        object RemovingAllWearables : RemoveAllWearablesState()
        object PendingRemovalForAllWearables : RemoveAllWearablesState()
        data class Failed(val failure: Exception) : RemoveAllWearablesState()
    }

    sealed class RemoveSingleWearableState {
        data class RemovingWearable(val wearable: WearablesManager.Wearable) :
            RemoveSingleWearableState()

        data class PendingWearableRemoval(val wearable: WearablesManager.Wearable) :
            RemoveSingleWearableState()

        data class CancelledWearableRemoval(val wearable: WearablesManager.Wearable) :
            RemoveSingleWearableState()

        data class Failed(val exception: Exception) : RemoveSingleWearableState()
    }

    sealed class NewWearableState {
        object AddingNewWearable : NewWearableState()
        object AddedNewWearable : NewWearableState()
    }
}