package com.launchkey.android.authenticator.sdk.ui.fake

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest

class FakeCircleCodeManager(val fakeAuthRequestManager: FakeAuthRequestManager) : CircleCodeManager {
    lateinit var expectedVerificationFlag: VerificationFlag
    lateinit var expectedChangedAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedRemoveAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedVerifyARAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    var circleCodeSet = false

    override fun isCircleCodeSet(): Boolean = circleCodeSet

    override fun verifyCircleCodeForAuthRequest(circleCode: MutableList<CircleCodeManager.CircleCodeTick>, authRequest: AuthRequest, authMethodAuthRequestVerificationCallback: AuthMethodAuthRequestVerificationCallback) {
        when (expectedVerifyARAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> {
                fakeAuthRequestManager.expectedAuthMethodsToVerify = fakeAuthRequestManager.expectedAuthMethodsToVerify.filterNot { it == AuthMethod.CIRCLE_CODE }
                authMethodAuthRequestVerificationCallback.onVerificationSuccess(expectedVerifyARAuthMethodVerificationCallbackResult.result as Boolean)
            }
            is FakeCallbackResult.Failed -> {
                val result = expectedVerifyARAuthMethodVerificationCallbackResult.result as FakeArVerificationFailureResult
                authMethodAuthRequestVerificationCallback.onVerificationFailure(result.authRequestSent, result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
    }

    override fun removeCircleCode(circleCode: MutableList<CircleCodeManager.CircleCodeTick>, authMethodVerificationCallback: AuthMethodVerificationCallback) {
        when (expectedRemoveAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> authMethodVerificationCallback.onVerificationSuccess()
            is FakeCallbackResult.Failed -> {
                val result = expectedRemoveAuthMethodVerificationCallbackResult.result as FakeVerificationFailureResult
                authMethodVerificationCallback.onVerificationFailure(result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
    }

    override fun getVerificationFlag(): VerificationFlag = expectedVerificationFlag

    override fun setCircleCode(circleCode: MutableList<CircleCodeManager.CircleCodeTick>, verificationFlagState: VerificationFlag.State) {

    }

    override fun changeVerificationFlag(circleCode: MutableList<CircleCodeManager.CircleCodeTick>, verificationFlagState: VerificationFlag.State, authMethodVerificationCallback: AuthMethodVerificationCallback) {
        when (expectedChangedAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> authMethodVerificationCallback.onVerificationSuccess()
            is FakeCallbackResult.Failed -> {
                val result = expectedChangedAuthMethodVerificationCallbackResult.result as FakeVerificationFailureResult
                authMethodVerificationCallback.onVerificationFailure(result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
    }
}