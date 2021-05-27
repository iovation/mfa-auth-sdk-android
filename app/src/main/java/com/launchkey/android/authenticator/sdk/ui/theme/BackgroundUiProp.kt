/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.graphics.drawable.Drawable

class BackgroundUiProp internal constructor(drawableBg: Drawable) {
    val drawableBg = drawableBg
        get() {
            return field.constantState!!.newDrawable().mutate()
        }
}