package com.launchkey.android.authenticator.sdk.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentAuthrequestBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestDenyFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestEndFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestEndFragment.State.Dismissed
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestErrorFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestFragmentViewModel.AuthRequestState.*
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestInfoFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.NewAuthRequestDialog.show
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify.AuthRequestVerifyStepFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.GenericAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.ProgressDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class AuthRequestFragment : BaseAppCompatFragment(R.layout.fragment_authrequest) {
    private var progressDialogFragment: ProgressDialogFragment? = null
    private var toolbarVisibility = View.GONE
    private val authRequestFragmentViewModel: AuthRequestFragmentViewModel by viewModels()
    private val binding by viewBinding(FragmentAuthrequestBinding::bind)
    private val toolbar: Toolbar
        get() = binding.authenticatorToolbar.root
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeObservers()
    }
    
    private fun setupUi() {
        val uiConfig = AuthenticatorUIManager.instance.config
        toolbar.visibility = uiConfig.themeObj().authRequestAppBar.visibility
        setupToolbar()
    }
    
    private fun setupToolbar() {
        toolbarVisibility = AuthenticatorUIManager.instance.config.themeObj().authRequestAppBar.visibility
        if (toolbarVisibility == View.GONE) return
        
        val menuClickListener = Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.request_deny -> {
                    authRequestFragmentViewModel.denyAuthRequest()
                    return@OnMenuItemClickListener true
                }
                R.id.request_details -> {
                    GenericAlertDialogFragment.show(
                        childFragmentManager,
                        requireContext(),
                        getString(R.string.ioa_ar_dialog_details_title),
                        if (authRequestFragmentViewModel.currentAuthRequest!!.hasContext())
                            authRequestFragmentViewModel.currentAuthRequest!!.context
                        else null,
                        null,
                        true,
                        null,
                        "None")
                    return@OnMenuItemClickListener true
                }
                else -> false
            }
        }
        
        toolbar.setOnMenuItemClickListener(menuClickListener)
        toolbar.visibility = toolbarVisibility
        updateToolbar(true)
    }
    
    private fun updateToolbar(isShown: Boolean) {
        toolbar.visibility = if (isShown) toolbarVisibility else View.GONE
        var title: String? = null
        val authRequest = authRequestFragmentViewModel.currentAuthRequest
        val hasRequest = authRequest != null
        if (hasRequest) {
            title = authRequest!!.serviceProfile.name
            if (authRequest.hasContext()) {
                toolbar.inflateMenu(R.menu.request_details)
            }
        }
        if (title == null) {
            title = getString(R.string.ioa_ar_default_toolbar_norequest)
        }
        toolbar.title = title
        toolbar.menu.clear()
        if (isShown) {
            toolbar.inflateMenu(R.menu.request_deny)
        }
    }
    
    private fun subscribeObservers() {
        authRequestFragmentViewModel.fetchState.observe(viewLifecycleOwner) { fetchState ->
            when (fetchState) {
                AuthRequestFragmentViewModel.FetchState.PushReceived -> authRequestFragmentViewModel.checkForAuthRequest()
                AuthRequestFragmentViewModel.FetchState.Fetching -> Unit
                AuthRequestFragmentViewModel.FetchState.FetchedEmptyAuthRequest -> notifyUiChangeToHostingActivity(false)
                is AuthRequestFragmentViewModel.FetchState.FetchedAuthRequest -> {
                    notifyUiChangeToHostingActivity(true)
                    navigateToInfoFragment(false)
                }
                is AuthRequestFragmentViewModel.FetchState.FetchedNewerAuthRequest -> showNewerAuthRequestDialog()
                is AuthRequestFragmentViewModel.FetchState.Failed -> { }
            }
        }
        
        authRequestFragmentViewModel.authRequestState.observe(viewLifecycleOwner) { dataState ->
            when (dataState) {
                is Sending -> showSendingDialog()
                is Accepted -> authRequestFragmentViewModel.startVerifyingAuthMethods()
                is Denying -> navigateToDenyFragment()
                is Verifying -> navigateToVerifyStepFragment()
                is Responded -> {
                    dismissSendingDialog()
                    clearAuthenticatorFragment()
                    val isAuthorized = dataState.isAuthorized
                    if (isAuthorized != null) {
                        showResult(isAuthorized)
                    } else {
                        notifyUiChangeToHostingActivity(false)
                    }
                }
                is AuthRequestFailed -> {
                    dismissSendingDialog()
                    navigateToErrorFragment()
                }
                is Failed -> {
                    dismissSendingDialog()
                    clearAuthenticatorFragment()
                    notifyUiChangeToHostingActivity(false)
                }
                else -> Unit
            }
        }
    }
    
    private fun clearAuthenticatorFragment() {
        childFragmentManager.findFragmentById(R.id.authenticator_frame)?.let {
            childFragmentManager.commit {
                remove(it)
            }
        }
        
    }
    
    private fun showSendingDialog() {
        dismissSendingDialog()
        progressDialogFragment = ProgressDialogFragment.show(
            null,
            getString(R.string.ioa_ar_dialog_responding),
            cancellable = false,
            indeterminate = true,
            childFragmentManager,
            ProgressDialogFragment::class.java.simpleName
        )
    }
    
    private fun dismissSendingDialog() {
        progressDialogFragment?.let {
            it.dismiss()
            progressDialogFragment = null
        }
    }
    
    private fun navigateToInfoFragment(newerAuthRequestReceived: Boolean) {
        if (!newerAuthRequestReceived && childFragmentManager.findFragmentByTag(TAG_AUTH_REQUEST_INFO_FRAGMENT) != null) {
            return
        }
        childFragmentManager.beginTransaction()
            .setCustomAnimations(ANIMATION_IN_RES, ANIMATION_OUT_RES)
            .replace(R.id.authenticator_frame, AuthRequestInfoFragment(), TAG_AUTH_REQUEST_INFO_FRAGMENT)
            .commit()
    }
    
    private fun navigateToDenyFragment() {
        if (childFragmentManager.findFragmentByTag(TAG_AUTH_REQUEST_DENY_FRAGMENT) != null) {
            return
        }
        childFragmentManager.beginTransaction()
            .setCustomAnimations(ANIMATION_IN_RES, ANIMATION_OUT_RES)
            .replace(R.id.authenticator_frame, AuthRequestDenyFragment(), TAG_AUTH_REQUEST_DENY_FRAGMENT)
            .commit()
    }
    
    private fun navigateToErrorFragment() {
        if (childFragmentManager.findFragmentByTag(TAG_AUTH_REQUEST_ERROR_FRAGMENT) != null) {
            return
        }
        
        childFragmentManager.beginTransaction()
            .setCustomAnimations(ANIMATION_IN_RES, ANIMATION_OUT_RES)
            .replace(R.id.authenticator_frame, AuthRequestErrorFragment(), TAG_AUTH_REQUEST_ERROR_FRAGMENT)
            .commit()
    }
    
    private fun navigateToVerifyStepFragment() {
        if (childFragmentManager.findFragmentByTag(TAG_AUTH_REQUEST_VERIFY_STEP_FRAGMENT) != null) {
            return
        }
        childFragmentManager.beginTransaction()
            .setCustomAnimations(ANIMATION_IN_RES, ANIMATION_OUT_RES)
            .replace(R.id.authenticator_frame, AuthRequestVerifyStepFragment(), TAG_AUTH_REQUEST_VERIFY_STEP_FRAGMENT)
            .commit()
    }
    
    private fun showResult(authorized: Boolean) {
        val authRequestEndFragment = childFragmentManager.findFragmentById(R.id.auth_request_result) as AuthRequestEndFragment
        authRequestEndFragment.setAuthorized(authorized)
        authRequestEndFragment.getState().observe(viewLifecycleOwner) { state ->
            if (state is AuthRequestEndFragment.State.Shown) {
                binding.authRequestResult.visibility = View.VISIBLE
            } else if (state is Dismissed) {
                binding.authRequestResult.visibility = View.INVISIBLE
                notifyUiChangeToHostingActivity(false)
            }
        }
    }
    
    private fun showNewerAuthRequestDialog() {
        clearAuthenticatorFragment()
        show(childFragmentManager, requireContext()) { _, _ ->
            notifyUiChangeToHostingActivity(true)
            navigateToInfoFragment(true)
        }
    }
    
    
    private fun notifyUiChangeToHostingActivity(isAuthRequestShown: Boolean) {
        updateToolbar(isAuthRequestShown)
        if (activity?.isFinishing != true) {
            (activity as? Listener)?.onUiChange(isAuthRequestShown)
        }
    }
    
    companion object {
        private val ANIMATION_IN_RES = R.anim.alpha_in
        private val ANIMATION_OUT_RES = R.anim.alpha_out
        private val TAG_AUTH_REQUEST_INFO_FRAGMENT = AuthRequestInfoFragment::class.java.simpleName
        private val TAG_AUTH_REQUEST_DENY_FRAGMENT = AuthRequestDenyFragment::class.java.simpleName
        private val TAG_AUTH_REQUEST_ERROR_FRAGMENT = AuthRequestErrorFragment::class.java.simpleName
        private val TAG_AUTH_REQUEST_VERIFY_STEP_FRAGMENT = AuthRequestVerifyStepFragment::class.java.simpleName
    }
    
    fun interface Listener {
        fun onUiChange(requestShown: Boolean)
    }
}