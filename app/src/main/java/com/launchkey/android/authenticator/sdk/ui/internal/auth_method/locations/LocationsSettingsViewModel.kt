package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.ui.internal.util.TimingCounter
import com.launchkey.android.authenticator.sdk.ui.internal.util.disposeWhenCancelled
import com.launchkey.android.authenticator.sdk.ui.internal.viewmodel.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationsSettingsViewModel(
    private val locationsManager: LocationsManager,
    private val nowProvider: TimingCounter.NowProvider,
    private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _newLocationState = SingleLiveEvent<NewLocationState>()
    val newLocationState: LiveData<NewLocationState>
        get() = _newLocationState
    
    private val _getStoredLocationsState = MutableLiveData<GetStoredLocationsState>()
    val getStoredLocationsState: LiveData<GetStoredLocationsState>
        get() = _getStoredLocationsState
    
    private val _removeSingleLocationState = SingleLiveEvent<RemoveSingleLocationState>()
    val removeSingleLocationState: LiveData<RemoveSingleLocationState>
        get() = _removeSingleLocationState
    
    private val _removeAllLocationsState = SingleLiveEvent<RemoveAllLocationsState>()
    val removeAllLocationsState: LiveData<RemoveAllLocationsState>
        get() = _removeAllLocationsState
    
    private lateinit var locationToRemove: LocationsManager.StoredLocation
    private var getStoredLocationsJob: Job? = null
    
    init {
        fetchLocations()
    }
    
    private suspend fun getAllLocations() =
        suspendCancellableCoroutine<List<LocationsManager.StoredLocation>> { continuation ->
            locationsManager.getStoredLocations(object :
                LocationsManager.GetStoredLocationsCallback {
                override fun onGetSuccess(locations: MutableList<LocationsManager.StoredLocation>) {
                    continuation.resume(locations)
                }
    
                override fun onGetFailure(e: Exception) {
                    continuation.resumeWithException(e)
                }
            }).disposeWhenCancelled(continuation)
        }
    
    fun fetchLocations() {
        getStoredLocationsJob?.let {
            if (it.isActive) {
                return
            }
        }
        
        getStoredLocationsJob = viewModelScope.launch(defaultDispatcher) {
            _getStoredLocationsState.postValue(GetStoredLocationsState.GettingStoredLocations)
            try {
                val locations = getAllLocations().map {
                    LocationItem(it, nowProvider.now)
                }
                _getStoredLocationsState.postValue(
                    GetStoredLocationsState.GotStoredLocations(locations)
                )
            } catch (e: Exception) {
                _getStoredLocationsState.postValue(GetStoredLocationsState.Failed(e))
            }
        }
    }
    
    fun requestNewLocation() {
        _newLocationState.postValue(NewLocationState.AddingNewLocation)
    }
    
    fun addedNewLocation() {
        if (_newLocationState.value !is NewLocationState.AddedNewLocation)
            _newLocationState.postValue(NewLocationState.AddedNewLocation)
    }
    
    private fun removeLocation(location: LocationsManager.StoredLocation) {
        viewModelScope.launch(defaultDispatcher) {
            try {
                if (location.isPendingRemoval) {
                    cancelRemoveLocation(location)
                    _removeSingleLocationState.postValue(
                        RemoveSingleLocationState.CancelledLocationRemoval(location)
                    )
                } else {
                    removeSingleLocation(location)
                    _removeSingleLocationState.postValue(
                        RemoveSingleLocationState.PendingLocationRemoval(
                            location
                        )
                    )
                }
                fetchLocations()
            } catch (e: Exception) {
                _removeSingleLocationState.postValue(RemoveSingleLocationState.Failed(e))
            }
        }
    }
    
    fun removeSelectedLocation() {
        removeLocation(locationToRemove)
    }
    
    fun setLocationToRemove(location: LocationsManager.StoredLocation) {
        locationToRemove = location
        _removeSingleLocationState.postValue(
            RemoveSingleLocationState.RemovingLocation(location)
        )
    }
    
    private suspend fun removeSingleLocation(location: LocationsManager.StoredLocation) =
        suspendCancellableCoroutine<Exception?> { continuation ->
            locationsManager.removeLocation(location.name,
                object : LocationsManager.RemoveLocationCallback {
                    override fun onRemoveSuccess() {
                        continuation.resume(null)
                    }
        
                    override fun onRemoveFailure(e: java.lang.Exception) {
                        continuation.resumeWithException(e)
                    }
                }).disposeWhenCancelled(continuation)
        }
    
    private suspend fun cancelRemoveLocation(location: LocationsManager.StoredLocation) =
        suspendCancellableCoroutine<Exception?> {
            locationsManager.cancelRemoveLocation(location.name,
                object : LocationsManager.CancelRemoveLocationCallback {
                    override fun onCancelRemoveLocationSuccess() {
                        it.resume(null)
                    }
        
                    override fun onCancelRemoveLocationFailure(e: java.lang.Exception) {
                        it.resumeWithException(e)
                    }
                })
        }
    
    fun requestRemoveAllLocations() {
        _removeAllLocationsState.postValue(RemoveAllLocationsState.RemovingAllLocations)
    }
    
    fun removeAllLocations() {
        viewModelScope.launch(defaultDispatcher) {
            val storedLocations: List<LocationsManager.StoredLocation>
            try {
                storedLocations = getAllLocations()
            } catch (exception: Exception) {
                _removeAllLocationsState.postValue(RemoveAllLocationsState.Failed(exception))
                return@launch
            }
            launch {
                storedLocations.forEach { location ->
                    try {
                        if (!location.isPendingRemoval) {
                            removeSingleLocation(location)
                        }
                    } catch (e: Exception) {
                        _removeAllLocationsState.postValue(RemoveAllLocationsState.Failed(e))
                        cancel()
                    }
                }
            }.invokeOnCompletion {
                if (it == null) {
                    _removeAllLocationsState.postValue(RemoveAllLocationsState.PendingRemovalForAllLocations)
                }
            }
        }
    }
    
    sealed class GetStoredLocationsState {
        object GettingStoredLocations : GetStoredLocationsState()
        data class GotStoredLocations(val locations: List<LocationItem>) :
            GetStoredLocationsState()
        
        data class Failed(val failure: Exception) : GetStoredLocationsState()
    }
    
    sealed class RemoveAllLocationsState {
        object RemovingAllLocations : RemoveAllLocationsState()
        object PendingRemovalForAllLocations : RemoveAllLocationsState()
        data class Failed(val failure: Exception) : RemoveAllLocationsState()
    }
    
    sealed class RemoveSingleLocationState {
        data class RemovingLocation(val location: LocationsManager.StoredLocation) :
            RemoveSingleLocationState()
        
        data class PendingLocationRemoval(val location: LocationsManager.StoredLocation) :
            RemoveSingleLocationState()
        
        data class CancelledLocationRemoval(val location: LocationsManager.StoredLocation) :
            RemoveSingleLocationState()
        
        data class Failed(val failure: Exception) : RemoveSingleLocationState()
    }
    
    sealed class NewLocationState {
        object AddingNewLocation : NewLocationState()
        object AddedNewLocation : NewLocationState()
    }
    
}