/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.view.View

class AuthRequestAppBar internal constructor(visibility: Int) {
    val visibility: Int = when (visibility) {
        View.VISIBLE, View.INVISIBLE, View.GONE -> visibility
        else -> View.VISIBLE
    }
}