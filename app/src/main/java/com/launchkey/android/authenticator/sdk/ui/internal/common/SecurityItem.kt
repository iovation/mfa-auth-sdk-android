/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.common

import android.os.Parcelable
import androidx.annotation.StringRes
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.InvalidAuthMethodInputException
import com.launchkey.android.authenticator.sdk.ui.R
import kotlinx.android.parcel.Parcelize

@Parcelize
class SecurityItem private constructor(
    val type: AuthMethod,
    @StringRes val titleRes: Int,
    @StringRes val contentDescriptionRes: Int,
    @StringRes val helpMessageRes: Int,
    val verificationFlag: ParcelableVerificationFlag?,
    val togglePendingState: TogglePendingState
) : Parcelable {
    companion object {
        @JvmStatic
        @Throws(InvalidAuthMethodInputException::class)
        fun makeSecurityItem(
            type: AuthMethod,
            verificationFlag: VerificationFlag?,
            now: Long
        ): SecurityItem {
            val togglePendingState =
                if (verificationFlag == null || verificationFlag.millisUntilToggled == null) TogglePendingState.NotPending
                else TogglePendingState.PendingToggle(verificationFlag.millisUntilToggled!! + now)
            val parcelableVerificationFlag =
                if (verificationFlag != null) ParcelableVerificationFlag(verificationFlag) else null
            return when (type) {
                AuthMethod.PIN_CODE -> SecurityItem(
                    AuthMethod.PIN_CODE,
                    R.string.ioa_sec_factor_pin,
                    R.string.ioa_acc_security_settings_pincode,
                    R.string.ioa_sec_factor_pin_description,
                    parcelableVerificationFlag,
                    togglePendingState
                )
                AuthMethod.CIRCLE_CODE -> SecurityItem(
                    AuthMethod.CIRCLE_CODE,
                    R.string.ioa_sec_factor_cir,
                    R.string.ioa_acc_security_settings_circlecode,
                    R.string.ioa_sec_factor_cir_description,
                    parcelableVerificationFlag,
                    togglePendingState
                )
                AuthMethod.LOCATIONS -> SecurityItem(
                    AuthMethod.LOCATIONS,
                    R.string.ioa_sec_factor_geo,
                    R.string.ioa_acc_security_settings_locations,
                    R.string.ioa_sec_factor_geo_description,
                    parcelableVerificationFlag,
                    togglePendingState
                )
                AuthMethod.WEARABLES -> SecurityItem(
                    AuthMethod.WEARABLES,
                    R.string.ioa_sec_factor_bp,
                    R.string.ioa_acc_security_settings_wearables,
                    R.string.ioa_sec_factor_bp_description,
                    parcelableVerificationFlag,
                    togglePendingState
                )
                AuthMethod.BIOMETRIC -> SecurityItem(
                    AuthMethod.BIOMETRIC,
                    R.string.ioa_sec_factor_fs,
                    R.string.ioa_acc_security_settings_fingerprintscan,
                    R.string.ioa_sec_factor_fs_description,
                    parcelableVerificationFlag,
                    togglePendingState
                )
                else -> throw InvalidAuthMethodInputException("Not a valid factor type.")
            }
        }
    }
    
    sealed class TogglePendingState : Parcelable {
        @Parcelize
        object NotPending : TogglePendingState()
        
        @Parcelize
        data class PendingToggle(val toggledAtTimeInMillis: Long) : TogglePendingState()
    }
    
    @Parcelize
    class ParcelableVerificationFlag(
        private val state: VerificationFlag.State,
        private val isPendingToggle: Boolean,
        private val millisUntilToggled: Long?
    ) : Parcelable, VerificationFlag {
        
        constructor(verificationFlag: VerificationFlag) : this(
            verificationFlag.state,
            verificationFlag.isPendingToggle,
            verificationFlag.millisUntilToggled
        )
        
        override fun getState(): VerificationFlag.State = state
        
        override fun isPendingToggle(): Boolean = isPendingToggle
        
        override fun getMillisUntilToggled(): Long? = millisUntilToggled
    }
}