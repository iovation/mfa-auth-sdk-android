/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.location_tracker

import android.location.Location
import java.util.*

abstract class BaseLocationTracker : LocationTracker {
    override var latestLocation: Location? = null
    private val mExternalListeners: MutableList<LocationTracker.LocationUpdateListener> =
        ArrayList()
    
    protected fun notifyExternalListeners(location: Location?) {
        if (location == null) {
            return
        }
        if (isBetterLocation(location, latestLocation)) {
            latestLocation = location
        } else {
            // New location not better than before, no need to update
            return
        }
        for (listener in mExternalListeners) {
            listener.onLocationUpdate(location)
        }
    }
    
    override fun registerListener(listener: LocationTracker.LocationUpdateListener?) {
        if (listener != null) {
            mExternalListeners.add(listener)
            
            // Notify latest location if available
            if (hasLatestLocation()) {
                listener.onLocationUpdate(latestLocation!!)
            }
        }
    }
    
    override fun unregisterListener(listener: LocationTracker.LocationUpdateListener?) {
        if (listener != null) {
            mExternalListeners.remove(listener)
        }
    }
    
    override fun hasLatestLocation(): Boolean {
        return latestLocation != null
    }
    
    override fun stop(): Location? {
        // Make sure subclass calls this method via super at last to return latest location
        return latestLocation
    }
    
    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     *
     * From: https://developer.android.com/guide/topics/location/strategies.html
     */
    protected fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }
        
        // Check whether the new location fix is newer or older
        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer = timeDelta > TWO_MINUTES
        val isSignificantlyOlder = timeDelta < -TWO_MINUTES
        val isNewer = timeDelta > 0
        
        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false
        }
        
        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200
        
        // Check if the old and new location are from the same provider
        val isFromSameProvider = isSameProvider(
            location.provider,
            currentBestLocation.provider
        )
        
        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true
        } else if (isNewer && !isLessAccurate) {
            return true
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true
        }
        return false
    }
    
    /** Checks whether two providers are the same  */
    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else provider1 == provider2
    }
    
    companion object {
        private const val TWO_MINUTES = 1000 * 60 * 2
    }
}