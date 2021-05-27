/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.launchkey.android.authenticator.sdk.ui.R
import kotlin.math.min

class PinIndicator @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mNum = 0
    private var mTotal = 0
    private var mColorSet = 0
    private var mColorUnset = 0
    private var mPaintSet: Paint? = null
    private var mPaintUnset: Paint? = null
    private var mPaintOuterRing: Paint? = null

    //most padding and coordinates are handled in integer but drawing circles require float values overall
    private var mPadHorizontal = 0f
    private var mPadInBetween = 0f
    private var mIndividualPadHorizonal = 0f
    private var mRadius = 0f
    private var mCenterY = 0f
    private var mRadiusInnerRing = 0f
    private fun init(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PinIndicator, 0, 0)
        if (a.hasValue(R.styleable.PinIndicator_colorSet)) {
            mColorSet = a.getColor(R.styleable.PinIndicator_colorSet, Color.WHITE)
        }
        if (a.hasValue(R.styleable.PinIndicator_colorUnset)) {
            mColorUnset = a.getColor(R.styleable.PinIndicator_colorUnset, Color.GRAY)
        }
        if (a.hasValue(R.styleable.PinIndicator_numTotal)) {
            mTotal = a.getInteger(R.styleable.PinIndicator_numTotal, 0)
        }
        a.recycle()
        init()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        prepare()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun init() {
        mPaintSet = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintSet!!.style = Paint.Style.FILL
        mPaintSet!!.color = mColorSet
        mPaintUnset = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintUnset!!.style = Paint.Style.FILL
        mPaintUnset!!.color = mColorUnset
        mPaintOuterRing = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintOuterRing!!.style = Paint.Style.STROKE
        mPaintOuterRing!!.color = mColorSet
    }

    private fun prepare() {
        mPadHorizontal = (paddingLeft + paddingRight).toFloat()
        val padVertical = (paddingTop + paddingBottom).toFloat()
        val w = width.toFloat() - mPadHorizontal / 2f
        val h = height.toFloat() - padVertical / 2f

        //padding in between each indicator is half the width of an indicator (indicator = 2x)
        // and number of padding spaces is num - 1
        //i.e.:     [I] pad [I] pad [I]      3 indicators, 2 padding spaces, parts = 8
        val parts = mTotal * 2 + (mTotal - 1)
        mPadInBetween = w / parts.toFloat()
        val initialIndividualWidth = 2f * mPadInBetween

        //define the minimum dimension with the given space
        val size = minOf(h, initialIndividualWidth).toInt()

        //then measure the padding left for the greater dimension which will help center the indicator
        mIndividualPadHorizonal = initialIndividualWidth - size
        val individualPadVertical = h - size
        mRadius = size.toFloat() / 2f
        mRadiusInnerRing = INNER_RING * mRadius
        mCenterY = padVertical / 2f + individualPadVertical / 2f + mRadius
        mPaintOuterRing!!.strokeWidth =
            context.resources.getDimensionPixelSize(R.dimen.lk_pin_thick_outer_ring).toFloat()
    }

    fun setColorSet(colorSet: Int) {
        mColorSet = colorSet
        init()
        invalidate()
    }

    fun setColorUnset(colorUnset: Int) {
        mColorUnset = colorUnset
        init()
        invalidate()
    }

    var total: Int
        get() = mTotal
        set(total) {
            if (total == total) {
                return
            }
            mTotal = total
            prepare()
            invalidate()
        }

    fun increaseSet() {
        var newSet = set + 1
        if (newSet > total) {
            newSet = total
        }
        set = newSet
    }

    var set: Int
        get() = mNum
        set(numSet) {
            if (numSet < 0 || numSet > total) {
                throw RuntimeException("num of set indicators has to be X, where X >= 0 and X <= Total")
            }
            if (numSet == set) {
                return
            }
            mNum = numSet
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var cx = 0f
        for (index in 0 until total) {
            cx = (mPadHorizontal / 2f + mIndividualPadHorizonal / 2f + mRadius
                    + index.toFloat() * (mPadInBetween + 2f * mRadius + mIndividualPadHorizonal))

            //inner ring/circle
            canvas.drawCircle(
                cx,
                mCenterY,
                mRadiusInnerRing,
                (if (index <= set - 1) mPaintSet else mPaintUnset)!!
            )

            //outer ring
            canvas.drawCircle(
                cx,
                mCenterY,
                mRadius,
                mPaintOuterRing!!
            )
        }
    }

    companion object {
        private const val INNER_RING = 0.7f //inner radius = X times available radius
    }

    init {
        init(attrs)
    }
}