package com.launchkey.android.authenticator.sdk.ui.fake;

import android.os.Bundle
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorConfig
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.core.authentication_management.Device
import com.launchkey.android.authenticator.sdk.core.authentication_management.Session
import com.launchkey.android.authenticator.sdk.core.authentication_management.event_callback.*
import com.launchkey.android.authenticator.sdk.core.util.Disposable
import org.junit.Assert.assertEquals
import java.lang.Exception

class FakeAuthenticatorManager : AuthenticatorManager {
    lateinit var expectedDeviceLinkedEventCallbackResult: FakeCallbackResult<*>
    lateinit var expectedUnlinkedDeviceEventCallbackResult: FakeCallbackResult<*>
    lateinit var expectedGetDevicesEventCallbackResult: FakeCallbackResult<*>
    lateinit var expectedGetSessionsEventCallbackResult: FakeCallbackResult<*>
    lateinit var expectedEndSessionEventCallbackResult: FakeCallbackResult<*>
    lateinit var actualLastEndedSession: Session
    lateinit var expectedEndSessionsEventCallbackResult: FakeCallbackResult<*>
    var expectedIsDeviceLinked: Boolean = false
    lateinit var expectedCurrentDevice: Device
    private val callbacks: MutableList<AuthenticatorManager.Event<*>> = mutableListOf()
    lateinit var authenticatorConfig: AuthenticatorConfig
    override fun initialize(config: AuthenticatorConfig) {
        authenticatorConfig = config
    }

    override fun getConfig(): AuthenticatorConfig = authenticatorConfig

    override fun registerForEvents(vararg events: AuthenticatorManager.Event<*>) {
        callbacks.addAll(events)
    }

    override fun unregisterForEvents(vararg events: AuthenticatorManager.Event<*>) {
        callbacks.removeAll(events)
    }

    override fun linkDevice(sdkKey: String, linkingCode: String, optionalDeviceName: String?, allowNameOverride: Boolean, deviceLinkedEventCallback: DeviceLinkedEventCallback?): Disposable {
        when (expectedDeviceLinkedEventCallbackResult) {
            is FakeCallbackResult.Success -> {
                val device = expectedDeviceLinkedEventCallbackResult.result as Device
                deviceLinkedEventCallback?.onSuccess(device)
                callbacks.filterIsInstance<DeviceLinkedEventCallback>().forEach { it.onSuccess(device) }
            }
            is FakeCallbackResult.Failed -> {
                val exception = expectedDeviceLinkedEventCallbackResult.result as Exception
                deviceLinkedEventCallback?.onFailure(exception)
                callbacks.filterIsInstance<DeviceLinkedEventCallback>().forEach { it.onFailure(exception) }
            }
        }
        return FakeDisposable()
    }

    override fun unlinkDevice(device: Device?, unlinkDeviceEventCallback: UnlinkDeviceEventCallback?): Disposable {
        when (expectedUnlinkedDeviceEventCallbackResult) {
            is FakeCallbackResult.Success -> {
                val unlinkedDevice = expectedUnlinkedDeviceEventCallbackResult.result as Device
                unlinkDeviceEventCallback?.onSuccess(unlinkedDevice)
                callbacks.filterIsInstance<UnlinkDeviceEventCallback>().forEach { it.onSuccess(unlinkedDevice) }
            }
            is FakeCallbackResult.Failed -> {
                val exception = expectedUnlinkedDeviceEventCallbackResult.result as Exception
                unlinkDeviceEventCallback?.onFailure(exception)
                callbacks.filterIsInstance<UnlinkDeviceEventCallback>().forEach { it.onFailure(exception) }
            }
        }
        return FakeDisposable()
    }

    override fun isDeviceLinked(): Boolean = expectedIsDeviceLinked

    override fun getCurrentDevice(): Device = expectedCurrentDevice

    override fun getDevices(getDevicesEventCallback: GetDevicesEventCallback?): Disposable {
        when (expectedGetDevicesEventCallbackResult) {
            is FakeCallbackResult.Success -> {
                val devices = expectedGetDevicesEventCallbackResult.result as List<Device>
                getDevicesEventCallback?.onSuccess(devices)
                callbacks.filterIsInstance<GetDevicesEventCallback>().forEach { it.onSuccess(devices) }
            }
            is FakeCallbackResult.Failed -> {
                val exception = expectedGetDevicesEventCallbackResult.result as Exception
                getDevicesEventCallback?.onFailure(exception)
                callbacks.filterIsInstance<GetDevicesEventCallback>().forEach { it.onFailure(exception) }
            }
        }
        return FakeDisposable()
    }

    override fun getSessions(getSessionsEventCallback: GetSessionsEventCallback?): Disposable {
        when (expectedGetSessionsEventCallbackResult) {
            is FakeCallbackResult.Success -> {
                val sessions = expectedGetSessionsEventCallbackResult.result as List<Session>
                getSessionsEventCallback?.onSuccess(sessions)
                callbacks.filterIsInstance<GetSessionsEventCallback>().forEach { it.onSuccess(sessions) }
            }
            is FakeCallbackResult.Failed -> {
                val exception = expectedGetSessionsEventCallbackResult.result as Exception
                getSessionsEventCallback?.onFailure(exception)
                callbacks.filterIsInstance<GetSessionsEventCallback>().forEach { it.onFailure(exception) }
            }
        }
        return FakeDisposable()
    }

    override fun endAllSessions(endAllSessionsEventCallback: EndAllSessionsEventCallback?): Disposable {
        when (expectedEndSessionsEventCallbackResult) {
            is FakeCallbackResult.Success -> {
                val void = expectedEndSessionsEventCallbackResult.result as Void
                endAllSessionsEventCallback?.onSuccess(void)
                callbacks.filterIsInstance<EndAllSessionsEventCallback>().forEach { it.onSuccess(void) }
            }
            is FakeCallbackResult.Failed -> {
                val exception = expectedEndSessionsEventCallbackResult.result as Exception
                endAllSessionsEventCallback?.onFailure(exception)
                callbacks.filterIsInstance<EndAllSessionsEventCallback>().forEach { it.onFailure(exception) }
            }
        }
        return FakeDisposable()
    }

    override fun endSession(session: Session, endSessionEventCallback: EndSessionEventCallback?): Disposable {
        actualLastEndedSession = session
        when (expectedEndSessionEventCallbackResult) {
            is FakeCallbackResult.Success -> {
                val endedSession = expectedEndSessionEventCallbackResult.result as Session
                endSessionEventCallback?.onSuccess(endedSession)
                callbacks.filterIsInstance<EndSessionEventCallback>().forEach { it.onSuccess(endedSession) }
            }
            is FakeCallbackResult.Failed -> {
                val exception = expectedEndSessionEventCallbackResult.result as Exception
                endSessionEventCallback?.onFailure(exception)
                callbacks.filterIsInstance<EndSessionEventCallback>().forEach { it.onFailure(exception) }
            }
        }
        return FakeDisposable()
    }

    fun thenLastEndedSessionIs(expectedLastEndSession: Session) {
        assertEquals(expectedLastEndSession, actualLastEndedSession)
    }

    override fun setPushDeviceToken(deviceToken: String?) {
        // Do nothing
    }

    override fun handlePushPayload(data: Bundle?) {
        // Do nothing
    }

    override fun handlePushPayload(data: MutableMap<String, String>?) {
        // Do nothing
    }

    override fun handleThirdPartyPushPackage(pushPackage: String?) {
        // Do nothing
    }
}
