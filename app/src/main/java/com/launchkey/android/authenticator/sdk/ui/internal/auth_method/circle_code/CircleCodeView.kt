/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager.CircleCodeTick
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class CircleCodeView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    companion object {
        private const val HIGHLIGHT_ALPHA = 0.3f
        private const val HIGHLIGHT_RADIUS_RATIO = 0.95f
        private const val CENTER_RADIUS_SPACE_RATIO = 0.25f
        private const val MARK_STROKE_DP = 2f
        private const val MARK_BEGIN_SPACE_RATIO = 0.85f
        private const val MARK_END_SPACE_RATIO = 0.95f
        private const val FRAMES_PER_MILLI = (1000 / 60).toLong()
    }

    private lateinit var slices: Array<Slice?>

    @Volatile
    private var ignoreTouches = false
    private var lastInvalidate: Long = 0
    private var listener: Listener? = null
    private var sliceCurrentlySelected: CircleCodeTick? = null
    private val circleCode = mutableListOf<CircleCodeTick>()
    private var centerRadius = 10f
    private var minimumDistanceFromCenter = 10f
    private var isInteracting = false
    private var validArea = true
    private var boundsRadius = 0f
    private val bounds = RectF()
    private val paintCenter: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = UiUtils.dpToPx(context, MARK_STROKE_DP).toFloat()
    }
    private val paintMarks: Paint = Paint(paintCenter)
    private val paintHighlight: Paint = Paint(paintCenter)

    init {
        val circleCodeUiProp = AuthenticatorUIManager.instance.config.themeObj().circleCode
        setHighlightColor(circleCodeUiProp.colorHighlight)
        setMarksColor(circleCodeUiProp.colorMarks)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val minDimen = min(w, h)
        val wOffset = (w - minDimen) / 2
        val hOffset = (h - minDimen) / 2
        bounds[wOffset.toFloat(), hOffset.toFloat(), (wOffset + minDimen).toFloat()] = (hOffset + minDimen).toFloat()
        measure()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (ignoreTouches) {
            return false
        }
        val action = event.action
        val x = event.x
        val y = event.y
        val dist = distanceFromCenter(x, y)
        isInteracting = true
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                //first touch down outside of the radius (corners of the View) are invalid areas
                //NOT falling under ACTION_MOVE since outside of radius IS valid if started
                // in a valid radius.
                if (dist > boundsRadius) {
                    return false
                }
                validArea = true //reset for ACTION_DOWN
                val nowInvalidArea = dist < minimumDistanceFromCenter

                //if first time it's an invalid area then call onActionUp() once
                if (validArea && nowInvalidArea) {
                    validArea = false
                }
                if (!validArea) {
                    return false
                }
                val prevSliceSelected = sliceCurrentlySelected
                val flippedAngle = (360 - angleFromFirstSlice(x, y)) % 360
                sliceCurrentlySelected = CircleCodeTick.getTickFromAngle(flippedAngle)
                if (prevSliceSelected != sliceCurrentlySelected) {
                    processTick(sliceCurrentlySelected)
                    performClick()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val nowInvalidArea = dist < minimumDistanceFromCenter
                if (validArea && nowInvalidArea) {
                    validArea = false
                }
                if (!validArea) {
                    return false
                }
                val prevSliceSelected = sliceCurrentlySelected
                val flippedAngle = (360 - angleFromFirstSlice(x, y)) % 360
                sliceCurrentlySelected = CircleCodeTick.getTickFromAngle(flippedAngle)
                if (prevSliceSelected != sliceCurrentlySelected) {
                    processTick(sliceCurrentlySelected)
                    performClick()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!validArea) {
                    validArea = true
                    return false
                }
                captureCircleCode()
            }
        }
        forceInvalidate()
        return true
    }

    override fun invalidate() {
        val now = SystemClock.elapsedRealtime()
        if (lastInvalidate + FRAMES_PER_MILLI < now) {
            return
        }
        lastInvalidate = now
        super.invalidate()
    }

    override fun performClick(): Boolean {
        contentDescription = resources.getString(R.string.ioa_sec_cir_octant_content_description, tickToValue(sliceCurrentlySelected!!) + 1)
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //marks
        for (s in slices) {
            canvas.drawLine(
                    s!!.markPoints[0],
                    s.markPoints[1],
                    s.markPoints[2],
                    s.markPoints[3],
                    paintMarks)
        }

        //highlight
        if (isInteracting) {
            canvas.drawPath(slices[tickToValue(sliceCurrentlySelected!!)]!!.highlightPath, paintHighlight)
        }

        //center
        canvas.drawCircle(
                bounds.centerX(),
                bounds.centerY(),
                centerRadius,
                (if (isInteracting) paintCenter else paintMarks))
    }

    private fun captureCircleCode() {
        onActionUp()
        notifyCircleCodeEntered()
        forceInvalidate()
        reset()
    }

    private fun onActionUp() {
        contentDescription = resources.getString(R.string.ioa_sec_cir_content_description)
        isInteracting = false
        processTick(sliceCurrentlySelected)
    }

    private fun notifyCircleCodeEntered() {
        listener?.onCircleCodeEntered(ArrayList(circleCode))
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    private fun processTick(circleCodeTick: CircleCodeTick?) {
        if (!inputShouldBeRecorded(circleCodeTick)) return

        circleCode.add(circleCodeTick!!)
        vibrate()
    }

    private fun inputShouldBeRecorded(circleCodeTick: CircleCodeTick?): Boolean {
        return circleCode.isEmpty() || circleCode.last() != circleCodeTick
    }

    private fun forceInvalidate() {
        super.invalidate()
    }

    fun ignoreTouches(ignoreTouches: Boolean) {
        this.ignoreTouches = ignoreTouches
    }

    private fun setHighlightColor(highlightColor: Int) {
        val solidHighlightColor = Color.argb(
                255,
                Color.red(highlightColor),
                Color.green(highlightColor),
                Color.blue(highlightColor))
        paintCenter.color = solidHighlightColor
        paintHighlight.color = solidHighlightColor
        paintHighlight.alpha = (255f * HIGHLIGHT_ALPHA).toInt()
    }

    private fun setMarksColor(marksColor: Int) {
        paintMarks.color = marksColor
    }

    private fun measure() {
        boundsRadius = bounds.width() / 2f //bounds must be square.
        centerRadius = boundsRadius * CENTER_RADIUS_SPACE_RATIO
        minimumDistanceFromCenter = centerRadius
        val numberOfButtons = CircleCodeTick.values().size
        slices = arrayOfNulls(numberOfButtons)
        var angleMark: Int
        var markPointBegin: IntArray
        var markPointEnd: IntArray
        val markBeginDistance = boundsRadius * MARK_BEGIN_SPACE_RATIO
        val markEndDistance = boundsRadius * MARK_END_SPACE_RATIO
        var angleHighlightBegin: Int
        var highlightPath: Path
        val highlightBoundsSpacing = (1f - HIGHLIGHT_RADIUS_RATIO) * bounds.width() / 2f
        val highlightBounds = RectF(
                bounds.left + highlightBoundsSpacing,
                bounds.top + highlightBoundsSpacing,
                bounds.right - highlightBoundsSpacing,
                bounds.bottom - highlightBoundsSpacing
        )
        val incrementAngle = CircleCodeTick.ANGLE_OFFSET.toInt()
        for (i in 0 until numberOfButtons) {
            angleMark = i * incrementAngle
            markPointBegin = positionInCircleAt(angleMark, markBeginDistance)
            markPointEnd = positionInCircleAt(angleMark, markEndDistance)
            angleHighlightBegin = angleMark - incrementAngle / 2
            highlightPath = Path()
            highlightPath.moveTo(bounds.centerX(), bounds.centerY())
            highlightPath.arcTo(highlightBounds, angleHighlightBegin.toFloat(), incrementAngle.toFloat())
            highlightPath.close()
            slices[i] = Slice(floatArrayOf(markPointBegin[0].toFloat(), markPointBegin[1].toFloat(), markPointEnd[0].toFloat(), markPointEnd[1].toFloat()),
                    highlightPath)
        }
    }

    private fun vibrate() {
        UiUtils.vibratePinCircleFeedback(context)
    }

    private fun reset() {
        validArea = false
        circleCode.clear()
        sliceCurrentlySelected = null
        forceInvalidate()
    }

    private fun angleFromFirstSlice(x: Float, y: Float): Int {
        return Math.toDegrees(atan2((bounds.centerY() - y).toDouble(), (x - bounds.centerX()).toDouble())).toInt()
    }

    private fun positionInCircleAt(angle: Int, radius: Float): IntArray {
        //[x, y]
        val points = IntArray(2)
        points[0] = (radius * cos(Math.toRadians(angle.toDouble()))).toInt()
        points[1] = (radius * sin(Math.toRadians(angle.toDouble()))).toInt()

        //relative to the center
        points[0] += bounds.centerX().toInt()
        points[1] += bounds.centerY().toInt()
        return points
    }

    private fun distanceFromCenter(x: Float, y: Float): Float {
        val dx = bounds.centerX() - x
        val dy = bounds.centerY() - y
        val d = sqrt(dx.toDouble().pow(2.0) + dy.toDouble().pow(2.0))
        return abs(d.toInt()).toFloat()
    }

    private fun tickToValue(circleCodeTick: CircleCodeTick): Int {
        return when (circleCodeTick) {
            CircleCodeTick.RIGHT -> 0
            CircleCodeTick.UP_RIGHT -> 1
            CircleCodeTick.UP -> 2
            CircleCodeTick.UP_LEFT -> 3
            CircleCodeTick.LEFT -> 4
            CircleCodeTick.DOWN_LEFT -> 5
            CircleCodeTick.DOWN -> 6
            CircleCodeTick.DOWN_RIGHT -> 7
            else -> throw IllegalArgumentException("Unrecognized CircleCodeTick passed in")
        }
    }

    private class Slice(val markPoints: FloatArray, val highlightPath: Path)

    fun interface Listener {
        fun onCircleCodeEntered(circleCode: List<CircleCodeTick>)
    }
}