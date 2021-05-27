/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.content.Context
import android.content.res.ColorStateList
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import com.launchkey.android.authenticator.sdk.core.auth_request_management.DenialReason
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.ViewAuthDenialoptionsBinding
import com.launchkey.android.authenticator.sdk.ui.internal.view.TimerDisplay

class IntRequestViewDenial @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr), TimerDisplay {
    companion object {
        private const val PARAMS_WIDTH = RadioGroup.LayoutParams.MATCH_PARENT
        private const val PARAMS_HEIGHT = RadioGroup.LayoutParams.WRAP_CONTENT
    }

    private val binding: ViewAuthDenialoptionsBinding =
            ViewAuthDenialoptionsBinding.inflate(LayoutInflater.from(context), this, true)
    private var radioTintList: ColorStateList? = null
    private var options: List<DenialReason>? = null
    private val paramsMargin: Int
    private val paramsPaddingLeft: Int
    private var callback: DenialActions? = null

    init {
        paramsMargin = resources.getDimensionPixelSize(R.dimen.lk_spacing_s)
        paramsPaddingLeft = resources.getDimensionPixelSize(R.dimen.lk_spacing_xxs)
        binding.authDoActionNegative.isEnabled = false
        binding.authDoActionNegative.setTextTop(R.string.ioa_ar_arb_directive_hold)
        binding.authDoActionNegative.setTextMain(R.string.ioa_ar_denialoptions_button_action)
        binding.authDoActionNegative.setOnClickListener {
            if (callback != null) {
                callback!!.onDenialReasonChosen(options!![binding.authDoOptions.checkedRadioButtonId])
            }
        }
        val denialOptionsUiProp = AuthenticatorUIManager.instance.config.themeObj().denialOptions
        setColors(denialOptionsUiProp.colorNormal, denialOptionsUiProp.colorChecked)
        isSaveEnabled = true
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val viewState = SavedState(superState)
        viewState.checkedId = binding.authDoOptions.checkedRadioButtonId
        return viewState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val viewState = state as SavedState
        super.onRestoreInstanceState(viewState.superState)
        binding.authDoOptions.check(viewState.checkedId)
    }

    private fun setColors(normal: Int, checked: Int) {
        val states = arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
        val colors = intArrayOf(
                normal,
                checked
        )
        radioTintList = ColorStateList(states, colors)
    }

    fun setCallback(callback: (DenialReason?) -> Unit) {
        this.callback = object : DenialActions {
            override fun onDenialReasonChosen(denialReason: DenialReason?) {
                callback(denialReason)
            }
        }
    }

    fun setOptions(options: List<DenialReason>?) {
        if (options == null || options.isEmpty()) {
            binding.authDoActionNegative.isEnabled = true
            return
        }
        this.options = options
        var option: DenialReason
        for (index in options.indices) {
            option = options[index]
            val radio = RadioButton(context)
            radio.id = index
            radio.text = option.message
            radio.setPadding(paramsPaddingLeft, 0, 0, 0)
            radio.setPaddingRelative(paramsPaddingLeft, 0, 0, 0)
            radio.textAlignment = TEXT_ALIGNMENT_VIEW_START
            radio.setOnClickListener { binding.authDoOptions.check(radio.id) }

            // Colors must be set before these options are passed so the list can be built
            if (radioTintList != null) {
                radio.buttonTintList = radioTintList
                radio.setTextColor(radioTintList)
            }
            val params = RadioGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT)
            params.setMargins(paramsMargin, paramsMargin, paramsMargin, paramsMargin)
            binding.authDoOptions.addView(radio, params)
        }
        binding.authDoOptions.setOnCheckedChangeListener { _, checkedId ->
            binding.authDoActionNegative.isEnabled = checkedId != -1 }
    }

    override fun onTimerUpdate(remainingMillis: Long, progress: Float) {
        binding.authDoTimer.setProgress(remainingMillis, progress)
    }

    interface DenialActions {
        fun onDenialReasonChosen(denialReason: DenialReason?)
    }

    private class SavedState : BaseSavedState {
        var checkedId = 0

        constructor(source: Parcel) : super(source) {
            checkedId = source.readInt()
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(checkedId)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(i: Int): Array<SavedState?> {
                    return arrayOfNulls(i)
                }
            }
        }
    }
}