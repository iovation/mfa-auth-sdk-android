/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.launchkey.android.authenticator.sdk.ui.R

class MaxDimensionsFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var mMaxHeight = 0
    private var mMaxWidth = 0
    private var mPaddingPercentHorizontal = 0f
    private var mPaddingPercentVertical = 0f
    private var mPaddingFactorIfLimitedSize = 1f
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        var limitedSizeHorizontally = false
        var limitedSizeVertically = false
        if (mMaxWidth > 0 && mMaxWidth < measuredWidth) {
            val measuredMode = MeasureSpec.getMode(widthMeasureSpec)
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, measuredMode)
            limitedSizeHorizontally = true
        }
        if (mMaxHeight > 0 && mMaxHeight < measuredHeight) {
            val measuredMode = MeasureSpec.getMode(heightMeasureSpec)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, measuredMode)
            limitedSizeVertically = true
        }
        var paddingPercentHorizontal = mPaddingPercentHorizontal
        var paddingPercentVertical = mPaddingPercentVertical
        if (limitedSizeHorizontally) {
            paddingPercentHorizontal *= mPaddingFactorIfLimitedSize
        }
        if (limitedSizeVertically) {
            paddingPercentVertical *= mPaddingFactorIfLimitedSize
        }
        val paddingHorizontal = (paddingPercentHorizontal * mMaxWidth).toInt()
        val paddingVertical = (paddingPercentVertical * mMaxHeight).toInt()
        setPadding(paddingHorizontal / 2, paddingVertical / 2, paddingHorizontal / 2, paddingVertical / 2)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    init {
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.MaxDimensionsFrameLayout, defStyleAttr, 0)
        if (a.hasValue(R.styleable.MaxDimensionsFrameLayout_mdfl_maxWidth)) {
            mMaxWidth = a.getDimensionPixelSize(R.styleable.MaxDimensionsFrameLayout_mdfl_maxWidth, 0)
        }
        if (a.hasValue(R.styleable.MaxDimensionsFrameLayout_mdfl_maxHeight)) {
            mMaxHeight = a.getDimensionPixelSize(R.styleable.MaxDimensionsFrameLayout_mdfl_maxHeight, 0)
        }
        if (a.hasValue(R.styleable.MaxDimensionsFrameLayout_mdfl_paddingPercentHorizontal)) {
            val paddingPercentHorizontalInt = a.getInteger(
                    R.styleable.MaxDimensionsFrameLayout_mdfl_paddingPercentHorizontal, 0)
            if (paddingPercentHorizontalInt > 0) {
                mPaddingPercentHorizontal = paddingPercentHorizontalInt.toFloat() / 100f
            }
        }
        if (a.hasValue(R.styleable.MaxDimensionsFrameLayout_mdfl_paddingPercentVertical)) {
            val paddingPercentVerticalInt = a.getInteger(
                    R.styleable.MaxDimensionsFrameLayout_mdfl_paddingPercentVertical, 0)
            if (paddingPercentVerticalInt > 0) {
                mPaddingPercentVertical = paddingPercentVerticalInt.toFloat() / 100f
            }
        }
        if (a.hasValue(R.styleable.MaxDimensionsFrameLayout_mdfl_paddingFactorIfLimitedSize)) {
            mPaddingFactorIfLimitedSize = a.getFloat(
                    R.styleable.MaxDimensionsFrameLayout_mdfl_paddingFactorIfLimitedSize, 1f)
        }
        a.recycle()
    }
}