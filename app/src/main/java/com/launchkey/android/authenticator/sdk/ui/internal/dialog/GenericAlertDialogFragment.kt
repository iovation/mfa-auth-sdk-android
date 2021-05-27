package com.launchkey.android.authenticator.sdk.ui.internal.dialog

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.launchkey.android.authenticator.sdk.ui.R

object GenericAlertDialogFragment {
    @JvmStatic
    fun show(
        fragmentManager: FragmentManager?,
        context: Context,
        title: String?,
        message: String?,
        positiveButtonText: String?,
        cancellable: Boolean,
        negativeButtonText: String?,
        tag: String?
    ): AlertDialogFragment {
        val builder = AlertDialogFragment.Builder()
        if (title != null) {
            builder.setTitle(title)
        }
        if (message != null) {
            builder.setMessage(message)
        }
        if (positiveButtonText != null) {
            builder.setPositiveButtonText(positiveButtonText)
        } else {
            builder.setPositiveButtonText(context.getString(R.string.ioa_generic_ok))
        }
        builder.setCancellable(cancellable)
        if (negativeButtonText != null) {
            builder.setNegativeButtonText(negativeButtonText)
        }
        if (tag != null) {
            builder.setTag(tag)
        }
        
        return builder.build().apply {
            show(fragmentManager!!, tag)
        }
    }
}