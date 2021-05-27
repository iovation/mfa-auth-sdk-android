package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.launchkey.android.authenticator.sdk.ui.R

internal fun Snackbar.setMaxLines(maxLines: Int) = apply {
    view.findViewById<TextView>(R.id.snackbar_text)?.maxLines = maxLines
}