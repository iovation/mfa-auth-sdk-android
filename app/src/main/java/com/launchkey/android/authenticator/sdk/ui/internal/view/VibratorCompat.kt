/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class VibratorCompat(context: Context) {
    private val context: Context
    private val vibrator: Vibrator
    fun vibrate(durationMillis: Long) {
        if (PackageManager.PERMISSION_GRANTED != context.packageManager
                        .checkPermission(Manifest.permission.VIBRATE, context.packageName)) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(durationMillis)
        }
    }

    init {
        requireNotNull(context) { "Context cannot be null" }
        this.context = context.applicationContext
        vibrator = this.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}