package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.content.Context
import android.widget.Toast

internal fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG) .show()
}

internal fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT) .show()
}