package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.AuthMethodAlreadySetException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.circle_code.CircleCodeTooLongException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.circle_code.CircleCodeTooShortException
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentCircleCodeAddCheckBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code.CircleCodeAddViewModel.State.CircleCodeAwaitingVerification
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code.CircleCodeAddViewModel.State.CircleCodeSetFailed
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.HelpDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.setTextTemporarily
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class CircleCodeAddFragment : BaseAppCompatFragment(R.layout.fragment_circle_code_add_check) {
    private val binding by viewBinding(FragmentCircleCodeAddCheckBinding::bind)
    private val circleCodeAddViewModel: CircleCodeAddViewModel by viewModels({ requireParentFragment() })
    private lateinit var instructions: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        UiUtils.applyThemeToMenu(inflater, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                HelpDialogFragment.show(
                    childFragmentManager,
                    requireContext(),
                    getString(R.string.ioa_sec_cir_help_title),
                    getString(R.string.ioa_sec_cir_help_message))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPanel()
        subscribeObservers()
    }
    
    private fun setupPanel() {
        val headerViews = UiUtils.prepKbaAddCheckHeader(
            binding.root,
            R.id.panel_header,
            true,
            false,
            R.id.circle_pad,
            resources,
            emptyList())
        
        val verifyWhen = headerViews[0] as TextView
        verifyWhen.setText(R.string.ioa_sec_panel_verify_always)
        
        instructions = headerViews[2] as TextView
        instructions.setText(R.string.ioa_sec_cir_check_enter)
        
        val stateSwitch = headerViews[1] as SwitchCompat
        stateSwitch.setOnCheckedChangeListener { _, isChecked ->
            verifyWhen.setText(if (isChecked) R.string.ioa_sec_panel_verify_always else R.string.ioa_sec_panel_verify_whenrequired)
        }
        
        binding.circlePad.setListener { circleCode ->
            circleCodeAddViewModel.addCircleCode(circleCode,
                if (stateSwitch.isChecked) VerificationFlag.State.ALWAYS
                else VerificationFlag.State.WHEN_REQUIRED)
        }
    }
    
    private fun subscribeObservers() {
        circleCodeAddViewModel.state.observe(viewLifecycleOwner, { state ->
            when (state) {
                is CircleCodeAwaitingVerification -> {
                    instructions.setText(R.string.ioa_sec_cir_add_repeat)
                }
                is CircleCodeSetFailed -> {
                    instructions.setText(R.string.ioa_sec_cir_check_enter)
                    instructions.setTextTemporarily(
                        viewLifecycleOwner.lifecycle,
                        getString(
                            when (state.exception) {
                                is CircleCodesDoNotMatchException -> R.string.ioa_sec_cir_add_error_mismatch
                                is AuthMethodAlreadySetException -> R.string.ioa_sec_cir_add_error_alreadyset
                                is CircleCodeTooShortException -> R.string.ioa_sec_cir_add_error_too_short
                                is CircleCodeTooLongException -> R.string.ioa_sec_cir_add_error_too_long
                                else -> throw state.exception
                            }),
                        Constants.BRIEF_INSTRUCTIONS_DURATION_MILLI)
            
                }
                else -> Unit // handled by activity
            }
        })
    }
}