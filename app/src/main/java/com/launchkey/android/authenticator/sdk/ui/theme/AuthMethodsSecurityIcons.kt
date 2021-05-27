/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes

class AuthMethodsSecurityIcons @JvmOverloads internal constructor(iconVisibility: Int, iconPinCode: Drawable? = null, iconCircleCode: Drawable? = null, iconGeofencing: Drawable? = null, iconWearable: Drawable? = null, iconFingerprintScan: Drawable? = null) {
    val iconVisibility: Int = when (iconVisibility) {
        View.VISIBLE, View.INVISIBLE, View.GONE -> iconVisibility
        else -> View.VISIBLE
    }
    val iconPinCode: Drawable? = iconPinCode
        get() {
            return if (field == null) null else field.constantState!!.newDrawable().mutate()
        }
    val iconCircleCode: Drawable? = iconCircleCode
        get() {
            return if (field == null) null else field.constantState!!.newDrawable().mutate()
        }
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

    @DrawableRes
    var iconPinCodeRes: Int? = null
        private set
    @DrawableRes
    var iconCircleCodeRes: Int? = null
        private set
    @DrawableRes
    var iconGeofencingRes: Int? = null
        private set
    @DrawableRes
    var iconWearableRes: Int? = null
        private set
    @DrawableRes
    var iconFingerprintScanRes: Int? = null
        private set
    var colorIcon: Int? = null
        private set
    var resProvided = false
        private set

    internal constructor(iconVisibility: Int, iconPinCodeRes: Int, iconCircleCodeRes: Int, iconGeofencingRes: Int, iconWearableRes: Int, iconFingerprintScanRes: Int, iconColor: Int) : this(iconVisibility, iconPinCodeRes, iconCircleCodeRes, iconGeofencingRes, iconWearableRes, iconFingerprintScanRes) {
        colorIcon = iconColor
    }

    internal constructor(iconVisibility: Int, iconPinCodeRes: Int, iconCircleCodeRes: Int, iconGeofencingRes: Int, iconWearableRes: Int, iconFingerprintScanRes: Int) : this(iconVisibility, null, null, null, null, null) {
        this.iconPinCodeRes = iconPinCodeRes
        this.iconCircleCodeRes = iconCircleCodeRes
        this.iconGeofencingRes = iconGeofencingRes
        this.iconWearableRes = iconWearableRes
        this.iconFingerprintScanRes = iconFingerprintScanRes
        resProvided = true
    }
}