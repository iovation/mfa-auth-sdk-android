/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import com.launchkey.android.authenticator.sdk.ui.R

class ListHeadersUiProp internal constructor(visibilityFlag: Int, @ColorInt colorBackground: Int, @ColorInt colorText: Int) {
    var visibility = 0

    @get:ColorInt
    @ColorInt
    var colorBg: Int
        private set

    @get:ColorInt
    @ColorInt
    var colorText: Int
        private set

    internal constructor(visibilityFlag: Int) : this(visibilityFlag, 0, 0) {
        colorBg = 0
        colorText = 0
    }

    init {
        visibility = when (visibilityFlag) {
            View.VISIBLE, View.INVISIBLE, View.GONE -> visibilityFlag
            else -> View.VISIBLE
        }
        colorBg = colorBackground
        this.colorText = colorText
    }
}