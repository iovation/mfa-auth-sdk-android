package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map

import android.graphics.Color
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.android.parcel.Parcelize
import kotlin.math.max
import kotlin.math.min

class MapViewModel(
    val strokeColor: Int,
    val fillColor: Int,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _geoFenceState =
        savedStateHandle.getLiveData<GeoFenceState>(KEY_GEO_FENCE_STATE)
    val geoFenceState: LiveData<GeoFenceState>
        get() = _geoFenceState
    
    private var _scaleState =
        savedStateHandle.getLiveData<ScaleState>(KEY_SCALE_STATE)
    val scaleState: LiveData<ScaleState>
        get() = _scaleState
    
    private fun isGeoFenceSet() = geoFenceState.value is GeoFenceState.GeoFenceSet
    
    fun setGeoFence(latitude: Double, longitude: Double, radius: Double) {
        _geoFenceState.postValue(
            GeoFenceState.GeoFenceSet(
                latitude = latitude,
                longitude = longitude,
                radius = radius
            )
        )
    }
    
    fun scaleBegin() {
        _scaleState.postValue(ScaleState.BeginScale(Color.TRANSPARENT))
    }
    
    fun scale(factor: Float) {
        if (!isGeoFenceSet()) return
        val currentGeoFence = geoFenceState.value as GeoFenceState.GeoFenceSet
        val scaleFactor = max(SCALE_FACTOR_MIN, min(SCALE_FACTOR_MAX, factor))
        val scaledRadius = (currentGeoFence.radius * scaleFactor).coerceIn(RADIUS_MIN, RADIUS_MAX)
        setGeoFence(currentGeoFence.latitude, currentGeoFence.longitude, scaledRadius)
    }
    
    fun scaleEnd() {
        _scaleState.postValue(ScaleState.EndScale(fillColor))
    }
    
    sealed class GeoFenceState {
        object NotSet : GeoFenceState()
        
        @Parcelize
        data class GeoFenceSet(
            val latitude: Double,
            val longitude: Double,
            val radius: Double,
        ) : GeoFenceState(), Parcelable
    }
    
    sealed class ScaleState(val fillColor: Int) : Parcelable {
        @Parcelize
        data class BeginScale(val newFillColor: Int) : ScaleState(newFillColor)
        
        @Parcelize
        data class EndScale(val oldFillColor: Int) : ScaleState(oldFillColor)
    }
    
    companion object {
        private const val KEY_GEO_FENCE_STATE = "geofence state"
        private const val KEY_SCALE_STATE = "scale state"
        private const val SCALE_FACTOR_MIN = 0.1f
        private const val SCALE_FACTOR_MAX = 2f
        private const val RADIUS_MIN = 10.0
        private const val RADIUS_MAX = Int.MAX_VALUE.toDouble()
        const val GEO_FENCE_FILL_ALPHA = 51
        const val RADIUS_METERS_DEFAULT = 50.0
        const val STROKE_WIDTH_DEFAULT = 3f
    }
}