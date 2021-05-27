package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentOpenStreetMapsBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polygon
import java.io.File
import kotlin.math.min

class OpenStreetMapsFragment : Fragment(R.layout.fragment_open_street_maps) {
    private val binding by viewBinding(FragmentOpenStreetMapsBinding::bind)
    private val mapViewModel: MapViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val userLocationViewModel: UserLocationViewModel by viewModels { requireParentFragment().defaultViewModelProviderFactory }
    private var geoFenceOverlay: Polygon? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            loadOpenStreetMapsConfiguration()
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
        subscribeObservers()
    }
    
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
    
    private fun loadOpenStreetMapsConfiguration() {
        val cacheSizeBytes = (1000 * 1000).toLong()
        val context = requireContext()
        val cacheDir = File(context.cacheDir, "osm-cache")
        with(Configuration.getInstance()) {
            userAgentValue = context.packageName
            osmdroidBasePath = cacheDir
            osmdroidTileCache = cacheDir
            tileFileSystemCacheMaxBytes = cacheSizeBytes
            load(
                context,
                context.getSharedPreferences(
                    context.packageName + "_preferences",
                    Context.MODE_PRIVATE
                )
            )
        }
    }
    
    private fun setupMap() {
        with(binding.mapView) {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            
            val eventHandler: MapEventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    mapViewModel.setGeoFence(
                        p.latitude,
                        p.longitude,
                        MapViewModel.RADIUS_METERS_DEFAULT
                    )
                    
                    return true
                }
                
                override fun longPressHelper(p: GeoPoint): Boolean = false
            }
            
            overlayManager.add(MapEventsOverlay(eventHandler))
        }
    }
    
    private fun subscribeObservers() {
        userLocationViewModel.locationState.observe(viewLifecycleOwner) { locationState ->
            when (locationState) {
                is UserLocationViewModel.LocationState.Retrieved -> {
                    val center = GeoPoint(locationState.latitude, locationState.longitude)
                    val circlePoints = Polygon.pointsAsCircle(center, USER_LOCATION_OVERLAY_RADIUS)
                    val userLocationOverlay = Polygon().apply {
                        points = circlePoints
                        fillPaint.flags = Paint.ANTI_ALIAS_FLAG
                        fillPaint.style = Paint.Style.FILL
                        fillPaint.color = mapViewModel.strokeColor
                        outlinePaint.color = mapViewModel.strokeColor
                        outlinePaint.strokeWidth = MapViewModel.STROKE_WIDTH_DEFAULT
                    }
        
                    binding.mapView.overlayManager.add(userLocationOverlay)
        
                    with(binding.mapView.controller) {
                        if (locationState.zoom >= 0) {
                            setZoom(min(locationState.zoom, ZOOM_MAX).toDouble())
                        }
            
                        setCenter(center)
                    }
        
                    binding.mapView.invalidate()
                }
                UserLocationViewModel.LocationState.Retrieving -> Unit
            }
        }
        
        mapViewModel.geoFenceState.observe(viewLifecycleOwner) { geoFenceState ->
            when (geoFenceState) {
                MapViewModel.GeoFenceState.NotSet -> Unit
                is MapViewModel.GeoFenceState.GeoFenceSet -> updateGeoFenceOverlay(
                    geoFenceState.latitude,
                    geoFenceState.longitude,
                    geoFenceState.radius,
                    mapViewModel.strokeColor,
                    mapViewModel.fillColor
                )
            }
        }
        
        mapViewModel.scaleState.observe(viewLifecycleOwner) { scaleState ->
            geoFenceOverlay?.fillPaint?.color = scaleState.fillColor
            binding.mapView.invalidate()
        }
    }
    
    private fun updateGeoFenceOverlay(
        latitude: Double,
        longitude: Double,
        radius: Double,
        strokeColor: Int,
        fillColor: Int
    ) {
        if (geoFenceOverlay == null) {
            geoFenceOverlay = Polygon().apply {
                fillPaint.color = fillColor
                outlinePaint.color = strokeColor
            }
            
            binding.mapView.overlayManager.add(geoFenceOverlay)
        }
        
        val circleOverlayCenter = GeoPoint(latitude, longitude)
        val circlePoints = Polygon.pointsAsCircle(circleOverlayCenter, radius)
        val radiusInPixels = binding.mapView.projection.metersToEquatorPixels(radius.toFloat())
        
        geoFenceOverlay?.let {
            it.outlinePaint.strokeWidth = (radiusInPixels * STROKE_WIDTH_GEO_FENCE_SCALE).coerceIn(
                STROKE_WIDTH_MIN,
                STROKE_WIDTH_MAX
            )
            it.points = circlePoints
        }
        
        binding.mapView.invalidate()
    }
    
    companion object {
        private const val STROKE_WIDTH_MIN = 2f
        private const val STROKE_WIDTH_MAX = 10f
        private const val STROKE_WIDTH_GEO_FENCE_SCALE = 0.05f
        private const val ZOOM_MAX = 19f
        private const val USER_LOCATION_OVERLAY_RADIUS = 5.0
    }
}