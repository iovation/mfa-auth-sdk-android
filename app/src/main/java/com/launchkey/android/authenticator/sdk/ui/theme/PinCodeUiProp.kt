/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class PinCodeUiProp {
    @ColorInt
    var labelColor = 0
        private set
    var labelColors: ColorStateList? = null
        private set
    var drawableBg: Drawable
        get() {
            return field.constantState!!.newDrawable().mutate()
        }
        private set

    internal constructor(backgroundDrawable: Drawable, @ColorInt labelColor: Int) {
        drawableBg = backgroundDrawable
        this.labelColor = labelColor
    }

    internal constructor(backgroundDrawable: Drawable, labelColors: ColorStateList?) {
        drawableBg = backgroundDrawable
        this.labelColors = labelColors
    }
}