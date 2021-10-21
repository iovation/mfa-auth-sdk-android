/*
 *  Copyright (c) 2016. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentBluetoothSettingsBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.ItemAdapter
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.TimerViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.VerificationFlagViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.DialogFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.GenericAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.ProgressDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.*

class WearablesSettingsFragment : BaseAppCompatFragment(R.layout.fragment_bluetooth_settings) {
    companion object {
        private const val REMOVE_ALL_TAG = "REMOVE_ALL_TAG"
        private const val REMOVE_SINGLE_TAG = "REMOVE_SINGLE_TAG"
    }

    private val binding: FragmentBluetoothSettingsBinding by viewBinding(FragmentBluetoothSettingsBinding::bind)
    private val wearablesSettingsViewModel: WearablesSettingsViewModel by viewModels({ requireParentFragment() })
    private val verificationFlagViewModel: VerificationFlagViewModel by lazy { ViewModelProvider(this).get(VerificationFlagViewModel.WEARABLES, VerificationFlagViewModel::class.java) }
    private val timerViewModel: TimerViewModel by viewModels()
    private var loadingDialog: ProgressDialogFragment? = null
    private val confirmRemoveDialog: AlertDialogFragment?
        get() = childFragmentManager.findFragmentByTag(REMOVE_SINGLE_TAG) as? AlertDialogFragment
    private val confirmRemoveSingleDialogViewModel: DialogFragmentViewModel by lazy { ViewModelProvider(this, defaultViewModelProviderFactory).get(REMOVE_SINGLE_TAG, DialogFragmentViewModel::class.java) }
    private val confirmRemoveAllDialogViewModel: DialogFragmentViewModel by lazy { ViewModelProvider(this, defaultViewModelProviderFactory).get(REMOVE_ALL_TAG, DialogFragmentViewModel::class.java) }
    private val yesClick = SingleRemoveDialogListener()
    private val adapter: ItemAdapter<WearablesManager.Wearable>
        get() = binding.bluetoothSettingsList.adapter as ItemAdapter<WearablesManager.Wearable>

    private inner class SingleRemoveDialogListener : DialogInterface.OnClickListener {
        lateinit var wearable: WearablesManager.Wearable
        override fun onClick(dialog: DialogInterface?, which: Int) {
            if (confirmRemoveDialog == null) return
            if (wearable.isPendingRemoval) {
                wearablesSettingsViewModel.cancelRemoveWearable(wearable)
            } else {
                wearablesSettingsViewModel.removeWearable(wearable)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bluetooth_add, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val onRemoveAllListener = View.OnClickListener { confirmRemoveAllDialogViewModel.changeState(DialogFragmentViewModel.State.NeedsToBeShown) }
        confirmRemoveAllDialogViewModel.state.observe(viewLifecycleOwner) { state ->
            if (state is DialogFragmentViewModel.State.NeedsToBeShown) {
                GenericAlertDialogFragment.show(childFragmentManager,
                        requireContext(),
                        getString(R.string.ioa_sec_bp_sett_dialog_remove_all_title),
                        getString(R.string.ioa_sec_bp_sett_dialog_remove_all_messsage),
                        getString(R.string.ioa_generic_yes),
                        true,
                        getString(R.string.ioa_generic_cancel),
                        REMOVE_ALL_TAG)
                confirmRemoveAllDialogViewModel.changeState(DialogFragmentViewModel.State.Shown)
            } else if (state is DialogFragmentViewModel.State.Shown) {
                val yesClick = DialogInterface.OnClickListener { dialog, which ->
                    wearablesSettingsViewModel.removeAllWearables()
                }
                val removeAllDialog = childFragmentManager.findFragmentByTag(REMOVE_ALL_TAG) as AlertDialogFragment?
                removeAllDialog!!.setPositiveButtonClickListener(yesClick)
            }
        }

        binding.bluetoothSettingsList.adapter = ItemAdapter<WearablesManager.Wearable>(
                AuthenticatorUIManager.instance.config.themeObj(),
                TimeAgo(requireContext()),
                R.string.ioa_calabash_sett_remove_wear_item_format,
                R.drawable.ic_bluetooth_black_24dp
        ) { wearableToRemove ->
            yesClick.wearable = wearableToRemove.item
            confirmRemoveSingleDialogViewModel.changeState(DialogFragmentViewModel.State.NeedsToBeShown)

        }
        binding.bluetoothSettingsList.layoutManager = LinearLayoutManager(requireContext())
        binding.bluetoothSettingsList.setHasFixedSize(true)
        confirmRemoveSingleDialogViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                DialogFragmentViewModel.State.NeedsToBeShown -> {
                    val item = yesClick.wearable
                    val pendingRemoval = item.isPendingRemoval
                    val name = item.name
                    val header = if (pendingRemoval) getString(R.string.ioa_sec_bp_sett_dialog_undoremove_single_title) else getString(R.string.ioa_sec_bp_sett_dialog_remove_single_title)
                    val message = if (pendingRemoval) getString(R.string.ioa_sec_bp_sett_dialog_undoremoval_message_format, name) else getString(R.string.ioa_sec_bp_sett_dialog_remove_single_message_format, name)
                    GenericAlertDialogFragment.show(childFragmentManager,
                            requireContext(),
                            header,
                            message,
                            getString(R.string.ioa_generic_yes),
                            true,
                            getString(R.string.ioa_generic_cancel),
                            REMOVE_SINGLE_TAG)
                    confirmRemoveSingleDialogViewModel.changeState(DialogFragmentViewModel.State.Shown)
                }
                DialogFragmentViewModel.State.Shown -> {
                    confirmRemoveDialog?.setPositiveButtonClickListener(yesClick)
                }
            }
        }
        loadingDialog = ProgressDialogFragment.newInstance(null, getString(R.string.ioa_sec_bp_sett_loading), false, true)

        val settingsPanel = binding.bluetoothSettingsPanel

        wearablesSettingsViewModel.getStoredWearablesState.observe(viewLifecycleOwner) {
            when (it) {
                is WearablesSettingsViewModel.GetStoredWearablesState.Success -> {
                    timerViewModel.stopTimers()
                    adapter.submitList(it.wearables)
                    adapter.notifyDataSetChanged()
                    loadingDialog!!.dismiss()
                    val timerItems: List<TimerViewModel.TimerItem<WearableItem>> =
                            it.wearables.mapNotNull { wearableItem ->
                                when (wearableItem.pendingState) {
                                    ItemAdapter.ItemPendingState.NotPending -> null
                                    is ItemAdapter.ItemPendingState.PendingActivation -> TimerViewModel.TimerItem(
                                            wearableItem,
                                            (wearableItem.pendingState as ItemAdapter.ItemPendingState.PendingActivation).activatedAtTimeInMillis
                                    )
                                    is ItemAdapter.ItemPendingState.PendingRemoval -> TimerViewModel.TimerItem(
                                            wearableItem,
                                            (wearableItem.pendingState as ItemAdapter.ItemPendingState.PendingRemoval).removedAtTimeInMillis
                                    )
                                }
                            }

                    timerViewModel.startTimers(timerItems)
                }
                is WearablesSettingsViewModel.GetStoredWearablesState.Failure -> requireActivity().onBackPressed()
            }
        }

        verificationFlagViewModel.verificationFlag.observe(viewLifecycleOwner) { verificationFlagState ->
            when (verificationFlagState) {
                is VerificationFlagViewModel.VerificationFlagState.Failed -> requireActivity().finish()
                VerificationFlagViewModel.VerificationFlagState.FetchingVerificationFlag -> Unit
                is VerificationFlagViewModel.VerificationFlagState.Pending -> UiUtils.updateSettingsPanelWithFactorState(
                        settingsPanel,
                        verificationFlagState.verificationFlag,
                        verificationFlagState.millisUntilToggled,
                        false
                )
                is VerificationFlagViewModel.VerificationFlagState.GotVerificationFlag -> {
                    UiUtils.updateSettingsPanelWithFactorState(
                            settingsPanel,
                            verificationFlagState.verificationFlag,
                            0,
                            false
                    )
                }
            }
        }

        wearablesSettingsViewModel.cancelRemoveState.observe(viewLifecycleOwner) {
            when (it) {
                is WearablesSettingsViewModel.CancelRemoveState.Success -> {
                    timerViewModel.cancelTimerForItem(it.wearable)
                }
                is WearablesSettingsViewModel.CancelRemoveState.Failure -> requireActivity().onBackPressed()
            }
        }

        wearablesSettingsViewModel.removeState.observe(viewLifecycleOwner) {
            when (it) {
                is WearablesSettingsViewModel.RemoveState.Success -> { /* getStoredWearables() called from dialog to refresh list */ }
                is WearablesSettingsViewModel.RemoveState.Failure -> requireActivity().onBackPressed()
            }
        }

        wearablesSettingsViewModel.removeAllState.observe(viewLifecycleOwner) {
            // Go back regardless
            requireActivity().onBackPressed()
        }

        timerViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TimerViewModel.State.ItemFinished<*> -> {
                    // remove or activate the location
                    val item = state as TimerViewModel.State.ItemFinished<WearableItem>
                    adapter.notifyTimerFinished(item.timerItem.item)
                }
                is TimerViewModel.State.ItemUpdated<*> -> {
                    // update the location
                    val item = state as TimerViewModel.State.ItemUpdated<WearableItem>
                    adapter.notifyTimerUpdate(item.timerItem.item, item.remainingMillis)
                }
                TimerViewModel.State.AllItemsFinished -> {
                    wearablesSettingsViewModel.fetchWearables()
                }
            }
        }

        with(settingsPanel) {
            setRemoveText(R.string.ioa_sec_bp_sett_panel_remove_text)
            setRemoveButtonText(R.string.ioa_sec_bp_sett_panel_remove_button)
            setOnRemoveButtonClick(onRemoveAllListener)
            disallowSwitchSwipe()
            setOnSwitchClickedListener {
                verificationFlagViewModel.toggleVerificationFlag(
                        if (isSwitchOn) VerificationFlag.State.ALWAYS
                        else VerificationFlag.State.WHEN_REQUIRED
                )
            }
        }
        loadingDialog!!.show(childFragmentManager, ProgressDialogFragment::class.java.simpleName)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (R.id.bluetooth_add == id) {
            // TODO: 10/15/21 don't call parent fragments method
            (requireParentFragment() as WearablesFragment).goToAdd(true)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}