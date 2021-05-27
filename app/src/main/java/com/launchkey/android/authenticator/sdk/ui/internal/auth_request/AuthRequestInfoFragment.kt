package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentAuthRequestInfoBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.IntRequestViewInfo.InfoActions
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.ExpirationTimerTracker
import com.launchkey.android.authenticator.sdk.ui.internal.util.ExpirationTimerTracker.State.Expired
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class AuthRequestInfoFragment : BaseAppCompatFragment(R.layout.fragment_auth_request_info) {
    private val binding: FragmentAuthRequestInfoBinding by viewBinding(FragmentAuthRequestInfoBinding::bind)
    private val authRequestFragmentViewModel: AuthRequestFragmentViewModel by viewModels(ownerProducer = { requireParentFragment() })
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authInfoView = binding.authInfo
        val authRequest = authRequestFragmentViewModel.currentAuthRequest!!
        val serProfile = authRequest.serviceProfile
        val details = authRequest.context
        val title = authRequest.title
        val finalTitle = if (title.isNotEmpty()) title else (if (serProfile.name.isEmpty()) null else serProfile.name)!!
        authInfoView.setText(finalTitle, details)
        authInfoView.setCallback(object : InfoActions {
            override fun onDeny() {
                authRequestFragmentViewModel.denyAuthRequest()
            }

            override fun onContinue() {
                authRequestFragmentViewModel.acceptAuthRequest()
            }
        })
        
        val expirationTimerTracker = ExpirationTimerTracker(
                System.currentTimeMillis(),
                authRequest.createdAtMillis,
                authRequest.expiresAtMillis,
                TimingCounter.DefaultTimeProvider(),
                Handler(Looper.getMainLooper()),
                viewLifecycleOwner)
        
        expirationTimerTracker.state.observe(viewLifecycleOwner, { state ->
            if (state is ExpirationTimerTracker.State.Update) {
                authInfoView.onTimerUpdate(state.remainingMillis, state.progress)
            } else if (state is Expired) {
                authRequestFragmentViewModel.setAuthRequestExpired()
            }
        })
    }
}