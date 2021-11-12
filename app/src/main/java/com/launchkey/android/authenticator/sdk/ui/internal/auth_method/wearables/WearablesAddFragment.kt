/*
 *  Copyright (c) 2016. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.BluetoothDisabledException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.BluetoothPermissionException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.WearableWithSameNameExistsException
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentWearablesAddBinding
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.GenericAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.HelpDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.SetNameDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.*

class WearablesAddFragment : BaseAppCompatFragment(R.layout.fragment_wearables_add) {
    companion object {
        private const val DIALOG_BLUETOOTH_DISABLED = "BT_DISABLED_ALERT"
        private const val DIALOG_SET_NAME = "SET_NAME"
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val binding: FragmentWearablesAddBinding by viewBinding(FragmentWearablesAddBinding::bind)
    private val wearablesAddViewModel: WearablesAddViewModel by viewModels({ requireParentFragment() })
    private val wearablesScanViewModel: WearablesScanViewModel by viewModels()
    private val bluetoothPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT
        else Manifest.permission.BLUETOOTH

    private val enabledBluetoothLauncher =
        registerForActivityResult(StartActivityForResult(), ActivityResultCallback { result ->
            if (result == null) return@ActivityResultCallback
            if (result.resultCode == Activity.RESULT_OK) {
                scanWearables()
            } else {
                requireActivity().onBackPressed()
            }
        })

    private val setNameDialogFragment: SetNameDialogFragment?
        get() = childFragmentManager.findFragmentByTag(DIALOG_SET_NAME) as? SetNameDialogFragment

    private val setNameDialogDone = SetNameDialogFragment.SetNameListener { _, name ->
        wearablesAddViewModel.addSelectedWearableWithName(name!!)
    }

    private val setNameDialogCancel = DialogInterface.OnCancelListener {
        wearablesAddViewModel.cancelNaming()
    }

    private val discoveredWearablesAdapter: DiscoveredWearablesAdapter
        get() = binding.wearablesAddRecyclerview.adapter as DiscoveredWearablesAdapter

    private val bluetoothDisabledDialogFragment: AlertDialogFragment?
        get() = childFragmentManager.findFragmentByTag(DIALOG_BLUETOOTH_DISABLED) as? AlertDialogFragment

    private val bluetoothDialogOk = DialogInterface.OnClickListener { _, _ ->
        requestBluetoothEnabling()
    }

    private val bluetoothDialogCancel = DialogInterface.OnCancelListener {
        requireActivity().onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetoothDisabledDialogFragment?.setPositiveButtonClickListener(bluetoothDialogOk)
        bluetoothDisabledDialogFragment?.setCancelListener(bluetoothDialogCancel)

        setNameDialogFragment?.setPositiveButtonClickListener(setNameDialogDone)
        setNameDialogFragment?.setCancelListener(setNameDialogCancel)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
                if (permissionGranted) {
                    scanWearables()
                } else {
                    showBluetoothPermissionDeniedSnackbar(bluetoothPermission, false)
                }
            }

        setupRefresh()
        setupToggle()
        setupWearablesList()
        subscribeObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        UiUtils.applyThemeToMenu(inflater, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_help) {
            HelpDialogFragment.show(
                childFragmentManager,
                requireContext(),
                getString(R.string.ioa_sec_bp_help_title),
                getString(R.string.ioa_sec_bp_help_message)
            )
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRefresh() {
        with(binding.proximityAddSwiperefresh) {
            setOnRefreshListener { scanWearables() }
            isRefreshing = true
            setColorSchemeColors(
                UiUtils.getColorFromTheme(
                    requireContext(),
                    R.attr.authenticatorColorAccent,
                    R.color.lk_wl_default_accent
                )
            )
        }
    }

    private fun setupToggle() {
        val includeUnnamedDevicesToggle = binding.proximityAddFilterSwitch
        includeUnnamedDevicesToggle.setOnCheckedChangeListener { _, isChecked ->
            val scanState =
                (wearablesScanViewModel.scanState.value as WearablesScanViewModel.WearablesScanState.FoundAvailableWearables)

            discoveredWearablesAdapter.submitList(
                if (isChecked) scanState.wearablesWithNames + scanState.wearablesWithoutNames
                else scanState.wearablesWithNames
            )
        }
        binding.proximityAddFilterLayout.setOnClickListener {
            includeUnnamedDevicesToggle.isChecked = !includeUnnamedDevicesToggle.isChecked
        }
    }

    private fun setupWearablesList() {
        with(binding.wearablesAddRecyclerview) {
            adapter = DiscoveredWearablesAdapter { wearablesAddViewModel.startNamingWearable(it) }
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun subscribeObservers() {
        wearablesScanViewModel.scanState.observe(viewLifecycleOwner) { scanState ->
            when (scanState) {
                WearablesScanViewModel.WearablesScanState.Scanning -> {
                    binding.proximityAddSwiperefresh.isRefreshing = true
                    binding.proximityAddEmpty.setText(R.string.ioa_sec_bp_add_searching)
                }
                is WearablesScanViewModel.WearablesScanState.FoundAvailableWearables -> {
                    binding.proximityAddSwiperefresh.isRefreshing = false
                    updateDevicesDisplayed(
                        scanState.wearablesWithNames,
                        scanState.wearablesWithoutNames,
                        binding.proximityAddFilterSwitch.isChecked
                    )
                }
                is WearablesScanViewModel.WearablesScanState.FailedToGetAvailableWearables -> {
                    binding.proximityAddSwiperefresh.isRefreshing = false
                    val emptyView = binding.proximityAddEmpty
                    emptyView.makeVisible()
                    emptyView.setText(R.string.ioa_sec_bp_add_empty)
                    when (scanState.failure) {
                        is BluetoothDisabledException -> {
                            showBluetoothDisabledDialog()
                        }
                        is BluetoothPermissionException -> {
                            if (shouldShowRequestPermissionRationale(bluetoothPermission)) {
                                showBluetoothPermissionDeniedSnackbar(bluetoothPermission, true)
                            } else {
                                requestPermissionLauncher.launch(bluetoothPermission)
                            }
                        }
                        else -> requireActivity().onBackPressed() // exit
                    }
                }
            }
        }

        wearablesAddViewModel.addWearableState.observe(viewLifecycleOwner) {
            when (it) {
                is WearablesAddViewModel.AddWearableState.SelectingWearable -> {
                }
                is WearablesAddViewModel.AddWearableState.NamingWearable -> {
                    showSetNameDialog(it.wearable)
                }
                is WearablesAddViewModel.AddWearableState.AddedNewWearable -> {
                    setNameDialogFragment?.dismiss()
                }
                is WearablesAddViewModel.AddWearableState.FailedToAddWearable -> {
                    setNameDialogFragment?.setErrorMessage(getErrorMessage(it.failure))
                }
            }
        }
    }

    private fun getErrorMessage(exception: Exception): String = when (exception) {
        is WearablesAddViewModel.WearableNameTooShortException -> {
            resources.getQuantityString(
                R.plurals.ioa_sec_bp_add_error_invalidname_message_format,
                Constants.MINIMUM_INPUT_LENGTH,
                Constants.MINIMUM_INPUT_LENGTH
            )
        }
        is WearableWithSameNameExistsException -> {
            getString(R.string.ioa_sec_bp_add_error_usedname_message)
        }
        else -> {
            getString(R.string.ioa_error_unknown_format, exception.message)
        }
    }

    private fun updateDevicesDisplayed(
        wearablesWithNames: List<WearablesManager.Wearable>,
        wearablesWithoutNames: List<WearablesManager.Wearable>,
        includeWearablesWithoutNames: Boolean
    ) {
        val wearables =
            if (includeWearablesWithoutNames) wearablesWithNames + wearablesWithoutNames
            else wearablesWithNames

        discoveredWearablesAdapter.submitList(wearables)
        binding.proximityAddEmpty.setText(R.string.ioa_sec_bp_add_empty)
        if (wearables.isEmpty()) binding.proximityAddEmpty.makeVisible()
        else binding.proximityAddEmpty.makeInvisible()
    }

    private fun scanWearables() {
        wearablesScanViewModel.scanForAvailableWearables()
    }

    private fun showBluetoothDisabledDialog() {
        if (bluetoothDisabledDialogFragment != null) return

        GenericAlertDialogFragment.show(
            childFragmentManager,
            requireContext(),
            getString(R.string.ioa_sec_bp_add_error_bluetoothdisabled_title),
            getString(R.string.ioa_sec_bp_add_error_bluetoothdisabled_message),
            null,
            true,
            null,
            DIALOG_BLUETOOTH_DISABLED
        ).also {
            it.setPositiveButtonClickListener(bluetoothDialogOk)
            it.setCancelListener(bluetoothDialogCancel)
        }
    }

    private fun showSetNameDialog(wearable: WearablesManager.Wearable) {
        SetNameDialogFragment.show(
            requireContext(),
            childFragmentManager,
            R.string.ioa_sec_bp_add_dialog_setname_title,
            R.string.ioa_sec_bp_add_dialog_setname_hint,
            R.string.ioa_generic_done,
            wearable.name,
            DIALOG_SET_NAME
        ).also {
            it.setPositiveButtonClickListener(setNameDialogDone)
            it.setCancelListener(setNameDialogCancel)
        }
    }

    private fun showBluetoothPermissionDeniedSnackbar(
        bluetoothPermission: String,
        showAction: Boolean
    ) {
        Snackbar.make(
            binding.ioaThemeLayoutsRoot,
            R.string.ioa_misc_permission_denied_bluetooth,
            Snackbar.LENGTH_LONG
        ).apply {
            if (showAction) {
                setAction(R.string.ioa_misc_permission_request_permission_action) {
                    requestPermissionLauncher.launch(bluetoothPermission)
                }
                setActionTextColor(
                    UiUtils.getThemeColor(
                        requireContext(),
                        R.attr.authenticatorColorAccent
                    )
                )
            }
            setMaxLines(10)
            show()
        }
    }

    private fun requestBluetoothEnabling() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enabledBluetoothLauncher.launch(enableBtIntent)
    }
}
