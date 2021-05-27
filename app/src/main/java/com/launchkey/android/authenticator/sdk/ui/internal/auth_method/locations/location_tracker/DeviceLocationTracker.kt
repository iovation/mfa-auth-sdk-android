/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.location_tracker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import java.util.*

class DeviceLocationTracker private constructor(appContext: Context) :
    PermissionsBaseLocationTracker() {
    private val mManager: LocationManager
    private val mListener: LocationListener
    private val mEnabledProviders: MutableList<String>
    override fun start() {
        
        // Trigger notification so quick location is set as latest known
        notifyExternalListeners(lastKnownLocation)
        requestLocationUpdates()
    }
    
    // Check is done by caller, should probably change that
    @get:SuppressLint("MissingPermission")
    private val lastKnownLocation: Location?
        // Check is done by caller, should probably change that
        private get() {
            var quickLocation = latestLocation
            if (quickLocation != null) {
                return quickLocation
            }
            if (mEnabledProviders.isEmpty()) {
                return null
            }
            if (mEnabledProviders.contains(LocationManager.GPS_PROVIDER)) {
                quickLocation = mManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (quickLocation == null && mEnabledProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                quickLocation = mManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            return quickLocation
        }
    
    @SuppressLint("MissingPermission") // Check is done by caller, should probably change that
    private fun requestLocationUpdates() {
        val minTimeMillis = (2 * 1000).toLong()
        var provider: String
        for (p in mEnabledProviders.indices) {
            provider = mEnabledProviders[p]
            mManager.requestLocationUpdates(provider, minTimeMillis, 0f, mListener)
        }
    }
    
    override fun stop(): Location? {
        mManager.removeUpdates(mListener)
        
        // This will return latest location
        //  obtained via notifications
        return super.stop()
    }
    
    // If check by super class is not OK, then bubble error up
    override val checkCode: Int
        get() {
            
            // If check by super class is not OK, then bubble error up
            val parentCheck = super.checkCode
            if (parentCheck != LocationTracker.Companion.CHECK_OK) {
                return parentCheck
            }
            if (!mManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return LocationTracker.Companion.CHECK_ERROR_SOURCE_DISABLED_GPS
            }
            return if (!mManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationTracker.Companion.CHECK_ERROR_SOURCE_DISABLED_NETWORK
            } else LocationTracker.Companion.CHECK_OK
        }
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var mInstance: DeviceLocationTracker? = null
        fun getInstance(context: Context): DeviceLocationTracker {
            if (mInstance == null) {
                synchronized(DeviceLocationTracker::class.java) {
                    if (mInstance == null) {
                        mInstance = DeviceLocationTracker(context.applicationContext)
                    }
                }
            }
            return mInstance!!
        }
    }
    
    init {
        mManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                notifyExternalListeners(location)
            }
            
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        setContextForPermissionsCheck(appContext)
        mEnabledProviders = ArrayList()
        mEnabledProviders.addAll(mManager.getProviders(true))
    }
}