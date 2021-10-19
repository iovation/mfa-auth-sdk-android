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
    }

    private val _addWearableState: MutableLiveData<AddWearableState> =
        savedStateHandle.getLiveData(HANDLE_KEY_ADD_WEARABLE_STATE)

    val addWearableState: LiveData<AddWearableState>
        get() = _addWearableState


    fun addWearable(wearable: WearablesManager.Wearable, name: String) =
        executor.run {
            try {
                if (name.trim().length < Constants.MINIMUM_INPUT_LENGTH) throw WearableNameTooShortException

                wearable.name = name
                wearablesManager.addWearable(
                    wearable,
                    object : WearablesManager.AddWearableCallback {
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

    fun cancelNaming() {
        _addWearableState.postValue(AddWearableState.SelectingWearable)
    }

    fun startNamingWearable(wearable: WearablesManager.Wearable) {
        _addWearableState.postValue(AddWearableState.NamingWearable(wearable))
    }

    fun getSelectedWearable() = if (addWearableState.value is AddWearableState.NamingWearable)
        (addWearableState.value as AddWearableState.NamingWearable).wearable
    else null

    fun isSupported(): Boolean = wearablesManager.isSupported

    internal object WearableNameTooShortException : RuntimeException()

    sealed class AddWearableState {
        object SelectingWearable : AddWearableState()
        data class NamingWearable(val wearable: WearablesManager.Wearable) : AddWearableState()
        data class AddedNewWearable(val wearable: WearablesManager.Wearable) : AddWearableState()
        data class FailedToAddWearable(val failure: Exception) : AddWearableState()
    }
}
