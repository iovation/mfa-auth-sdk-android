/*
 *  Copyright (c) 2016. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.BluetoothDisabledException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.BluetoothPermissionException
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.wearables.WearableWithSameNameExistsException
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentWearablesAddBinding
import com.launchkey.android.authenticator.sdk.ui.databinding.ItemBluetoothDeviceDiscoverBinding
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.*
import com.launchkey.android.authenticator.sdk.ui.internal.util.*
import java.util.*

class WearablesAddFragment : BaseAppCompatFragment(R.layout.fragment_wearables_add), OnRefreshListener {
    companion object {
        private const val BT_DISABLED_ALERT_RESULT = "BT_DISABLED_ALERT_RESULT"
        private const val BT_DISABLED_ALERT = "BT_DISABLED_ALERT"
    }

    private val binding: FragmentWearablesAddBinding by viewBinding(FragmentWearablesAddBinding::bind)
    private val wearablesAddViewModel: WearablesAddViewModel by viewModels({ requireParentFragment() })
    private val devicesDisplayed: MutableList<WearablesManager.Wearable> = mutableListOf()
    private val devicesWithNames: MutableList<WearablesManager.Wearable> = mutableListOf()
    private val devicesWithoutNames: MutableList<WearablesManager.Wearable> = mutableListOf()
    private var selectedItem: WearablesManager.Wearable? = null
    private var bluetoothOffNotified = false
    private val btLauncher = registerForActivityResult(StartActivityForResult(), ActivityResultCallback { result ->
        if (result == null) return@ActivityResultCallback
        if (result.resultCode == Activity.RESULT_OK) {
            onRefresh()
        } else {
            bluetoothDisabledAlertDialog.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
        }
    })
    private lateinit var bluetoothDisabledAlertDialog: DialogFragmentViewModel
    private lateinit var bluetoothDisabledResultAlertDialog: DialogFragmentViewModel
    private lateinit var setNameAlertDialog: DialogFragmentViewModel
    private val okClick = DialogInterface.OnClickListener { _, _ ->
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        btLauncher.launch(enableBtIntent)
    }
    private val cancelListener = DialogInterface.OnCancelListener { requireActivity().onBackPressed() }
    private val okClickResult = DialogInterface.OnClickListener { dialog, which -> cancelListener.onCancel(dialog) }
    private val cancelListenerResult = DialogInterface.OnCancelListener { requireActivity().onBackPressed() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.proximityAddSwiperefresh.setOnRefreshListener(this)
        binding.proximityAddSwiperefresh.isRefreshing = true
        binding.proximityAddSwiperefresh.setColorSchemeColors(
                UiUtils.getColorFromTheme(requireContext(), R.attr.authenticatorColorAccent, R.color.lk_wl_default_accent))
        bluetoothDisabledAlertDialog = ViewModelProvider(this, defaultViewModelProviderFactory).get(BT_DISABLED_ALERT_RESULT, DialogFragmentViewModel::class.java)
        bluetoothDisabledResultAlertDialog = ViewModelProvider(this, defaultViewModelProviderFactory).get(BT_DISABLED_ALERT, DialogFragmentViewModel::class.java)
        setNameAlertDialog = ViewModelProvider(this, defaultViewModelProviderFactory).get(SetNameDialogFragment::class.java.simpleName, DialogFragmentViewModel::class.java)
        val toggle = binding.proximityAddFilterSwitch
        toggle.setOnCheckedChangeListener { _, isChecked -> updateDevicesDisplayed(isChecked) }
        val toggleLayout: View = binding.proximityAddFilterLayout
        toggleLayout.setOnClickListener { toggle.isChecked = !toggle.isChecked }
        val adapter = DiscoveredWearablesAdapter(devicesDisplayed) { onItemSelect(it) }
        val recyclerView = binding.wearablesAddRecyclerview
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        if (!wearablesAddViewModel.isSupported()) {
            requireActivity().onBackPressed()
        }
        bluetoothDisabledAlertDialog.state.observe(viewLifecycleOwner) { state ->
            if (state is DialogFragmentViewModel.State.NeedsToBeShown) {
                GenericAlertDialogFragment.show(childFragmentManager,
                        this@WearablesAddFragment.requireContext(),
                        getString(R.string.ioa_generic_warning),
                        getString(R.string.ioa_sec_bp_add_error_bluetoothdisabled_message),
                        null,
                        true,
                        null,
                        BT_DISABLED_ALERT)
                bluetoothDisabledAlertDialog.changeState(DialogFragmentViewModel.State.Shown)
            } else if (state is DialogFragmentViewModel.State.Shown) {
                val bluetoothDisabledDialog = childFragmentManager.findFragmentByTag(BT_DISABLED_ALERT) as AlertDialogFragment
                bluetoothDisabledDialog.setPositiveButtonClickListener(okClickResult)
                bluetoothDisabledDialog.setCancelListener(cancelListenerResult)
            }
        }
        bluetoothDisabledResultAlertDialog.state.observe(viewLifecycleOwner) { state ->
            if (state is DialogFragmentViewModel.State.NeedsToBeShown) {
                GenericAlertDialogFragment.show(childFragmentManager,
                        this@WearablesAddFragment.requireContext(),
                        getString(R.string.ioa_sec_bp_add_error_bluetoothdisabled_title),
                        getString(R.string.ioa_sec_bp_add_error_bluetoothdisabled_message),
                        null,
                        true,
                        null,
                        BT_DISABLED_ALERT_RESULT)
                bluetoothDisabledResultAlertDialog.changeState(DialogFragmentViewModel.State.Shown)
            } else if (state is DialogFragmentViewModel.State.Shown) {
                val bluetoothDisabledDialog = childFragmentManager.findFragmentByTag(BT_DISABLED_ALERT_RESULT) as AlertDialogFragment
                bluetoothDisabledDialog.setPositiveButtonClickListener(okClick)
                bluetoothDisabledDialog.setCancelListener(cancelListener)
            }
        }
        setNameAlertDialog.state.observe(viewLifecycleOwner) { state ->
            if (state is DialogFragmentViewModel.State.NeedsToBeShown) {
                SetNameDialogFragment.show(
                        requireContext(),
                        childFragmentManager,
                        R.string.ioa_sec_bp_add_dialog_setname_title,
                        R.string.ioa_sec_bp_add_dialog_setname_hint,
                        R.string.ioa_generic_done,
                        selectedItem!!.name)
                setNameAlertDialog.changeState(DialogFragmentViewModel.State.Shown)
            } else if (state is DialogFragmentViewModel.State.Shown) {
                val setNameDialogFragment = childFragmentManager.findFragmentByTag(SetNameDialogFragment::class.java.simpleName) as SetNameDialogFragment
                setNameDialogFragment.setPositiveButtonClickListener(object : SetNameDialogFragment.SetNameListener {
                    override fun onNameSet(dialog: SetNameDialogFragment?, name: String?) {
                        selectedItem!!.name = name!!
                        wearablesAddViewModel.addWearable(selectedItem!!)
                    }
                })
            }
        }

        wearablesAddViewModel.availableWearablesState.observe(viewLifecycleOwner) {
            when (it) {
                is WearablesAddViewModel.AvailableWearablesState.AvailableWearablesSuccess -> {
                    val nameFilter = { withName: Boolean ->
                        filter@{ wearable: WearablesManager.Wearable -> Boolean
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
                    if (it.exception is BluetoothDisabledException ||
                            it.exception is BluetoothPermissionException) {
                        requestBluetoothEnabling()
                    } else {
                        // Exit
                        requireActivity().onBackPressed()
                    }
                }
            }
        }
        wearablesAddViewModel.addWearableState.observe(viewLifecycleOwner) {
            when (it) {
                is WearablesAddViewModel.AddWearableState.AddedNewWearable -> {
                    val setNameDialogFragment = childFragmentManager.findFragmentByTag(SetNameDialogFragment::class.java.simpleName) as SetNameDialogFragment
                    setNameDialogFragment.dismiss()
                    (requireParentFragment() as WearablesFragment).wearableAdded = true
                    requireActivity().onBackPressed()
                }
                is WearablesAddViewModel.AddWearableState.FailedToAddWearable -> {
                    val errorMessage = getErrorMessage(it.failure)
                    val setNameDialogFragment = childFragmentManager.findFragmentByTag(SetNameDialogFragment::class.java.simpleName) as SetNameDialogFragment
                    setNameDialogFragment.setErrorMessage(errorMessage)
                }
            }
        }
    }

    private fun getErrorMessage(exception: Exception): String? {
        if (exception is WearablesAddViewModel.WearableNameTooShortException) {
            return resources.getQuantityString(
                    R.plurals.ioa_sec_bp_add_error_invalidname_message_format, Constants.MINIMUM_INPUT_LENGTH, Constants.MINIMUM_INPUT_LENGTH)
        }

        return if (exception is WearableWithSameNameExistsException) {
            getString(R.string.ioa_sec_bp_add_error_usedname_message)
        } else {
            getString(R.string.ioa_error_unknown_format, exception.message)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        UiUtils.applyThemeToMenu(inflater, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_help) {
            HelpDialogFragment.show(childFragmentManager, requireContext(), getString(R.string.ioa_sec_bp_help_title), getString(R.string.ioa_sec_bp_help_message))
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

    private fun requestBluetoothEnabling() {
        if (bluetoothOffNotified) {
            return
        }
        bluetoothOffNotified = true
        bluetoothDisabledResultAlertDialog.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
    }

    private fun onItemSelect(genericDevice: WearablesManager.Wearable) {
        selectedItem = genericDevice
        setNameAlertDialog.changeState(DialogFragmentViewModel.State.NeedsToBeShown)
    }

    private class DiscoveredWearablesAdapter(
            private val devices: List<WearablesManager.Wearable>,
            private val onItemClickListener: ((WearablesManager.Wearable) -> Unit)
            ) : RecyclerView.Adapter<DiscoveredWearablesAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemBluetoothDeviceDiscoverBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                val listItemsUiProp = AuthenticatorUIManager.instance.config.themeObj().listItems
                root.background = listItemsUiProp.colorBg
                bluetoothTextTitle.setTextColor(listItemsUiProp.colorText)
            })
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = devices[position]
            val label = if (item.name.trim { it <= ' ' }.isEmpty()) item.id else item.name
            holder.binding.bluetoothTextTitle.text = label
            holder.binding.root.setOnClickListener { onItemClickListener(devices[position]) }
        }

        override fun getItemCount(): Int = devices.size

        private class ViewHolder(val binding: ItemBluetoothDeviceDiscoverBinding) : RecyclerView.ViewHolder(binding.root)
    }
}