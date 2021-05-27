/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import android.widget.TextView
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.ViewAuthInfoBinding
import com.launchkey.android.authenticator.sdk.ui.internal.view.ExpirationTimer
import com.launchkey.android.authenticator.sdk.ui.internal.view.TimerDisplay

class IntRequestViewInfo @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr), TimerDisplay {
    private var responseCallback: InfoActions? = null
    private val binding: ViewAuthInfoBinding = ViewAuthInfoBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val denyButton = binding.authInfoActionNegative
        denyButton.setTextTop(R.string.ioa_ar_arb_directive_hold)
        denyButton.setTextMain(R.string.ioa_ar_arb_action_deny)
        denyButton.setOnClickListener {
            if (responseCallback != null) {
                responseCallback!!.onDeny()
            }
        }

        val continueButton = binding.authInfoActionPositive
        continueButton.setTextTop(R.string.ioa_ar_arb_directive_hold)
        continueButton.setTextMain(R.string.ioa_ar_arb_action_authorize)
        // Set listener to notify to continue when method verification will happen.
        // This button will be handed via provider when no verification is required.
        continueButton.setOnClickListener {
            this.responseCallback?.onContinue()
        }
    }

    fun setCallback(callback: InfoActions?) {
        responseCallback = callback
    }

    fun setText(title: String?, details: String?) {
        binding.authInfoLabelTitle.text = title
        if (details != null && details.trim { it <= ' ' }.isNotEmpty()) {
            binding.authInfoLabelDetails.visibility = VISIBLE
            binding.authInfoTextDetails.visibility = VISIBLE
            binding.authInfoTextDetails.text = details
        }
    }

    override fun onTimerUpdate(remainingMillis: Long, progress: Float) {
        binding.authInfoTimer.setProgress(remainingMillis, progress)
    }

    interface InfoActions {
        fun onDeny()
        fun onContinue()
    }
}