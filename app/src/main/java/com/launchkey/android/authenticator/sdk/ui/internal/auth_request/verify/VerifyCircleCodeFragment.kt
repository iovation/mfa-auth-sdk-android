package com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentAuthRequestVerifyCircleBinding
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.setTextTemporarily

class VerifyCircleCodeFragment : BaseAppCompatFragment(R.layout.fragment_auth_request_verify_circle) {
    private val verificationViewModel: AuthRequestVerificationViewModel
        by viewModels(ownerProducer = { requireParentFragment() })
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAuthRequestVerifyCircleBinding.bind(view)
        val circleCodeWidget = binding.circleCodeView.viewAuthMethodCirclecodeWidget
        val circleCodeViewer = binding.circleCodeView.viewAuthMethodCirclecodeViewer
        
        circleCodeWidget.setListener { circleCode ->
            circleCodeWidget.ignoreTouches(true)
            verificationViewModel.verifyCircleCode(circleCode, object : AuthMethodAuthRequestVerificationCallback {
                // handled in viewmodel
                override fun onVerificationSuccess(authRequestWasSent: Boolean) {}
    
                override fun onVerificationFailure(authRequestWasSent: Boolean, failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                    lifecycleScope.launchWhenResumed {
                        if (!authRequestWasSent) {
                            circleCodeWidget.ignoreTouches(false)
                        }
            
                        circleCodeViewer.setTextTemporarily(
                            lifecycle,
                            getString(R.string.ioa_sec_cir_check_error_wrong),
                            Constants.BRIEF_INSTRUCTIONS_DURATION_MILLI)
                    }
                }
            })
        }
    }
}