package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentCircleCodeBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.AuthMethodActivity
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AutoUnlinkAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AutoUnlinkWarningAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.DialogFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.util.*
import com.launchkey.android.authenticator.sdk.ui.internal.util.setNavigationButton


class CircleCodeFragment : BaseAppCompatFragment(R.layout.fragment_circle_code) {
    private lateinit var toolbar: Toolbar
    private val binding by viewBinding(FragmentCircleCodeBinding::bind)
    private val circleCodeAddViewModel: CircleCodeAddViewModel by viewModels()
    private val circleCodeCheckViewModel: CircleCodeCheckViewModel by viewModels()
    private val unlinkDialogViewModel: DialogFragmentViewModel by lazy { ViewModelProvider(this).get(AutoUnlinkAlertDialogFragment::class.java.simpleName, DialogFragmentViewModel::class.java) }
    private val mode: AuthMethodActivity.Page by bundleArgument(AuthMethodActivity.PAGE_KEY)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()

        if (savedInstanceState == null) {
            showMode(mode)
        }
        
        subscribeObservers()
    }

    private fun setupToolbar() {
        toolbar = binding.circleCodeToolbar.root
        toolbar.setTitle(R.string.ioa_sec_cir_add_title)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
    }
    
    private fun subscribeObservers() {
        childFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                when (f) {
                    is CircleCodeAddFragment -> updateToolbar(AuthMethodActivity.Page.ADD)
                    is CircleCodeSettingsFragment -> updateToolbar(AuthMethodActivity.Page.SETTINGS)
                    is CircleCodeCheckFragment -> updateToolbar(AuthMethodActivity.Page.CHECK)
                }
            }
        }, false)
        
        circleCodeAddViewModel.state.observe(viewLifecycleOwner) { addState ->
            when (addState) {
                is CircleCodeAddViewModel.State.CircleCodeSetSuccess -> UiUtils.finishAddingFactorActivity(requireActivity())
            }
        }
        
        circleCodeCheckViewModel.requestState.observe(viewLifecycleOwner) { requestState ->
            when (requestState) {
                CircleCodeCheckViewModel.RequestState.ChangeRequested,
                CircleCodeCheckViewModel.RequestState.RemoveRequested -> showMode(AuthMethodActivity.Page.CHECK)
                CircleCodeCheckViewModel.RequestState.ChangeRequestSuccess -> childFragmentManager.popBackStack()
                CircleCodeCheckViewModel.RequestState.RemoveRequestSuccess -> requireActivity().finish()
                else -> Unit
            }
        }
        
        circleCodeCheckViewModel.unlinkState.observe(viewLifecycleOwner) { unlinkState ->
            when (unlinkState) {
                is CircleCodeCheckViewModel.UnlinkState.UnlinkWarningTriggered -> {
                    AutoUnlinkWarningAlertDialogFragment.show(
                            childFragmentManager,
                            requireActivity(),
                            unlinkState.attemptsRemaining)
                }
                is CircleCodeCheckViewModel.UnlinkState.UnlinkTriggered -> {
                    unlinkDialogViewModel.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
                }
            }
        }

        unlinkDialogViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                DialogFragmentViewModel.State.NeedsToBeShown -> {
                    AutoUnlinkAlertDialogFragment.show(
                            childFragmentManager,
                            requireActivity(),
                            AuthenticatorManager.instance.config.thresholdAutoUnlink()
                    )
                }
                DialogFragmentViewModel.State.Gone -> requireActivity().finish()
            }
        }
    }
    
    private fun updateToolbar(mode: AuthMethodActivity.Page) {
        when (mode) {
            AuthMethodActivity.Page.ADD -> {
                toolbar.setNavigationButton(UiUtils.NavButton.CANCEL)
                toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
                toolbar.setTitle(R.string.ioa_sec_cir_add_title)
            }
            AuthMethodActivity.Page.SETTINGS -> {
                toolbar.setNavigationButton(UiUtils.NavButton.BACK)
                toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
                toolbar.setTitle(R.string.ioa_sec_cir_sett_title)
            }
            AuthMethodActivity.Page.CHECK -> {
                toolbar.setNavigationButton(UiUtils.NavButton.CANCEL)
                toolbar.setNavigationOnClickListener { childFragmentManager.popBackStack() }
                toolbar.setTitle(R.string.ioa_sec_cir_check_title)
            }
        }
    }
    
    private fun showMode(mode: AuthMethodActivity.Page) {
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace(binding.circleCodeFragmentContainer.id, when (mode) {
                AuthMethodActivity.Page.ADD -> CircleCodeAddFragment::class.java
                AuthMethodActivity.Page.SETTINGS -> CircleCodeSettingsFragment::class.java
                AuthMethodActivity.Page.CHECK -> CircleCodeCheckFragment::class.java.also {
                    addToBackStack(null)
                }
            }, null)
        }
    }
}
