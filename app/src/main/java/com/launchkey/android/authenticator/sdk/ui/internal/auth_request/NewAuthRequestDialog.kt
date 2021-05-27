package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.FragmentManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AlertDialogFragment

object NewAuthRequestDialog {
    @JvmStatic
    fun show(fragmentManager: FragmentManager?,
             context: Context,
             positiveButtonClickListener: DialogInterface.OnClickListener?) {
        val tag = NewAuthRequestDialog::class.java.simpleName
        val dialog = AlertDialogFragment.Builder()
                .setTitle(context.getString(R.string.ioa_ar_dialog_newdialog_title))
                .setMessage(context.getString(R.string.ioa_ar_dialog_newdialog_message))
                .setPositiveButtonText(context.getString(R.string.ioa_generic_ok))
                .setCancellable(true)
                .setTag(tag)
                .build()
        dialog.setPositiveButtonClickListener(positiveButtonClickListener)
        dialog.show(fragmentManager!!, tag)
    }
}