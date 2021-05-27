/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.graphics.drawable.Drawable

class AuthMethodsBusyIcons internal constructor(iconGeofencing: Drawable?, iconWearable: Drawable?, iconFingerprintScan: Drawable?) {
    val iconGeofencing: Drawable? = iconGeofencing
        get() {
            return if (field == null) null else field.constantState!!.newDrawable().mutate()
        }
    val iconWearable: Drawable? = iconWearable
        get() {
            return if (field == null) null else field.constantState!!.newDrawable().mutate()
        }
    val iconFingerprintScan: Drawable? = iconFingerprintScan
        get() {
            return if (field == null) null else field.constantState!!.newDrawable().mutate()
        }
    var iconGeofencingRes = 0
        private set
    var iconWearableRes = 0
        private set
    var iconFingerprintScanRes = 0
        private set
    var resProvided = false
        private set

    internal constructor(drawableGeofencingRes: Int, drawableWearableRes: Int, drawableFingerprintScanRes: Int) : this(null, null, null) {
        iconGeofencingRes = drawableGeofencingRes
        iconWearableRes = drawableWearableRes
        iconFingerprintScanRes = drawableFingerprintScanRes
        resProvided = true
    }
}