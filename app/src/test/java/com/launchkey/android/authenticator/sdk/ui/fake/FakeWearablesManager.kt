package com.launchkey.android.authenticator.sdk.ui.fake

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest
import com.launchkey.android.authenticator.sdk.core.util.Disposable

class FakeWearablesManager(val fakeAuthRequestManager: FakeAuthRequestManager) : WearablesManager {
    lateinit var expectedAddAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedGetAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedGetAvailableAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedGetVerificationFlagCallbackResult: FakeCallbackResult<*>
    lateinit var expectedChangedAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedCancelRemoveAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedRemoveAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedVerifyARAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    var expectedIsSupported = false

    override fun verifyWearablesForAuthRequest(authRequest: AuthRequest, authMethodAuthRequestVerificationCallback: AuthMethodAuthRequestVerificationCallback): Disposable {
        when (expectedVerifyARAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> {
                fakeAuthRequestManager.expectedAuthMethodsToVerify = fakeAuthRequestManager.expectedAuthMethodsToVerify.filterNot {it == AuthMethod.WEARABLES}
                authMethodAuthRequestVerificationCallback.onVerificationSuccess(expectedVerifyARAuthMethodVerificationCallbackResult.result as Boolean)
            }
            is FakeCallbackResult.Failed -> {
                val result = expectedVerifyARAuthMethodVerificationCallbackResult.result as FakeArVerificationFailureResult
                authMethodAuthRequestVerificationCallback.onVerificationFailure(result.authRequestSent, result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
        return FakeDisposable()
    }

    override fun getVerificationFlag(getWearablesVerificationFlagCallback: WearablesManager.GetWearablesVerificationFlagCallback): Disposable {
        when (expectedGetVerificationFlagCallbackResult) {
            is FakeCallbackResult.Success -> getWearablesVerificationFlagCallback.onGetSuccess(expectedGetVerificationFlagCallbackResult.result as VerificationFlag)
            is FakeCallbackResult.Failed -> {
                getWearablesVerificationFlagCallback.onGetFailure(expectedGetVerificationFlagCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun cancelRemoveWearable(wearable: WearablesManager.Wearable, cancelRemoveWearableCallback: WearablesManager.CancelRemoveWearableCallback): Disposable {
        when (expectedCancelRemoveAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> cancelRemoveWearableCallback.onCancelRemoveSuccess()
            is FakeCallbackResult.Failed -> {
                cancelRemoveWearableCallback.onCancelRemoveFailure(expectedCancelRemoveAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun removeWearable(wearable: WearablesManager.Wearable, removeWearableCallback: WearablesManager.RemoveWearableCallback): Disposable {
        when (expectedRemoveAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> removeWearableCallback.onRemoveSuccess()
            is FakeCallbackResult.Failed -> {
                removeWearableCallback.onRemoveFailure(expectedRemoveAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun isSupported(): Boolean = expectedIsSupported

    override fun getAvailableWearables(getAvailableWearablesCallback: WearablesManager.GetAvailableWearablesCallback): Disposable {
        when (expectedGetAvailableAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> getAvailableWearablesCallback.onGetSuccess(expectedGetAvailableAuthMethodVerificationCallbackResult.result as MutableList<WearablesManager.Wearable>)
            is FakeCallbackResult.Failed -> {
                getAvailableWearablesCallback.onGetFailure(expectedGetAvailableAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun changeVerificationFlag(verificationFlagState: VerificationFlag.State, changeWearablesVerificationFlagCallback: WearablesManager.ChangeWearablesVerificationFlagCallback): Disposable {
        when (expectedChangedAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> changeWearablesVerificationFlagCallback.onChangeSuccess(expectedChangedAuthMethodVerificationCallbackResult.result as VerificationFlag)
            is FakeCallbackResult.Failed -> {
                changeWearablesVerificationFlagCallback.onChangeFailure(expectedChangedAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun getStoredWearables(getStoredWearablesCallback: WearablesManager.GetStoredWearablesCallback): Disposable {
        when (expectedGetAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> getStoredWearablesCallback.onGetSuccess(expectedGetAuthMethodVerificationCallbackResult.result as MutableList<WearablesManager.Wearable>)
            is FakeCallbackResult.Failed -> {
                getStoredWearablesCallback.onGetFailure(expectedGetAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun addWearable(wearable: WearablesManager.Wearable, addWearableCallback: WearablesManager.AddWearableCallback): Disposable {
        when (expectedAddAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> addWearableCallback.onAddSuccess()
            is FakeCallbackResult.Failed -> {
                addWearableCallback.onAddFailure(expectedAddAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }
}