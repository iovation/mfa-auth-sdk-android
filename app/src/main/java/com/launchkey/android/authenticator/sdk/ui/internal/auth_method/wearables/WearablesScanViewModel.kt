package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WearablesScanViewModel(
    private val wearablesManager: WearablesManager,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    init {
        scanForAvailableWearables()
    }

    private val _scanState = MutableLiveData<WearablesScanState>(WearablesScanState.Scanning)

    val scanState: LiveData<WearablesScanState>
        get() = _scanState

    fun scanForAvailableWearables() {
        viewModelScope.launch(defaultDispatcher) {
            _scanState.postValue(WearablesScanState.Scanning)
            try {
                val (wearablesWithNames, wearablesWithoutNames) =
                    getAvailableWearables().partition { wearable -> wearable.name.isNotBlank() }
                _scanState.postValue(
                    WearablesScanState.FoundAvailableWearables(
                        wearablesWithNames,
                        wearablesWithoutNames
                    )
                )
            } catch (e: java.lang.Exception) {
                _scanState.postValue(WearablesScanState.FailedToGetAvailableWearables(e))
            }
        }
    }

    private suspend fun getAvailableWearables() =
        suspendCancellableCoroutine<List<WearablesManager.Wearable>> { continuation ->
            wearablesManager.getAvailableWearables(object :
                WearablesManager.GetAvailableWearablesCallback {
                override fun onGetSuccess(wearables: MutableList<WearablesManager.Wearable>) {
                    continuation.resume(wearables)
                }

                override fun onGetFailure(exception: java.lang.Exception) {
                    continuation.resumeWithException(exception)
                }
            })
        }

    sealed class WearablesScanState {
        object Scanning : WearablesScanState()

        data class FoundAvailableWearables(
            val wearablesWithNames: List<WearablesManager.Wearable>,
            val wearablesWithoutNames: List<WearablesManager.Wearable>
        ) : WearablesScanState()

        data class FailedToGetAvailableWearables(val failure: Exception) : WearablesScanState()
    }
}