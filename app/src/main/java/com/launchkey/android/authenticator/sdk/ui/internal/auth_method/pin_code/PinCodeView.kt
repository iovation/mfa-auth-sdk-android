/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.ViewPinpadBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils

class PinCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val W_H_RATIO = 3f / 4f
        private const val STRING_BULLET = "\u25CF"
        private const val STRING_LTR = "\u200E"
        private val FAILED_ATTEMPT_ANIM_RES = R.anim.shake3
        private const val ALPHA_GT_EQ_MIN = 1.0f
        private const val ALPHA_LT_MIN = 0.3f
    }

    private val numKeys: Array<PinCodeKey>
    private val allKeys: Array<Array<View?>>
    private val deleteFrame: View
    private val delete: ImageView
    private val checkFrame: View
    private val check: ImageButton
    private var mViewer: TextView? = null
    private var isEditText = false
    private var mInput = ""
    private var failedAttemptAnim: Animation? = null
    private var obscureInput = false
    private var includingLast = false

    var listener: Listener? = null

    init {
        var overlayColorSelector = ContextCompat.getColorStateList(
                context, R.color.default_pinpad_button_overlay
        )
        val getAttrs = intArrayOf(R.attr.authenticatorPinPadButtonOverlayColorSelector)
        val a = context.obtainStyledAttributes(attrs, getAttrs)
        if (a.hasValue(0)) {
            overlayColorSelector = a.getColorStateList(0)
        }
        a.recycle()

        failedAttemptAnim = AnimationUtils.loadAnimation(getContext(), FAILED_ATTEMPT_ANIM_RES)
        val binding = ViewPinpadBinding.inflate(LayoutInflater.from(getContext()), this)
        val one: PinCodeKey = binding.ppButton1
        val two: PinCodeKey = binding.ppButton2
        val three: PinCodeKey = binding.ppButton3
        val four: PinCodeKey = binding.ppButton4
        val five: PinCodeKey = binding.ppButton5
        val six: PinCodeKey = binding.ppButton6
        val seven: PinCodeKey = binding.ppButton7
        val eight: PinCodeKey = binding.ppButton8
        val nine: PinCodeKey = binding.ppButton9
        val zero: PinCodeKey = binding.ppButton0
        numKeys = arrayOf(one, two, three, four, five, six, seven, eight, nine, zero)
        deleteFrame = binding.ppImagebuttonDeleteFrame
        delete = binding.ppImageDelete
        checkFrame = binding.ppImagebuttonCheckFrame
        check = binding.ppImagebuttonCheck
        allKeys = arrayOf(
            arrayOf(one, two, three),
            arrayOf(four, five, six),
            arrayOf(seven, eight, nine),
            arrayOf(deleteFrame, zero, checkFrame)
        )
        val numberListener = { number: Int ->
            UiUtils.vibratePinCircleFeedback(getContext())
            onPressed(number)
        }
        val checkListener = OnClickListener {
            if (!checkFrame.isClickable) {
                return@OnClickListener
            }
            setCheckFrameClickingPermission(false)
            UiUtils.vibratePinCircleFeedback(getContext())
            notifyPinSet()
        }
        val deleteListener = OnClickListener {
            UiUtils.vibratePinCircleFeedback(getContext())
            onDelete()
        }
        val deleteAllListener = OnLongClickListener {
            UiUtils.vibratePinCircleFeedback(getContext())
            clear()
            true
        }
        for (key in numKeys) {
            val number = key.getNumber()
            key.contentDescription =
                context.getString(R.string.ioa_acc_pincode_number_format, number)
            key.setOnClickListener(numberListener)
        }
        deleteFrame.setOnClickListener(deleteListener)
        deleteFrame.setOnLongClickListener(deleteAllListener)

        // Applying clickable and focusable properties programmatically.
        // Setting them up via XML had no apparent effect.
        // Calling setOnClickListener on all keys overrides their
        // clickable property. Could be trickling down to ImageViews.
        delete.isClickable = false
        delete.isFocusable = false
        delete.isFocusableInTouchMode = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            delete.isScreenReaderFocusable = false
        }
        checkFrame.setOnClickListener(checkListener)

        // Applying clickable and focusable properties programmatically.
        // Setting them up via XML had no apparent effect.
        // Calling setOnClickListener on all keys overrides their
        // clickable property. Could be trickling down to ImageViews.
        check.isClickable = false
        check.isFocusable = false
        check.isFocusableInTouchMode = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            check.isScreenReaderFocusable = false
        }
        tintViewDrawable(delete, overlayColorSelector)
        tintViewDrawable(check, overlayColorSelector)
        animateCheckButton(inputCoversMinimum = false, forceAnimation = true)

        val pinCodeUiProp = AuthenticatorUIManager.instance.config.themeObj().pinCode
        setButtonBackground(pinCodeUiProp.drawableBg)
        if (pinCodeUiProp.labelColors != null) {
            setLabelColor(pinCodeUiProp.labelColors)
        } else {
            setLabelColor(pinCodeUiProp.labelColor)
        }
    }

    fun ignoreTouches(ignoreTouches: Boolean) {
        for (keyViewArray in allKeys) {
            for (keyView in keyViewArray) {
                if (keyView != checkFrame) {
                    keyView!!.isClickable = !ignoreTouches
                    keyView.isEnabled = !ignoreTouches
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // Find whether the available width is greater than the max width, while maintaining the
        // width-height ratio
        val maxWidthInPixels =
            resources.getDimension(R.dimen.lk_security_factor_pinpad_maxwidth).toInt()
        var maxPossibleWidth = resolveSize(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.makeMeasureSpec(maxWidthInPixels, MeasureSpec.AT_MOST)
        )
        var maxPossibleHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (maxPossibleWidth < W_H_RATIO * maxPossibleHeight) {
            maxPossibleHeight = (maxPossibleWidth.toFloat() / W_H_RATIO).toInt()
        } else {
            maxPossibleWidth = (maxPossibleHeight.toFloat() * W_H_RATIO).toInt()
        }

        // Convert the width and height into a spec to be used for measuring the keys
        val maxPossibleWidthSpec =
            MeasureSpec.makeMeasureSpec(maxPossibleWidth, MeasureSpec.AT_MOST)
        val maxPossibleHeightSpec =
            MeasureSpec.makeMeasureSpec(maxPossibleHeight, MeasureSpec.AT_MOST)
        resizeKeys(maxPossibleWidthSpec, maxPossibleHeightSpec)
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    private fun resizeKeys(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // Distribute the available width and height amongst the keys
        val w = MeasureSpec.getSize(widthMeasureSpec) / allKeys[0].size.toFloat()
        val h = MeasureSpec.getSize(heightMeasureSpec) / allKeys.size.toFloat()
        for (keyArray in allKeys) {
            for (key in keyArray) {
                key?.measure(
                    MeasureSpec.makeMeasureSpec(w.toInt(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(h.toInt(), MeasureSpec.EXACTLY)
                )
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        repositionKeys(left, top, right, bottom)
    }

    private fun repositionKeys(left: Int, top: Int, right: Int, bottom: Int) {

        // Presumably the keys are all the same size
        val widthOfEachKey = allKeys[0][0]!!.measuredWidth
        val heightOfEachKey = allKeys[0][0]!!.measuredHeight

        // The offset for the keys are relative to the frame, not the screen
        val newLeft = (right - left - widthOfEachKey * allKeys[0].size) / 2
        val newTop = (bottom - top - heightOfEachKey * allKeys.size) / 2

        // Layout the keys in the center of the screen
        for (i in allKeys.indices) {
            for (j in allKeys[i].indices) {
                val horizontalOffset = j * widthOfEachKey + newLeft
                val verticalOffset = i * heightOfEachKey + newTop
                allKeys[i][j]!!.layout(
                    horizontalOffset, verticalOffset, horizontalOffset + widthOfEachKey,
                    verticalOffset + heightOfEachKey
                )
            }
        }
    }

    private fun tintViewDrawable(v: ImageView?, l: ColorStateList?) {
        if (v == null || v.drawable == null) {
            return
        }
        val d = v.drawable
        val wD = DrawableCompat.wrap(d)
        DrawableCompat.setTintList(wD.constantState!!.newDrawable(), l)
        v.setImageDrawable(wD)
    }

    fun startPinCodeFailedAnimation() {
        startAnimation(failedAttemptAnim)
    }

    private fun onPressed(digit: Int) {
        input += digit
    }

    private fun onDelete() {
        if (input.length >= 2) {
            input = input.substring(0, input.length - 1)
        } else if (input.length == 1) {
            clear()
        }
    }

    private var inputCoveredMinimum = false
    fun animateCheckButton(inputCoversMinimum: Boolean) {
        animateCheckButton(inputCoversMinimum, false)
    }

    private fun animateCheckButton(inputCoversMinimum: Boolean, forceAnimation: Boolean) {
        if (!forceAnimation && inputCoversMinimum == inputCoveredMinimum) {
            return
        }
        inputCoveredMinimum = inputCoversMinimum
        setCheckFrameClickingPermission(inputCoversMinimum)
        checkFrame.alpha =
            if (inputCoversMinimum) ALPHA_GT_EQ_MIN else ALPHA_LT_MIN
    }

    private var input: String
        get() = mInput.trim()
        private set(input) {
            val deleted = input.length > input.length
            mInput = input.trim()
            updateViewer(deleted)
            notifyPinChange()
        }

    fun clear() {
        input = ""
    }

    fun setObscureInput(obscureInput: Boolean, includingLast: Boolean) {
        this.obscureInput = obscureInput
        this.includingLast = includingLast
    }

    private fun notifyPinSet() {
        listener?.onPinSet(input)
    }

    private fun notifyPinChange() {
        listener?.onPinChange(input)
    }

    private fun updateViewer(isActionDelete: Boolean) = viewer?.let {
        var input = input
        if (obscureInput) {
            var len = input.length
            val last = if (len == 0 || isActionDelete) "" else input.substring(input.length - 1)

            //skip last to later add if not deleted
            len = if (isActionDelete) len else len - 1
            val inputBuilder = StringBuilder().append(STRING_LTR)
            for (i in 0 until len) {
                inputBuilder.append(STRING_BULLET)
            }
            inputBuilder.append(last)
            input = inputBuilder.toString()
        }

        //hide all, at this point, all are obscured but the last one if none has been deleted
        if (!isActionDelete && includingLast && input.isNotBlank()) {
            val lastDigit = input.substring(input.length - 1)
            input = input.replace(lastDigit, STRING_BULLET)
        }

        //bug setting empty string to TEXTVIEW not reliable
        if (!isEditText) {
            viewer!!.visibility = if (input.isEmpty()) INVISIBLE else VISIBLE
        }
        viewer!!.text = input
    }

    fun showCheckButton() {
        checkFrame.visibility = VISIBLE
    }

    private fun setCheckFrameClickingPermission(checkFrameClickable: Boolean) {
        checkFrame.isClickable = checkFrameClickable
    }

    var viewer: TextView?
        get() = mViewer
        set(viewer) {
            mViewer = viewer?.also {
                isEditText = viewer is EditText
                updateViewer(true)
            }
        }

    fun setLabelColor(color: Int) {
        for (key in numKeys) {
            key.setTextColor(color)
        }
        delete.setImageDrawable(UiUtils.tintDrawable(delete.drawable, color))
        check.setImageDrawable(UiUtils.tintDrawable(check.drawable, color))
    }

    fun setLabelColor(colors: ColorStateList?) {
        for (key in numKeys) {
            key.setTextColor(colors)
        }
        tintViewDrawable(delete, colors)
        tintViewDrawable(check, colors)
    }

    fun setButtonBackground(buttonBackground: Drawable?) {
        if (buttonBackground != null) {
            for (key in numKeys) {
                key.background = buttonBackground.constantState!!.newDrawable().mutate();
            }
            deleteFrame.background = buttonBackground.constantState!!.newDrawable().mutate();
            checkFrame.background = buttonBackground.constantState!!.newDrawable().mutate();
        }
    }

    interface Listener {
        fun onPinSet(pinCode: String)
        fun onPinChange(pinCode: String)
    }
}