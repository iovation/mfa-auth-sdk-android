package com.launchkey.android.authenticator.sdk.ui.internal.dialog

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.launchkey.android.authenticator.sdk.ui.R

object AutoUnlinkAlertDialogFragment {
    @JvmStatic
    fun show(fragmentManager: FragmentManager?,
             context: Context,
             threshold: Int) {
        val title = context.resources.getString(R.string.ioa_misc_autounlink_title)
        val message = context.resources.getQuantityString(R.plurals.ioa_misc_autounlink_message_format, threshold, threshold)
        val tag = AutoUnlinkAlertDialogFragment::class.java.simpleName
        AlertDialogFragment.Builder()
                .setTitle(title)
                .setMessage(message)
                .setPositiveButtonText(context.getString(R.string.ioa_generic_ok))
                .setTag(tag)
                .setCancellable(false)
                .build()
                .show(fragmentManager!!, tag)
    }
}