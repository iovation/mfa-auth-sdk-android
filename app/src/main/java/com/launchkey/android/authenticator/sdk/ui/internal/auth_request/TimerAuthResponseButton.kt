/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.internal.util.FpsRefreshHelper

open class TimerAuthResponseButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AuthResponseButton(context, attrs, defStyleAttr) {
    private val timerAnimator: ValueAnimator?
    private val onAnimUpdate: AnimatorUpdateListener
    private val onAnimEvents: Animator.AnimatorListener
    private val refresher: FpsRefreshHelper
    private val timerDrawable: Drawable?
    private var bgRect: RectF? = null
    private var timerRect: RectF? = null
    private var handlerTouches = false
    private fun updateProgress(progress: Float) {
        if (timerRect != null) {
            val RTL_ON = resources.getBoolean(R.bool.is_right_to_left)
            if (RTL_ON) {
                timerRect!!.left = width * (1 - progress)
            } else {
                timerRect!!.right = width * progress
            }
        }
    }

    private fun startTimer() {
        if (timerAnimator == null) {
            return
        }
        timerAnimator.addListener(onAnimEvents)
        timerAnimator.addUpdateListener(onAnimUpdate)
        timerAnimator.start()
    }

    private fun cancelTimer() {
        if (timerAnimator != null) {
            timerAnimator.removeAllListeners()
            timerAnimator.removeAllUpdateListeners()
            timerAnimator.cancel()
        }
        updateProgress(0.0f)
        refresher.forceInvalidate()
    }

    private fun stopHandlingTouchEvents() {
        handlerTouches = false
        cancelTimer()
    }

    fun setTimerColorResource(@ColorRes colorResource: Int) {
        val color = ContextCompat.getColor(context, colorResource)
        setTimerColor(color)
    }

    fun setTimerColor(@ColorInt color: Int) {
        if (timerDrawable != null) {
            timerDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            refresher.forceInvalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (timerRect == null) {
            val RTL_ON = resources.getBoolean(R.bool.is_right_to_left)
            timerRect = if (RTL_ON) {
                RectF(w.toFloat(), 0f, w.toFloat(), h.toFloat())
            } else {
                RectF(0f, 0f, 0f, h.toFloat())
            }
        }
        if (bgRect == null) {
            bgRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        }
        timerDrawable?.setBounds(0, 0, w, h)
        super.onSizeChanged(w, h, oldw, oldh)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handlerTouches = true
                cancelTimer()
                startTimer()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!handlerTouches) {
                    return super.onTouchEvent(event)
                }
                val withinBoundaries = bgRect!!.contains(event.x, event.y)
                if (!withinBoundaries) {
                    stopHandlingTouchEvents()
                    return super.onTouchEvent(event)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!handlerTouches) {
                    return super.onTouchEvent(event)
                }
                stopHandlingTouchEvents()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        if (timerDrawable != null) {
            canvas.save()
            canvas.clipRect(timerRect!!)
            timerDrawable.draw(canvas)
            canvas.restore()
        }
        super.onDraw(canvas)
    }

    companion object {
        private const val FPS = 60
        private const val DEFAULT_COLOR_TIMER = Color.GRAY
        private const val DEFAULT_DURATION_HOLD: Long = 270
        private val DEFAULT_INTERPOLATOR: Interpolator = LinearInterpolator()
    }

    init {
        refresher = FpsRefreshHelper(this, FPS)
        timerAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        timerAnimator.setDuration(DEFAULT_DURATION_HOLD)
        timerAnimator.setInterpolator(DEFAULT_INTERPOLATOR)
        onAnimUpdate = AnimatorUpdateListener { valueAnimator ->
            updateProgress(valueAnimator.animatedValue as Float)
            refresher.invalidate()
        }
        onAnimEvents = object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                refresher.forceInvalidate()
            }

            override fun onAnimationEnd(animator: Animator) {
                refresher.forceInvalidate()
                if (handlerTouches) {
                    performClick()
                }
                stopHandlingTouchEvents()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        }
        val state = if (background == null) null else background.constantState
        timerDrawable = state?.newDrawable() ?: background
        timerDrawable?.mutate()
        setTimerColor(DEFAULT_COLOR_TIMER)
    }
}