package com.launchkey.android.authenticator.sdk.ui.fake

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.GeofencesManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest
import com.launchkey.android.authenticator.sdk.core.util.Disposable

class FakeGeofencesManager(val fakeAuthRequestManager: FakeAuthRequestManager) : GeofencesManager {
    lateinit var expectedAuthMethodRequestVerificationCallback: FakeCallbackResult<*>
    override fun verifyGeofencesForAuthRequest(authRequest: AuthRequest, authMethodAuthRequestVerificationCallback: AuthMethodAuthRequestVerificationCallback): Disposable {
        when (expectedAuthMethodRequestVerificationCallback) {
            is FakeCallbackResult.Success -> {
                fakeAuthRequestManager.expectedAuthMethodsToVerify = fakeAuthRequestManager.expectedAuthMethodsToVerify.filterNot { it == AuthMethod.GEOFENCING }
                authMethodAuthRequestVerificationCallback.onVerificationSuccess(expectedAuthMethodRequestVerificationCallback.result as Boolean)
            }
            is FakeCallbackResult.Failed -> {
                val result = expectedAuthMethodRequestVerificationCallback.result as FakeArVerificationFailureResult
                authMethodAuthRequestVerificationCallback.onVerificationFailure(result.authRequestSent, result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
        return FakeDisposable()
    }
}