/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.linking

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.core.exception.InvalidLinkingCodeException
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.ActivityLinkBinding
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.*
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.SetNameDialogFragment.SetNameListener
import com.launchkey.android.authenticator.sdk.ui.internal.linking.LinkViewModel.State.CodeReady
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatActivity
import com.launchkey.android.authenticator.sdk.ui.internal.util.CoreExceptionToMessageConverter
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class LinkActivity : BaseAppCompatActivity(R.layout.activity_link) {
    companion object {
        const val EXTRA_TYPE = "pair-type"
        const val EXTRA_SDK_KEY = "sdkKey"
        const val TYPE_ENTER_CODE = 1
        const val TYPE_SCAN_QR = 2
        private val FRAGMENT_FRAME_ID = R.id.link_frame_fragment
        private const val PERMISSIONS_DIALOG = "permissions_dialog"
    }

    private var mode = 0
    private var typeInfoTextRes = 0
    private var sdkKey: String? = null
    private val permissionDialogViewModel: DialogFragmentViewModel by lazy { ViewModelProvider(this).get(PERMISSIONS_DIALOG, DialogFragmentViewModel::class.java) }
    private var codeCheckDialog: ProgressDialogFragment? = null
    private var cameraPermissionGranted = false
    private var deviceNameDialog: SetNameDialogFragment? = null
    private val viewModelProviderFactory: AbstractSavedStateViewModelFactory = object : AbstractSavedStateViewModelFactory() {
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return if (modelClass == LinkViewModel::class.java) {
                LinkViewModel(AuthenticatorManager.instance, sdkKey!!, handle) as T
            } else {
                super.create(key, modelClass, handle)
            }
        }
    }

    override fun getDefaultViewModelProviderFactory(): AbstractSavedStateViewModelFactory {
        return viewModelProviderFactory
    }

    private val binding by viewBinding(ActivityLinkBinding::bind, R.id.ioa_theme_layouts_root)
    private val linkViewModel: LinkViewModel by viewModels()
    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraPermissionGranted = granted
        if (granted) {
            setModeInUiThread(mode, true)
        } else {
            permissionDialogViewModel.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gotta do this before setting up the view model or the sdkKey will be null
        var mode = 0
        if (intent != null && intent.extras != null) {
            mode = intent.getIntExtra(EXTRA_TYPE, mode)
            sdkKey = intent.getStringExtra(EXTRA_SDK_KEY)
        }
        linkViewModel.state.observe(this) { state ->
            when (state) {
                is LinkViewModel.State.Success -> {
                    codeCheckDialog?.dismiss()
                    codeCheckDialog = null
                    UiUtils.toast(this@LinkActivity, R.string.ioa_link_linked, false)
                    finish()
                }
                is LinkViewModel.State.Loading -> {
                    codeCheckDialog = ProgressDialogFragment.newInstance(null, getString(R.string.ioa_link_dialog_linking_verifyingcode_message), false, true).apply {
                        show(supportFragmentManager, "CODE_CHECK_DIALOG")
                    }
                }
                is LinkViewModel.State.Failed -> {
                    handleLinkFailure(state.failure)
                }
                is CodeReady -> {
                    onCodeRetrieve(state.code)
                }
                else -> {
                    throw RuntimeException("Invalid state")
                }
            }
        }
        permissionDialogViewModel.state.observe(this) {
            when (it) {
                is DialogFragmentViewModel.State.NeedsToBeShown -> {
                    GenericAlertDialogFragment.show(supportFragmentManager,
                            this@LinkActivity,
                            getString(R.string.ioa_sec_dialog_permissiondenied_title),
                            getString(R.string.ioa_link_scancode_error_permissiondenied),
                            getString(R.string.ioa_sec_dialog_permissiondenied_button_positive),
                            false,
                            getString(R.string.ioa_sec_dialog_permissiondenied_button_neutral),
                            PERMISSIONS_DIALOG)
                }
                is DialogFragmentViewModel.State.Shown -> {
                    val onNegativeClick = DialogInterface.OnClickListener { _, _ -> setModeInUiThread(TYPE_ENTER_CODE, true) }
                    val settingsClick = DialogInterface.OnClickListener { _, _ ->
                        val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        appDetails.data = Uri.fromParts("package", this@LinkActivity.packageName, null)
                        this@LinkActivity.startActivity(appDetails)
                    }
                    val permissionDialog = supportFragmentManager.findFragmentByTag(PERMISSIONS_DIALOG) as AlertDialogFragment
                    permissionDialog.setPositiveButtonClickListener(settingsClick)
                    permissionDialog.setCancelListener { finish() }
                    permissionDialog.setNegativeButtonClickListener(onNegativeClick)
                }
            }
        }
        setSupportActionBar(binding.linkToolbar.root)
        UiUtils.updateToolbarIcon(binding.linkToolbar.root, UiUtils.NavButton.BACK)
        binding.linkToolbar.root.setNavigationOnClickListener { onBackPressed() }
        if (supportFragmentManager.findFragmentById(FRAGMENT_FRAME_ID) is LinkEnterCodeFragment) {
            mode = TYPE_ENTER_CODE
        } else if (supportFragmentManager.findFragmentById(FRAGMENT_FRAME_ID) is LinkScanCodeFragment) {
            mode = TYPE_SCAN_QR
        }
        setMode(mode, false)
        if (supportFragmentManager.findFragmentByTag(SetNameDialogFragment::class.java.simpleName) != null) {
            showDeviceNameDialog((linkViewModel.state.value as CodeReady?)!!.code)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        UiUtils.applyThemeToMenu(menuInflater, menu)
        val modeMenuRes = if (mode == TYPE_SCAN_QR) R.menu.link_entercode else R.menu.link_scancode
        menuInflater.inflate(modeMenuRes, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_entercode || id == R.id.action_scancode) {
            cycleModes()
            return true
        } else if (id == R.id.action_help) {
            HelpDialogFragment.show(supportFragmentManager,
                    this,
                    getString(R.string.ioa_generic_help),
                    getString(typeInfoTextRes))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setInfoWithMode(mode) //force update of the title
    }

    private fun showDeviceNameDialog(code: String) {
        deviceNameDialog = supportFragmentManager.findFragmentByTag(SetNameDialogFragment::class.java.simpleName) as SetNameDialogFragment?
        val setNameListener = object : SetNameListener {
            override fun onNameSet(dialog: SetNameDialogFragment?, name: String?) {
                var name = name
                dialog?.dismiss()
                name = name?.trim { it <= ' ' } ?: ""
                onLink(code, name)
            }
        }
        val onCancel = DialogInterface.OnCancelListener {
            val shouldReload = mode == TYPE_SCAN_QR
            setModeInUiThread(mode, shouldReload)
        }
        if (deviceNameDialog == null) {
            deviceNameDialog = SetNameDialogFragment.show(this,
                    supportFragmentManager,
                    R.string.ioa_link_dialog_setname_title,
                    R.string.ioa_link_dialog_setname_hint,
                    R.string.ioa_generic_ok,
                    UiUtils.defaultDeviceName,
                    setNameListener,
                    onCancel,
                    true)
        } else {
            deviceNameDialog!!.setPositiveButtonClickListener(setNameListener)
            deviceNameDialog!!.setCancelListener(onCancel)
        }
    }

    private fun onCodeRetrieve(code: String) {
        if (deviceNameDialog != null) {
            deviceNameDialog!!.dismiss()
            deviceNameDialog = null
        }
        showDeviceNameDialog(code)
    }

    private fun onLink(code: String, deviceName: String) {
        codeCheckDialog = ProgressDialogFragment.newInstance(null, getString(R.string.ioa_link_dialog_linking_verifyingcode_message), false, true).apply {
            show(supportFragmentManager, "CODE_CHECK_DIALOG")
        }
        linkViewModel.linkDevice(code, deviceName)
    }

    private fun handleLinkFailure(e: Exception) {
        if (codeCheckDialog != null) {
            codeCheckDialog!!.dismiss()
        }
        codeCheckDialog = null
        val defaultErrorMessage = CoreExceptionToMessageConverter.convert(e, this@LinkActivity)

        // Display the right error based on the mode, use default otherwise
        when (e) {
            is InvalidRegexLinkingException -> {
                notifyError(R.string.ioa_link_entercode_error_illegalcharacters)
            }
            is InvalidLinkingCodeException -> {
                val customError = if (mode == TYPE_ENTER_CODE) R.string.ioa_link_error_invalid_enter else R.string.ioa_link_error_invalid_scan
                notifyError(customError)
            }
            else -> {
                notifyError(defaultErrorMessage)
            }
        }
    }

    private fun cycleModes() {
        setMode(if (mode == TYPE_ENTER_CODE) TYPE_SCAN_QR else TYPE_ENTER_CODE, false)
    }

    private fun setModeInUiThread(mode: Int, forceSet: Boolean) {
        runOnUiThread { setMode(mode, forceSet) }
    }

    private fun setMode(mode: Int, forceSet: Boolean) {
        if (!forceSet && mode == this.mode) {
            return
        }
        this.mode = mode
        if (mode == TYPE_ENTER_CODE && supportFragmentManager.findFragmentById(FRAGMENT_FRAME_ID) is LinkEnterCodeFragment) {
            return
        } else if (mode == TYPE_SCAN_QR && supportFragmentManager.findFragmentById(FRAGMENT_FRAME_ID) is LinkScanCodeFragment) {
            return
        }

        //check camera permission and then force set the mode if OK.
        if (mode == TYPE_SCAN_QR && !cameraPermissionGranted) {
            permissionsLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        val f: Fragment = if (mode == TYPE_ENTER_CODE) LinkEnterCodeFragment() else LinkScanCodeFragment()
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.alpha_in, R.anim.alpha_out)
                .replace(FRAGMENT_FRAME_ID, f)
                .commitAllowingStateLoss()
        invalidateOptionsMenu()
        setInfoWithMode(this.mode)
    }

    private fun setInfoWithMode(mode: Int) {
        binding.linkToolbar.root.setTitle(
                if (mode == TYPE_ENTER_CODE) R.string.ioa_link_entercode_title else R.string.ioa_link_scancode_title)
        typeInfoTextRes = if (mode == TYPE_ENTER_CODE) R.string.ioa_link_entercode_info else R.string.ioa_link_scancode_info
        binding.linkToolbar.root.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_in))
    }

    private fun notifyError(messageResourceId: Int) {
        notifyError(getString(messageResourceId))
    }

    private fun notifyError(message: String?) {
        HelpDialogFragment.show(supportFragmentManager,
                this,
                getString(R.string.ioa_link_error_title),
                message)
    }
}