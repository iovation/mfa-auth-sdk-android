/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ScaleGestureDetector
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.launchkey.android.authenticator.sdk.core.auth_method_management.exception.locations.LocationWithSameNameExistsException
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentLocationsAddBinding
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map.GoogleMapsFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map.MapViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map.OpenStreetMapsFragment
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.HelpDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.SetNameDialogFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.CoreExceptionToMessageConverter
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.makeVisible
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class LocationsAddFragment : BaseAppCompatFragment(R.layout.fragment_locations_add) {
    private var setNameDialogFragment: SetNameDialogFragment? = null
    private var isGeoFenceSet = false
    private val locationsAddViewModel: LocationsAddViewModel by viewModels({ requireParentFragment() })
    private val mapViewModel: MapViewModel by viewModels()
    private val binding by viewBinding(FragmentLocationsAddBinding::bind)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        UiUtils.applyThemeToMenu(inflater, menu)
        inflater.inflate(R.menu.save, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                if (isGeoFenceSet) promptForName()
                else LocationsWaitDialog.show(requireContext(), childFragmentManager)
                true
            }
            R.id.action_help -> {
                HelpDialogFragment.show(
                    childFragmentManager,
                    requireContext(),
                    getString(R.string.ioa_sec_geo_help_title),
                    getString(R.string.ioa_sec_geo_help_message)
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val locationsEnabled = UiUtils.wasPermissionGranted(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) || !UiUtils.areLocationServicesTurnedOn(requireContext())
        
        if (!locationsEnabled) return
        
        setupOverlayScaling()
        subscribeObservers()
        
        if (savedInstanceState == null) {
            when {
                isOpenStreetMapsSupported() -> {
                    childFragmentManager.commit {
                        replace(
                            binding.fragmentMapContainer.id,
                            OpenStreetMapsFragment::class.java,
                            null
                        )
                    }
                }
                isGoogleMapsSupported() -> {
                    childFragmentManager.commit {
                        replace(
                            binding.fragmentMapContainer.id,
                            GoogleMapsFragment::class.java,
                            null
                        )
                    }
                }
                else -> {
                    binding.noMapProviderTextview.makeVisible()
                }
            }
        }
    }
    
    private fun isOpenStreetMapsSupported(): Boolean {
        return try {
            Class.forName(
                "org.osmdroid.views.MapView",
                false,
                Thread.currentThread().contextClassLoader
            )
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
    
    private fun isGoogleMapsSupported(): Boolean {
        try {
            Class.forName(
                "com.google.android.gms.common.GoogleApiAvailability",
                false,
                Thread.currentThread().contextClassLoader
            )
            val errorCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context)
            if (errorCode != ConnectionResult.SUCCESS) {
                return false
            }
            
            Class.forName(
                "com.google.android.gms.maps.GoogleMap",
                false,
                Thread.currentThread().contextClassLoader
            )
            Class.forName(
                "com.google.android.gms.maps.SupportMapFragment",
                false,
                Thread.currentThread().contextClassLoader
            )
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }
    }
    
    @SuppressLint("ClickableViewAccessibility") // Due to ScalingFrameLayout's use of a OnTouchListener object
    private fun setupOverlayScaling() {
        binding.root.setOnTouchListener { _, _ -> true }
        binding.root.setScaleListener(object :
            ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                mapViewModel.scaleBegin()
                return true
            }
    
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                mapViewModel.scale(detector.scaleFactor)
                return true
            }
    
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                mapViewModel.scaleEnd()
            }
        })
    }
    
    private fun subscribeObservers() {
        mapViewModel.geoFenceState.observe(viewLifecycleOwner) {
            isGeoFenceSet = it is MapViewModel.GeoFenceState.GeoFenceSet
        }
        
        locationsAddViewModel.addLocationState.observe(viewLifecycleOwner) { locationsState ->
            when (locationsState) {
                LocationsAddViewModel.AddLocationState.AddedNewLocation -> setNameDialogFragment?.dismiss()
                is LocationsAddViewModel.AddLocationState.FailedToAddLocation -> {
                    setNameDialogFragment?.setErrorMessage(
                        when (locationsState.failure) {
                            is LocationsAddViewModel.LocationNameTooShortException -> resources.getQuantityString(
                                R.plurals.ioa_sec_geo_add_error_invalidname_message_format,
                                Constants.MINIMUM_INPUT_LENGTH,
                                Constants.MINIMUM_INPUT_LENGTH
                            )
                            is LocationWithSameNameExistsException -> getString(R.string.ioa_sec_geo_add_error_usedname_message)
                            else -> CoreExceptionToMessageConverter.convert(
                                locationsState.failure,
                                requireContext()
                            )
                        }
                    )
                }
            }
        }
    }
    
    private fun promptForName() {
        setNameDialogFragment = SetNameDialogFragment.show(
            requireContext(),
            childFragmentManager,
            R.string.ioa_sec_geo_add_dialog_setname_title,
            R.string.ioa_sec_geo_add_dialog_setname_hint,
            R.string.ioa_generic_done,
            null,
            { _, name ->
                val (lat, lng, rad) = (mapViewModel.geoFenceState.value as MapViewModel.GeoFenceState.GeoFenceSet)
                locationsAddViewModel.addLocation(name!!, lat, lng, rad)
            },
            null,
            false
        )
    }
}