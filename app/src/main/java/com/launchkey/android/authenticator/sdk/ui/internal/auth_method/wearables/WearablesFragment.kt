package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentWearablesBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.AuthMethodActivity
import com.launchkey.android.authenticator.sdk.ui.SecurityFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.bundleArgument
import com.launchkey.android.authenticator.sdk.ui.internal.util.setNavigationButton
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class WearablesFragment : BaseAppCompatFragment(R.layout.fragment_wearables) {
    private val binding by viewBinding(FragmentWearablesBinding::bind)
    private val page: AuthMethodActivity.Page by bundleArgument(AuthMethodActivity.PAGE_KEY)
    var wearableAdded = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (page == AuthMethodActivity.Page.ADD) {
                    if (wearableAdded) {
                        requireActivity().setResult(Activity.RESULT_OK, Intent().apply {
                            putExtra(SecurityFragment.REQUEST_CODE, SecurityFragment.REQUEST_ADD_WEARABLES)
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
        if (savedInstanceState != null) return
        setupScreen()
        setupToolbar(page)
    }

    private fun setupToolbar(page: AuthMethodActivity.Page?) {
        val page = page ?: this.page
        with(binding.wearablesToolbar.root) {
            setNavigationButton(
                    when (page) {
                        AuthMethodActivity.Page.ADD -> UiUtils.NavButton.CANCEL
                        AuthMethodActivity.Page.SETTINGS -> UiUtils.NavButton.BACK
                        else -> throw IllegalArgumentException("Unknown argument")
                    }
            )
            (requireActivity() as AppCompatActivity).setSupportActionBar(this)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
        }
    }

    private fun setupScreen() {
        when (page) {
            AuthMethodActivity.Page.ADD -> {
                goToAdd()
            }
            AuthMethodActivity.Page.SETTINGS -> {
                childFragmentManager.commit {
                    replace(binding.fragmentContainer.id, WearablesSettingsFragment())
                }
            }
        }
    }

    fun goToAdd(backstack: Boolean = false) {
        setupToolbar(AuthMethodActivity.Page.ADD)
        childFragmentManager.commit {
            replace(binding.fragmentContainer.id, WearablesAddFragment())
            if (backstack) addToBackStack(null)
        }
    }
}