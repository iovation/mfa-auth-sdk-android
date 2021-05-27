package com.launchkey.android.authenticator.sdk.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.core.authentication_management.Device
import com.launchkey.android.authenticator.sdk.core.authentication_management.event_callback.GetDevicesEventCallback
import com.launchkey.android.authenticator.sdk.core.authentication_management.event_callback.UnlinkDeviceEventCallback
import com.launchkey.android.authenticator.sdk.core.exception.DeviceNotLinkedException
import com.launchkey.android.authenticator.sdk.core.exception.DeviceUnlinkedButFailedToNotifyServerException
import com.launchkey.android.authenticator.sdk.ui.internal.util.disposeWhenCancelled
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DevicesViewModel(
    private val authenticatorManager: AuthenticatorManager,
    private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle?
) : ViewModel() {
    private val _state = MutableLiveData<State>()
    val state: LiveData<State>
        get() = _state

    init {
        refreshDevices()
    }

    fun refreshDevices() = viewModelScope.launch(defaultDispatcher) {
        _state.postValue(State.Loading)
        try {
            val linkedDevices = getDevices()
            _state.postValue(State.GetDevicesSuccess(linkedDevices))
        } catch (e: Exception) {
            _state.postValue(State.Failed(e))
        }
    }

    private suspend fun getDevices() = suspendCancellableCoroutine<List<Device>> { continuation ->
        if (!authenticatorManager.isDeviceLinked) {
            continuation.resumeWithException(
                DeviceNotLinkedException("Current device is not linked, can not unlink devices while unlinked")
            )
        }

        authenticatorManager.getDevices(object : GetDevicesEventCallback() {
            override fun onSuccess(devices: List<Device>) {
                continuation.resume(devices)
            }

            override fun onFailure(e: Exception) {
                continuation.resumeWithException(e)
            }
        }).disposeWhenCancelled(continuation)
    }

    fun unlinkDevice(device: Device) = viewModelScope.launch(defaultDispatcher) {
        try {
            val unlinkedDevice = unlinkSingleDevice(device)
            _state.postValue(State.UnlinkDeviceSuccess(unlinkedDevice))
        } catch (e: Exception) {
            _state.postValue(State.Failed(e))
        }
    }

    private suspend fun unlinkSingleDevice(device: Device) =
        suspendCancellableCoroutine<Device> { continuation ->
            if (!authenticatorManager.isDeviceLinked) {
                continuation.resumeWithException(
                    DeviceNotLinkedException("Current device is not linked, can not unlink devices while unlinked")
                )
            }

            authenticatorManager.unlinkDevice(device, object : UnlinkDeviceEventCallback() {
                override fun onSuccess(device: Device) {
                    continuation.resume(device)
                }

                override fun onFailure(e: Exception) {
                    if (e is DeviceUnlinkedButFailedToNotifyServerException)
                        continuation.resume(device)
                    else
                        continuation.resumeWithException(e)
                }
            }).disposeWhenCancelled(continuation)
        }

    sealed class State {
        object Loading : State()
        data class GetDevicesSuccess(val devices: List<Device>) : State()
        data class UnlinkDeviceSuccess(val unlinkedDevice: Device) : State()
        data class Failed(val failure: Exception) : State()
    }
}