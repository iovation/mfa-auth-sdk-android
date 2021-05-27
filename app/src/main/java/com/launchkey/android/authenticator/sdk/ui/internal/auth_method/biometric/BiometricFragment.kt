package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.biometric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentBiometricBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.AuthMethodActivity
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.*
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.bundleArgument
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding
import java.lang.IllegalArgumentException

class BiometricFragment : BaseAppCompatFragment(R.layout.fragment_biometric) {
    private val binding by viewBinding(FragmentBiometricBinding::bind)
    private val biometricAddViewModel: BiometricAddViewModel by viewModels()
    private val biometricCheckViewModel: BiometricCheckViewModel by viewModels()
    private val unlinkDialogViewModel: DialogFragmentViewModel by lazy { ViewModelProvider(this).get(AutoUnlinkAlertDialogFragment::class.java.simpleName, DialogFragmentViewModel::class.java) }
    private val page: AuthMethodActivity.Page by bundleArgument(AuthMethodActivity.PAGE_KEY)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace(binding.fragmentContainer.id, when (page) {
                    AuthMethodActivity.Page.ADD -> BiometricAddFragment()
                    AuthMethodActivity.Page.SETTINGS -> BiometricCheckFragment()
                    else -> throw IllegalArgumentException("Unkown argument")
                })
            }
        }
        setupToolbar(page == AuthMethodActivity.Page.ADD)
        subscribeObservers(page == AuthMethodActivity.Page.ADD)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        UiUtils.applyThemeToMenu(menuInflater, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                HelpDialogFragment.show(
                    childFragmentManager,
                    requireActivity(),
                    getString(R.string.ioa_sec_fs_help_title),
                    getString(R.string.ioa_sec_fs_help_message)
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar(addingBiometric: Boolean) {
        if (addingBiometric) {
            binding.biometricToolbar.root.setTitle(R.string.ioa_sec_fs_add_title)
            UiUtils.updateToolbarIcon(binding.biometricToolbar.root, UiUtils.NavButton.BACK)
        } else {
            binding.biometricToolbar.root.setTitle(R.string.ioa_sec_fs_sett_title)
            UiUtils.updateToolbarIcon(binding.biometricToolbar.root, UiUtils.NavButton.CANCEL)
        }

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.biometricToolbar.root)
        binding.biometricToolbar.root.setNavigationOnClickListener { requireActivity().onBackPressed() }
    }

    private fun subscribeObservers(addingBiometric: Boolean) {
        if (addingBiometric) {
            biometricAddViewModel.biometricState.observe(viewLifecycleOwner) { addState ->
                if (addState is BiometricAddViewModel.BiometricState.Set) {
                    UiUtils.finishAddingFactorActivity(requireActivity())
                }
            }
        } else {
            biometricCheckViewModel.biometricState.observe(viewLifecycleOwner) { checkState ->
                if (checkState is BiometricCheckViewModel.BiometricState.Removed) {
                    requireActivity().finish()
                }
            }

            biometricCheckViewModel.unlinkState.observe(viewLifecycleOwner) { unlinkState ->
                when (unlinkState) {
                    is BiometricCheckViewModel.UnlinkState.UnlinkWarningTriggered ->
                        AutoUnlinkWarningAlertDialogFragment.show(
                            childFragmentManager,
                            requireActivity(),
                            unlinkState.attemptsRemaining
                        )
                    is BiometricCheckViewModel.UnlinkState.UnlinkTriggered ->
                        unlinkDialogViewModel.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
                }
            }

            unlinkDialogViewModel.state.observe(viewLifecycleOwner) {
                when (it) {
                    is DialogFragmentViewModel.State.NeedsToBeShown -> {
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
    }
}