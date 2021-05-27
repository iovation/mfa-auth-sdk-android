/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.ViewPinpadKeyBinding
import com.launchkey.android.authenticator.sdk.ui.internal.view.FittingTextView
import com.launchkey.android.authenticator.sdk.ui.internal.view.PercentagePaddingLinearLayout

class PinCodeKey @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PercentagePaddingLinearLayout(context, attrs, defStyleAttr) {
    private val binding: ViewPinpadKeyBinding
    init {
        isClickable = true
        binding = ViewPinpadKeyBinding.inflate(LayoutInflater.from(context), this)
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.PinCodeKey, 0, 0)
        val number = a.getString(R.styleable.PinCodeKey_numberText)
        val label = a.getString(R.styleable.PinCodeKey_labelText)
        val numberOnly = a.getBoolean(R.styleable.PinCodeKey_numberOnly, false)
        a.recycle()

        val numberText = binding.pinpadkeyTextNumber
        numberText.text = number
        numberText.gravity =
                if (numberOnly) Gravity.CENTER else Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM

        val labelText = binding.pinpadkeyTextLabel
        labelText.text = label
        labelText.visibility = if (numberOnly) GONE else VISIBLE
    }

    fun setTextColor(textColor: Int) {
        binding.pinpadkeyTextNumber.setTextColor(textColor)
        binding.pinpadkeyTextLabel.setTextColor(textColor)
        invalidate()
    }

    fun setTextColor(colors: ColorStateList?) {
        binding.pinpadkeyTextNumber.setTextColor(colors)
        binding.pinpadkeyTextLabel.setTextColor(colors)
    }

    fun getNumber() = Integer.parseInt(binding.pinpadkeyTextNumber.text.toString())

    fun setOnClickListener(onClickListener: (Int) -> Unit) {
        setOnClickListener(OnClickListener { onClickListener(getNumber()) })
    }
}