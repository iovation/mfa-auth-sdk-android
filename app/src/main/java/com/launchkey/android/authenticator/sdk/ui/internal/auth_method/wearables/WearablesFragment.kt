package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.SecurityFragment
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentWearablesBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.AuthMethodActivity
import com.launchkey.android.authenticator.sdk.ui.internal.util.*

class WearablesFragment : BaseAppCompatFragment(R.layout.fragment_wearables) {
    private val binding by viewBinding(FragmentWearablesBinding::bind)
    private val startPage: AuthMethodActivity.Page by bundleArgument(AuthMethodActivity.PAGE_KEY)
    private val wearablesAddViewModel: WearablesAddViewModel by viewModels()
    private val wearablesSettingsViewModel: WearablesSettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace(
                    binding.fragmentContainer.id,
                    when (startPage) {
                        AuthMethodActivity.Page.ADD -> WearablesAddFragment::class.java
                        AuthMethodActivity.Page.SETTINGS -> WearablesSettingsFragment::class.java
                        else -> throw IllegalStateException("Unexpected argument")
                    },
                    null
                )
            }
        }

        setupToolbar()
        subscribeObservers()

        childFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                updateToolbar(
                    when (f::class.java) {
                        WearablesAddFragment::class.java -> AuthMethodActivity.Page.ADD
                        WearablesSettingsFragment::class.java -> AuthMethodActivity.Page.SETTINGS
                        else -> throw IllegalStateException("Invalid fragment $f")
                    }
                )
            }

            // TODO: 10/15/21 onFragmentResumed should check for bluetooth permission
        }, false)

    }

    private fun setupToolbar() {
        with(binding.wearablesToolbar.root) {
            title = ""
            (requireActivity() as AppCompatActivity).setSupportActionBar(this)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            updateToolbar(startPage)
        }
    }

    private fun updateToolbar(page: AuthMethodActivity.Page) {
        with(binding.wearablesToolbar.root) {
            when (page) {
                AuthMethodActivity.Page.ADD -> {
                    setTitle(R.string.ioa_sec_bp_title)
                    setNavigationButton(UiUtils.NavButton.CANCEL)
                }
                AuthMethodActivity.Page.SETTINGS -> {
                    setTitle(R.string.ioa_sec_bp_sett_title)
                    setNavigationButton(UiUtils.NavButton.BACK)
                }
                else -> throw IllegalArgumentException("Unknown argument")
            }
        }
    }

    private fun subscribeObservers() {
        wearablesAddViewModel.addWearableState.observe(viewLifecycleOwner) { addWearableState ->
            when (addWearableState) {
                is WearablesAddViewModel.AddWearableState.AddedNewWearable -> {
                    if (startPage == AuthMethodActivity.Page.ADD) {
                        UiUtils.finishAddingFactorActivity(
                            requireActivity(),
                            SecurityFragment.REQUEST_ADD_WEARABLES
                        )
                    } else {
                        wearablesSettingsViewModel.addedNewWearable()
                    }
                }
                else -> Unit
            }
        }

        // only use the wearablesSettingsViewModel if we started with SETTINGS
        if (startPage != AuthMethodActivity.Page.SETTINGS) return

        wearablesSettingsViewModel.newWearableState.observe(viewLifecycleOwner) { newWearableState ->
            when (newWearableState) {
                WearablesSettingsViewModel.NewWearableState.AddedNewWearable -> {
                    childFragmentManager.popBackStack()
                    wearablesSettingsViewModel.fetchWearables()
                }
                WearablesSettingsViewModel.NewWearableState.AddingNewWearable -> {
                    childFragmentManager.commit {
                        replace(
                            binding.fragmentContainer.id,
                            WearablesAddFragment::class.java,
                            null
                        )
                        addToBackStack(WearablesAddFragment::class.java.simpleName)
                    }
                }
            }
        }

        wearablesSettingsViewModel.removeSingleWearableState.observe(viewLifecycleOwner) { removeSingleWearableState ->
            if (removeSingleWearableState is WearablesSettingsViewModel.RemoveSingleWearableState.Failed) {
                requireActivity().finish()
            }
        }

        wearablesSettingsViewModel.removeAllWearablesState.observe(viewLifecycleOwner) { removeAllWearablesState ->
            when (removeAllWearablesState) {
                WearablesSettingsViewModel.RemoveAllWearablesState.PendingRemovalForAllWearables,
                is WearablesSettingsViewModel.RemoveAllWearablesState.Failed -> {
                    requireActivity().finish()
                }
                else -> Unit
            }
        }

        wearablesSettingsViewModel.getStoredWearablesState.observe(viewLifecycleOwner) { getStoredWearablesState ->
            when (getStoredWearablesState) {
                is WearablesSettingsViewModel.GetStoredWearablesState.Failed -> {
                    requireActivity().finish()
                }
                is WearablesSettingsViewModel.GetStoredWearablesState.GotStoredWearables -> {
                    if (getStoredWearablesState.wearables.isEmpty()) {
                        requireActivity().finish()
                    }
                }
                else -> Unit
            }
        }
    }
}
