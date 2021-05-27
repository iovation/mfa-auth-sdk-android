/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui

import android.os.Bundle
import com.launchkey.android.authenticator.sdk.ui.databinding.ActivitySecurityBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatActivity
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class SecurityActivity : BaseAppCompatActivity(R.layout.activity_security) {
    private val binding: ActivitySecurityBinding by viewBinding(ActivitySecurityBinding::bind, R.id.ioa_theme_layouts_root)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.securityToolbar.root)
        UiUtils.updateToolbarIcon(binding.securityToolbar.root, UiUtils.NavButton.BACK)
        binding.securityToolbar.root.setNavigationOnClickListener { onBackPressed() }
    }
}