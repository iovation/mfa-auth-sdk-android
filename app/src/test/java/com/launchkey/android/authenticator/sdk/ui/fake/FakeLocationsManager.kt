package com.launchkey.android.authenticator.sdk.ui.fake

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest
import com.launchkey.android.authenticator.sdk.core.util.Disposable

class FakeLocationsManager(val fakeAuthRequestManager: FakeAuthRequestManager) : LocationsManager {
    lateinit var expectedAddAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedGetAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedGetVerificationFlagCallbackResult: FakeCallbackResult<*>
    lateinit var expectedChangedAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedCancelRemoveAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedRemoveAuthMethodVerificationCallbackResult: FakeCallbackResult<*>
    lateinit var expectedVerifyARAuthMethodVerificationCallbackResult: FakeCallbackResult<*>

    override fun verifyLocationsForAuthRequest(authRequest: AuthRequest, authMethodAuthRequestVerificationCallback: AuthMethodAuthRequestVerificationCallback): Disposable {
        when (expectedVerifyARAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> {
                fakeAuthRequestManager.expectedAuthMethodsToVerify = fakeAuthRequestManager.expectedAuthMethodsToVerify.filterNot { it == AuthMethod.LOCATIONS }
                authMethodAuthRequestVerificationCallback.onVerificationSuccess(expectedVerifyARAuthMethodVerificationCallbackResult.result as Boolean)
            }
            is FakeCallbackResult.Failed -> {
                val result = expectedVerifyARAuthMethodVerificationCallbackResult.result as FakeArVerificationFailureResult
                authMethodAuthRequestVerificationCallback.onVerificationFailure(result.authRequestSent, result.failure, result.unlinkTriggered, result.unlinkWarningTriggered, result.attemptsRemaining)
            }
        }
        return FakeDisposable()
    }

    override fun addLocation(location: LocationsManager.Location, addLocationCallback: LocationsManager.AddLocationCallback): Disposable {
        when (expectedAddAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> addLocationCallback.onAddSuccess()
            is FakeCallbackResult.Failed -> {
                addLocationCallback.onAddFailure(expectedAddAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun removeLocation(locationName: String, removeLocationCallback: LocationsManager.RemoveLocationCallback): Disposable {
        when (expectedRemoveAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> removeLocationCallback.onRemoveSuccess()
            is FakeCallbackResult.Failed -> {
                removeLocationCallback.onRemoveFailure(expectedRemoveAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun getVerificationFlag(getLocationsVerificationFlagCallback: LocationsManager.GetLocationsVerificationFlagCallback): Disposable {
        when (expectedGetVerificationFlagCallbackResult) {
            is FakeCallbackResult.Success -> getLocationsVerificationFlagCallback.onGetSuccess(expectedGetVerificationFlagCallbackResult.result as VerificationFlag)
            is FakeCallbackResult.Failed -> {
                getLocationsVerificationFlagCallback.onGetFailure(expectedGetVerificationFlagCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun getStoredLocations(getStoredLocationsCallback: LocationsManager.GetStoredLocationsCallback): Disposable {
        when (expectedGetAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> getStoredLocationsCallback.onGetSuccess(expectedGetAuthMethodVerificationCallbackResult.result as MutableList<LocationsManager.StoredLocation>)
            is FakeCallbackResult.Failed -> {
                getStoredLocationsCallback.onGetFailure(expectedGetAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun cancelRemoveLocation(locationName: String, cancelRemoveLocationCallback: LocationsManager.CancelRemoveLocationCallback): Disposable {
        when (expectedCancelRemoveAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> cancelRemoveLocationCallback.onCancelRemoveLocationSuccess()
            is FakeCallbackResult.Failed -> {
                cancelRemoveLocationCallback.onCancelRemoveLocationFailure(expectedCancelRemoveAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }

    override fun changeVerificationFlag(verificationFlagState: VerificationFlag.State, changeLocationsVerificationFlagCallback: LocationsManager.ChangeLocationsVerificationFlagCallback): Disposable {
        when (expectedChangedAuthMethodVerificationCallbackResult) {
            is FakeCallbackResult.Success -> changeLocationsVerificationFlagCallback.onChangeSuccess(expectedChangedAuthMethodVerificationCallbackResult.result as VerificationFlag)
            is FakeCallbackResult.Failed -> {
                changeLocationsVerificationFlagCallback.onChangeFailure(expectedChangedAuthMethodVerificationCallbackResult.result as Exception)
            }
        }
        return FakeDisposable()
    }
}