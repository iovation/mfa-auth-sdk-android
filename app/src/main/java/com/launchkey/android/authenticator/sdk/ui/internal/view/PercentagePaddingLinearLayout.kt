/*
 *  Copyright (c) 2016. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

open class PercentagePaddingLinearLayout @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private val mPaddingPercent: Int
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val pH = getPaddingForDimension(MeasureSpec.getSize(widthMeasureSpec))
        val pV = getPaddingForDimension(MeasureSpec.getSize(heightMeasureSpec))
        setPadding(pH, pV, pH, pV)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getPaddingForDimension(dimension: Int): Int {
        return (mPaddingPercent.toFloat() * (dimension.toFloat() / 100f)).toInt()
    }

    companion object {
        private const val PADDING_PERCENT_DEFAULT = 10
    }

    init {
        var paddingPercent = tag as String?
        if (paddingPercent == null) {
            paddingPercent = PADDING_PERCENT_DEFAULT.toString()
        }
        mPaddingPercent = Integer.valueOf(paddingPercent)
    }
}