/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.content.pm.PackageManager
import android.os.Vibrator
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class VibratorCompatTest {
    @Mock
    private val mockContext: Context = mock()
    @Test(expected = NullPointerException::class)
    fun testInstantiation() {
        VibratorCompat(mockContext)
    }
    
    @Test
    fun testVibratorNotCalled() {
        val mockPackageManager = Mockito.mock(PackageManager::class.java)
        Mockito.`when`(
            mockPackageManager.checkPermission(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
            )
        ).thenReturn(1)
        Mockito.`when`(mockContext!!.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockContext.packageName).thenReturn("asdf")
        Mockito.`when`(mockContext.applicationContext).thenReturn(mockContext)
        val mockVibrator = Mockito.mock(Vibrator::class.java)
        Mockito.`when`(
            mockContext.getSystemService(
                ArgumentMatchers.any(
                    String::class.java
                )
            )
        ).thenReturn(mockVibrator)
        val vibrator = VibratorCompat(mockContext)
        vibrator.vibrate(1000)
        Mockito.verify(mockVibrator, Mockito.times(0)).vibrate(ArgumentMatchers.anyLong())
    }
    
    @Test
    fun testVibratorCalled() {
        val mockPackageManager = Mockito.mock(PackageManager::class.java)
        Mockito.`when`(
            mockPackageManager.checkPermission(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
            )
        ).thenReturn(0)
        Mockito.`when`(mockContext!!.packageManager).thenReturn(mockPackageManager)
        Mockito.`when`(mockContext.packageName).thenReturn("asdf")
        Mockito.`when`(mockContext.applicationContext).thenReturn(mockContext)
        val mockVibrator = Mockito.mock(Vibrator::class.java)
        Mockito.`when`(
            mockContext.getSystemService(
                ArgumentMatchers.any(
                    String::class.java
                )
            )
        ).thenReturn(mockVibrator)
        val vibrator = VibratorCompat(mockContext)
        vibrator.vibrate(1000)
        Mockito.verify(mockVibrator, Mockito.times(1)).vibrate(ArgumentMatchers.anyLong())
    }
}