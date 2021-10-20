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
    private val wearablesAddViewModel: WearablesAddViewModel by viewModels()
    private val binding by viewBinding(FragmentWearablesBinding::bind)
    private val startPage: AuthMethodActivity.Page by bundleArgument(AuthMethodActivity.PAGE_KEY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            setupScreen()
        }

        setupToolbar()
        subscribeObservers()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    childFragmentManager.popBackStack()
                    if (childFragmentManager.backStackEntryCount == 0) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })

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

    private fun setupScreen() {
        when (startPage) {
            AuthMethodActivity.Page.ADD -> {
                goToAdd()
            }
            AuthMethodActivity.Page.SETTINGS -> {
                childFragmentManager.commit {
                    replace(
                        binding.fragmentContainer.id,
                        WearablesSettingsFragment::class.java,
                        null,
                        null
                    )
                }
            }
            else -> throw IllegalStateException("Invalid Fragment Start Page")
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
                        // pop add fragment
                        childFragmentManager.popBackStack()
                    }
                }
                is WearablesAddViewModel.AddWearableState.FailedToAddWearable -> {
                    TODO("Failure? Permission or disabled?")
                }
                else -> Unit
            }
        }
    }

    fun goToAdd(backstack: Boolean = false) {
        childFragmentManager.commit {
            replace(binding.fragmentContainer.id, WearablesAddFragment::class.java, null, null)
            if (backstack) addToBackStack(null)
        }
    }
}