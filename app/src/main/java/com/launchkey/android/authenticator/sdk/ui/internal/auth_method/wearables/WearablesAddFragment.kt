/*
 *  Copyright (c) 2016. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.BluetoothDisabledException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.BluetoothPermissionException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.WearableWithSameNameExistsException
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentWearablesAddBinding
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.*
import com.launchkey.android.authenticator.sdk.ui.internal.util.*

class WearablesAddFragment : BaseAppCompatFragment(R.layout.fragment_wearables_add) {
    companion object {
        private const val DIALOG_BLUETOOTH_DISABLED = "BT_DISABLED_ALERT"
        private const val DIALOG_SET_NAME = "SET_NAME"
    }

    private val binding: FragmentWearablesAddBinding by viewBinding(FragmentWearablesAddBinding::bind)
    private val wearablesAddViewModel: WearablesAddViewModel by viewModels({ requireParentFragment() })
    private val devicesDisplayed: MutableList<WearablesManager.Wearable> = mutableListOf()
    private val devicesWithNames: MutableList<WearablesManager.Wearable> = mutableListOf()
    private val devicesWithoutNames: MutableList<WearablesManager.Wearable> = mutableListOf()

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

    private val setNameDialogDone =
        SetNameDialogFragment.SetNameListener { _, name ->
            val selectedWearable = wearablesAddViewModel.getSelectedWearable()!!
            wearablesAddViewModel.addWearable(selectedWearable, name!!)
        }

    private val setNameDialogCancel =
        DialogInterface.OnCancelListener { wearablesAddViewModel.cancelNaming() }

    private val discoveredWearablesAdapter: DiscoveredWearablesAdapter
        get() = binding.wearablesAddRecyclerview.adapter as DiscoveredWearablesAdapter

    private val bluetoothDisabledDialogFragment: AlertDialogFragment?
        get() = childFragmentManager.findFragmentByTag(DIALOG_BLUETOOTH_DISABLED) as? AlertDialogFragment

    private val bluetoothDialogOk =
        DialogInterface.OnClickListener { _, _ ->
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
            updateDevicesDisplayed(isChecked)
        }
        binding.proximityAddFilterLayout.setOnClickListener {
            includeUnnamedDevicesToggle.isChecked = !includeUnnamedDevicesToggle.isChecked
        }
    }

    private fun setupWearablesList() {
        with(binding.wearablesAddRecyclerview) {
            adapter = DiscoveredWearablesAdapter(devicesDisplayed) {
                wearablesAddViewModel.startNamingWearable(it)
            }
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun subscribeObservers() {
        wearablesAddViewModel.availableWearablesState.observe(viewLifecycleOwner) { availableWearablesState ->
            when (availableWearablesState) {
                is WearablesAddViewModel.AvailableWearablesState.ScanningDevices -> {
                    binding.proximityAddSwiperefresh.isRefreshing = true
                    binding.proximityAddEmpty.setText(R.string.ioa_sec_bp_add_searching)
                }
                is WearablesAddViewModel.AvailableWearablesState.AvailableWearablesSuccess -> {
                    binding.proximityAddSwiperefresh.isRefreshing = false
                    val nameFilter = { withName: Boolean ->
                        filter@{ wearable: WearablesManager.Wearable ->
                            Boolean
                            val s = wearable.name.trim { it <= ' ' }
                            return@filter if (withName) s.isNotEmpty() else s.isEmpty()
                        }
                    }
                    devicesWithNames.clear()
                    devicesWithNames.addAll(availableWearablesState.wearables.filter(nameFilter(true)))
                    devicesWithoutNames.clear()
                    devicesWithoutNames.addAll(
                        availableWearablesState.wearables.filter(
                            nameFilter(
                                false
                            )
                        )
                    )
                    updateDevicesDisplayed(binding.proximityAddFilterSwitch.isChecked)
                }
                is WearablesAddViewModel.AvailableWearablesState.FailedToGetAvailableWearables -> {
                    binding.proximityAddSwiperefresh.isRefreshing = false
                    val emptyView = binding.proximityAddEmpty
                    emptyView.makeVisible()
                    emptyView.setText(R.string.ioa_sec_bp_add_empty)
                    when (availableWearablesState.exception) {
                        // TODO: 10/18/21 BluetoothPermissionException needs a separate case?
                        is BluetoothDisabledException, is BluetoothPermissionException -> {
                            showBluetoothDisabledDialog()
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
                    val setNameDialogFragment =
                        childFragmentManager.findFragmentByTag(SetNameDialogFragment::class.java.simpleName) as SetNameDialogFragment
                    setNameDialogFragment.dismiss()
                    (requireParentFragment() as WearablesFragment).wearableAdded = true
                    requireActivity().onBackPressed()
                }
                is WearablesAddViewModel.AddWearableState.FailedToAddWearable -> {
                    val errorMessage = getErrorMessage(it.failure)
                    val setNameDialogFragment =
                        childFragmentManager.findFragmentByTag(SetNameDialogFragment::class.java.simpleName) as SetNameDialogFragment
                    setNameDialogFragment.setErrorMessage(errorMessage)
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

    private fun updateDevicesDisplayed(toggle: Boolean) {
        devicesDisplayed.clear()
        devicesDisplayed.addAll(devicesWithNames)
        if (toggle) {
            devicesDisplayed.addAll(devicesWithoutNames)
        }

        binding.wearablesAddRecyclerview.adapter!!.notifyDataSetChanged()

        binding.proximityAddEmpty.setText(R.string.ioa_sec_bp_add_empty)
        val emptyView = binding.proximityAddEmpty
        binding.proximityAddSwiperefresh.isRefreshing = false
        if (devicesDisplayed.isEmpty()) {
            emptyView.makeVisible()
        } else {
            emptyView.makeInvisible()
        }
    }

    private fun scanWearables() {
//        devicesWithNames.clear()
//        devicesWithoutNames.clear()
//        devicesDisplayed.clear()
//        binding.wearablesAddRecyclerview.adapter!!.notifyDataSetChanged()
        wearablesAddViewModel.getAvailableWearables()
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
        if (setNameDialogFragment != null) return

        SetNameDialogFragment.show(
            requireContext(),
            childFragmentManager,
            R.string.ioa_sec_bp_add_dialog_setname_title,
            R.string.ioa_sec_bp_add_dialog_setname_hint,
            R.string.ioa_generic_done,
            wearable.name
        ).also {
            it.setPositiveButtonClickListener(setNameDialogDone)
            it.setCancelListener(setNameDialogCancel)
        }
    }

    private fun requestBluetoothEnabling() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enabledBluetoothLauncher.launch(enableBtIntent)
    }
}
