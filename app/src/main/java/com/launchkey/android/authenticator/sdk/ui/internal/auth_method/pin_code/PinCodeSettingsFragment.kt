/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentPincodeSettingsBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.SettingsPanel
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AutoUnlinkAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AutoUnlinkWarningAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.DialogFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.DialogFragmentViewModel.State.NeedsToBeShown
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class PinCodeSettingsFragment :
    BaseAppCompatFragment(R.layout.fragment_pincode_settings) {
    private lateinit var panel: SettingsPanel
    private lateinit var toolbar: Toolbar

    private val pinCodeCheckViewModel: PinCodeCheckViewModel by viewModels({ requireParentFragment() })
    private val unlinkDialogViewModel: DialogFragmentViewModel by lazy { ViewModelProvider(this).get(AutoUnlinkAlertDialogFragment::class.java.simpleName, DialogFragmentViewModel::class.java) }
    private val binding by viewBinding(FragmentPincodeSettingsBinding::bind)

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                updateToolbar(false)
                childFragmentManager.popBackStack(
                        PinCodeCheckFragment::class.java.simpleName,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                isEnabled = false
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        setupPanel()
        subscribeObservers()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
        onBackPressedCallback.isEnabled = false
    }

    private fun setupToolbar() {
        toolbar = binding.pincodeSettingsToolbar.root
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        updateToolbar(false)
    }

    private fun setupPanel() {
        panel = binding.pincodeSettingsPanel.apply {
            setRemoveButtonText(R.string.ioa_sec_panel_remove_single)
            setOnRemoveButtonClick { pinCodeCheckViewModel.requestRemovePinCode() }
            setOnSwitchClickedListener {
                isSwitchOn = !isSwitchOn
                pinCodeCheckViewModel.requestChangeVerificationFlag()
            }
        }
    }

    private fun subscribeObservers() {
        pinCodeCheckViewModel.verificationFlagState.observe(viewLifecycleOwner, { state ->
            updateVerificationSwitch(state)
        })

        pinCodeCheckViewModel.requestState.observe(viewLifecycleOwner) {
            when (it) {
                is PinCodeCheckViewModel.RequestState.ChangeRequested,
                is PinCodeCheckViewModel.RequestState.RemoveRequested -> {
                    showCheckFragment()
                }
                is PinCodeCheckViewModel.RequestState.ChangeRequestSuccess -> {
                    onBackPressedCallback.handleOnBackPressed()
                    panel.isSwitchOn = !panel.isSwitchOn
                }
                is PinCodeCheckViewModel.RequestState.RemoveRequestSuccess -> {
                    requireActivity().finish()
                }
                else -> Unit
            }
        }

        pinCodeCheckViewModel.unlinkState.observe(viewLifecycleOwner) {
            when (it) {
                is PinCodeCheckViewModel.UnlinkState.UnlinkWarningTriggered -> {
                    AutoUnlinkWarningAlertDialogFragment.show(
                            childFragmentManager,
                            requireActivity(),
                            it.attemptsRemaining
                    )
                }
                is PinCodeCheckViewModel.UnlinkState.UnlinkTriggered -> {
                    unlinkDialogViewModel.changeState(NeedsToBeShown)
                }
            }
        }

        unlinkDialogViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is NeedsToBeShown -> {
                    AutoUnlinkAlertDialogFragment.show(childFragmentManager,
                            requireActivity(),
                            AuthenticatorManager.instance.config.thresholdAutoUnlink())
                    unlinkDialogViewModel.changeState(DialogFragmentViewModel.State.Shown)
                }
                is DialogFragmentViewModel.State.Gone -> {
                    requireActivity().finish()
                }
            }
        }
    }

    private fun updateVerificationSwitch(state: VerificationFlag.State) {
        panel.isSwitchOn = state == VerificationFlag.State.ALWAYS
        panel.setVerifiedWhenText(UiUtils.getStringResFromUserSetState(state))
    }

    private fun updateToolbar(checkingPinCode: Boolean) {
        UiUtils.updateToolbarIcon(
                binding.pincodeSettingsToolbar.root,
                if (checkingPinCode) UiUtils.NavButton.CANCEL
                else UiUtils.NavButton.BACK
        )

        toolbar.setNavigationOnClickListener {
            if (checkingPinCode) {
                onBackPressedCallback.handleOnBackPressed()
            } else {
                requireActivity().onBackPressed()
            }
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(
                if (checkingPinCode) R.string.ioa_sec_pin_check_title
                else R.string.ioa_sec_pin_sett_title
        )
    }

    private fun showCheckFragment() {
        onBackPressedCallback.isEnabled = true
        updateToolbar(true)
        childFragmentManager.beginTransaction()
            .replace(binding.checkPinCodeFragment.id, PinCodeCheckFragment())
            .addToBackStack(PinCodeCheckFragment::class.java.simpleName)
            .commit()

        // Makes the change to toolbar and fragment seem like one action instead of two
        childFragmentManager.executePendingTransactions()
    }
}