/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentLocationsSettingsBinding
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

class LocationsSettingsFragment :
    BaseAppCompatFragment(R.layout.fragment_locations_settings) {
    companion object {
        private const val REMOVE_ALL_TAG = "REMOVE_ALL_TAG"
        private const val REMOVE_SINGLE_TAG = "REMOVE_SINGLE_TAG"
    }

    private lateinit var settingsPanel: SettingsPanel
    private val binding by viewBinding(FragmentLocationsSettingsBinding::bind)
    private val adapter: ItemAdapter<LocationsManager.StoredLocation>
        get() = binding.geofencingSettingsList.adapter as ItemAdapter<LocationsManager.StoredLocation>
    private val locationsSettingsViewModel: LocationsSettingsViewModel by viewModels({ requireParentFragment() })
    private val verificationFlagViewModel: VerificationFlagViewModel by lazy { ViewModelProvider(this).get(VerificationFlagViewModel.LOCATIONS, VerificationFlagViewModel::class.java) }
    private val timerViewModel: TimerViewModel by viewModels()
    
    private val loadingDialogFragment: ProgressDialogFragment?
        get() = childFragmentManager.findFragmentByTag(ProgressDialogFragment::class.java.simpleName) as? ProgressDialogFragment
    private val removeSingleLocationDialogFragment: AlertDialogFragment?
        get() = childFragmentManager.findFragmentByTag(REMOVE_SINGLE_TAG) as? AlertDialogFragment
    private val removeAllLocationsDialogFragment: AlertDialogFragment?
        get() = childFragmentManager.findFragmentByTag(REMOVE_ALL_TAG) as? AlertDialogFragment
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.geofencing_add, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.geofencing_add -> {
                locationsSettingsViewModel.requestNewLocation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettingsPanel()
        setupLocationsList()
        subscribeObservers()
        
        removeSingleLocationDialogFragment?.setPositiveButtonClickListener { _, _ ->
            locationsSettingsViewModel.removeSelectedLocation()
        }
        removeAllLocationsDialogFragment?.setPositiveButtonClickListener { _, _ ->
            locationsSettingsViewModel.removeAllLocations()
        }
    }
    
    private fun setupLocationsList() {
        val adapter = ItemAdapter<LocationsManager.StoredLocation>(
            AuthenticatorUIManager.instance.config.themeObj(),
            TimeAgo(requireContext()),
            R.string.ioa_calabash_sett_remove_geo_item_format,
            R.drawable.ic_place_black_24dp
        ) { locationToRemove ->
            locationsSettingsViewModel.setLocationToRemove(locationToRemove.item)
        }
        
        binding.geofencingSettingsList.adapter = adapter
        binding.geofencingSettingsList.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun setupSettingsPanel() {
        settingsPanel = binding.geofencingSettingsPanel.apply {
            setRemoveText(R.string.ioa_sec_geo_sett_panel_remove_text)
            setRemoveButtonText(R.string.ioa_sec_geo_sett_panel_remove_button)
            setVerifiedWhenText(R.string.ioa_sec_panel_verify_always)
            setOnRemoveButtonClick {
                locationsSettingsViewModel.requestRemoveAllLocations()
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
        
        locationsSettingsViewModel.getStoredLocationsState.observe(viewLifecycleOwner) { fetchLocationsState ->
            loadingDialogFragment?.dismiss()
            when (fetchLocationsState) {
                is LocationsSettingsViewModel.GetStoredLocationsState.GotStoredLocations -> {
                    timerViewModel.stopTimers()
                    adapter.submitList(fetchLocationsState.locations)
                    val timerItems: List<TimerViewModel.TimerItem<LocationItem>> =
                        fetchLocationsState.locations.mapNotNull { locationItem ->
                            when (locationItem.pendingState) {
                                ItemAdapter.ItemPendingState.NotPending -> null
                                is ItemAdapter.ItemPendingState.PendingActivation -> TimerViewModel.TimerItem(
                                    locationItem,
                                    (locationItem.pendingState as ItemAdapter.ItemPendingState.PendingActivation).activatedAtTimeInMillis
                                )
                                is ItemAdapter.ItemPendingState.PendingRemoval -> TimerViewModel.TimerItem(
                                    locationItem,
                                    (locationItem.pendingState as ItemAdapter.ItemPendingState.PendingRemoval).removedAtTimeInMillis
                                )
                            }
                        }
        
                    timerViewModel.startTimers(timerItems)
                }
                LocationsSettingsViewModel.GetStoredLocationsState.GettingStoredLocations -> {
                    if (loadingDialogFragment == null)
                        ProgressDialogFragment.show(
                            null,
                            getString(R.string.ioa_sec_geo_sett_loading),
                            cancellable = false,
                            indeterminate = true,
                            childFragmentManager,
                            ProgressDialogFragment::class.java.simpleName
                        )
                }
                else -> Unit
            }
        }
        
        locationsSettingsViewModel.newLocationState.observe(viewLifecycleOwner) { newLocationState ->
            when (newLocationState) {
                LocationsSettingsViewModel.NewLocationState.AddingNewLocation -> {
                    timerViewModel.stopTimers()
                }
                else -> Unit
            }
        }
        
        locationsSettingsViewModel.removeSingleLocationState.observe(viewLifecycleOwner) { removeSingleLocationState ->
            when (removeSingleLocationState) {
                is LocationsSettingsViewModel.RemoveSingleLocationState.CancelledLocationRemoval -> {
                    timerViewModel.cancelTimerForItem(removeSingleLocationState.location)
                }
                is LocationsSettingsViewModel.RemoveSingleLocationState.RemovingLocation -> {
                    val locationToRemove = removeSingleLocationState.location
                    val name = locationToRemove.name
                    val header: String
                    val message: String
                    if (locationToRemove.isPendingRemoval) {
                        header = getString(R.string.ioa_sec_geo_sett_dialog_undoremove_single_title)
                        message = getString(
                            R.string.ioa_sec_geo_sett_dialog_undoremoval_message_format,
                            name
                        )
                    } else {
                        header = getString(R.string.ioa_sec_geo_sett_dialog_remove_single_title)
                        message = getString(
                            R.string.ioa_sec_geo_sett_dialog_remove_single_message_format,
                            name
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
                        locationsSettingsViewModel.removeSelectedLocation()
                    }
                }
                else -> Unit
            }
        }
        
        locationsSettingsViewModel.removeAllLocationsState.observe(viewLifecycleOwner) { removeAllLocationsState ->
            if (removeAllLocationsState is LocationsSettingsViewModel.RemoveAllLocationsState.RemovingAllLocations) {
                GenericAlertDialogFragment.show(
                    childFragmentManager,
                    requireContext(),
                    getString(R.string.ioa_sec_geo_sett_dialog_remove_all_title),
                    getString(R.string.ioa_sec_geo_sett_dialog_remove_all_messsage),
                    getString(R.string.ioa_generic_yes),
                    true,
                    getString(R.string.ioa_generic_cancel),
                    REMOVE_ALL_TAG
                ).setPositiveButtonClickListener { _, _ ->
                    locationsSettingsViewModel.removeAllLocations()
                }
            }
        }
        
        timerViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TimerViewModel.State.ItemFinished<*> -> {
                    // remove or activate the location
                    val item = state as TimerViewModel.State.ItemFinished<LocationItem>
                    adapter.notifyTimerFinished(item.timerItem.item)
                }
                is TimerViewModel.State.ItemUpdated<*> -> {
                    // update the location
                    val item = state as TimerViewModel.State.ItemUpdated<LocationItem>
                    adapter.notifyTimerUpdate(item.timerItem.item, item.remainingMillis)
                }
                TimerViewModel.State.AllItemsFinished -> {
                    locationsSettingsViewModel.fetchLocations()
                }
            }
        }
    }
}