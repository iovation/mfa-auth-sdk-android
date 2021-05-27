/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.core.authentication_management.Device
import com.launchkey.android.authenticator.sdk.core.authentication_management.event_callback.UnlinkDeviceEventCallback
import com.launchkey.android.authenticator.sdk.core.exception.DeviceUnlinkedButFailedToNotifyServerException
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.DialogFragmentViewModel

abstract class BaseAppCompatActivity : AppCompatActivity {
    // TODO: 12/8/20 Remove this when all auth method activities are converted to fragments
    private val viewModelProviderFactory: AbstractSavedStateViewModelFactory by lazy { AbstractSavedStateViewModelFactory() }
    
    open inner class AbstractSavedStateViewModelFactory :
        androidx.lifecycle.AbstractSavedStateViewModelFactory(this, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return when (modelClass) {
                DialogFragmentViewModel::class.java ->
                    DialogFragmentViewModel(handle) as T
                else ->
                    super.create(modelClass)
            }
        }
    }
    
    constructor() : super()
    constructor(@LayoutRes layoutId: Int) : super(layoutId)

    private val coreAuthenticatorManager = AuthenticatorManager.instance
    private val authenticatorUIManager = AuthenticatorUIManager.instance
    private val onUnlinkCallback: UnlinkDeviceEventCallback = object : UnlinkDeviceEventCallback() {
        override fun onSuccess(device: Device) {
            if (device.isCurrent) {
                finish()
            }
        }
        
        override fun onFailure(e: Exception) {
            if (e is DeviceUnlinkedButFailedToNotifyServerException) {
                finish()
            }
        }
    }
    
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        val root = findViewById<View>(R.id.ioa_theme_layouts_root)
        root.rootView.filterTouchesWhenObscured = true
        UiUtils.themeStatusBar(this)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        UiUtils.applyMarshmallowSecureFlagToWindow(this)
        setTheme(authenticatorUIManager.config.theme())
        super.onCreate(savedInstanceState)
        if (IntentUtils.isIntentInvalid(intent)) {
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        coreAuthenticatorManager.registerForEvents(onUnlinkCallback)
    }
    
    override fun onPause() {
        stopListeningForUnlink()
        super.onPause()
    }
    
    override fun startActivity(intent: Intent) {
        IntentUtils.addInternalVerification(intent)
        super.startActivity(intent)
    }
    
    override fun startActivity(intent: Intent, options: Bundle?) {
        IntentUtils.addInternalVerification(intent)
        super.startActivity(intent, options)
    }
    
    // TODO: 12/8/20 Remove this when all auth method activities are converted to fragments
    override fun getDefaultViewModelProviderFactory(): AbstractSavedStateViewModelFactory {
        return viewModelProviderFactory
    }
    
    protected fun stopListeningForUnlink() {
        coreAuthenticatorManager.unregisterForEvents(onUnlinkCallback)
    }
}