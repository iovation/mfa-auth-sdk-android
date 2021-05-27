/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class SettingsHeadersUiProp internal constructor(colorBackground: Drawable,
                                                 @field:ColorInt @get:ColorInt @param:ColorInt val colorText: Int) {
    val colorBackground = colorBackground
        get() {
            return field.constantState!!.newDrawable().mutate()
        }
}