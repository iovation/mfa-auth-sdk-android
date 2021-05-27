package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.core.util.Disposable
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.VerificationFlagViewModel

class WearablesAsyncVerificationFlagManager(private val wearablesManager: WearablesManager) : VerificationFlagViewModel.AsyncVerificationFlagManager {
    override fun getVerificationFlag(callback: (VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult) -> Unit): Disposable {
        return wearablesManager.getVerificationFlag(object : WearablesManager.GetWearablesVerificationFlagCallback {
            override fun onGetSuccess(verificationFlag: VerificationFlag) {
                callback(VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult.Success(verificationFlag))
            }
            override fun onGetFailure(e: Exception) {
                callback(VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult.Failure(e))
            }
        })
    }

    override fun changeVerificationFlag(state: VerificationFlag.State, callback: (VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult) -> Unit): Disposable {
        return wearablesManager.changeVerificationFlag(state, object : WearablesManager.ChangeWearablesVerificationFlagCallback {
            override fun onChangeSuccess(verificationFlag: VerificationFlag) {
                callback(VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult.Success(verificationFlag))
            }
            override fun onChangeFailure(e: Exception) {
                callback(VerificationFlagViewModel.AsyncVerificationFlagManager.VerificationFlagResult.Failure(e))
            }
        })
    }
}