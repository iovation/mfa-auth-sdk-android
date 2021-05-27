/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.view.View

@Deprecated(message = "No longer used, reference *Prop classes in AuthenticatorTheme directly")
interface UiProp {
    val tagRes: Int
    fun apply(view: View)
}