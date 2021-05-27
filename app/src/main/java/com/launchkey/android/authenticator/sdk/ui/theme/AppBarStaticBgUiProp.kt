/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import androidx.annotation.ColorInt

class AppBarStaticBgUiProp internal constructor(parentProp: AppBarUiProp) {
    @get:ColorInt
    @ColorInt
    val colorBg: Int = parentProp.backgroundColor
}