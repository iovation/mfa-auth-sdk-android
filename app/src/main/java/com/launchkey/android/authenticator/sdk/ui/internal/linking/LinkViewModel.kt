package com.launchkey.android.authenticator.sdk.ui.internal.linking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.core.authentication_management.Device
import com.launchkey.android.authenticator.sdk.core.authentication_management.event_callback.DeviceLinkedEventCallback
import com.launchkey.android.authenticator.sdk.core.exception.DeviceAlreadyLinkedException
import com.launchkey.android.authenticator.sdk.core.exception.MalformedLinkingCodeException
import com.launchkey.android.authenticator.sdk.core.util.Disposable
import com.launchkey.android.authenticator.sdk.ui.internal.linking.LinkViewModel.State.CodeReady
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent

class LinkViewModel internal constructor(private val authenticatorManager: AuthenticatorManager,
                                         private val sdkKey: String,
                                         savedStateHandle: SavedStateHandle?) : ViewModel() {
    private val _state: MutableLiveData<State> = SingleLiveEvent()
    val state: LiveData<State>
        get() = _state
    fun linkDevice(code: String, deviceName: String?) {
        try {
            authenticatorManager.linkDevice(sdkKey, code, deviceName, true, object : DeviceLinkedEventCallback() {
                override fun onSuccess(device: Device) {
                    _state.value = State.Success(device)
                }
                override fun onFailure(e: Exception) {
                    _state.value = State.Failed(e)
                }
            })
        } catch (e: DeviceAlreadyLinkedException) {
            _state.value = State.Failed(e)
        } catch (e: MalformedLinkingCodeException) {
            _state.value = State.Failed(e)
        }
    }

    fun codeReady(code: String) {
        _state.value = CodeReady(code)
    }

    fun exception(e: Exception) {
        _state.value = State.Failed(e)
    }

    sealed class State {
        class Success internal constructor(val device: Device) : State()
        class Failed internal constructor(val failure: Exception) : State()
        class Loading internal constructor(val disposable: Disposable) : State()
        class CodeReady internal constructor(val code: String) : State()
    }
}