/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class NegativeButtonUiProp {
    var drawableBg: Drawable
        get() {
            return field.constantState!!.newDrawable().mutate()
        }
        private set

    // One of the following
    @get:ColorInt
    @ColorInt
    var colorText = 0
        private set
    var colorStateListText: ColorStateList? = null
        private set

    internal constructor(drawableBackground: Drawable, @ColorInt colorText: Int) {
        drawableBg = drawableBackground
        this.colorText = colorText
    }

    internal constructor(drawableBackground: Drawable, colorStateListText: ColorStateList?) {
        drawableBg = drawableBackground
        this.colorStateListText = colorStateListText
    }
}