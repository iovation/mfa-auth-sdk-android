package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class WearablesAddViewModel(
    private val wearablesManager: WearablesManager,
    private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private var wearableToAdd: WearablesManager.Wearable? = null

    private val _addWearableState =
        SingleLiveEvent<AddWearableState>(AddWearableState.SelectingWearable)

    val addWearableState: LiveData<AddWearableState>
        get() = _addWearableState

    fun addSelectedWearableWithName(name: String) {
        viewModelScope.launch(defaultDispatcher) {
            if (name.trim().length < Constants.MINIMUM_INPUT_LENGTH) {
                _addWearableState.postValue(
                    AddWearableState.FailedToAddWearable(WearableNameTooShortException)
                )
            } else {
                wearableToAdd?.let {
                    it.name = name
                    addWearable(it)
                }
            }
        }
    }

    private fun addWearable(wearable: WearablesManager.Wearable) {
        viewModelScope.launch(defaultDispatcher) {
            try {
                wearablesManager.addWearable(
                    wearable,
                    object : WearablesManager.AddWearableCallback {
                        override fun onAddSuccess() {
                            val state = AddWearableState.AddedNewWearable(wearable)
                            _addWearableState.postValue(state)
                        }

                        override fun onAddFailure(e: Exception) {
                            _addWearableState.postValue(AddWearableState.FailedToAddWearable(e))
                        }
                    })
            } catch (e: Exception) {
                _addWearableState.postValue(AddWearableState.FailedToAddWearable(e))

            }
        }
    }

    fun cancelNaming() {
        wearableToAdd = null
        _addWearableState.postValue(AddWearableState.SelectingWearable)
    }

    fun startNamingWearable(wearable: WearablesManager.Wearable) {
        wearableToAdd = wearable
        _addWearableState.postValue(AddWearableState.NamingWearable(wearable))
    }

    internal object WearableNameTooShortException : RuntimeException()

    sealed class AddWearableState {
        object SelectingWearable : AddWearableState()
        data class NamingWearable(val wearable: WearablesManager.Wearable) : AddWearableState()
        data class AddedNewWearable(val wearable: WearablesManager.Wearable) : AddWearableState()
        data class FailedToAddWearable(val failure: Exception) : AddWearableState()
    }
}
