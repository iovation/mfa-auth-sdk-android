/*
 *  Copyright (c) 2017. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations.location_tracker

import android.Manifest
import android.content.Context
import android.os.Build
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils

abstract class PermissionsBaseLocationTracker : BaseLocationTracker() {
    private var mContext: Context? = null
    private var mSupportsRuntimePermissions = false
    protected fun setContextForPermissionsCheck(context: Context?) {
        mContext = context
        mSupportsRuntimePermissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
    
    override val checkCode: Int
        get() {
            if (mSupportsRuntimePermissions) {
                val fineLocationPermissionGranted =
                    UiUtils.wasPermissionGranted(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                if (!fineLocationPermissionGranted) {
                    return LocationTracker.Companion.CHECK_ERROR_PERMISSIONS_DENIED
                }
            }
            return LocationTracker.Companion.CHECK_OK
        }
}