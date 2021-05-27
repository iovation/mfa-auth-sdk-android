package com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentAuthRequestVerifyStepBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify.AuthRequestVerificationViewModel.VerificationState.*
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AutoUnlinkAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AutoUnlinkWarningAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.ExpirationTimerTracker
import com.launchkey.android.authenticator.sdk.ui.internal.util.ExpirationTimerTracker.State.Expired
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class AuthRequestVerifyStepFragment : BaseAppCompatFragment(R.layout.fragment_auth_request_verify_step) {
    private val binding by viewBinding(FragmentAuthRequestVerifyStepBinding::bind)
    private val authRequestFragmentViewModel: AuthRequestFragmentViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val verificationViewModel: AuthRequestVerificationViewModel by viewModels()
    private lateinit var expirationTimerTracker: ExpirationTimerTracker
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authRequest = authRequestFragmentViewModel.currentAuthRequest!!
        verificationViewModel.setAuthRequestToBeVerified(authRequest)
        val serProfile = authRequest.serviceProfile
        val title = authRequest.title
        val finalTitle = if (title.isNotEmpty()) title else (if (serProfile.name.isEmpty()) null else serProfile.name)!!
        binding.authStepLabelTitle.text = finalTitle
        
        binding.authStepActionNegative.setTextTop(R.string.ioa_ar_arb_directive_hold)
        binding.authStepActionNegative.setTextMain(R.string.ioa_ar_arb_action_deny)
        binding.authStepActionNegative.setOnClickListener { authRequestFragmentViewModel.denyAuthRequest() }
        setupExpirationTimer(authRequest.createdAtMillis, authRequest.expiresAtMillis)
        subscribeObservers()
    }
    
    private fun setupExpirationTimer(startTime: Long, endTime: Long) {
        expirationTimerTracker = ExpirationTimerTracker(
            System.currentTimeMillis(),
            startTime,
            endTime,
            TimingCounter.DefaultTimeProvider(),
            Handler(Looper.getMainLooper()),
            viewLifecycleOwner)
    
        expirationTimerTracker.state.observe(viewLifecycleOwner, { state ->
            when (state) {
                is ExpirationTimerTracker.State.Update -> binding.authStepTimer.setProgress(state.remainingMillis, state.progress)
                is Expired -> authRequestFragmentViewModel.setAuthRequestExpired()
            }
        })
    }
    
    private fun subscribeObservers() {
        verificationViewModel.verificationState.observe(viewLifecycleOwner, { verificationState ->
            when (verificationState) {
                is VerifyingAuthMethod -> verifyAuthMethod(verificationState.authMethod)
                is VerifiedAllAuthMethods -> authRequestFragmentViewModel.sendAuthRequest()
                is AutoFailed -> verificationViewModel.stopVerifyingPassiveAuthMethods()
                is UnlinkTriggered -> AutoUnlinkAlertDialogFragment.show(parentFragmentManager, requireParentFragment().requireContext(), verificationState.unlinkThreshold)
                is UnlinkWarningTriggered -> AutoUnlinkWarningAlertDialogFragment.show(parentFragmentManager, requireParentFragment().requireContext(), verificationState.attemptsRemaining)
                else -> Unit
            }
        })
        
        verificationViewModel.currentStep.observe(viewLifecycleOwner, { currentStep ->
            binding.authStepLabelProgressNums.text = resources.getString(R.string.ioa_ar_step_num_format, currentStep, verificationViewModel.amountToVerify())
        })
    }
    
    private fun verifyAuthMethod(authMethod: AuthMethod) {
        childFragmentManager.commit {
            val methodResourceId: Int
            val fragmentClass: Class<out Fragment>
            val args = Bundle()
            when (authMethod) {
                AuthMethod.PIN_CODE -> {
                    methodResourceId = R.string.ioa_ar_step_pin
                    fragmentClass = VerifyPinCodeFragment::class.java
                }
                AuthMethod.CIRCLE_CODE -> {
                    methodResourceId = R.string.ioa_ar_step_cir
                    fragmentClass = VerifyCircleCodeFragment::class.java
                }
                AuthMethod.GEOFENCING -> {
                    methodResourceId = R.string.ioa_ar_step_geo
                    fragmentClass = VerifyPassiveAuthMethodFragment::class.java
                    args.putSerializable(VerifyPassiveAuthMethodFragment.ARG_AUTH_METHOD, authMethod)
                }
                AuthMethod.LOCATIONS -> {
                    methodResourceId = R.string.ioa_ar_step_geo
                    fragmentClass = VerifyPassiveAuthMethodFragment::class.java
                    args.putSerializable(VerifyPassiveAuthMethodFragment.ARG_AUTH_METHOD, authMethod)
                }
                AuthMethod.BIOMETRIC -> {
                    methodResourceId = R.string.ioa_ar_step_fp
                    fragmentClass = VerifyPassiveAuthMethodFragment::class.java
                    args.putSerializable(VerifyPassiveAuthMethodFragment.ARG_AUTH_METHOD, authMethod)
                }
                AuthMethod.WEARABLES -> {
                    methodResourceId = R.string.ioa_ar_step_bp
                    fragmentClass = VerifyPassiveAuthMethodFragment::class.java
                    args.putSerializable(VerifyPassiveAuthMethodFragment.ARG_AUTH_METHOD, authMethod)
                }
            }
            
            binding.authStepLabelProgressMethod.text = getString(methodResourceId)
            val fragmentTag = authMethod.toString()
            if (childFragmentManager.findFragmentByTag(fragmentTag) == null) {
                setCustomAnimations(ANIMATION_IN_RES, ANIMATION_OUT_RES)
                replace(binding.authStepFrame.id, fragmentClass, args, fragmentTag)
            }
        }
    }
    
    companion object {
        private val ANIMATION_IN_RES = R.anim.alpha_in
        private val ANIMATION_OUT_RES = R.anim.alpha_out
    }
}