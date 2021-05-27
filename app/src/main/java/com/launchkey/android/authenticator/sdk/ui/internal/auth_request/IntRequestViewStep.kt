/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentAuthRequestVerifyStepBinding
import com.launchkey.android.authenticator.sdk.ui.internal.view.ExpirationTimer
import com.launchkey.android.authenticator.sdk.ui.internal.view.TimerDisplay

internal class IntRequestViewStep @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr), TimerDisplay {
    companion object {
        private val ANIMATION_IN_RES = R.anim.alpha_in
    }
    private val binding: FragmentAuthRequestVerifyStepBinding =
            FragmentAuthRequestVerifyStepBinding.inflate(LayoutInflater.from(context), this, true)
    private val params: FrameLayout.LayoutParams
    private val animIn: Animation
    private var callback: StepActions? = null

    init {
        val deny: AuthResponseButton = binding.authStepActionNegative
        deny.setTextTop(R.string.ioa_ar_arb_directive_hold)
        deny.setTextMain(R.string.ioa_ar_arb_action_deny)
        deny.setOnClickListener {
            callback?.onDeny()
        }
        params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        animIn = AnimationUtils.loadAnimation(context, ANIMATION_IN_RES)
    }

    fun setCallback(callback: StepActions?) {
        this.callback = callback
    }

    fun setView(view: View?) {
        val container = binding.authStepFrame
        container.removeAllViews()
        container.addView(view, params)
        container.startAnimation(animIn)
    }

    fun setTitle(title: String?) {
        binding.authStepLabelTitle.text = title
    }

    fun setProgressMethod(method: String?) {
        binding.authStepLabelProgressMethod.text = method
    }

    fun setProgressNums(nums: String?) {
        binding.authStepLabelProgressNums.text = nums
    }

    override fun onTimerUpdate(remainingMillis: Long, progress: Float) {
        binding.authStepTimer.setProgress(remainingMillis, progress)
    }

    internal interface StepActions {
        fun onDeny()
    }
}