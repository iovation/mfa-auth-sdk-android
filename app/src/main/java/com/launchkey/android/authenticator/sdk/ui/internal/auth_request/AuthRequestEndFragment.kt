/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentAuthRequestEndBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestEndFragment.State.Dismissed
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class AuthRequestEndFragment : BaseAppCompatFragment(R.layout.fragment_auth_request_end) {
    companion object {
        private const val DISMISSAL_DELAY: Long = 1500
    }

    private val authenticatorUIManagerConfig = AuthenticatorUIManager.instance.config
    private val handler = Handler(Looper.getMainLooper())
    private val state = MutableLiveData<State>()
    private val dismissRunnable = Runnable { notifyDismissal() }
    private var dismissalDelay: Long = 0
    private val binding: FragmentAuthRequestEndBinding by viewBinding(FragmentAuthRequestEndBinding::bind)
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Delay + any 'in' animation duration for this View:
        dismissalDelay = DISMISSAL_DELAY + UiUtils.getRequestUiAnimDuration(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(dismissRunnable)
    }

    fun setAuthorized(isAuthorized: Boolean) {
        val tintColor = getTintColor(isAuthorized)
        binding.authEndImage.setImageResource(if (isAuthorized) R.drawable.ic_result_authorized else R.drawable.ic_result_denied)
        binding.authEndImage.setColorFilter(tintColor)
        binding.authEndImage.requestFocus()
        binding.authEndImage.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_ASSERTIVE
        val contentDescriptionRes = if (isAuthorized) R.string.ioa_acc_auth_result_authorized else R.string.ioa_acc_auth_result_denied
        binding.authEndImage.contentDescription = resources.getString(contentDescriptionRes)
        handler.postDelayed(dismissRunnable, dismissalDelay)
        state.value = State.Shown()
    }

    private fun getTintColor(isAuthorized: Boolean): Int {
        return if (isAuthorized) {
            authenticatorUIManagerConfig.themeObj().authResponseAuthorized
        } else authenticatorUIManagerConfig.themeObj().authResponseDenied
    }

    private fun notifyDismissal() {
        state.value = Dismissed()
    }

    fun getState(): LiveData<State> {
        return state
    }

    abstract class State {
        class Shown : State()
        class Dismissed : State()
    }
}