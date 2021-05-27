package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.map

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.location_tracker.LocationTracker
import kotlinx.android.parcel.Parcelize

class UserLocationViewModel(
    private val locationTracker: LocationTracker,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _locationState =
        savedStateHandle.getLiveData<LocationState>(HANDLE_KEY_CURRENT_LOCATION)
    val locationState: LiveData<LocationState>
        get() = _locationState
    
    private val locationUpdateListener = LocationTracker.LocationUpdateListener {
        savedStateHandle[HANDLE_KEY_CURRENT_LOCATION] = LocationState.Retrieved(
            it.latitude,
            it.longitude,
            DEFAULT_ZOOM
        )
        locationTracker.stop()
    }
    
    init {
        locationTracker.registerListener(locationUpdateListener)
        savedStateHandle[HANDLE_KEY_CURRENT_LOCATION] = LocationState.Retrieving
        locationTracker.start()
    }
    
    override fun onCleared() {
        super.onCleared()
        locationTracker.unregisterListener(locationUpdateListener)
    }
    
    sealed class LocationState {
        @Parcelize
        object Retrieving : LocationState(), Parcelable
        
        @Parcelize
        data class Retrieved(val latitude: Double, val longitude: Double, val zoom: Float) :
            LocationState(), Parcelable
    }
    
    companion object {
        private const val HANDLE_KEY_CURRENT_LOCATION = "location state"
        private const val DEFAULT_ZOOM = 20f
    }
}