/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.location_tracker

import android.location.Location

interface LocationTracker {
    /**
     * This call will have the tracker start
     * making use of the sensors/services to
     * get the location of the current device.
     *
     * Location updates are evaluated before
     * being propagated via [LocationUpdateListener]
     * registered and unregistered via
     * [.registerListener]
     * and
     * [.unregisterListener]
     */
    fun start()
    
    /**
     * Method to register listener for location updates.
     */
    fun registerListener(listener: LocationUpdateListener?)
    
    /**
     * Method to unregister a listener already registered for
     * location updates.
     */
    fun unregisterListener(listener: LocationUpdateListener?)
    
    /**
     * @return true if there's a location already captured
     * by the tracker.
     * @see {@link .getLatestLocation
     */
    fun hasLatestLocation(): Boolean
    
    /**
     * @return Latest location captured by the tracker.
     * Useful for one-time checks.
     */
    val latestLocation: Location?
    
    /**
     * This call will stop the tracker from making
     * use of the sensors/services to get the
     * location of the current device.
     * @return The last known location.
     *
     * @see {@link .hasLatestLocation
     * @see {@link .getLatestLocation
     */
    fun stop(): Location?
    
    /**
     * Check for the right conditions for
     * the tracker to work. If something
     * other than [.CHECK_OK] is returned,
     * the tracker won't be able to
     * get a fix on a location.
     * @return Resulting code. Compare
     * against CHECK_* flags.
     */
    val checkCode: Int
    
    /**
     * Interface for listeners of new location
     * updates.
     */
    fun interface LocationUpdateListener {
        /**
         * Callback method that should never
         * return a null Location object.
         * @param location Latest location
         * object.
         */
        fun onLocationUpdate(location: Location)
    }
    
    companion object {
        const val CHECK_OK = 1
        const val CHECK_ERROR_SOURCE_DISABLED_GPS = 2
        const val CHECK_ERROR_SOURCE_DISABLED_NETWORK = 4
        const val CHECK_ERROR_PERMISSIONS_DENIED = 8
    }
}