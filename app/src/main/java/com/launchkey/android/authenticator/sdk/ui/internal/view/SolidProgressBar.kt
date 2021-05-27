/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SolidProgressBar @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var mPaint: Paint? = null
    private val mColor = 0
    private var mProgress = 1f
    private var mRect: RectF? = null
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun init() {
        mPaint = Paint()
        mPaint!!.style = Paint.Style.FILL
    }

    fun setColor(color: Int) {
        mPaint!!.color = color
        invalidate()
    }

    fun setProgress(progress: Float) {
        var progress = progress
        if (progress < 0) {
            progress = 0f
        }
        if (progress > 1f) {
            progress = 1f
        }
        mProgress = progress
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(
                mRect!!.left,
                mRect!!.top,
                mRect!!.width() * mProgress,
                mRect!!.bottom,
                mPaint!!)
    }

    companion object {
        private const val DEFAULT_COLOR = Color.BLUE
    }

    init {
        init()
    }
}