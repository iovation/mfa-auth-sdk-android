/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.launchkey.android.authenticator.sdk.ui.databinding.ViewAuthresponsebuttonBinding
import com.launchkey.android.authenticator.sdk.ui.internal.view.VibratorCompat

open class AuthResponseButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val ALPHA_ENABLED = 1.0f
        private const val ALPHA_DISABLED = 0.5f
        private const val DEFAULT_DURATION_VIBRATION: Long = 25
    }
    private var bgRect: RectF? = null
    private val vibrator: VibratorCompat?
    private val binding: ViewAuthresponsebuttonBinding = ViewAuthresponsebuttonBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        vibrator = VibratorCompat(context)
        isClickable = true
        isFocusable = true
    }

    fun setTextTop(textRes: Int) {
        binding.arbTop.setText(textRes)
    }

    fun setTextMain(textRes: Int) {
        binding.arbMain.setText(textRes)
    }

    fun setTextColor(@ColorInt color: Int) {
        binding.arbTop.setTextColor(color)
        binding.arbMain.setTextColor(color)
    }

    fun setTextColorRes(@ColorRes colorRes: Int) {
        val color = ContextCompat.getColor(context, colorRes)
        binding.arbTop.setTextColor(color)
        binding.arbMain.setTextColor(color)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) ALPHA_ENABLED else ALPHA_DISABLED
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (bgRect == null) {
            bgRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    // As a ViewGroup (extending FrameLayout) this
    // method is necessary to avoid trying to
    // dispatch to children views and instead
    // consume the event via its own onTouchEvent(MotionEvent)
    // method by returning true. Same for subclasses.
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (!isEnabled) {
            true
        } else super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        vibrator?.vibrate(DEFAULT_DURATION_VIBRATION)
        return super.performClick()
    }
}