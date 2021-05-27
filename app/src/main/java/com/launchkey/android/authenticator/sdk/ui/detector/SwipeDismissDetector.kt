/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.detector

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ListView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.launchkey.android.authenticator.sdk.ui.R

class SwipeDismissDetector(private val mContext: Context, private val mView: View, private val mPosition: Int) : OnTouchListener {
    private var mListViewToDisallowTouch: ListView? = null
    private var mSwipeRefreshToDisallowTouch: SwipeRefreshLayout? = null
    private var mXDown = 0f
    private var mXDelta = 0f
    private var mDisallowedTouchOnList = false
    private var mGestureIsHorizontal = false
    private val mAnimationMax: Long
    private var mLastUpdate: Long = 0
    private var mDismissThreshold = DISMISS_THRESHOLD
    private var mOutOfBoundsLimit = OUT_OF_BOUNDS_LIMIT
    private var mListener: SwipeDismissListener? = null
    private var mDragMarginLeft = 0
    fun setDismissThreshold(threshold: Float) {
        mDismissThreshold = threshold
    }

    fun setOutOfBoundsLimit(pixels: Float) {
        mOutOfBoundsLimit = pixels
    }

    fun setListView(listView: ListView?) {
        mListViewToDisallowTouch = listView
    }

    fun setSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout?) {
        mSwipeRefreshToDisallowTouch = swipeRefreshLayout
    }

    fun setOnSwipeDismissListener(l: SwipeDismissListener?) {
        mListener = l
    }

    @SuppressLint("ClickableViewAccessibility") // Not sure how to handle this yet
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val currentX = event.rawX
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mXDown = currentX
                return mXDown > mDragMarginLeft //ok if past the needed margin
            }
            MotionEvent.ACTION_MOVE -> {
                mXDelta = currentX - mXDown
                mGestureIsHorizontal = Math.abs(mXDelta) >= GESTURE_HORIZONTAL_SPEED
                if (mGestureIsHorizontal) {
                    notifySwipeStart(mXDelta)
                    setDisallowedTouchOnList(true)
                    moveView(mView, mXDelta)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                moveView(mView, mXDelta)
                checkUp(mView)
                setDisallowedTouchOnList(false)
                return true
            }
        }
        return false
    }

    fun resetView() {
        moveView(mView, 0f, true)
    }

    private fun moveView(v: View, distance: Float) {
        moveView(v, distance, false)
    }

    @Synchronized
    private fun moveView(v: View?, distance: Float, forceUpdate: Boolean) {
        val now = SystemClock.elapsedRealtime()
        mLastUpdate = if (forceUpdate || now >= mLastUpdate + UPDATE_INTERVAL_MIN) {
            now
        } else {
            return
        }
        if (v != null) {

            /* commented limited bounds to allow bi-directional dragging

            if (distance < -mOutOfBoundsLimit) {
                distance = -mOutOfBoundsLimit;
            }
            */

            //preferred approach is supported at 11+ and min is 10, so take into account...
            v.x = distance
        }
    }

    private fun checkUp(v: View?) {
        if (v == null) {
            return
        }
        val width = v.width.toFloat()
        val threshold = Math.abs(mXDelta) / width
        val dismiss = threshold > mDismissThreshold
        val fromX = v.x
        var toX = 0f
        if (dismiss) {
            toX = if (mXDelta > 0) width else -width
        }
        val animDurationDelta = Math.abs(fromX - toX)
        val animDurationRatio = animDurationDelta / width
        val duration = (mAnimationMax * animDurationRatio).toLong()

        /*
        float delta = dismiss ? width - mXDelta : mXDelta;
        delta = Math.abs(delta);
        float deltaWidthRatio = delta / width;
        long duration = (long) (mAnimationMax * deltaWidthRatio);
        */startPositionValueAnimator(dismiss, duration, fromX, toX)
    }

    private fun setDisallowedTouchOnList(disallowTouch: Boolean) {
        if (mDisallowedTouchOnList == disallowTouch) {
            return
        }
        mDisallowedTouchOnList = disallowTouch
        if (mListViewToDisallowTouch != null) {
            mListViewToDisallowTouch!!.requestDisallowInterceptTouchEvent(disallowTouch)
        }
        if (mSwipeRefreshToDisallowTouch != null) {
            val enable = !disallowTouch
            mSwipeRefreshToDisallowTouch!!.isEnabled = enable
            mSwipeRefreshToDisallowTouch!!.requestDisallowInterceptTouchEvent(disallowTouch)
        }
    }

    private fun startPositionValueAnimator(dismiss: Boolean, duration: Long, vararg positions: Float) {
        val updateListener = AnimatorUpdateListener { animation ->
            val x = animation.animatedValue as Float
            moveView(mView, x, true)
        }
        val animatorListener: Animator.AnimatorListener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                if (mListener != null && dismiss) {
                    mListener!!.onItemDismissStart(mPosition)
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!dismiss) {
                    resetView()
                }
                if (mListener != null && dismiss) {
                    mListener!!.onItemDismissEnd(mPosition)
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        }
        val animator = ValueAnimator
                .ofFloat(*positions)
                .setDuration(duration)
        animator.addUpdateListener(updateListener)
        animator.addListener(animatorListener)
        animator.start()
    }

    private fun notifySwipeStart(delta: Float) {
        if (mListener != null) {
            val direction = if (delta > 0) SwipeDismissListener.DIRECTION_RIGHT else SwipeDismissListener.DIRECTION_LEFT
            mListener!!.onItemSwipe(direction)
        }
    }

    interface SwipeDismissListener {
        fun onItemSwipe(direction: Int)
        fun onItemDismissStart(position: Int)
        fun onItemDismissEnd(position: Int)

        companion object {
            const val DIRECTION_LEFT = 0
            const val DIRECTION_RIGHT = 1
        }
    }

    companion object {
        private const val DISMISS_THRESHOLD = 0.45f
        private const val GESTURE_HORIZONTAL_SPEED = 30f
        private const val UPDATE_INTERVAL_MIN = 1000L / 60L
        private const val OUT_OF_BOUNDS_LIMIT = 50f
    }

    init {
        mAnimationMax = mContext.resources.getInteger(
                R.integer.lk_authorizations_item_reset_or_dismiss_max).toLong()
        mDragMarginLeft = mContext.resources.getDimensionPixelSize(
                R.dimen.lk_authorizations_item_drag_margin_left)
    }
}