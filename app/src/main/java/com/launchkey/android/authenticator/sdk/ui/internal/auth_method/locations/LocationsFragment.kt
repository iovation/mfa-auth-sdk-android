package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentLocationsBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.AuthMethodActivity
import com.launchkey.android.authenticator.sdk.ui.SecurityFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.bundleArgument
import com.launchkey.android.authenticator.sdk.ui.internal.util.setNavigationButton
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class LocationsFragment : BaseAppCompatFragment(R.layout.fragment_locations) {
    private val binding by viewBinding(FragmentLocationsBinding::bind)
    private val startPage by bundleArgument<AuthMethodActivity.Page>(AuthMethodActivity.PAGE_KEY)
    private val locationsAddViewModel: LocationsAddViewModel by viewModels()
    private val locationsSettingsViewModel: LocationsSettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace(
                    binding.fragmentMapContainer.id,
                    when (startPage) {
                        AuthMethodActivity.Page.ADD -> LocationsAddFragment::class.java
                        AuthMethodActivity.Page.SETTINGS -> LocationsSettingsFragment::class.java
                        else -> throw IllegalArgumentException("Unexpected argument")
                    }, null
                )
            }
        }
        
        setupToolbar()
        subscribeObservers()
        
        childFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                updateToolbar(
                    when (f::class.java) {
                        LocationsAddFragment::class.java -> AuthMethodActivity.Page.ADD
                        LocationsSettingsFragment::class.java -> AuthMethodActivity.Page.SETTINGS
                        else -> throw IllegalStateException("Invalid fragment $f")
                    }
                )
            }
    
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                if (f::class.java == LocationsAddFragment::class.java
                    && !UiUtils.wasPermissionGranted(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    || !UiUtils.areLocationServicesTurnedOn(requireActivity())
                ) requireActivity().finish()
            }
        }, false)
    }
    
    private fun setupToolbar() {
        with(binding.locationsToolbar.root) {
            setTitle(R.string.ioa_sec_cir_add_title)
            (requireActivity() as AppCompatActivity).setSupportActionBar(this)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            updateToolbar(startPage)
        }
    }
    
    private fun updateToolbar(page: AuthMethodActivity.Page) {
        when (page) {
            AuthMethodActivity.Page.ADD -> {
                binding.locationsToolbar.root.setTitle(R.string.ioa_sec_geo_title)
                binding.locationsToolbar.root.setNavigationButton(UiUtils.NavButton.CANCEL)
            }
            AuthMethodActivity.Page.SETTINGS -> {
                binding.locationsToolbar.root.setTitle(R.string.ioa_sec_geo_sett_title)
                binding.locationsToolbar.root.setNavigationButton(UiUtils.NavButton.BACK)
            }
        }
    }
    
    private fun subscribeObservers() {
        locationsAddViewModel.addLocationState.observe(viewLifecycleOwner) { addLocationState ->
            when (addLocationState) {
                LocationsAddViewModel.AddLocationState.AddedNewLocation ->
                    if (startPage == AuthMethodActivity.Page.ADD) UiUtils.finishAddingFactorActivity(
                        requireActivity(),
                        SecurityFragment.REQUEST_ADD_LOCATIONS
                    ) else {
                        locationsSettingsViewModel.addedNewLocation()
                    }
                is LocationsAddViewModel.AddLocationState.FailedToAddLocation -> Unit
            }
        }
        
        // only use the locationsSettingsViewModel if we started with SETTINGS
        if (startPage != AuthMethodActivity.Page.SETTINGS) return
        
        locationsSettingsViewModel.newLocationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                LocationsSettingsViewModel.NewLocationState.AddedNewLocation -> {
                    childFragmentManager.popBackStack()
                    locationsSettingsViewModel.fetchLocations()
                }
                LocationsSettingsViewModel.NewLocationState.AddingNewLocation -> {
                    childFragmentManager.commit {
                        replace(
                            binding.fragmentMapContainer.id,
                            LocationsAddFragment::class.java,
                            null
                        )
                        addToBackStack(LocationsAddFragment::class.java.simpleName)
                    }
                }
            }
        }
        
        locationsSettingsViewModel.removeSingleLocationState.observe(viewLifecycleOwner) { removeSingleLocationState ->
            if (removeSingleLocationState is LocationsSettingsViewModel.RemoveSingleLocationState.Failed) requireActivity().finish()
        }
        
        locationsSettingsViewModel.removeAllLocationsState.observe(viewLifecycleOwner) { removeAllLocationsState ->
            when (removeAllLocationsState) {
                LocationsSettingsViewModel.RemoveAllLocationsState.PendingRemovalForAllLocations,
                is LocationsSettingsViewModel.RemoveAllLocationsState.Failed -> requireActivity().finish()
                else -> Unit
            }
        }
        
        locationsSettingsViewModel.getStoredLocationsState.observe(viewLifecycleOwner) { getStoredLocationsState ->
            when (getStoredLocationsState) {
                is LocationsSettingsViewModel.GetStoredLocationsState.Failed -> requireActivity().finish()
                is LocationsSettingsViewModel.GetStoredLocationsState.GotStoredLocations -> {
                    if (getStoredLocationsState.locations.isEmpty()) requireActivity().finish()
                }
                else -> Unit
            }
        }
    }
}