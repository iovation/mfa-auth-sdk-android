package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager.Location
import com.launchkey.android.authenticator.sdk.ui.internal.common.Constants
import com.launchkey.android.authenticator.sdk.ui.internal.util.disposeWhenCancelled
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationsAddViewModel(
    private val locationsManager: LocationsManager,
    private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _addLocationState =
        savedStateHandle.getLiveData<AddLocationState>(HANDLE_KEY_LOCATIONS_STATE)
    val addLocationState: LiveData<AddLocationState>
        get() = _addLocationState
    
    fun addLocation(locationName: String, latitude: Double, longitude: Double, radius: Double) =
        viewModelScope.launch(defaultDispatcher) {
            try {
                if (locationName.trim().length < Constants.MINIMUM_INPUT_LENGTH)
                    throw LocationNameTooShortException
                
                locationsManager.addLocation(Location(
                    locationName,
                    latitude,
                    longitude,
                    radius
                ), object : LocationsManager.AddLocationCallback {
                    override fun onAddSuccess() {
                        _addLocationState.postValue(AddLocationState.AddedNewLocation)
                    }
    
                    override fun onAddFailure(e: Exception) {
                        _addLocationState.postValue(AddLocationState.FailedToAddLocation(e))
                    }
    
                })
            } catch (e: Exception) {
                _addLocationState.postValue(AddLocationState.FailedToAddLocation(e))
            }
        }
    
    private suspend fun getStoredLocations() =
        suspendCancellableCoroutine<List<LocationsManager.StoredLocation>> { continuation ->
            locationsManager.getStoredLocations(object :
                LocationsManager.GetStoredLocationsCallback {
                override fun onGetSuccess(locations: List<LocationsManager.StoredLocation>) {
                    continuation.resume(locations)
                }
    
                override fun onGetFailure(e: Exception) {
                    continuation.resumeWithException(e)
                }
            }).disposeWhenCancelled(continuation)
        }
    
    internal object LocationNameTooShortException : RuntimeException()
    
    sealed class AddLocationState {
        object AddedNewLocation : AddLocationState()
        
        @Parcelize
        data class FailedToAddLocation(val failure: Exception) : AddLocationState(), Parcelable
    }
    
    companion object {
        private const val HANDLE_KEY_LOCATIONS_STATE = "locations state"
    }
}