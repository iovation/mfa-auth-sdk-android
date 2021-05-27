package com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.callback.AuthMethodAuthRequestVerificationCallback
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentAuthRequestVerifyPinBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeView
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.makeVisible
import com.launchkey.android.authenticator.sdk.ui.internal.util.setTextTemporarily

class VerifyPinCodeFragment : BaseAppCompatFragment(R.layout.fragment_auth_request_verify_pin) {
    private val verificationViewModel: AuthRequestVerificationViewModel
        by viewModels(ownerProducer = { requireParentFragment() })
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAuthRequestVerifyPinBinding.bind(view)
        val pinCodeViewer = binding.pincodeView.viewAuthMethodPincodeViewer
        
        with(binding.pincodeView.viewAuthMethodPincodeWidget) {
            viewer = pinCodeViewer
            setObscureInput(obscureInput = true, includingLast = true)
            showCheckButton()
            
            listener = object : PinCodeView.Listener {
                override fun onPinSet(pinCode: String) {
                    ignoreTouches(true)
                    verificationViewModel.verifyPinCode(pinCode, object : AuthMethodAuthRequestVerificationCallback {
                        // handled in viewmodel
                        override fun onVerificationSuccess(authRequestWasSent: Boolean) {}
    
                        override fun onVerificationFailure(authRequestWasSent: Boolean, failure: AuthMethodFailure, unlinkTriggered: Boolean, unlinkWarningTriggered: Boolean, attemptsRemaining: Int?) {
                            lifecycleScope.launchWhenResumed {
                                if (!authRequestWasSent) {
                                    ignoreTouches(false)
                                }
            
                                clear()
                                startPinCodeFailedAnimation()
            
                                with(binding.pincodeView.viewAuthMethodPincodeViewer) {
                                    makeVisible()
                                    text = ""
                                    setTextTemporarily(
                                        lifecycle,
                                        getString(R.string.ioa_sec_pin_check_error_wrong),
                                        Constants.BRIEF_INSTRUCTIONS_DURATION_MILLI)
                                }
                            }
                        }
                    })
                }
                
                override fun onPinChange(pinCode: String) {
                    animateCheckButton(pinCode.isNotEmpty())
                }
            }
        }
    }
}