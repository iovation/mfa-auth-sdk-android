/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */

package com.launchkey.android.authenticator.sdk.ui.internal.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Vibrator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VibratorCompatTest {

    @Mock
    private Context mockContext;

    @Test(expected = NullPointerException.class)
    public void testInstantiation() {
        new VibratorCompat(null);
    }

    @Test
    public void testVibratorNotCalled() {
        PackageManager mockPackageManager = mock(PackageManager.class);
        when(mockPackageManager.checkPermission(anyString(), anyString())).thenReturn(1);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockContext.getPackageName()).thenReturn("asdf");
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        Vibrator mockVibrator = mock(Vibrator.class);
        when(mockContext.getSystemService(any(String.class))).thenReturn(mockVibrator);
        VibratorCompat vibrator = new VibratorCompat(mockContext);
        vibrator.vibrate(1000);
        verify(mockVibrator, times(0)).vibrate(anyLong());
    }

    @Test
    public void testVibratorCalled() {
        PackageManager mockPackageManager = mock(PackageManager.class);
        when(mockPackageManager.checkPermission(anyString(), anyString())).thenReturn(0);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockContext.getPackageName()).thenReturn("asdf");
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        Vibrator mockVibrator = mock(Vibrator.class);
        when(mockContext.getSystemService(any(String.class))).thenReturn(mockVibrator);
        VibratorCompat vibrator = new VibratorCompat(mockContext);
        vibrator.vibrate(1000);
        verify(mockVibrator, times(1)).vibrate(anyLong());
    }
}
