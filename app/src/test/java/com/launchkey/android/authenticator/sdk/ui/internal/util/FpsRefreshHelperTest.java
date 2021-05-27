/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */

package com.launchkey.android.authenticator.sdk.ui.internal.util;

import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FpsRefreshHelperTest {

    private final long RATE = 1000L / (long) FpsRefreshHelper.FPS_MAX;

    @Mock private View mMockView;
    @Mock private TimingCounter.NowProvider mockNowProvider;

    @Test
    public void testInstantiation() {
        FpsRefreshHelper helper = new FpsRefreshHelper(null);
        assertNotNull(helper);
    }

    @Test
    public void testNullViewNonCrash() {
        FpsRefreshHelper fpsRefreshHelper = getFpsRefreshHelper();
        when(mockNowProvider.getNow()).thenReturn(100L);
        fpsRefreshHelper.forceInvalidate();
        // Test passing if no crash happens
    }

    @Test
    public void testNoUpdateAllowed() {
        FpsRefreshHelper fpsRefreshHelper = getFpsRefreshHelper();
        when(mockNowProvider.getNow()).thenReturn(0L);
        fpsRefreshHelper.forceInvalidate();
        when(mockNowProvider.getNow()).thenReturn(RATE/2L);
        final boolean invalidated = fpsRefreshHelper.invalidate();
        assertFalse(invalidated);
    }

    @Test
    public void testUpdateAllowedSame() {
        FpsRefreshHelper fpsRefreshHelper = getFpsRefreshHelper();
        when(mockNowProvider.getNow()).thenReturn(0L);
        fpsRefreshHelper.forceInvalidate();
        when(mockNowProvider.getNow()).thenReturn(RATE);
        final boolean invalidated = fpsRefreshHelper.invalidate();
        assertTrue(invalidated);
    }

    @Test
    public void testUpdateAllowedGreater() {
        FpsRefreshHelper fpsRefreshHelper = getFpsRefreshHelper();
        when(mockNowProvider.getNow()).thenReturn(0L);
        fpsRefreshHelper.forceInvalidate();
        when(mockNowProvider.getNow()).thenReturn(RATE * 2L);
        final boolean invalidated = fpsRefreshHelper.invalidate();
        assertTrue(invalidated);
    }

    @Test
    public void testUpdateForced() {
        FpsRefreshHelper fpsRefreshHelper = getFpsRefreshHelper();
        when(mockNowProvider.getNow()).thenReturn(0L);
        fpsRefreshHelper.forceInvalidate();
        when(mockNowProvider.getNow()).thenReturn(RATE/2L);
        final boolean invalidated = fpsRefreshHelper.forceInvalidate();
        assertTrue(invalidated);
    }

    private FpsRefreshHelper getFpsRefreshHelper() {
        return new FpsRefreshHelper(mMockView, FpsRefreshHelper.FPS_MAX, mockNowProvider);
    }
}
