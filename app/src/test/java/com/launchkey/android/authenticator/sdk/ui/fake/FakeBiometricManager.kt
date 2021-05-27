package com.launchkey.android.authenticator.sdk.ui.fake

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.BiometricManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest
import com.launchkey.android.authenticator.sdk.core.util.Disposable

class FakeBiometricManager(val fakeAuthRequestManager: FakeAuthRequestManager) : BiometricManager {
    lateinit var expectedVerificationFlag: VerificationFlag
    lateinit var expectedChangedAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedRemoveAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedVerifyARAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    var biometricSet = false
    var biometricSupported = false
    override fun removeBiometric(authMethodVerificationCallback: AuthMethodVerificationCallback): Disposable {
        when (expectedRemoveAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> authMethodVerificationCallback.onVerificationSuccess()
            is FakeCallbackResult.Failed -> {
                val result = expectedRemoveAuthMethodVerificationCallbackResult.result as FakeVerificationFailureResult
                authMethodVerificationCallback.onVerificationFailure(result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
        return FakeDisposable()
    }

    override fun verifyBiometricForAuthRequest(authRequest: AuthRequest, authMethodAuthRequestVerificationCallback: AuthMethodAuthRequestVerificationCallback): Disposable {
        when (expectedVerifyARAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> {
                fakeAuthRequestManager.expectedAuthMethodsToVerify = fakeAuthRequestManager.expectedAuthMethodsToVerify.filterNot { it == AuthMethod.BIOMETRIC }
                authMethodAuthRequestVerificationCallback.onVerificationSuccess(expectedVerifyARAuthMethodVerificationCallbackResult.result as Boolean)
            }
            is FakeCallbackResult.Failed -> {
                val result = expectedVerifyARAuthMethodVerificationCallbackResult.result as FakeArVerificationFailureResult
                authMethodAuthRequestVerificationCallback.onVerificationFailure(result.authRequestSent, result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
        return FakeDisposable()
    }

    override fun needsUiToCancelBiometric(): Boolean = false

    override fun setBiometric(verificationFlagState: VerificationFlag.State, setBiometricCallback: BiometricManager.SetBiometricCallback): Disposable {
        return FakeDisposable()
    }

    override fun isBiometricSet(): Boolean = biometricSet

    override fun getVerificationFlag(): VerificationFlag = expectedVerificationFlag
    override fun changeVerificationFlag(verificationFlagState: VerificationFlag.State, authMethodVerificationCallback: AuthMethodVerificationCallback): Disposable {
        when (expectedChangedAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> authMethodVerificationCallback.onVerificationSuccess()
            is FakeCallbackResult.Failed -> {
                val result = expectedChangedAuthMethodVerificationCallbackResult.result as FakeVerificationFailureResult
                authMethodVerificationCallback.onVerificationFailure(result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
        return FakeDisposable()
    }

    override fun isSupported(): Boolean = biometricSupported
}