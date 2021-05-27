/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

open class TarbUiProp {
    var useReferences = false
        private set

    @DrawableRes
    var backgroundResId = 0
        private set

    @ColorRes
    val colorTextResId: Int

    @ColorRes
    val fillColorResId: Int
    var background: Drawable? = null
        get() {
            return if (field == null) null else field!!.constantState!!.newDrawable().mutate()
        }
        private set

    @ColorInt
    var colorText = 0
        private set

    @ColorInt
    var colorFill = 0
        private set

    internal constructor(@DrawableRes backgroundResId: Int, @ColorRes textColorResId: Int, @ColorRes fillColorResId: Int) {
        this.backgroundResId = backgroundResId
        colorTextResId = textColorResId
        this.fillColorResId = fillColorResId
        useReferences = true
    }

    internal constructor(background: Drawable?, @ColorInt textColor: Int, @ColorInt fillColor: Int) {
        this.background = background
        colorText = textColor
        colorFill = fillColor
        useReferences = false
        colorTextResId = 0
        fillColorResId = 0
    }
}