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
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentBiometricSettingsBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.SettingsPanel
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.DialogFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class BiometricCheckFragment :
    BaseAppCompatFragment(R.layout.fragment_biometric_settings) {
    private lateinit var panel: SettingsPanel
    private val scanDialog: BiometricScanDialogFragment?
        get() {
            return childFragmentManager.findFragmentByTag(BiometricScanDialogFragment::class.java.simpleName) as? BiometricScanDialogFragment
        }
    private val binding by viewBinding(FragmentBiometricSettingsBinding::bind)
    private val biometricScanDialogViewModel: DialogFragmentViewModel by lazy { ViewModelProvider(this).get(BiometricScanDialogFragment::class.java.simpleName, DialogFragmentViewModel::class.java) }
    private val biometricCheckViewModel: BiometricCheckViewModel by viewModels({ requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        panel = binding.fingerprintSettingsPanel.apply {
            setRemoveButtonText(R.string.ioa_sec_fs_remove_button)
            setOnRemoveButtonClick { removeBiometric() }
            setOnSwitchClickedListener {
                isSwitchOn = !isSwitchOn
                toggleVerificationFlag()
            }
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        biometricCheckViewModel.biometricState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BiometricCheckViewModel.BiometricState.Failed ->
                    scanDialog?.displayFailureWarning(state.failure)
                BiometricCheckViewModel.BiometricState.Toggled,
                BiometricCheckViewModel.BiometricState.Removed -> scanDialog?.dismiss()
                BiometricCheckViewModel.BiometricState.ScanningRemove -> Unit
                BiometricCheckViewModel.BiometricState.ScanningToggle -> Unit
            }
        }

        biometricCheckViewModel.verificationFlagState.observe(viewLifecycleOwner) { flagState ->
            panel.isSwitchOn = flagState == VerificationFlag.State.ALWAYS
            panel.setVerifiedWhenText(UiUtils.getStringResFromUserSetState(flagState))
        }

        biometricScanDialogViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                DialogFragmentViewModel.State.NeedsToBeShown -> {
                    BiometricScanDialogFragment.show(childFragmentManager,
                            requireContext(),
                            getString(R.string.ioa_sec_fs_begin_scan_message))
                    biometricScanDialogViewModel.changeState(DialogFragmentViewModel.State.Shown)
                }
                DialogFragmentViewModel.State.Gone -> {
                    biometricCheckViewModel.cancelScan()
                }
            }
        }
    }

    private fun removeBiometric() {
        if (biometricCheckViewModel.needsUiToCancel()) {
            biometricScanDialogViewModel.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
        }

        biometricCheckViewModel.scanBiometric(true)
    }

    private fun toggleVerificationFlag() {
        if (biometricCheckViewModel.needsUiToCancel()) {
            biometricScanDialogViewModel.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
        }

        biometricCheckViewModel.scanBiometric(false)
    }
}