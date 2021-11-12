/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.DrawableRes
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.InvalidAuthMethodInputException
import com.launchkey.android.authenticator.sdk.ui.ItemListDialogFragment.IconItem
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentSecurityBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.AuthMethodActivity
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.SecurityItemAdapter
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.TimerViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.common.SecurityItem
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.HelpDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.ProgressDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.*
import com.launchkey.android.authenticator.sdk.ui.internal.util.IntentUtils.addInternalVerification
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils.areLocationServicesTurnedOn
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils.getDrawableAttrFromContext
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils.wasPermissionGranted
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SecurityViewModel
import java.util.*

class SecurityFragment : BaseAppCompatFragment(R.layout.fragment_security),
    MenuItem.OnMenuItemClickListener {
    private lateinit var timeAgo: TimeAgo
    private lateinit var setFactorsAdapter: SecurityItemAdapter
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionsLauncher: ActivityResultLauncher<String>
    private lateinit var permissionPendingMethod: AuthMethod // should only ever be Wearables or Locations
    private val securityViewModel: SecurityViewModel by viewModels()
    private val timerViewModel: TimerViewModel by viewModels()
    private val binding: FragmentSecurityBinding by viewBinding(FragmentSecurityBinding::bind)

    private val loadingDialogFragment: ProgressDialogFragment?
        get() = childFragmentManager.findFragmentByTag(ProgressDialogFragment::class.java.simpleName) as? ProgressDialogFragment

    private val addDialogFragment: ItemListDialogFragment?
        get() = childFragmentManager.findFragmentByTag(ItemListDialogFragment::class.java.simpleName) as? ItemListDialogFragment

    private val loadingOnCancelListener =
        DialogInterface.OnCancelListener { securityViewModel.cancelJobs() }

    private val addOnClickListener = DialogInterface.OnClickListener { _, which ->
        if (which < 0) {
            return@OnClickListener
        }

        // Implied success
        val item = (securityViewModel.state.value as SecurityViewModel.GetAuthMethodsState.Success)
            .availableSecurityItems[which]

        when (item.type) {
            AuthMethod.PIN_CODE, AuthMethod.CIRCLE_CODE, AuthMethod.BIOMETRIC -> {
                startAuthMethodActivity(item.type, AuthMethodActivity.Page.ADD)
            }
            AuthMethod.WEARABLES -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    || wasPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                ) {
                    startAuthMethodActivity(AuthMethod.WEARABLES, AuthMethodActivity.Page.ADD)
                } else {
                    // Below Android 12 AND ACCESS_FINE_LOCATION permission not granted
                    Snackbar.make(
                        binding.ioaThemeLayoutsRoot,
                        R.string.ioa_misc_permission_ble_location_suggestion,
                        Snackbar.LENGTH_LONG
                    ).setMaxLines(10).show()
                    permissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            AuthMethod.LOCATIONS -> {
                permissionPendingMethod = item.type
                if (wasPermissionGranted(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    startAuthMethodActivityIfLocationServicesAreTurnedOn()
                } else {
                    permissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else -> throw IllegalStateException("Unknown activity to start")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timeAgo = TimeAgo(requireActivity())
        setupUi()
        subscribeObservers()
        activityLauncher = this.registerForActivityResult(
            StartActivityForResult(),
            ActivityResultCallback { result ->
                if (result == null) {
                    return@ActivityResultCallback
                }
                val resultCode = result.resultCode
                securityViewModel.getAuthMethods()
                if (resultCode == Activity.RESULT_OK) {
                    result.data?.let {
                        val requestCode = it.getIntExtra(REQUEST_CODE, 0)
                        val possibleUnmaskedRequestCode =
                            getPossibleUnmaskedRequestCode(requestCode)
                        val addGeo = REQUEST_ADD_LOCATIONS == possibleUnmaskedRequestCode
                        val addBt = REQUEST_ADD_WEARABLES == possibleUnmaskedRequestCode
                        if (addGeo || addBt) {
                            notifyUserOfPassiveFactorCooldown(
                                if (addBt) AuthMethod.WEARABLES else AuthMethod.LOCATIONS
                            )
                        }
                    }
                }
            })

        permissionsLauncher =
            this.registerForActivityResult(RequestPermission()) { permissionGranted ->
                when {
                    permissionPendingMethod == AuthMethod.WEARABLES -> startAuthMethodActivity(
                        permissionPendingMethod,
                        AuthMethodActivity.Page.ADD
                    ) // locations permission isn't required but recommended for wearables
                    permissionGranted == true -> startAuthMethodActivityIfLocationServicesAreTurnedOn()
                    else -> showPermissionDeniedSnackbar()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.security, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return onMenuItemClick(item)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return if (item.itemId == R.id.security_add) {
            val state = securityViewModel.state.value
            if (state is SecurityViewModel.GetAuthMethodsState.Success) {
                showAvailableAuthMethodsDialog(state.availableSecurityItems)
            }
            true
        } else false
    }

    private fun subscribeObservers() {
        securityViewModel.state.observe(viewLifecycleOwner, { state ->
            when (state) {
                is SecurityViewModel.GetAuthMethodsState.Success -> {
                    val setSecurityItems: List<SecurityItem> = state.setSecurityItems
                    loadingDialogFragment?.dismiss()
                    timerViewModel.stopTimers()
                    setFactorsAdapter.submitList(setSecurityItems)
                    updateSecurityRecyclerViewVisibility(setSecurityItems)

                    // Manual update in visibility of header based on presence of set factors if necessary
                    if (setSecurityItems.isEmpty()) binding.securityTextEnabledfactors.makeInvisible()
                    else binding.securityTextEnabledfactors.makeVisible()

                    val timers = setSecurityItems.mapNotNull { item ->
                        when (item.togglePendingState) {
                            SecurityItem.TogglePendingState.NotPending -> null
                            is SecurityItem.TogglePendingState.PendingToggle -> TimerViewModel.TimerItem(
                                item,
                                item.togglePendingState.toggledAtTimeInMillis
                            )
                        }
                    }

                    timerViewModel.startTimers(timers)
                }
                is SecurityViewModel.GetAuthMethodsState.Failed -> {
                    addDialogFragment?.dismiss()
                    Snackbar.make(
                        binding.ioaThemeLayoutsRoot,
                        R.string.ioa_sec_error_unlinkedunallowed, Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
                is SecurityViewModel.GetAuthMethodsState.Loading -> {
                    ProgressDialogFragment.show(
                        null,
                        getString(R.string.ioa_sec_loading),
                        cancellable = true,
                        indeterminate = true,
                        childFragmentManager,
                        ProgressDialogFragment::class.java.simpleName
                    ).also {
                        it.setCancelListener(loadingOnCancelListener)
                    }
                    setFactorsAdapter.submitList(emptyList())
                    updateSecurityRecyclerViewVisibility(emptyList())
                }
            }
        })
        timerViewModel.state.observe(viewLifecycleOwner) { timerState ->
            when (timerState) {
                TimerViewModel.State.AllItemsFinished -> Unit
                is TimerViewModel.State.ItemFinished<*> -> {
                    val item = timerState as TimerViewModel.State.ItemFinished<SecurityItem>
                    setFactorsAdapter.notifyTimerUpdate(item.timerItem.item, 0L)
                }
                is TimerViewModel.State.ItemUpdated<*> -> {
                    val item = timerState as TimerViewModel.State.ItemUpdated<SecurityItem>
                    setFactorsAdapter.notifyTimerUpdate(item.timerItem.item, item.remainingMillis)
                }
            }
        }

    }

    private fun setupUi() {
        setFactorsAdapter = SecurityItemAdapter(
            securityViewModel.getAuthenticatorTheme(),
            timeAgo
        ) { authMethod -> startAuthMethodActivity(authMethod, AuthMethodActivity.Page.SETTINGS) }

        with(binding.securityRecyclerView) {
            adapter = setFactorsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        addDialogFragment?.setOnClickListener(addOnClickListener)
        loadingDialogFragment?.setCancelListener(loadingOnCancelListener)
    }

    private fun updateSecurityRecyclerViewVisibility(items: List<SecurityItem?>) {
        if (items.isEmpty()) {
            binding.securityEmpty.visibility = View.VISIBLE
            binding.securityRecyclerView.visibility = View.INVISIBLE
        } else {
            binding.securityEmpty.visibility = View.INVISIBLE
            binding.securityRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun notifyUserOfPassiveFactorCooldown(authMethod: AuthMethod) {
        val activationDelayInSeconds = securityViewModel.getActivationDelay(authMethod).toLong()
        if (activationDelayInSeconds <= 0L) return

        val timeRemaining = timeAgo.timeAgoWithDiff(activationDelayInSeconds * 1000, true)
        val titleId =
            if (authMethod === AuthMethod.WEARABLES) R.string.ioa_sec_dialog_bp_firstadded_title
            else R.string.ioa_sec_dialog_geo_firstadded_title
        val messageFormatId =
            if (authMethod === AuthMethod.WEARABLES) R.string.ioa_sec_dialog_bp_firstadded_message_format
            else R.string.ioa_sec_dialog_geo_firstadded_message_format
        val message = getString(messageFormatId, timeRemaining.toLowerCase(Locale.getDefault()))
        HelpDialogFragment.show(childFragmentManager, requireContext(), getString(titleId), message)
    }

    private fun showAvailableAuthMethodsDialog(availableSecurityItems: List<SecurityItem>) {
        if (availableSecurityItems.isNotEmpty()) {
            ItemListDialogFragment.show(
                requireContext(),
                childFragmentManager,
                R.string.ioa_sec_dialog_add_title,
                getIconItems(availableSecurityItems),
                ItemListDialogFragment::class.java.simpleName,
                addOnClickListener
            )
        } else {
            HelpDialogFragment.show(
                childFragmentManager,
                requireContext(),
                getString(R.string.ioa_sec_error_allfactorsenabled_title),
                getString(
                    R.string.ioa_sec_error_allfactorsenabled_message
                )
            )
        }
    }

    private fun getIconItems(availableSecurityItems: List<SecurityItem>): Array<IconItem> {
        val items = mutableListOf<IconItem>()
        for (item in availableSecurityItems) {
            //rely on SecurityItem.helpMessageRes for sub-label
            items.add(
                IconItem(
                    getIconResFromSecurityItem(requireContext(), item),
                    item.titleRes,
                    item.helpMessageRes
                )
            )
        }
        return items.toTypedArray()
    }

    private fun startAuthMethodActivityIfLocationServicesAreTurnedOn() {
        if (areLocationServicesTurnedOn(requireContext()) || permissionPendingMethod == AuthMethod.WEARABLES) {
            startAuthMethodActivity(permissionPendingMethod, AuthMethodActivity.Page.ADD)
        } else {
            showLocationServicesTurnedOffSnackbar()
        }
    }

    private fun startAuthMethodActivity(authMethod: AuthMethod, page: AuthMethodActivity.Page) {
        val intent = Intent(requireActivity(), AuthMethodActivity::class.java).apply {
            putExtra(AuthMethodActivity.AUTH_METHOD_KEY, authMethod)
            putExtra(AuthMethodActivity.PAGE_KEY, page)
            addInternalVerification(this)
        }
        activityLauncher.launch(intent)
    }

    private fun showLocationServicesTurnedOffSnackbar() {
        Snackbar.make(
            binding.ioaThemeLayoutsRoot,
            R.string.ioa_misc_permission_locations_services_turned_off,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.ioa_misc_permission_settings_action) {
            val appDetails = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            it.context.startActivity(appDetails)
        }.setActionTextColor(
            UiUtils.getThemeColor(
                requireContext(),
                R.attr.authenticatorColorAccent
            )
        ).setMaxLines(10).show()
    }

    private fun showPermissionDeniedSnackbar() {
        Snackbar.make(
            binding.ioaThemeLayoutsRoot,
            R.string.ioa_misc_permission_denied_location,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.ioa_misc_permission_settings_action) {
            val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            appDetails.data = Uri.fromParts("package", requireContext().packageName, null)
            addInternalVerification(appDetails)
            requireContext().startActivity(appDetails)
        }.setActionTextColor(
            UiUtils.getThemeColor(
                requireContext(),
                R.attr.authenticatorColorAccent
            )
        ).setMaxLines(10).show()
    }

    @Deprecated("")
    interface SecureTouchFilter {
        fun shouldBlockIfObscured(): Boolean
    }

    companion object {
        const val REQUEST_CODE = "request_code"
        const val REQUEST_ADD_LOCATIONS = 20

        @Deprecated("")
        val REQUEST_ADD_GEO = REQUEST_ADD_LOCATIONS
        const val REQUEST_ADD_WEARABLES = 30

        @Deprecated("")
        val REQUEST_ADD_BT = REQUEST_ADD_WEARABLES

        @DrawableRes
        private fun getIconResFromSecurityItem(context: Context, securityItem: SecurityItem): Int {
            return when (securityItem.type) {
                AuthMethod.PIN_CODE -> getDrawableAttrFromContext(
                    context,
                    R.attr.authenticatorFactorPinIcon,
                    R.drawable.ic_dialpad_black_24dp
                )
                AuthMethod.CIRCLE_CODE -> getDrawableAttrFromContext(
                    context,
                    R.attr.authenticatorFactorCircleIcon,
                    R.drawable.ic_settings_backup_restore_black_24dp
                )
                AuthMethod.LOCATIONS -> getDrawableAttrFromContext(
                    context,
                    R.attr.authenticatorFactorGeofencingIcon,
                    R.drawable.ic_place_black_24dp
                )
                AuthMethod.WEARABLES -> getDrawableAttrFromContext(
                    context,
                    R.attr.authenticatorFactorBluetoothIcon,
                    R.drawable.ic_bluetooth_black_24dp
                )
                AuthMethod.BIOMETRIC -> getDrawableAttrFromContext(
                    context,
                    R.attr.authenticatorFactorFingerprintIcon,
                    R.drawable.ic_fingerprint_black_24dp
                )
                else -> throw InvalidAuthMethodInputException("Not a valid factor type.")
            }
        }
    }
}