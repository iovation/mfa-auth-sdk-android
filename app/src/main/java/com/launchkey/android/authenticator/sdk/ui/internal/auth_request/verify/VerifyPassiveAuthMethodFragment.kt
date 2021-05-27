package com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentAuthRequestVerifyPassiveBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.bundleArgument
import com.launchkey.android.authenticator.sdk.ui.internal.util.showShortToast
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class VerifyPassiveAuthMethodFragment : BaseAppCompatFragment(R.layout.fragment_auth_request_verify_passive) {
    private lateinit var checkingSpinnerAnimation: Animation
    private val binding by viewBinding(FragmentAuthRequestVerifyPassiveBinding::bind)
    private val verificationViewModel: AuthRequestVerificationViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val passiveAuthMethod: AuthMethod by bundleArgument(ARG_AUTH_METHOD)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkingSpinnerAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.infinite_rotation)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyThemeToBusyItem(binding.toincludeLaunchWaitingImage, passiveAuthMethod)
    }

    private fun applyThemeToBusyItem(image: ImageView, securityItemType: AuthMethod) {
        val busyItemUiProp = AuthenticatorUIManager.instance.config.themeObj().methodsBusyIcons
        when (securityItemType) {
            AuthMethod.LOCATIONS, AuthMethod.GEOFENCING -> if (busyItemUiProp.resProvided) {
                image.setImageResource(busyItemUiProp.iconGeofencingRes)
            } else if (busyItemUiProp.iconGeofencing != null) {
                image.setImageDrawable(busyItemUiProp.iconGeofencing)
            }
            AuthMethod.WEARABLES -> if (busyItemUiProp.resProvided) {
                image.setImageResource(busyItemUiProp.iconWearableRes)
            } else if (busyItemUiProp.iconWearable != null) {
                image.setImageDrawable(busyItemUiProp.iconWearable)
            }
            AuthMethod.BIOMETRIC -> if (busyItemUiProp.resProvided) {
                image.setImageResource(busyItemUiProp.iconFingerprintScanRes)
            } else if (busyItemUiProp.iconFingerprintScan != null) {
                image.setImageDrawable(busyItemUiProp.iconFingerprintScan)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        binding.toincludeLaunchWaitingCustomspinner.startAnimation(checkingSpinnerAnimation)
        verifyPassiveAuthMethod()
    }
    
    override fun onPause() {
        binding.toincludeLaunchWaitingCustomspinner.clearAnimation()
        verificationViewModel.stopVerifyingPassiveAuthMethods()
        super.onPause()
    }
    
    private fun verifyPassiveAuthMethod() {
        when(passiveAuthMethod) {
            AuthMethod.LOCATIONS -> verificationViewModel.verifyLocations(null)
            AuthMethod.GEOFENCING -> verificationViewModel.verifyGeofences(null)
            AuthMethod.WEARABLES -> verificationViewModel.verifyWearables(null)
            AuthMethod.BIOMETRIC -> verificationViewModel.verifyBiometric(object : AuthMethodAuthRequestVerificationCallback {
                // handled in ViewModel
                override fun onVerificationSuccess(authRequestWasSent: Boolean) {}
    
                override fun onVerificationFailure(authRequestWasSent: Boolean, failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                    context?.showShortToast(UiUtils.getBiometricSensorErrorMessage(failure))
                }
    
            })
            else -> throw IllegalArgumentException("The AuthMethod " + passiveAuthMethod.name + " is not a passive AuthMethod.")
        }
    }
    
    companion object {
        const val ARG_AUTH_METHOD = "auth method"
    }
}