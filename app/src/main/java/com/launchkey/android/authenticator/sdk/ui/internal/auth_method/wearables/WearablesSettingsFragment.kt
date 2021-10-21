/*
 *  Copyright (c) 2016. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentBluetoothSettingsBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.ItemAdapter
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.SettingsPanel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.TimerViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.VerificationFlagViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.GenericAlertDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.ProgressDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class WearablesSettingsFragment : BaseAppCompatFragment(R.layout.fragment_bluetooth_settings) {
    companion object {
        private const val REMOVE_ALL_TAG = "REMOVE_ALL_TAG"
        private const val REMOVE_SINGLE_TAG = "REMOVE_SINGLE_TAG"
    }

    private lateinit var settingsPanel: SettingsPanel
    private val binding by viewBinding(FragmentBluetoothSettingsBinding::bind)
    private val adapter: ItemAdapter<WearablesManager.Wearable>
        get() = binding.bluetoothSettingsList.adapter as ItemAdapter<WearablesManager.Wearable>
    private val wearablesSettingsViewModel: WearablesSettingsViewModel by viewModels({ requireParentFragment() })
    private val verificationFlagViewModel: VerificationFlagViewModel by lazy {
        ViewModelProvider(this).get(
            VerificationFlagViewModel.WEARABLES,
            VerificationFlagViewModel::class.java
        )
    }
    private val timerViewModel: TimerViewModel by viewModels()

    private val loadingDialogFragment: ProgressDialogFragment?
        get() = childFragmentManager.findFragmentByTag(ProgressDialogFragment::class.java.simpleName) as? ProgressDialogFragment
    private val removeSingleWearableDialogFragment: AlertDialogFragment?
        get() = childFragmentManager.findFragmentByTag(REMOVE_SINGLE_TAG) as? AlertDialogFragment
    private val removeAllWearablesDialogFragment: AlertDialogFragment?
        get() = childFragmentManager.findFragmentByTag(REMOVE_ALL_TAG) as? AlertDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bluetooth_add, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bluetooth_add -> {
                wearablesSettingsViewModel.requestNewWearable()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettingsPanel()
        setupWearablesList()
        subscribeObservers()

        removeSingleWearableDialogFragment?.setPositiveButtonClickListener { _, _ ->
            wearablesSettingsViewModel.removeSelectedWearable()
        }
        removeAllWearablesDialogFragment?.setPositiveButtonClickListener { _, _ ->
            wearablesSettingsViewModel.removeAllWearables()
        }
    }

    private fun setupSettingsPanel() {
        settingsPanel = binding.bluetoothSettingsPanel.apply {
            setRemoveText(R.string.ioa_sec_bp_sett_panel_remove_text)
            setRemoveButtonText(R.string.ioa_sec_bp_sett_panel_remove_button)
            setVerifiedWhenText(R.string.ioa_sec_panel_verify_always)
            setOnRemoveButtonClick {
                wearablesSettingsViewModel.requestRemoveAllWearables()
            }
            disallowSwitchSwipe()
            setOnSwitchClickedListener {
                verificationFlagViewModel.toggleVerificationFlag(
                    if (isSwitchOn) VerificationFlag.State.ALWAYS
                    else VerificationFlag.State.WHEN_REQUIRED
                )
            }
        }
    }

    private fun setupWearablesList() {
        binding.bluetoothSettingsList.adapter = ItemAdapter<WearablesManager.Wearable>(
            AuthenticatorUIManager.instance.config.themeObj(),
            TimeAgo(requireContext()),
            R.string.ioa_calabash_sett_remove_wear_item_format,
            R.drawable.ic_bluetooth_black_24dp
        ) { wearableToRemove ->
            wearablesSettingsViewModel.setWearableToRemove(wearableToRemove.item)
        }
        binding.bluetoothSettingsList.setHasFixedSize(true)
        binding.bluetoothSettingsList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeObservers() {
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

        wearablesSettingsViewModel.getStoredWearablesState.observe(viewLifecycleOwner) { fetchWearablesState ->
            loadingDialogFragment?.dismiss()
            when (fetchWearablesState) {
                is WearablesSettingsViewModel.GetStoredWearablesState.GotStoredWearables -> {
                    timerViewModel.stopTimers()
                    adapter.submitList(fetchWearablesState.wearables)
                    val timerItems: List<TimerViewModel.TimerItem<WearableItem>> =
                        fetchWearablesState.wearables.mapNotNull { wearableItem ->
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
                WearablesSettingsViewModel.GetStoredWearablesState.GettingStoredWearables -> {
                    if (loadingDialogFragment == null) {
                        ProgressDialogFragment.show(
                            null,
                            getString(R.string.ioa_sec_bp_sett_loading),
                            cancellable = false,
                            indeterminate = false,
                            childFragmentManager,
                            ProgressDialogFragment::class.java.simpleName
                        )
                    }
                }
                else -> Unit
            }
        }

        wearablesSettingsViewModel.newWearableState.observe(viewLifecycleOwner) { newWearableState ->
            when (newWearableState) {
                WearablesSettingsViewModel.NewWearableState.AddingNewWearable -> {
                    timerViewModel.stopTimers()
                }
                else -> Unit
            }
        }

        wearablesSettingsViewModel.removeSingleWearableState.observe(viewLifecycleOwner) { removeSingleWearableState ->
            when (removeSingleWearableState) {
                is WearablesSettingsViewModel.RemoveSingleWearableState.CancelledWearableRemoval -> {
                    timerViewModel.cancelTimerForItem(removeSingleWearableState.wearable)
                }
                is WearablesSettingsViewModel.RemoveSingleWearableState.RemovingWearable -> {
                    val wearableToRemove = removeSingleWearableState.wearable
                    val header: String
                    val message: String
                    if (wearableToRemove.isPendingRemoval) {
                        header = getString(R.string.ioa_sec_bp_sett_dialog_undoremove_single_title)
                        message = getString(
                            R.string.ioa_sec_bp_sett_dialog_undoremoval_message_format,
                            wearableToRemove.name
                        )
                    } else {
                        header = getString(R.string.ioa_sec_bp_sett_dialog_remove_single_title)
                        message = getString(
                            R.string.ioa_sec_bp_sett_dialog_remove_single_message_format,
                            wearableToRemove.name
                        )
                    }

                    GenericAlertDialogFragment.show(
                        childFragmentManager,
                        requireContext(),
                        header,
                        message,
                        getString(R.string.ioa_generic_yes),
                        true,
                        getString(R.string.ioa_generic_cancel),
                        REMOVE_SINGLE_TAG
                    ).setPositiveButtonClickListener { _, _ ->
                        wearablesSettingsViewModel.removeSelectedWearable()
                    }
                }
                else -> Unit
            }
        }

        wearablesSettingsViewModel.removeAllWearablesState.observe(viewLifecycleOwner) { removeAllWearablesState ->
            if (removeAllWearablesState is WearablesSettingsViewModel.RemoveAllWearablesState.RemovingAllWearables) {
                GenericAlertDialogFragment.show(
                    childFragmentManager,
                    requireContext(),
                    getString(R.string.ioa_sec_bp_sett_dialog_remove_all_title),
                    getString(R.string.ioa_sec_bp_sett_dialog_remove_all_messsage),
                    getString(R.string.ioa_generic_yes),
                    true,
                    getString(R.string.ioa_generic_cancel),
                    REMOVE_ALL_TAG
                ).setPositiveButtonClickListener { _, _ ->
                    wearablesSettingsViewModel.removeAllWearables()
                }
            }
        }

        timerViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TimerViewModel.State.ItemFinished<*> -> {
                    // remove or activate the wearable
                    val item = state as TimerViewModel.State.ItemFinished<WearableItem>
                    adapter.notifyTimerFinished(item.timerItem.item)
                }
                is TimerViewModel.State.ItemUpdated<*> -> {
                    // update the wearable
                    val item = state as TimerViewModel.State.ItemUpdated<WearableItem>
                    adapter.notifyTimerUpdate(item.timerItem.item, item.remainingMillis)
                }
                TimerViewModel.State.AllItemsFinished -> {
                    wearablesSettingsViewModel.fetchWearables()
                }
            }
        }
    }
}
