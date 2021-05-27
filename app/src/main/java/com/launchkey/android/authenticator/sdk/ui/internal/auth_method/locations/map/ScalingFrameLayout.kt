/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.FrameLayout
import androidx.annotation.AttrRes

class ScalingFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mOnTouchListener: OnTouchListener? = null
    fun setScaleListener(onScaleListener: OnScaleGestureListener?) {
        mScaleGestureDetector = ScaleGestureDetector(context, onScaleListener)
    }
    
    override fun setOnTouchListener(onTouchListener: OnTouchListener) {
        mOnTouchListener = onTouchListener
    }
    
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (mOnTouchListener != null) {
            mOnTouchListener!!.onTouch(this, event)
        }
        val isMultiTouch = event.pointerCount >= 2
        if (isMultiTouch) {
            onTouchEventForDetector(event)
        }
        return isMultiTouch
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        onTouchEventForDetector(event)
        return true
    }
    
    private fun onTouchEventForDetector(event: MotionEvent) {
        if (mScaleGestureDetector != null) {
            mScaleGestureDetector!!.onTouchEvent(event)
        }
    }
}