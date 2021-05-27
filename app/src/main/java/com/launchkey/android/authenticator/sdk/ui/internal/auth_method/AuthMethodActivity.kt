package com.launchkey.android.authenticator.sdk.ui.internal.auth_method

import android.os.Bundle
import androidx.fragment.app.commit
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.AuthMethodActivityBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.biometric.BiometricFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.circle_code.CircleCodeFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.LocationsFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables.WearablesFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatActivity
import com.launchkey.android.authenticator.sdk.ui.internal.util.intentExtra
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding
import java.lang.IllegalArgumentException

class AuthMethodActivity : BaseAppCompatActivity(R.layout.auth_method_activity) {
    companion object {
        const val AUTH_METHOD_KEY = "auth_method"
        const val PAGE_KEY = "page"
    }

    enum class Page {
        // CHECK should never be passed to this activity as an argument, it is just convenient for
        // the child fragments to reuse this enum instead of translating to their own version with
        // CHECK added.
        ADD, SETTINGS, CHECK
    }

    private val authMethod: AuthMethod by intentExtra(AUTH_METHOD_KEY)
    private val page: Page by intentExtra(PAGE_KEY)
    private val binding: AuthMethodActivityBinding by viewBinding(AuthMethodActivityBinding::bind, R.id.ioa_theme_layouts_root)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (page == Page.CHECK) throw IllegalArgumentException("CHECK should never be passed into " + AuthMethodActivity::class.java.simpleName)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(binding.authMethodFragmentContainer.id, when (authMethod) {
                    // TODO: Fill in for each method
                    AuthMethod.CIRCLE_CODE -> CircleCodeFragment::class.java
                    AuthMethod.LOCATIONS -> LocationsFragment::class.java
                    AuthMethod.BIOMETRIC -> BiometricFragment::class.java
                    AuthMethod.PIN_CODE -> PinCodeFragment::class.java
                    AuthMethod.WEARABLES -> WearablesFragment::class.java
                    else -> throw IllegalArgumentException("Unknown argument")
                }, Bundle().apply {
                    putSerializable(PAGE_KEY, page)
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stopListeningForUnlink()
    }
}