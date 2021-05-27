package com.launchkey.android.authenticator.sdk.ui.internal.util

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethodType
import com.launchkey.android.authenticator.sdk.core.failure.Failure
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.auth.*
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.permission.BluetoothNotGrantedAuthMethodPermissionFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.permission.LocationServicesNotGrantedAuthMethodPermissionFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.sensor.*
import com.launchkey.android.authenticator.sdk.core.failure.auth_request.config.AuthRequestConfigFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_request.policy.AuthRequestAmountPolicyFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_request.policy.AuthRequestTypePolicyFailure
import com.launchkey.android.authenticator.sdk.ui.R
import java.util.*

object FailureUtils {
    fun getMessageForFailure(failure: Failure): FailureDetails {
        if (failure is AuthRequestAmountPolicyFailure) {
            return FailureDetails(R.string.ioa_ar_error_result_poli_amount_title,
                    R.plurals.ioa_ar_error_result_poli_amount_message_format, failure.remainingAmountRequired)
        } else if (failure is AuthRequestTypePolicyFailure) {
            val missingAuthMethodTypes = failure.remainingFactorTypesRequired
            val remainingFactorsStringResources: MutableList<List<Int>> = ArrayList()
            if (missingAuthMethodTypes.contains(AuthMethodType.KNOWLEDGE)) {
                remainingFactorsStringResources.add(Arrays.asList(
                        R.string.ioa_sec_factor_pin,
                        R.string.ioa_sec_factor_cir))
            } else if (missingAuthMethodTypes.contains(AuthMethodType.INHERENCE)) {
                remainingFactorsStringResources.add(Arrays.asList(
                        R.string.ioa_sec_factor_geo,
                        R.string.ioa_sec_factor_fs))
            } else if (missingAuthMethodTypes.contains(AuthMethodType.POSSESSION)) {
                remainingFactorsStringResources.add(Arrays.asList(
                        R.string.ioa_sec_factor_bp))
            }
            return FailureDetails(R.string.ioa_ar_error_result_poli_type_title,
                    R.string.ioa_ar_error_result_poli_type_message_format,
                    "",
                    ", ",
                    remainingFactorsStringResources)
        } else if (failure is AuthRequestConfigFailure) {
            // We don't currently show a different error message for type vs. amount
            return FailureDetails(R.string.ioa_ar_error_result_conf_title,
                    R.string.ioa_ar_error_result_conf_message)
        } else if (failure is AuthMethodFailure) {
            if (failure is BluetoothNotGrantedAuthMethodPermissionFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_perm_bt_title,
                        R.string.ioa_ar_error_result_perm_bt_message)
            } else if (failure is LocationServicesNotGrantedAuthMethodPermissionFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_perm_loc_title,
                        R.string.ioa_ar_error_result_perm_loc_message)
            } else if (failure is WrongLocationFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_auth_loc_title,
                        R.string.ioa_ar_error_result_auth_loc_message)
            } else if (failure is MissingLocationFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_auth_loc_missing_title,
                        R.string.ioa_ar_error_result_auth_loc_missing_message)
            } else if (failure is WearableNotConnectedFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_auth_bt_title,
                        R.string.ioa_ar_error_result_auth_bt_message)
            } else if (failure is BiometricUnrecognizedFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_auth_fs_title,
                        R.string.ioa_ar_error_result_auth_fs_message)
            } else if (failure is PINCodeWrongFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_auth_pc_title,
                        R.string.ioa_ar_error_result_auth_pc_message)
            } else if (failure is CircleCodeWrongFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_auth_cc_title,
                        R.string.ioa_ar_error_result_auth_cc_message)
            } else if (failure is LocationServicesDisabledFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_sens_loc_title,
                        R.string.ioa_ar_error_result_sens_loc_message)
            } else if (failure is LocationServicesMockedFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_auth_loc_title,
                        R.string.ioa_ar_error_location_mock_dev)
            } else if (failure is BiometricScannerDisabledFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_auth_fs_title,
                        R.string.ioa_ar_error_result_auth_fs_message)
            } else if (failure is BiometricScannerTimeoutFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_auth_fs_title,
                        R.string.ioa_ar_error_fs_timeout)
            } else if (failure is BluetoothNotSupportedFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_sens_bt_title,
                        R.string.ioa_ar_error_bt_notsupported)
            } else if (failure is BluetoothDisabledFailure) {
                return FailureDetails(R.string.ioa_ar_error_result_sens_bt_title,
                        R.string.ioa_ar_error_result_sens_bt_message)
            }
        }
        throw IllegalArgumentException("Invalid Failure value was passed")
    }
}