package com.launchkey.android.authenticator.sdk.ui.internal.dialog

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.launchkey.android.authenticator.sdk.ui.R

object AutoUnlinkWarningAlertDialogFragment {
    @JvmStatic
    fun show(fragmentManager: FragmentManager?,
             context: Context,
             attemptsRemaining: Int) {
        val title = context.resources.getQuantityString(R.plurals.ioa_misc_autounlink_warning_title_format, attemptsRemaining, attemptsRemaining)
        val message = context.resources.getQuantityString(R.plurals.ioa_misc_autounlink_warning_message_format, attemptsRemaining, attemptsRemaining)
        val tag = AutoUnlinkWarningAlertDialogFragment::class.java.simpleName
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