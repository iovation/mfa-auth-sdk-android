/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.location_tracker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class GoogleLocationTracker private constructor(appContext: Context) :
    PermissionsBaseLocationTracker() {
    private lateinit var mLocationManager: LocationManager
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mConnCallback: GoogleApiClient.ConnectionCallbacks
    private lateinit var mFailCallback: GoogleApiClient.OnConnectionFailedListener
    private lateinit var mOnLocation: LocationListener
    private var mIsRequestingUpdates = false
    @Synchronized
    private fun init(appContext: Context) {
        mOnLocation = LocationListener { location -> notifyExternalListeners(location) }
        mConnCallback = object : GoogleApiClient.ConnectionCallbacks {
            override fun onConnected(bundle: Bundle?) {
                val intervalMillis: Long = 200
                val fastestIntervalMillis: Long = 200
                val request = LocationRequest()
                    .setInterval(intervalMillis)
                    .setFastestInterval(fastestIntervalMillis)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, request, mOnLocation)
                mIsRequestingUpdates = true
            }
            
            override fun onConnectionSuspended(i: Int) {}
        }
        mFailCallback = GoogleApiClient.OnConnectionFailedListener { }
        mGoogleApiClient = GoogleApiClient.Builder(appContext)
            .addConnectionCallbacks(mConnCallback)
            .addOnConnectionFailedListener(mFailCallback)
            .addApi(LocationServices.API)
            .build()
        mLocationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    
    override fun start() {
        if (!mGoogleApiClient!!.isConnecting && !mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.connect()
        }
    }
    
    override fun stop(): Location? {
        if (mIsRequestingUpdates) {
            mIsRequestingUpdates = false
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mOnLocation)
        }
        if (mGoogleApiClient!!.isConnected || mGoogleApiClient!!.isConnecting) {
            mGoogleApiClient!!.disconnect()
        }
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
            if (!mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                return LocationTracker.Companion.CHECK_ERROR_SOURCE_DISABLED_GPS
            }
            return if (!mLocationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationTracker.Companion.CHECK_ERROR_SOURCE_DISABLED_NETWORK
            } else LocationTracker.Companion.CHECK_OK
        }
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mInstance: GoogleLocationTracker? = null
        fun getInstance(context: Context): GoogleLocationTracker {
            if (mInstance == null) {
                mInstance = GoogleLocationTracker(context.applicationContext)
            }
            return mInstance!!
        }
    }
    
    init {
        init(appContext)
        setContextForPermissionsCheck(appContext)
    }
}