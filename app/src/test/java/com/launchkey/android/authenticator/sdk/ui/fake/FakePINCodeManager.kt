package com.launchkey.android.authenticator.sdk.ui.fake

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.PINCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest

class FakePINCodeManager(val fakeAuthRequestManager: FakeAuthRequestManager) : PINCodeManager {
    lateinit var expectedVerificationFlag: VerificationFlag
    lateinit var expectedChangedAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedRemoveAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedVerifyARAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    var pinCodeSet = false
    override fun getVerificationFlag(): VerificationFlag = expectedVerificationFlag

    override fun changeVerificationFlag(pinCode: String, verificationFlagState: VerificationFlag.State, authMethodVerificationCallback: AuthMethodVerificationCallback) {
        when (expectedChangedAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> authMethodVerificationCallback.onVerificationSuccess()
            is FakeCallbackResult.Failed -> {
                val result = expectedChangedAuthMethodVerificationCallbackResult.result as FakeVerificationFailureResult
                authMethodVerificationCallback.onVerificationFailure(result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
    }

    override fun setPINCode(pinCode: String, verificationFlagState: VerificationFlag.State) {

    }

    override fun removePINCode(pinCode: String, authMethodVerificationCallback: AuthMethodVerificationCallback) {
        when (expectedRemoveAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> authMethodVerificationCallback.onVerificationSuccess()
            is FakeCallbackResult.Failed -> {
                val result = expectedRemoveAuthMethodVerificationCallbackResult.result as FakeVerificationFailureResult
                authMethodVerificationCallback.onVerificationFailure(result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
    }

    override fun verifyPINCodeForAuthRequest(pinCode: String, authRequest: AuthRequest, authMethodAuthRequestVerificationCallback: AuthMethodAuthRequestVerificationCallback) {
        when (expectedVerifyARAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> {
                fakeAuthRequestManager.expectedAuthMethodsToVerify = fakeAuthRequestManager.expectedAuthMethodsToVerify.filterNot { it == AuthMethod.PIN_CODE}
                authMethodAuthRequestVerificationCallback.onVerificationSuccess(expectedVerifyARAuthMethodVerificationCallbackResult.result as Boolean)
            }
            is FakeCallbackResult.Failed -> {
                val result = expectedVerifyARAuthMethodVerificationCallbackResult.result as FakeArVerificationFailureResult
                authMethodAuthRequestVerificationCallback.onVerificationFailure(result.authRequestSent, result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
    }

    override fun isPINCodeSet(): Boolean {
        return pinCodeSet
    }
}