package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.SecurityFragment
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentWearablesBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.AuthMethodActivity
import com.launchkey.android.authenticator.sdk.ui.internal.util.*

class WearablesFragment : BaseAppCompatFragment(R.layout.fragment_wearables) {
    private val binding by viewBinding(FragmentWearablesBinding::bind)
    private val startPage: AuthMethodActivity.Page by bundleArgument(AuthMethodActivity.PAGE_KEY)
    var wearableAdded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            setupScreen()
            setupToolbar()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (startPage == AuthMethodActivity.Page.ADD) {
                        if (wearableAdded) {
                            requireActivity().setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra(
                                    SecurityFragment.REQUEST_CODE,
                                    SecurityFragment.REQUEST_ADD_WEARABLES
                                )
                            })
                        }
                    } else {
                        childFragmentManager.popBackStack()
                    }
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
            (requireActivity() as AppCompatActivity).setSupportActionBar(this)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            updateToolbar(startPage)
        }
    }

    private fun updateToolbar(page: AuthMethodActivity.Page) {
        with(binding.wearablesToolbar.root) {
            when (page) {
                AuthMethodActivity.Page.ADD -> {
                    setNavigationButton(UiUtils.NavButton.CANCEL)
                    setTitle(R.string.ioa_sec_bp_title)
                }
                AuthMethodActivity.Page.SETTINGS -> {
                    setNavigationButton(UiUtils.NavButton.BACK)
                    setTitle(R.string.ioa_sec_bp_sett_title)
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

    fun goToAdd(backstack: Boolean = false) {
        childFragmentManager.commit {
            replace(binding.fragmentContainer.id, WearablesAddFragment())
            if (backstack) addToBackStack(null)
        }
    }
}