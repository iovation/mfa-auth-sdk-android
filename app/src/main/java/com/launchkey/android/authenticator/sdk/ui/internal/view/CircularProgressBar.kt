/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.internal.util.FpsRefreshHelper

class CircularProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val mPaintCircleBar: Paint
    private val mPaintCircleBg: Paint
    private var mRectCircle: RectF? = null
    private val mStartAngleOffset: Int
    private val mCutoutRatio: Float
    private var mCutout: Path? = null
    private var mProgress: Float
    private var mColorWarn: Int
    private var mColorNormal: Int
    private val mRefresher: FpsRefreshHelper
    fun setProgress(progress: Float) {
        if (progress < 0.0f || progress > 1.0f) {
            throw ArithmeticException("Progress should be within the [0.0 - 1.0] range.")
        }
        mProgress = progress
        mRefresher.invalidate()
    }

    fun setColors(colorBg: Int, colorBarNormal: Int, colorBarWarn: Int) {
        mPaintCircleBg.color = colorBg
        mColorNormal = colorBarNormal
        mColorWarn = colorBarWarn
    }

    fun setWarning(isWarning: Boolean) {
        mPaintCircleBar.color = if (isWarning) mColorWarn else mColorNormal
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (mRectCircle == null) {
            mRectCircle = RectF()
        }
        val min = Math.min(w, h)
        val offsetX = (w - min) / 2
        val offsetY = (h - min) / 2
        mRectCircle!![0f, 0f, min.toFloat()] = min.toFloat()
        mRectCircle!!.offset(offsetX.toFloat(), offsetY.toFloat())
        if (mCutout == null) {
            mCutout = Path()
        }
        mCutout!!.reset()
        val ratio = mRectCircle!!.width() / 2.0f * mCutoutRatio
        val RTL_ON = resources.getBoolean(R.bool.is_right_to_left)
        val direction = if (RTL_ON) Path.Direction.CCW else Path.Direction.CW
        mCutout!!.addCircle(mRectCircle!!.centerX(), mRectCircle!!.centerY(), ratio, direction)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // TODO: Uncomment once targeting 26+ and move Canvas#clipPath() into else block
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            canvas.clipOutPath(mCutout);
        } else ... */canvas.clipPath(mCutout!!, Region.Op.DIFFERENCE)
        val RTL_ON = resources.getBoolean(R.bool.is_right_to_left)
        val direction = if (RTL_ON) -1 else 1
        canvas.drawArc(mRectCircle!!, 0f, 360.0f, true, mPaintCircleBg)
        canvas.drawArc(mRectCircle!!, mStartAngleOffset.toFloat(), 360.0f * mProgress * direction, true, mPaintCircleBar)
    }

    companion object {
        private const val DEFAULT_COLOR_BG = Color.LTGRAY
        private const val DEFAULT_COLOR_BAR_NORMAL = Color.DKGRAY
        private const val DEFAULT_COLOR_BAR_WARN = Color.DKGRAY
        private const val DEFAULT_INITIAL_VAL = 0.0f
        private const val DEFAULT_CUTOUT_RATIO = 0.5f
        private const val DEFAULT_START_ANGLE_OFFSET = 0
    }

    init {
        mRefresher = FpsRefreshHelper(this)
        mColorWarn = DEFAULT_COLOR_BAR_WARN
        mColorNormal = DEFAULT_COLOR_BAR_NORMAL
        mPaintCircleBar = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintCircleBar.style = Paint.Style.FILL
        mPaintCircleBar.color = mColorNormal
        mPaintCircleBg = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintCircleBg.style = Paint.Style.FILL
        mPaintCircleBg.color = DEFAULT_COLOR_BG
        val arr = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBar)
        mCutoutRatio = arr.getFloat(R.styleable.CircularProgressBar_cpb_cutoutRatio, DEFAULT_CUTOUT_RATIO)
        mStartAngleOffset = arr.getInt(R.styleable.CircularProgressBar_cpb_startAngleOffset, DEFAULT_START_ANGLE_OFFSET)
        arr.recycle()
        mProgress = DEFAULT_INITIAL_VAL
        setProgress(mProgress)
    }
}