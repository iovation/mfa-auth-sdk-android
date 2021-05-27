package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations

import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.util.Disposable
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.VerificationFlagViewModel
import java.lang.Exception

class LocationsAsyncVerificationFlagManager(private val locationsManager: LocationsManager) : VerificationFlagViewModel.AsyncVerificationFlagManager {
    override fun getVerificationFlag(callback: (VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult) -> Unit): Disposable {
        return locationsManager.getVerificationFlag(object : LocationsManager.GetLocationsVerificationFlagCallback {
            override fun onGetSuccess(verificationFlag: VerificationFlag) {
                callback(VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult.Success(verificationFlag))
            }
            override fun onGetFailure(e: Exception) {
                callback(VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult.Failure(e))
            }
        })
    }

    override fun changeVerificationFlag(state: VerificationFlag.State, callback: (VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult) -> Unit): Disposable {
        return locationsManager.changeVerificationFlag(state, object : LocationsManager.ChangeLocationsVerificationFlagCallback {
            override fun onChangeSuccess(verificationFlag: VerificationFlag) {
                callback(VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult.Success(verificationFlag))
            }
            override fun onChangeFailure(e: Exception) {
                callback(VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult.Failure(e))
            }
        })
    }
}