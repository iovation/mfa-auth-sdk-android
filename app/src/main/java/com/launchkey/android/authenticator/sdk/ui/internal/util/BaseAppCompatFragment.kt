/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethodManagerFactory
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequestManager
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.fragment.DevicesViewModel
import com.launchkey.android.authenticator.sdk.ui.fragment.SessionsViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.TimerViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.VerificationFlagViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.biometric.BiometricAddViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.biometric.BiometricCheckViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code.CircleCodeAddViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code.CircleCodeCheckViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.LocationsAddViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.LocationsAsyncVerificationFlagManager
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.LocationsSettingsViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.location_tracker.LocationTrackerFactory
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map.MapViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map.MapViewModel.Companion.GEO_FENCE_FILL_ALPHA
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map.UserLocationViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PINCodeRequirement
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeCheckViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables.WearablesAddViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables.WearablesAsyncVerificationFlagManager
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables.WearablesScanViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables.WearablesSettingsViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify.AuthRequestVerificationViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.DialogFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SecurityViewModel
import kotlinx.coroutines.Dispatchers

abstract class BaseAppCompatFragment : Fragment {
    private val viewModelProviderFactory: ViewModelProvider.Factory by lazy {
        object : AbstractSavedStateViewModelFactory(this, null) {
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return when (modelClass) {
                    DevicesViewModel::class.java ->
                        DevicesViewModel(
                            AuthenticatorManager.instance,
                            Dispatchers.IO,
                            handle
                        ) as T
                    SessionsViewModel::class.java ->
                        SessionsViewModel(
                            AuthenticatorManager.instance,
                            Dispatchers.IO,
                            handle
                        ) as T
                    AuthRequestFragmentViewModel::class.java ->
                        AuthRequestFragmentViewModel(
                            AuthRequestManager.instance,
                            Dispatchers.IO,
                            handle
                        ) as T
                    SecurityViewModel::class.java ->
                        SecurityViewModel(
                            Dispatchers.IO,
                            AuthenticatorManager.instance,
                            AuthenticatorUIManager.instance,
                            AuthMethodManagerFactory.getPINCodeManager(),
                            AuthMethodManagerFactory.getCircleCodeManager(),
                            AuthMethodManagerFactory.getWearablesManager(),
                            AuthMethodManagerFactory.getBiometricManager(),
                            AuthMethodManagerFactory.getLocationsManager(),
                            TimingCounter.DefaultTimeProvider(),
                            handle
                        ) as T
                    AuthRequestVerificationViewModel::class.java ->
                        AuthRequestVerificationViewModel(
                            AuthRequestManager.instance,
                            AuthenticatorManager.instance,
                            AuthMethodManagerFactory.getPINCodeManager(),
                            AuthMethodManagerFactory.getCircleCodeManager(),
                            AuthMethodManagerFactory.getWearablesManager(),
                            AuthMethodManagerFactory.getBiometricManager(),
                            AuthMethodManagerFactory.getLocationsManager(),
                            AuthMethodManagerFactory.getGeofencesManager(),
                            Dispatchers.IO,
                            handle
                        ) as T
                    DialogFragmentViewModel::class.java ->
                        DialogFragmentViewModel(handle) as T
                    PinCodeAddViewModel::class.java ->
                        PinCodeAddViewModel(
                            AuthMethodManagerFactory.getPINCodeManager(),
                            PINCodeRequirement.pinCodeRequirements,
                            handle
                        ) as T
                    PinCodeCheckViewModel::class.java ->
                        PinCodeCheckViewModel(
                            AuthMethodManagerFactory.getPINCodeManager(),
                            AuthenticatorManager.instance.config.thresholdAutoUnlink(),
                            handle
                        ) as T
                    CircleCodeAddViewModel::class.java ->
                        CircleCodeAddViewModel(
                            AuthMethodManagerFactory.getCircleCodeManager(),
                            handle
                        ) as T
                    CircleCodeCheckViewModel::class.java ->
                        CircleCodeCheckViewModel(
                            AuthMethodManagerFactory.getCircleCodeManager(),
                            AuthenticatorManager.instance.config.thresholdAutoUnlink(),
                            handle
                        ) as T
                    BiometricAddViewModel::class.java ->
                        BiometricAddViewModel(
                            AuthMethodManagerFactory.getBiometricManager(),
                            Dispatchers.IO,
                            handle
                        ) as T
                    BiometricCheckViewModel::class.java ->
                        BiometricCheckViewModel(
                            AuthMethodManagerFactory.getBiometricManager(),
                            AuthenticatorManager.instance.config.thresholdAutoUnlink(),
                            handle
                        ) as T
                    MapViewModel::class.java -> {
                        val strokeColor =
                            AuthenticatorUIManager.instance.config.themeObj().geoFence.geoFenceColor
                        val fillColor = Color.argb(
                            GEO_FENCE_FILL_ALPHA,
                            Color.red(strokeColor),
                            Color.green(strokeColor),
                            Color.blue(strokeColor)
                        )

                        return MapViewModel(
                            strokeColor,
                            fillColor,
                            handle
                        ) as T
                    }
                    LocationsAddViewModel::class.java ->
                        LocationsAddViewModel(
                            AuthMethodManagerFactory.getLocationsManager(),
                            Dispatchers.IO,
                            handle
                        ) as T
                    LocationsSettingsViewModel::class.java ->
                        LocationsSettingsViewModel(
                            AuthMethodManagerFactory.getLocationsManager(),
                            TimingCounter.DefaultTimeProvider(),
                            Dispatchers.IO,
                            handle
                        ) as T
                    UserLocationViewModel::class.java ->
                        UserLocationViewModel(
                            LocationTrackerFactory(requireContext()).locationTracker,
                            handle
                        ) as T
                    VerificationFlagViewModel::class.java ->
                        VerificationFlagViewModel(
                            when (key) {
                                VerificationFlagViewModel.LOCATIONS -> LocationsAsyncVerificationFlagManager(
                                    AuthMethodManagerFactory.getLocationsManager()
                                )
                                VerificationFlagViewModel.WEARABLES -> WearablesAsyncVerificationFlagManager(
                                    AuthMethodManagerFactory.getWearablesManager()
                                )
                                else -> throw IllegalArgumentException("Unkown auth method")
                            },
                            TimingCounter.DefaultTimeProvider(),
                            Dispatchers.IO,
                            handle
                        ) as T
                    WearablesAddViewModel::class.java ->
                        WearablesAddViewModel(
                            AuthMethodManagerFactory.getWearablesManager(),
                            Dispatchers.IO
                        ) as T
                    WearablesScanViewModel::class.java ->
                        WearablesScanViewModel(
                            AuthMethodManagerFactory.getWearablesManager(),
                            Dispatchers.IO,
                        ) as T
                    WearablesSettingsViewModel::class.java ->
                        WearablesSettingsViewModel(
                            AuthMethodManagerFactory.getWearablesManager(),
                            TimingCounter.DefaultTimeProvider(),
                            Dispatchers.IO,
                            handle
                        ) as T
                    TimerViewModel::class.java ->
                        TimerViewModel(
                            TimingCounter.DefaultTimeProvider(),
                            Dispatchers.IO,
                            handle
                        ) as T
                    else ->
                        super@BaseAppCompatFragment.getDefaultViewModelProviderFactory()
                            .create(modelClass)
                }
            }
        }
    }

    constructor() : super()
    constructor(@LayoutRes layoutId: Int) : super(layoutId)

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return viewModelProviderFactory
    }

    override fun startActivity(intent: Intent) {
        IntentUtils.addInternalVerification(intent)
        super.startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        UiUtils.themeStatusBar(activity)
    }

    /**
     * Method to potentially unmask a request code modified by v4 Support Fragment/Activity to
     * redirect result back to the right fragment after
     * super.startActivityForResult -> super.onActivityResult
     * Note: Check comment in startActivityForResult above.
     *
     *
     * Here's the reference: https://stackoverflow.com/questions/10564474/wrong-requestcode-in-onactivityresult
     *
     * @param originalRequestCode Potentially masked request code
     *
     * @return Potentially unmasked request code
     */
    protected fun getPossibleUnmaskedRequestCode(originalRequestCode: Int): Int {
        return originalRequestCode and 0x0000ffff
    }
}