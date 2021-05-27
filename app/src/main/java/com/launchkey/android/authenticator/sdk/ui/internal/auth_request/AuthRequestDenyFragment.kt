package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentAuthRequestDenyBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.ExpirationTimerTracker
import com.launchkey.android.authenticator.sdk.ui.internal.util.ExpirationTimerTracker.State.Expired
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class AuthRequestDenyFragment : BaseAppCompatFragment(R.layout.fragment_auth_request_deny) {
    private val authRequestFragmentViewModel: AuthRequestFragmentViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val binding: FragmentAuthRequestDenyBinding by viewBinding(FragmentAuthRequestDenyBinding::bind)
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authRequest = authRequestFragmentViewModel.currentAuthRequest!!
        val denialReasons = authRequest.denialReasons
        val authDeny = binding.authDeny
        
        authDeny.setOptions(denialReasons)
        authDeny.setCallback { denialReason ->
            authRequestFragmentViewModel.denyAuthRequestWithDenialReason(authRequest, denialReason!!)
        }
        
        val expirationTimerTracker = ExpirationTimerTracker(
                System.currentTimeMillis(),
                authRequest.createdAtMillis,
                authRequest.expiresAtMillis,
                TimingCounter.DefaultTimeProvider(),
                Handler(Looper.getMainLooper()),
                viewLifecycleOwner)
        
        expirationTimerTracker.state.observe(viewLifecycleOwner, { state ->
            if (state is ExpirationTimerTracker.State.Update) {
                authDeny.onTimerUpdate(state.remainingMillis, state.progress)
            } else if (state is Expired) {
                authRequestFragmentViewModel.setAuthRequestExpired()
            }
        })
    }
}