package com.launchkey.android.authenticator.sdk.ui.fake

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequestManager
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequestResponse
import com.launchkey.android.authenticator.sdk.core.auth_request_management.DenialReason
import com.launchkey.android.authenticator.sdk.core.auth_request_management.event_callback.AuthRequestPushReceivedEvent
import com.launchkey.android.authenticator.sdk.core.auth_request_management.event_callback.AuthRequestResponseEvent
import com.launchkey.android.authenticator.sdk.core.auth_request_management.event_callback.GetAuthRequestEventCallback
import com.launchkey.android.authenticator.sdk.core.util.Disposable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class FakeAuthRequestManager : AuthRequestManager {
    lateinit var expectedGetAuthRequestEventCallbackResult: FakeCallbackResult<*>
    lateinit var expectedAuthMethodsToVerify: List<AuthMethod>
    var expectedAcceptAndSendResult: Boolean = false
    lateinit var expectedAuthRequestResponseEventResult: FakeCallbackResult<*>
    lateinit var expectedAuthRequestPushReceivedEventResult: FakeCallbackResult<*>
    private val lastEventsUnregisteredFor: MutableList<AuthRequestManager.Event<*>> = mutableListOf()
    private var unregisterForEventsWasCalled: Boolean = false
    private var denyAndSendWasCalled = false
    private var denyAndSendAuthRequest: AuthRequest? = null
    private var denyAndSendDenialReason: DenialReason? = null
    private val callbacks: MutableList<AuthRequestManager.Event<*>> = mutableListOf()
    var handler: (() -> Unit) -> Unit = {
        it()
    }
    override fun unregisterForEvents(vararg events: AuthRequestManager.Event<*>) {
        callbacks.removeAll(events)
        unregisterForEventsWasCalled = true
        lastEventsUnregisteredFor.clear()
        lastEventsUnregisteredFor.addAll(events)
    }

    fun thenUnregisterForEventsWasCalled() {
        assertTrue(unregisterForEventsWasCalled)
    }

    fun thenLastEventsUnregisteredFromAre(vararg events: AuthRequestManager.Event<*>) {
        assertEquals(events, lastEventsUnregisteredFor)
    }

    override fun getAuthMethodsToVerify(authRequest: AuthRequest): List<AuthMethod> = expectedAuthMethodsToVerify

    override fun denyAndSend(authRequest: AuthRequest, denialReason: DenialReason?) {
        denyAndSendWasCalled = true
        denyAndSendAuthRequest = authRequest
        denyAndSendDenialReason = denialReason
        send(authRequest)
    }

    fun thenDenyAndSendWasCalledWith(authRequest: AuthRequest, denialReason: DenialReason?) {
        assertTrue(denyAndSendWasCalled)
        assertEquals(denyAndSendAuthRequest, authRequest)
        assertEquals(denyAndSendDenialReason, denialReason)
    }

    override fun acceptAndSendIfFailed(authRequest: AuthRequest): Boolean {
        if (expectedAcceptAndSendResult) {
            send(authRequest)
        }
        return expectedAcceptAndSendResult
    }

    override fun send(authRequest: AuthRequest) {
        when (expectedAuthRequestResponseEventResult) {
            is FakeCallbackResult.Success -> {
                val authRequestResponse = expectedAuthRequestResponseEventResult.result as AuthRequestResponse
                callbacks.filterIsInstance<AuthRequestResponseEvent>().forEach { it.onSuccess(authRequestResponse) }
            }
            is FakeCallbackResult.Failed -> {
                val exception = expectedAuthRequestResponseEventResult.result as Exception
                callbacks.filterIsInstance<AuthRequestResponseEvent>().forEach { it.onFailure(exception) }
            }
        }
    }

    override fun registerForEvents(vararg events: AuthRequestManager.Event<*>) {
        callbacks.addAll(events)
    }

    override fun checkForAuthRequest(getAuthRequestEventCallback: GetAuthRequestEventCallback?): Disposable {
        when (expectedGetAuthRequestEventCallbackResult) {
            is FakeCallbackResult.Success -> {
                val authRequest = expectedGetAuthRequestEventCallbackResult.result as? AuthRequest
                getAuthRequestEventCallback?.onSuccess(authRequest)
                callbacks.filterIsInstance<GetAuthRequestEventCallback>().forEach { handler { it.onSuccess(authRequest) } }
            }
            is FakeCallbackResult.Failed -> {
                val exception = expectedGetAuthRequestEventCallbackResult.result as Exception
                getAuthRequestEventCallback?.onFailure(exception)
                callbacks.filterIsInstance<GetAuthRequestEventCallback>().forEach { handler { it.onFailure(exception) } }
            }
        }
        return FakeDisposable()
    }

    fun simulatePushReceived() {
        when (expectedAuthRequestPushReceivedEventResult) {
            is FakeCallbackResult.Success -> {
                val void = expectedAuthRequestPushReceivedEventResult.result as? Void
                callbacks.filterIsInstance<AuthRequestPushReceivedEvent>().forEach { it.onSuccess(void) }
            }
            is FakeCallbackResult.Failed -> {
                val exception = expectedAuthRequestPushReceivedEventResult.result as Exception
                callbacks.filterIsInstance<AuthRequestPushReceivedEvent>().forEach { it.onFailure(exception) }
            }
        }
    }
}