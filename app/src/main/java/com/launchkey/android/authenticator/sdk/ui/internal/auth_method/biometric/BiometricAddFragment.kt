/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.biometric

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentBiometricAddBinding
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.DialogFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class BiometricAddFragment : BaseAppCompatFragment(R.layout.fragment_biometric_add) {
    private var scanDialog: BiometricScanDialogFragment? = null
    private val binding by viewBinding(FragmentBiometricAddBinding::bind)
    private val biometricScanDialogViewModel: DialogFragmentViewModel by lazy {
        ViewModelProvider(
            this
        ).get(
            BiometricScanDialogFragment::class.java.simpleName,
            DialogFragmentViewModel::class.java
        )
    }
    private val biometricAddViewModel: BiometricAddViewModel by viewModels({ requireParentFragment() })
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fingerprintAddButtonStartscan.setOnClickListener { addBiometric() }
        binding.fingerprintAddSwitch.setOnCheckedChangeListener { _, isChecked ->
            biometricAddViewModel.setVerificationFlagState(
                if (isChecked) VerificationFlag.State.ALWAYS
                else VerificationFlag.State.WHEN_REQUIRED
            )
        }
        subscribeObservers()
    }
    
    private fun addBiometric() {
        if (biometricAddViewModel.needsUiToCancel()) {
            biometricScanDialogViewModel.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
        }
        
        biometricAddViewModel.scanBiometric()
    }
    
    private fun subscribeObservers() {
        biometricAddViewModel.verificationFlagState.observe(viewLifecycleOwner) { state ->
            binding.fingerprintAddSwitch.isChecked = state == VerificationFlag.State.ALWAYS
            binding.fingerprintAddTextVerifywhen.setText(
                if (binding.fingerprintAddSwitch.isChecked) R.string.ioa_sec_panel_verify_always
                else R.string.ioa_sec_panel_verify_whenrequired
            )
        }
        
        biometricAddViewModel.biometricState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BiometricAddViewModel.BiometricState.Failed ->
                    scanDialog?.displayFailureWarning(state.failure)
                BiometricAddViewModel.BiometricState.Set -> scanDialog?.dismiss()
                BiometricAddViewModel.BiometricState.Scanning -> Unit
            }
        }
        
        biometricScanDialogViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                DialogFragmentViewModel.State.NeedsToBeShown -> {
                    scanDialog = BiometricScanDialogFragment.show(
                        childFragmentManager,
                        requireContext(),
                        getString(R.string.ioa_sec_fs_begin_scan_message)
                    )
                    biometricScanDialogViewModel.changeState(DialogFragmentViewModel.State.Shown)
                }
                DialogFragmentViewModel.State.Gone -> {
                    biometricAddViewModel.cancelScan()
                }
            }
        }
    }
}