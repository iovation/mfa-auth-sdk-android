/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.biometric

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.DialogBiometricScannerContentBinding
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.DialogFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils

class BiometricScanDialogFragment : AlertDialogFragment() {
    private lateinit var binding: DialogBiometricScannerContentBinding
    override fun dismiss() {
        super.dismiss()
        dialogFragmentViewModel.changeState(DialogFragmentViewModel.State.Gone)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogBiometricScannerContentBinding.inflate(LayoutInflater.from(context), null, false)
        return binding.root
    }

    fun displayFailureWarning(authMethodFailure: AuthMethodFailure) {
        binding.scanMessage.text = UiUtils.getBiometricSensorErrorMessage(authMethodFailure)
    }

    companion object {
        fun show(
                fragmentManager: FragmentManager,
                context: Context,
                scanMessage: String
        ): BiometricScanDialogFragment {
            val arguments = Bundle().apply {
                putString(NEGATIVE_BUTTON_TEXT_ARG, context.getString(R.string.ioa_generic_cancel))
                putBoolean(CANCELLABLE_ARG, false)
                putString(MESSAGE_ARG, scanMessage)
            }

            return BiometricScanDialogFragment().apply {
                setArguments(arguments)
                show(fragmentManager, BiometricScanDialogFragment::class.java.simpleName)
            }
        }
    }
}