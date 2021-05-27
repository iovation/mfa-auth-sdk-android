package com.launchkey.android.authenticator.sdk.ui.internal.dialog

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.launchkey.android.authenticator.sdk.ui.R

class ProgressDialogFragment : AlertDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = requireArguments().getString(TITLE_ARG)
        val message = requireArguments().getString(MESSAGE_ARG)
        val indeterminate = requireArguments().getBoolean(INDETERMINATE)
        return buildProgressDialog(
            requireContext(),
            title,
            message,
            indeterminate)
    }
    
    companion object {
        private const val INDETERMINATE = "indeterminate"
        
        @JvmStatic
        fun newInstance(
            title: String?,
            message: String?,
            cancellable: Boolean,
            indeterminate: Boolean): ProgressDialogFragment {
            val frag = ProgressDialogFragment()
            val args = Bundle()
            args.putString(TITLE_ARG, title)
            args.putString(MESSAGE_ARG, message)
            args.putBoolean(CANCELLABLE_ARG, cancellable)
            args.putBoolean(INDETERMINATE, indeterminate)
            frag.arguments = args
            return frag
        }
        
        @JvmStatic
        fun show(
            title: String?,
            message: String?,
            cancellable: Boolean,
            indeterminate: Boolean,
            fragmentManager: FragmentManager,
            tag: String?
        ) : ProgressDialogFragment {
            return newInstance(title, message, cancellable, indeterminate).also {
                it.show(fragmentManager, tag)
            }
        }
        
        private fun buildProgressDialog(context: Context,
                                        title: String?,
                                        message: String?,
                                        indeterminate: Boolean): ProgressDialog {
            val progressDialog = ProgressDialog(context, R.style.AuthenticatorAlertDialogStyle)
            if (title != null) {
                progressDialog.setTitle(title)
            }
            if (message != null) {
                progressDialog.setMessage(message)
            }
            progressDialog.isIndeterminate = indeterminate
            return progressDialog
        }
    }
}