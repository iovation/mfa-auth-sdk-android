package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentCircleCodeSettingsBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class CircleCodeSettingsFragment : BaseAppCompatFragment(R.layout.fragment_circle_code_settings) {
    private val circleCodeCheckViewModel: CircleCodeCheckViewModel by viewModels({ requireParentFragment() })
    private val binding by viewBinding(FragmentCircleCodeSettingsBinding::bind)
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPanel()
        subscribeObservers()
    }
    
    private fun setupPanel() {
        with(binding.panelCircleCodeSettings) {
            setRemoveButtonText(R.string.ioa_sec_panel_remove_single)
            setOnRemoveButtonClick { circleCodeCheckViewModel.requestRemoveCircleCode() }
            disallowSwitchSwipe()
            setOnSwitchClickedListener {
                isSwitchOn = !isSwitchOn
                circleCodeCheckViewModel.requestToggleVerificationFlag()
            }
        }
    }
    
    private fun subscribeObservers() {
        circleCodeCheckViewModel.verificationFlagState.observe(viewLifecycleOwner) { verifyState ->
            binding.panelCircleCodeSettings.isSwitchOn = verifyState == VerificationFlag.State.ALWAYS
            binding.panelCircleCodeSettings.setVerifiedWhenText(UiUtils.getStringResFromUserSetState(verifyState))
        }
    }
}