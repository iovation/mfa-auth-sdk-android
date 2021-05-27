package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children

internal fun ViewGroup.findAllText(textFinder: (TextView) -> Unit) {
    for (child in children) {
        if (child is TextView) {
            textFinder(child)
        } else if (child is ViewGroup) {
            child.findAllText(textFinder)
        }
    }
}