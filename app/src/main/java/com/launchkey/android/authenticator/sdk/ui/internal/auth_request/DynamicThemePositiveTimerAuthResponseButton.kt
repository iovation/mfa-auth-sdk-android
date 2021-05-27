/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.content.Context
import android.util.AttributeSet
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager

class DynamicThemePositiveTimerAuthResponseButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TimerAuthResponseButton(context, attrs, defStyleAttr) {
    init {
        val tarbUiProp = AuthenticatorUIManager.instance.config.themeObj().arb
        if (tarbUiProp.useReferences) {
            setTextColorRes(tarbUiProp.colorTextResId)
            setTimerColorResource(tarbUiProp.fillColorResId)
            setBackgroundResource(tarbUiProp.backgroundResId)
        } else {
            setTextColor(tarbUiProp.colorText)
            setTimerColor(tarbUiProp.colorFill)
            background = tarbUiProp.background
        }
    }
}
