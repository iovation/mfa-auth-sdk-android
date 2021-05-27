/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.ViewAuthErrorBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.makeVisible
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class AuthRequestErrorFragment : BaseAppCompatFragment(R.layout.view_auth_error) {
    private val binding: ViewAuthErrorBinding by viewBinding(ViewAuthErrorBinding::bind)
    private val authRequestFragmentViewModel: AuthRequestFragmentViewModel by viewModels(ownerProducer = { requireParentFragment() })
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authRequest = authRequestFragmentViewModel.currentAuthRequest!!
        val failureDetails = authRequestFragmentViewModel.getFailureDetails()
        val serProfile = authRequest.serviceProfile
        val details = authRequest.context
        val title = authRequest.title
        val finalTitle = if (title.isNotEmpty()) title else (if (serProfile.name.isEmpty()) null else serProfile.name)!!
        val errorTitlePrefix = getString(R.string.ioa_ar_error_result_title_prefix)
        val errorTitleSuffix = failureDetails.getTitle(resources)
        val errorTitle = getString(R.string.ioa_ar_error_result_title_format, errorTitlePrefix, errorTitleSuffix)
        binding.authErrorLabelTitle.text = finalTitle
        binding.authErrorLabelResult.text = errorTitle
        binding.authErrorTextResult.text = failureDetails.getMessage(resources)
        if (details.isNotBlank()) {
            binding.authErrorLabelDetails.makeVisible()
            binding.authErrorTextDetails.makeVisible()
            binding.authErrorTextDetails.text = details
        }
        binding.authErrorButton.setOnClickListener { authRequestFragmentViewModel.setAuthRequestStateToResponded(null) }
        
        binding.authErrorLabelResult.setTextColor(AuthenticatorUIManager.instance.config.themeObj().authResponseFailed)
    }
}