/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class ListItemsUiProp(colorBg: Drawable,
                           @field:ColorInt @get:ColorInt @param:ColorInt val colorText: Int) {
    val colorBg = colorBg
        get() {
            return field.constantState!!.newDrawable().mutate()
        }
}