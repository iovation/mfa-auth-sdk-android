package com.launchkey.android.authenticator.sdk.ui.internal.dialog

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.launchkey.android.authenticator.sdk.ui.R

object HelpDialogFragment {
    @JvmStatic
    fun show(fragmentManager: FragmentManager?,
             context: Context,
             title: String?,
             message: String?) {
        val tag = HelpDialogFragment::class.java.simpleName
        AlertDialogFragment.Builder()
                .setTitle(title ?: context.getString(R.string.ioa_generic_help))
                .setMessage(message!!)
                .setPositiveButtonText(context.getString(R.string.ioa_generic_ok))
                .setTag(tag)
                .setCancellable(true)
                .build()
                .show(fragmentManager!!, tag)
    }
}