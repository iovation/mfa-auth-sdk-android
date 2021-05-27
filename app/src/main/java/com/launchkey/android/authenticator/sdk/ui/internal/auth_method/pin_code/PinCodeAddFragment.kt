/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.launchkey.android.authenticator.sdk.core.auth_method_management.PINCodeManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.AuthMethodAlreadySetException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.pin_code.PINCodeTooLongException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.pin_code.PINCodeTooShortException
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentPincodeAddBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinEmpty
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinInvalid
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinSetFailed
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinSetSuccess
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeAddViewModel.PinCodeState.PinValid
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.HelpDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class PinCodeAddFragment : BaseAppCompatFragment(R.layout.fragment_pincode_add),
    PinCodeView.Listener, CompoundButton.OnCheckedChangeListener {

    private lateinit var checkSwitch: SwitchCompat
    private lateinit var verifyWhen: TextView
    private lateinit var requirementsListView: RecyclerView
    private lateinit var requirementAdapter: PinCodeRequirementAdapter

    private val pinCodeAddViewModel: PinCodeAddViewModel by viewModels({ requireParentFragment() })
    private val binding by viewBinding(FragmentPincodeAddBinding::bind)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        setupVerificationHeaders()
        subscribeObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        UiUtils.applyThemeToMenu(menuInflater, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_help) {
            HelpDialogFragment.show(
                childFragmentManager,
                requireActivity(),
                getString(R.string.ioa_sec_pin_help_title),
                getString(R.string.ioa_sec_pin_help_message)
            )
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        pinCodeAddViewModel.setVerificationFlagState(if (isChecked) VerificationFlag.State.ALWAYS else VerificationFlag.State.WHEN_REQUIRED)
    }

    override fun onPinSet(pinCode: String) {
        pinCodeAddViewModel.setPinCode(pinCode)
    }

    override fun onPinChange(pinCode: String) {
        pinCodeAddViewModel.validatePin(pinCode)
    }

    private fun updatePINCodeRequirementsView(requirementsNotMet: List<PINCodeRequirement>) {
        requirementAdapter.currentList.forEachIndexed { i, pinCodeRequirement ->
            requirementsListView.findViewHolderForAdapterPosition(i)?.let { viewHolder ->
                with(viewHolder as PinCodeRequirementAdapter.ViewHolder) {
                    setRequirementMet(pinCodeRequirement !in requirementsNotMet)
                }
            }
        }
    }

    private fun setupVerificationHeaders() {
        val headerViews = UiUtils.prepKbaAddCheckHeader(
            binding.root,
            R.id.pincode_add_header,
            true,
            true,
            R.id.pincode_add_pinpad,
            resources,
            pinCodeAddViewModel.pinCodeRequirements
        )

        verifyWhen = headerViews[0] as TextView
        val code = headerViews[3] as TextView
        binding.pincodeAddPinpad.viewer = code
        binding.pincodeAddPinpad.showCheckButton()
        binding.pincodeAddPinpad.listener = this
        checkSwitch = headerViews[1] as SwitchCompat
        checkSwitch.setOnCheckedChangeListener(this)
        val instructions = headerViews[2] as TextView
        instructions.setText(R.string.ioa_sec_pin_check_enter)
        requirementsListView = headerViews[4] as RecyclerView
        requirementAdapter = requirementsListView.adapter!! as PinCodeRequirementAdapter
    }

    private fun setupToolbar() {
        val toolbar = binding.pincodeAddToolbar.root
        toolbar.setTitle(R.string.ioa_sec_pin_add_title)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        UiUtils.updateToolbarIcon(toolbar, UiUtils.NavButton.CANCEL)
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
    }

    private fun subscribeObservers() {
        pinCodeAddViewModel.verificationFlagState.observe(viewLifecycleOwner, { state ->
            checkSwitch.isChecked = state == VerificationFlag.State.ALWAYS
            verifyWhen.setText(
                if (checkSwitch.isChecked) R.string.ioa_sec_panel_verify_always
                else R.string.ioa_sec_panel_verify_whenrequired
            )
        })

        pinCodeAddViewModel.pinCodeState.observe(viewLifecycleOwner, { pinCodeState ->
            when (pinCodeState) {
                is PinValid -> {
                    binding.pincodeAddPinpad.animateCheckButton(true)
                    updatePINCodeRequirementsView(emptyList())
                }
                is PinInvalid -> {
                    binding.pincodeAddPinpad.animateCheckButton(false)
                    updatePINCodeRequirementsView(pinCodeState.requirementsNotMet)
                }
                is PinSetSuccess -> {
                    UiUtils.finishAddingFactorActivity(requireActivity())
                }
                is PinSetFailed -> {
                    val exception = pinCodeState.exception
                    handlePinSetFailure(exception)
                }
                is PinEmpty -> {
                    binding.pincodeAddPinpad.animateCheckButton(false)
                    updatePINCodeRequirementsView(pinCodeAddViewModel.pinCodeRequirements)
                }
            }
        })
    }

    private fun handlePinSetFailure(exception: Exception) {
        when (exception) {
            is AuthMethodAlreadySetException -> {
                UiUtils.toast(requireActivity(), R.string.ioa_sec_pin_add_error_alreadyset, false)
                requireActivity().finish()
            }
            is PINCodeTooLongException -> {
                UiUtils.toast(
                        requireActivity(),
                    getString(
                        R.string.ioa_sec_pin_add_length_too_long_format,
                        PINCodeManager.PIN_CODE_MAX.toString()
                    ),
                    false
                )
            }
            is PINCodeTooShortException -> {
                UiUtils.toast(
                        requireActivity(),
                    getString(
                        R.string.ioa_sec_pin_add_length_too_short_format,
                        PINCodeManager.PIN_CODE_MIN.toString()
                    ),
                    false
                )
            }
        }
    }
}