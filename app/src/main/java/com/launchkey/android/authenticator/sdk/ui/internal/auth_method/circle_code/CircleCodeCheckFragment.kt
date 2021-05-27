package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentCircleCodeAddCheckBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code.CircleCodeCheckViewModel.RequestState.RequestFailed
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.setTextTemporarily
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class CircleCodeCheckFragment : BaseAppCompatFragment(R.layout.fragment_circle_code_add_check) {
    private val circleCodeCheckViewModel: CircleCodeCheckViewModel by viewModels({ requireParentFragment() })
    private val binding by viewBinding(FragmentCircleCodeAddCheckBinding::bind)
    private lateinit var instructions: TextView
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPanel()
        subscribeObservers()
    }
    
    private fun setupPanel() {
        val headerViews = UiUtils.prepKbaAddCheckHeader(
            binding.root,
            R.id.panel_header,
            false,
            true,
            R.id.circle_pad,
            resources,
            emptyList())
        
        instructions = headerViews[2] as TextView
        instructions.setText(
            if (circleCodeCheckViewModel.requestState.value is CircleCodeCheckViewModel.RequestState.ChangeRequested)
                R.string.ioa_sec_cir_check_toverify
            else R.string.ioa_sec_cir_check_toremove)
        
        binding.circlePad.setListener { circleCode ->
            binding.circlePad.ignoreTouches(true)
            circleCodeCheckViewModel.verifyCircleCode(circleCode)
        }
    }
    
    private fun subscribeObservers() {
        circleCodeCheckViewModel.requestState.observe(viewLifecycleOwner, { requestState ->
            when (requestState) {
                is CircleCodeCheckViewModel.RequestState.ChangeRequested -> instructions.setText(R.string.ioa_sec_cir_check_toverify)
                is CircleCodeCheckViewModel.RequestState.RemoveRequested -> instructions.setText(R.string.ioa_sec_cir_check_toremove)
                is RequestFailed -> {
                    binding.circlePad.ignoreTouches(requestState.unlinked)
                    instructions.setTextTemporarily(
                        viewLifecycleOwner.lifecycle,
                        getString(R.string.ioa_sec_cir_check_error_wrong),
                        Constants.BRIEF_INSTRUCTIONS_DURATION_MILLI)
                }
                CircleCodeCheckViewModel.RequestState.ChangeRequestSuccess,
                CircleCodeCheckViewModel.RequestState.RemoveRequestSuccess -> Unit
            }
        })
    }
}