/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import com.launchkey.android.authenticator.sdk.ui.internal.util.FpsRefreshHelper
import java.util.*
import java.util.concurrent.TimeUnit

class ExpirationTimer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private val mText: TextView?
    private val mProgress: CircularProgressBar
    private val mRefresher: FpsRefreshHelper
    private var mColorFill = 0
    private var mColorWarning = 0
    private val mUiConfig: UiConfig
    private val mTimeAgo: TimeAgo
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (mText != null) {
            val newTextSize = mUiConfig.getTextSize(w, h)

            // Ignore if negative; layout imposing size
            if (newTextSize > 0) {
                mText.textSize = newTextSize
            }
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun setProgress(remainingMillis: Long, progress: Float) {
        if (remainingMillis < 0 || progress < 0.0f || progress > 1.0f) {
            return
        }
        val minSec = calculateMinSec(remainingMillis)
        val minutes = minSec[0].toInt()
        val seconds = minSec[1].toInt()
        contentDescription = getContentDescription(minutes, seconds, mTimeAgo, resources)
        val newLabel = convertMinSecToFormattedString(FORMAT_DEFAULT, minutes.toLong(), seconds.toLong())
        mText!!.text = newLabel
        val mustWarn = remainingMillis <= WARN_MILLISECONDS
        mText.setTextColor(if (mustWarn) mColorWarning else mColorFill)
        mProgress.setWarning(mustWarn)
        mProgress.setProgress(progress)
        mRefresher.invalidate()
    }

    fun setColors(colorBg: Int, colorFill: Int, colorWarning: Int) {
        mColorFill = colorFill
        mColorWarning = colorWarning
        mProgress.setColors(colorBg, mColorFill, mColorWarning)
        mRefresher.forceInvalidate()
    }

    private interface UiConfig {
        val layoutRes: Int
        fun getTextSize(viewWidth: Int, viewHeight: Int): Float
    }

    private inner class SmallUi : UiConfig {
        override val layoutRes: Int
            get() = R.layout.view_expirationtimer_small

        override fun getTextSize(viewWidth: Int, viewHeight: Int): Float {
            // Layout provided fixed sizes for all elements.
            return (-1).toFloat()
        }
    }

    private class BigUi : UiConfig {
        override val layoutRes: Int
            get() = R.layout.view_expirationtimer_big

        override fun getTextSize(viewWidth: Int, viewHeight: Int): Float {
            val minDim = Math.min(viewWidth, viewHeight)
            return 0.05f * minDim.toFloat()
        }
    }

    companion object {
        const val FORMAT_DEFAULT = "%02d:%02d"

        // Values matching res/values/attrs.xml for ExpirationTimer:
        private const val SIZE_SMALL = 0 // Small
        private const val WARN_MILLISECONDS = 10 * 1000
        private val COLOR_BG = Color.rgb(217, 217, 217)
        private val COLOR_BAR_NORMAL = Color.rgb(112, 112, 112)
        private val COLOR_BAR_WARN = Color.rgb(219, 65, 106)
        @JvmStatic
        fun getContentDescription(minutes: Int, seconds: Int, timeAgo: TimeAgo,
                                  res: Resources): String {
            val minutesLeft = if (minutes == 0) "" else timeAgo.timeAgoWithDiff((minutes * 60 * 1000).toLong(), true)
            val secondsLeft = if (seconds == 0) "" else timeAgo.timeAgoWithDiff((seconds * 1000).toLong(), true)
            val separator = if (minutesLeft.isEmpty() || secondsLeft.isEmpty()) "" else res.getString(R.string.ioa_acc_auth_timer_expiration_format_separator)
            return res
                    .getString(R.string.ioa_acc_auth_timer_expiration_format, minutesLeft, separator, secondsLeft)
                    .trim { it <= ' ' }
                    .replace(" +".toRegex(), " ")
        }

        @JvmStatic
        fun convertMinSecToFormattedString(format: String?, minutes: Long, seconds: Long): String {
            return String.format(Locale.US, format!!, if (minutes >= 0) minutes else 0, if (seconds >= 0 && seconds < 60) seconds else 0)
        }

        @JvmStatic
        fun calculateMinSec(remainingMillis: Long): LongArray {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
            val seconds = (TimeUnit.MILLISECONDS.toSeconds(remainingMillis)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingMillis)))
            return longArrayOf(if (minutes >= 0) minutes else 0, if (seconds >= 0) seconds else 0)
        }
    }

    init {
        val arr = context.obtainStyledAttributes(attrs, R.styleable.ExpirationTimer)
        val size = arr.getInteger(R.styleable.ExpirationTimer_et_size, SIZE_SMALL)
        arr.recycle()
        mUiConfig = if (size == SIZE_SMALL) SmallUi() else BigUi()
        mRefresher = FpsRefreshHelper(this)
        val root = LayoutInflater.from(context).inflate(mUiConfig.layoutRes, this, false)
        addView(root, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER))
        mText = root.findViewById(R.id.et_text)
        mText.isFocusable = false
        mText.isFocusableInTouchMode = false
        mText.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mText.isScreenReaderFocusable = false
        }
        mTimeAgo = TimeAgo(context)
        mProgress = root.findViewById(R.id.et_progress)
        setColors(COLOR_BG, COLOR_BAR_NORMAL, COLOR_BAR_WARN)
        if (isInEditMode) {
            val previewProgress = 0.6f
            val previewMillis = (1000 * 72).toLong() // 01:11
            setProgress(previewMillis, previewProgress)
        }
        val (colorBg, colorFill, colorWarn) = AuthenticatorUIManager.instance.config.themeObj().expirationTimer
        setColors(colorBg, colorFill, colorWarn)
    }
}