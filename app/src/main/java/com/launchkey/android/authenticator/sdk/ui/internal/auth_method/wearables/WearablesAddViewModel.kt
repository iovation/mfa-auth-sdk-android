package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import java.util.concurrent.ExecutorService

class WearablesAddViewModel(
        private val wearablesManager: WearablesManager,
        private val executor: ExecutorService,
        savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val HANDLE_KEY_ADD_WEARABLE_STATE = "add_wearables_state"
        private const val HANDLE_KEY_AVAILABLE_WEARABLE_STATE = "available_wearables_state"
    }

    private val _availableWearablesState: MutableLiveData<AvailableWearablesState> =
            if (savedStateHandle.contains(HANDLE_KEY_AVAILABLE_WEARABLE_STATE)) MutableLiveData(savedStateHandle.get(HANDLE_KEY_AVAILABLE_WEARABLE_STATE)!!) else MutableLiveData()
    val availableWearablesState: LiveData<AvailableWearablesState>
        get() = _availableWearablesState

    private val _addWearableState: MutableLiveData<AddWearableState> =
            if (savedStateHandle.contains(HANDLE_KEY_ADD_WEARABLE_STATE)) MutableLiveData(savedStateHandle.get(HANDLE_KEY_ADD_WEARABLE_STATE)!!) else MutableLiveData()

    val addWearableState: LiveData<AddWearableState>
        get() = _addWearableState

    init {
        getAvailableWearables()
    }

    fun addWearable(wearable: WearablesManager.Wearable) =
            executor.run {
                try {
                    if (wearable.name.trim().length < Constants.MINIMUM_INPUT_LENGTH) throw WearableNameTooShortException

                    wearablesManager.addWearable(wearable, object : WearablesManager.AddWearableCallback {
                        override fun onAddSuccess() {
                            val state = AddWearableState.AddedNewWearable(wearable)
//                            savedStateHandle.set(HANDLE_KEY_ADD_WEARABLE_STATE, state)
                            _addWearableState.postValue(state)
                        }

                        override fun onAddFailure(e: Exception) {
                            val state = AddWearableState.FailedToAddWearable(e)
//                            savedStateHandle.set(HANDLE_KEY_ADD_WEARABLE_STATE, state)
                            _addWearableState.postValue(state)
                        }

                    })
                } catch (e: Exception) {
                    val state = AddWearableState.FailedToAddWearable(e)
//                    savedStateHandle.set(HANDLE_KEY_ADD_WEARABLE_STATE, state)
                    _addWearableState.postValue(state)

                }
            }

    fun getAvailableWearables() =
            executor.run {
                wearablesManager.getAvailableWearables(object : WearablesManager.GetAvailableWearablesCallback {
                    override fun onGetSuccess(wearables: List<WearablesManager.Wearable>) {
                        val state = AvailableWearablesState.AvailableWearablesSuccess(wearables)
//                        savedStateHandle.set(HANDLE_KEY_AVAILABLE_WEARABLE_STATE, state)
                        _availableWearablesState.postValue(state)
                    }

                    override fun onGetFailure(e: Exception) {
                        val state = AvailableWearablesState.FailedToGetAvailableWearables(e)
//                        savedStateHandle.set(HANDLE_KEY_AVAILABLE_WEARABLE_STATE, state)
                        _availableWearablesState.postValue(state)
                    }
                })
            }

    fun isSupported(): Boolean = wearablesManager.isSupported

    internal object WearableNameTooShortException : RuntimeException()

    sealed class AddWearableState {
        data class AddedNewWearable(val wearable: WearablesManager.Wearable) : AddWearableState()
        data class FailedToAddWearable(val failure: Exception) : AddWearableState()
    }

    sealed class AvailableWearablesState {
        class AvailableWearablesSuccess(val wearables: List<WearablesManager.Wearable>) : AvailableWearablesState()
        data class FailedToGetAvailableWearables(val exception: Exception) : AvailableWearablesState()
    }
}
