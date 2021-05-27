package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AlertDialogFragment

object LocationsWaitDialog {
    fun show(
        context: Context,
        fm: FragmentManager?
    ) {
        AlertDialogFragment.Builder()
            .setTitle(context.getString(R.string.ioa_sec_geo_add_error_nogeofence_title))
            .setPositiveButtonText(context.getString(R.string.ioa_generic_ok))
            .setMessage(context.getString(R.string.ioa_sec_geo_add_error_nogeofence_message))
            .setCancellable(true)
            .build()
            .show(fm!!, LocationsWaitDialog::class.java.simpleName)
    }
}