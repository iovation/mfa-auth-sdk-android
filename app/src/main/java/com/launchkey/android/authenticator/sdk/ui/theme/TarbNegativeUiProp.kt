/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

class TarbNegativeUiProp : TarbUiProp {
    internal constructor(@DrawableRes backgroundResId: Int, @ColorRes textColorResId: Int, @ColorRes fillColorResId: Int) : super(backgroundResId, textColorResId, fillColorResId)

    internal constructor(background: Drawable?, @ColorInt textColor: Int, @ColorInt fillColor: Int) : super(background, textColor, fillColor)
}