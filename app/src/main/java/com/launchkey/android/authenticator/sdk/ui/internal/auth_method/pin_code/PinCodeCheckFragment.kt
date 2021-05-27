/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentPincodeCheckBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeCheckViewModel.RequestState
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class PinCodeCheckFragment :
    BaseAppCompatFragment(R.layout.fragment_pincode_check),
    PinCodeView.Listener {
    private lateinit var instructions: TextView
    private val binding by viewBinding(FragmentPincodeCheckBinding::bind)
    private val pinCodeCheckViewModel: PinCodeCheckViewModel by viewModels({ requireParentFragment().requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVerificationHeaders()
        subscribeObservers()
    }

    override fun onPinSet(pinCode: String) {
        pinCodeCheckViewModel.verifyPinCode(pinCode)
    }

    override fun onPinChange(pinCode: String) {
        binding.pincodeCheckPinpad.animateCheckButton(pinCode.isNotEmpty())
    }

    private fun setupVerificationHeaders() {
        val headerViews = UiUtils.prepKbaAddCheckHeader(
            binding.root,
            R.id.pincode_check_header,
            false,
            true,
            R.id.pincode_check_pinpad,
            resources,
            emptyList()
        )

        instructions = headerViews[2] as TextView
        instructions.setText(
            when (pinCodeCheckViewModel.requestState.value) {
                is RequestState.ChangeRequested, is RequestState.ChangeRequestFailed -> R.string.ioa_sec_pin_check_toverify
                else -> R.string.ioa_sec_pin_check_toremove
            }
        )

        val code = headerViews[3] as TextView
        binding.pincodeCheckPinpad.showCheckButton()
        binding.pincodeCheckPinpad.setObscureInput(obscureInput = true, includingLast = false)
        binding.pincodeCheckPinpad.viewer = code
        binding.pincodeCheckPinpad.listener = this
    }

    private fun subscribeObservers() {
        pinCodeCheckViewModel.requestState.observe(viewLifecycleOwner, { requestState ->
            when (requestState) {
                is RequestState.ChangeRequested -> {
                    instructions.setText(R.string.ioa_sec_pin_check_toverify)
                }
                is RequestState.RemoveRequested -> {
                    instructions.setText(R.string.ioa_sec_pin_check_toremove)
                }
                is RequestState.ChangeRequestFailed,
                is RequestState.RemoveRequestFailed -> {
                    binding.pincodeCheckPinpad.clear()
                    binding.pincodeCheckPinpad.startPinCodeFailedAnimation()
                    UiUtils.setTextTemporarily(
                        viewLifecycleOwner,
                        Handler(Looper.getMainLooper()),
                        instructions,
                        getString(R.string.ioa_sec_pin_check_error_wrong),
                        Constants.BRIEF_INSTRUCTIONS_DURATION_MILLI
                    )
                }
                else -> Unit
            }
        })
    }
}