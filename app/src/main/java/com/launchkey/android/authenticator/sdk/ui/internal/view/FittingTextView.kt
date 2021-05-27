/*
 *  Copyright (c) 2016. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.graphics.RectF
import android.text.InputType
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.launchkey.android.authenticator.sdk.ui.internal.view.FittingTextView

open class FittingTextView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context!!, attrs, defStyleAttr) {
    private val mInitialPaint: TextPaint?
    private val mTextBounds: RectF? = RectF()
    private var mMaxTextSize = TEXT_SIZE_MAX_PX
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateTextBounds()
        }
    }

    override fun setText(text: CharSequence, type: BufferType) {
        super.setText(text, type)
        updateTextBounds()
    }

    private fun updateTextBounds() {
        if (mTextBounds == null || mInitialPaint == null) {
            return
        }
        mTextBounds[paddingLeft.toFloat(), paddingTop.toFloat(), (
                width - paddingRight).toFloat()] = (
                height - paddingBottom).toFloat()
        val text = text.toString()
        val textWidth = mInitialPaint.measureText(text)
        var textSize = mInitialPaint.textSize
        var ratioWidth = Float.MAX_VALUE
        if (mTextBounds.width() > 0) {
            ratioWidth = mTextBounds.width() / textWidth
        }
        if (ratioWidth < 1.0f) {
            textSize *= ratioWidth
        }
        var ratioHeight = Float.MAX_VALUE
        if (mTextBounds.height() > 0) {
            ratioHeight = mTextBounds.height() / textSize
        }
        if (ratioHeight < 1.0f) {
            textSize *= ratioHeight
        }
        if (textSize < TEXT_SIZE_MIN_PX) {
            textSize = TEXT_SIZE_MIN_PX
        } else if (textSize > mMaxTextSize) {
            textSize = mMaxTextSize
        }
        textSize *= POST_MEASURE_SCALE
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    companion object {
        private const val TEXT_SIZE_MIN_PX = 10f
        private const val TEXT_SIZE_MAX_PX = 80f
        private const val POST_MEASURE_SCALE = 0.92f
        private val TAG = FittingTextView::class.java.simpleName
    }

    init {
        setLines(1)
        maxLines = 1
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        mInitialPaint = TextPaint(paint)
        mMaxTextSize = mInitialPaint.getTextSize() //processed and given to Paint in super method
    }
}