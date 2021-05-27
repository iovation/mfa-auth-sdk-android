package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class GoogleMapsFragment : SupportMapFragment() {
    private lateinit var googleMap: GoogleMap
    private val mapViewModel: MapViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val userLocationViewModel: UserLocationViewModel by viewModels { requireParentFragment().defaultViewModelProviderFactory }
    private var marker: Marker? = null
    private var geoFenceOverlay: Circle? = null
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMapAsync {
            setupMap(it)
            subscribeObservers()
        }
    }
    
    private fun setupMap(map: GoogleMap) {
        googleMap = map.apply {
            with(uiSettings) {
                isZoomControlsEnabled = true
                isZoomGesturesEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isMyLocationButtonEnabled = true
                isMyLocationEnabled = true
            }
            
            setOnMapClickListener { latLng ->
                mapViewModel.setGeoFence(
                    latLng.latitude,
                    latLng.longitude,
                    MapViewModel.RADIUS_METERS_DEFAULT
                )
            }
            
            geoFenceOverlay = null
            
            setOnMarkerClickListener { true } // Consumes event; avoids default behavior
        }
    }
    
    private fun subscribeObservers() {
        userLocationViewModel.locationState.observe(viewLifecycleOwner) { locationState ->
            when (locationState) {
                is UserLocationViewModel.LocationState.Retrieved -> {
                    googleMap.moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(
                                LatLng(locationState.latitude, locationState.longitude),
                                locationState.zoom
                            )
                        )
                    )
                }
                UserLocationViewModel.LocationState.Retrieving -> Unit
            }
        }
        
        mapViewModel.geoFenceState.observe(viewLifecycleOwner) { geoFenceState ->
            when (geoFenceState) {
                MapViewModel.GeoFenceState.NotSet -> Unit
                is MapViewModel.GeoFenceState.GeoFenceSet -> updateGeoFenceOverlay(
                    latitude = geoFenceState.latitude,
                    longitude = geoFenceState.longitude,
                    radius = geoFenceState.radius,
                    strokeColor = mapViewModel.strokeColor,
                    fillColor = mapViewModel.fillColor
                )
            }
        }
        
        mapViewModel.scaleState.observe(viewLifecycleOwner) { scaleState ->
            when (scaleState) {
                is MapViewModel.ScaleState.BeginScale -> geoFenceOverlay?.fillColor =
                    scaleState.fillColor
                is MapViewModel.ScaleState.EndScale -> geoFenceOverlay?.fillColor =
                    scaleState.fillColor
            }
        }
    }
    
    private fun updateGeoFenceOverlay(
        latitude: Double,
        longitude: Double,
        radius: Double,
        fillColor: Int,
        strokeColor: Int
    ) {
        val latLng = LatLng(latitude, longitude)
        if (marker == null) {
            marker = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .draggable(false)
            )
        }
        marker?.position = latLng
        
        if (geoFenceOverlay == null) {
            geoFenceOverlay = googleMap.addCircle(
                CircleOptions()
                    .center(latLng)
                    .radius(radius)
                    .strokeColor(strokeColor)
                    .strokeWidth(MapViewModel.STROKE_WIDTH_DEFAULT)
                    .fillColor(fillColor)
            )
        }
        
        geoFenceOverlay?.center = latLng
        geoFenceOverlay?.radius = radius
    }
}