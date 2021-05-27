/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.FrameLayout
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentPanelSettingsBinding

class SettingsPanel @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : FrameLayout(context!!, attrs, defStyleAttr, defStyleRes) {
    private val binding: FragmentPanelSettingsBinding
    fun setRemoveText(removeTextId: Int) {
        binding.panelSettingsTextRemove.setText(removeTextId)
    }

    fun setRemoveButtonText(removeButtonTextId: Int) {
        binding.panelSettingsButtonRemove.setText(removeButtonTextId)
    }

    fun setVerifiedWhenText(verifiedWhenTextId: Int) {
        binding.panelSettingsTextVerifiedwhen.setText(verifiedWhenTextId)
    }

    fun setVerifiedExtraText(verifiedExtraText: String?) {
        val visibility = if (verifiedExtraText == null || verifiedExtraText.trim { it <= ' ' }.isEmpty()) GONE else VISIBLE
        binding.panelSettingsTextExtra.text = verifiedExtraText
        binding.panelSettingsTextExtra.visibility = visibility
    }

    fun playVerifiedExtraTextAnim(animRes: Int) {
        if (animRes > 0) {
            binding.panelSettingsTextExtra.startAnimation(AnimationUtils.loadAnimation(context, animRes))
        }
    }

    fun setOnRemoveButtonClick(l: OnClickListener?) {
        binding.panelSettingsButtonRemove.setOnClickListener(l)
    }

    var isSwitchOn: Boolean
        get() = binding.panelSettingsSwitch.isChecked
        set(on) {
            binding.panelSettingsSwitch.isChecked = on
        }

    fun setOnSwitchChangeListener(l: CompoundButton.OnCheckedChangeListener?) {
        binding.panelSettingsSwitch.setOnCheckedChangeListener(l)
    }

    fun setOnSwitchClickedListener(listener: OnClickListener?) {
        binding.panelSettingsSwitch.setOnClickListener(listener)
    }

    fun disallowSwitchSwipe() {
        binding.panelSettingsSwitch.setOnTouchListener { v, event -> event.actionMasked == MotionEvent.ACTION_MOVE }
    }

    init {
        binding = FragmentPanelSettingsBinding.inflate(LayoutInflater.from(getContext()), this, true)
    }
}