package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentPincodeBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.AuthMethodActivity
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.bundleArgument
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding
import java.lang.IllegalArgumentException

class PinCodeFragment : BaseAppCompatFragment(R.layout.fragment_pincode) {
    private val page: AuthMethodActivity.Page by bundleArgument(AuthMethodActivity.PAGE_KEY)
    private val binding: FragmentPincodeBinding by viewBinding(FragmentPincodeBinding::bind)
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace(binding.pinCodeFragmentContainer.id, when (page) {
                    AuthMethodActivity.Page.ADD -> PinCodeAddFragment()
                    AuthMethodActivity.Page.SETTINGS -> PinCodeSettingsFragment()
                    else -> throw IllegalArgumentException("Unknown argument");
                })
            }
        }
    }
    
}