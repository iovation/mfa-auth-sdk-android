/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.location_tracker

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class LocationTrackerFactory(context: Context) {
    val locationTracker: LocationTracker = try {
        Class.forName(
            "com.google.android.gms.common.api.GoogleApiClient",
            false,
            context.classLoader
        )
        Class.forName(
            "com.google.android.gms.location.LocationServices",
            false,
            context.classLoader
        )
        Class.forName("com.google.android.gms.common.api.Api", false, context.classLoader)
        Class.forName(
            "com.google.android.gms.common.GoogleApiAvailability",
            false,
            context.classLoader
        )
        val result =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        
        if (result == ConnectionResult.SUCCESS) GoogleLocationTracker.getInstance(context)
        else DeviceLocationTracker.getInstance(context) // Fall back to device-based sensors
    } catch (ignored: ClassNotFoundException) { // Fall back to device-based sensors
        DeviceLocationTracker.getInstance(context)
    }
}