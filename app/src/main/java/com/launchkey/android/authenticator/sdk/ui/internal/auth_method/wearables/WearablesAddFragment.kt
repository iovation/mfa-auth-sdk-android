/*
 *  Copyright (c) 2016. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.BluetoothDisabledException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.BluetoothPermissionException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.WearableWithSameNameExistsException
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentWearablesAddBinding
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.*
import com.launchkey.android.authenticator.sdk.ui.internal.util.*
import java.util.*

class WearablesAddFragment : BaseAppCompatFragment(R.layout.fragment_wearables_add),
    OnRefreshListener {
    companion object {
        private const val BT_DISABLED_ALERT = "BT_DISABLED_ALERT"
    }

    private val bluetoothDisabledDialog: AlertDialogFragment?
        get() = childFragmentManager.findFragmentByTag(BT_DISABLED_ALERT) as? AlertDialogFragment

    private val binding: FragmentWearablesAddBinding by viewBinding(FragmentWearablesAddBinding::bind)
    private val wearablesAddViewModel: WearablesAddViewModel by viewModels({ requireParentFragment() })
    private val devicesDisplayed: MutableList<WearablesManager.Wearable> = mutableListOf()
    private val devicesWithNames: MutableList<WearablesManager.Wearable> = mutableListOf()
    private val devicesWithoutNames: MutableList<WearablesManager.Wearable> = mutableListOf()
    private var selectedItem: WearablesManager.Wearable? = null
    private val enabledBluetoothLauncher =
        registerForActivityResult(StartActivityForResult(), ActivityResultCallback { result ->
            if (result == null) return@ActivityResultCallback
            if (result.resultCode == Activity.RESULT_OK) {
                onRefresh()
            } else {
                requireActivity().onBackPressed()
            }
        })

    private lateinit var setNameAlertDialog: DialogFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetoothDisabledDialog?.setPositiveButtonClickListener { _, _ -> requestBluetoothEnabling() }
        bluetoothDisabledDialog?.setCancelListener { requireActivity().onBackPressed() }

        binding.proximityAddSwiperefresh.setOnRefreshListener(this)
        binding.proximityAddSwiperefresh.isRefreshing = true
        binding.proximityAddSwiperefresh.setColorSchemeColors(
            UiUtils.getColorFromTheme(
                requireContext(),
                R.attr.authenticatorColorAccent,
                R.color.lk_wl_default_accent
            )
        )

        setNameAlertDialog = ViewModelProvider(
            this,
            defaultViewModelProviderFactory
        ).get(SetNameDialogFragment::class.java.simpleName, DialogFragmentViewModel::class.java)
        val toggle = binding.proximityAddFilterSwitch
        toggle.setOnCheckedChangeListener { _, isChecked -> updateDevicesDisplayed(isChecked) }
        val toggleLayout: View = binding.proximityAddFilterLayout
        toggleLayout.setOnClickListener { toggle.isChecked = !toggle.isChecked }
        val adapter = DiscoveredWearablesAdapter(devicesDisplayed) { onItemSelect(it) }
        val recyclerView = binding.wearablesAddRecyclerview
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        setNameAlertDialog.state.observe(viewLifecycleOwner) { state ->
            if (state is DialogFragmentViewModel.State.NeedsToBeShown) {
                SetNameDialogFragment.show(
                    requireContext(),
                    childFragmentManager,
                    R.string.ioa_sec_bp_add_dialog_setname_title,
                    R.string.ioa_sec_bp_add_dialog_setname_hint,
                    R.string.ioa_generic_done,
                    selectedItem!!.name
                )
                setNameAlertDialog.changeState(DialogFragmentViewModel.State.Shown)
            } else if (state is DialogFragmentViewModel.State.Shown) {
                val setNameDialogFragment =
                    childFragmentManager.findFragmentByTag(SetNameDialogFragment::class.java.simpleName) as SetNameDialogFragment
                setNameDialogFragment.setPositiveButtonClickListener(object :
                    SetNameDialogFragment.SetNameListener {
                    override fun onNameSet(dialog: SetNameDialogFragment?, name: String?) {
                        selectedItem!!.name = name!!
                        wearablesAddViewModel.addWearable(selectedItem!!)
                    }
                })
            }
        }

        subscribeObservers()
    }

    private fun getErrorMessage(exception: Exception): String? {
        if (exception is WearablesAddViewModel.WearableNameTooShortException) {
            return resources.getQuantityString(
                R.plurals.ioa_sec_bp_add_error_invalidname_message_format,
                Constants.MINIMUM_INPUT_LENGTH,
                Constants.MINIMUM_INPUT_LENGTH
            )
        }

        return if (exception is WearableWithSameNameExistsException) {
            getString(R.string.ioa_sec_bp_add_error_usedname_message)
        } else {
            getString(R.string.ioa_error_unknown_format, exception.message)
        }
    }

    private fun subscribeObservers() {
        wearablesAddViewModel.availableWearablesState.observe(viewLifecycleOwner) {
            binding.proximityAddSwiperefresh.isRefreshing = false
            when (it) {
                is WearablesAddViewModel.AvailableWearablesState.AvailableWearablesSuccess -> {
                    val nameFilter = { withName: Boolean ->
                        filter@{ wearable: WearablesManager.Wearable ->
                            Boolean
                            val s = wearable.name.trim { it <= ' ' }
                            return@filter if (withName) s.isNotEmpty() else s.isEmpty()
                        }
                    }
                    devicesWithNames.clear()
                    devicesWithNames.addAll(it.wearables.filter(nameFilter(true)))
                    devicesWithoutNames.clear()
                    devicesWithoutNames.addAll(it.wearables.filter(nameFilter(false)))
                    updateDevicesDisplayed(binding.proximityAddFilterSwitch.isChecked)
                }
                is WearablesAddViewModel.AvailableWearablesState.FailedToGetAvailableWearables -> {
                    val emptyView = binding.proximityAddEmpty
                    emptyView.makeVisible()
                    emptyView.setText(R.string.ioa_sec_bp_add_empty)
                    when (it.exception) {
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

    override fun onRefresh() {
        devicesWithNames.clear()
        devicesWithoutNames.clear()
        devicesDisplayed.clear()
        binding.wearablesAddRecyclerview.adapter!!.notifyDataSetChanged()
        binding.proximityAddEmpty.setText(R.string.ioa_sec_bp_add_searching)
        wearablesAddViewModel.getAvailableWearables()
    }

    private fun showBluetoothDisabledDialog() {
        if (bluetoothDisabledDialog != null) return

        GenericAlertDialogFragment.show(
            childFragmentManager,
            requireContext(),
            getString(R.string.ioa_sec_bp_add_error_bluetoothdisabled_title),
            getString(R.string.ioa_sec_bp_add_error_bluetoothdisabled_message),
            null,
            true,
            null,
            BT_DISABLED_ALERT
        ).also {
            it.setPositiveButtonClickListener { _, _ -> requestBluetoothEnabling() }
            it.setCancelListener { requireActivity().onBackPressed() }
        }
    }

    private fun requestBluetoothEnabling() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enabledBluetoothLauncher.launch(enableBtIntent)
    }

    private fun onItemSelect(genericDevice: WearablesManager.Wearable) {
        selectedItem = genericDevice
        setNameAlertDialog.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
    }
}