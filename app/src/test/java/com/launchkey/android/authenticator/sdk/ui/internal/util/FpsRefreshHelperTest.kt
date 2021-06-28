/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.view.View
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FpsRefreshHelperTest {
    private val RATE = 1000L / FpsRefreshHelper.FPS_MAX.toLong()
    
    @Mock
    private val mMockView: View? = null
    
    @Mock
    private val mockNowProvider: TimingCounter.NowProvider? = null
    @Test
    fun testInstantiation() {
        val helper = FpsRefreshHelper(null)
        Assert.assertNotNull(helper)
    }
    
    @Test
    fun testNullViewNonCrash() {
        val fpsRefreshHelper = getFpsRefreshHelper()
        Mockito.`when`(mockNowProvider!!.now).thenReturn(100L)
        fpsRefreshHelper.forceInvalidate()
        // Test passing if no crash happens
    }
    
    @Test
    fun testNoUpdateAllowed() {
        val fpsRefreshHelper = getFpsRefreshHelper()
        Mockito.`when`(mockNowProvider!!.now).thenReturn(0L)
        fpsRefreshHelper.forceInvalidate()
        Mockito.`when`(mockNowProvider.now).thenReturn(RATE / 2L)
        val invalidated = fpsRefreshHelper.invalidate()
        Assert.assertFalse(invalidated)
    }
    
    @Test
    fun testUpdateAllowedSame() {
        val fpsRefreshHelper = getFpsRefreshHelper()
        Mockito.`when`(mockNowProvider!!.now).thenReturn(0L)
        fpsRefreshHelper.forceInvalidate()
        Mockito.`when`(mockNowProvider.now).thenReturn(RATE)
        val invalidated = fpsRefreshHelper.invalidate()
        Assert.assertTrue(invalidated)
    }
    
    @Test
    fun testUpdateAllowedGreater() {
        val fpsRefreshHelper = getFpsRefreshHelper()
        Mockito.`when`(mockNowProvider!!.now).thenReturn(0L)
        fpsRefreshHelper.forceInvalidate()
        Mockito.`when`(mockNowProvider.now).thenReturn(RATE * 2L)
        val invalidated = fpsRefreshHelper.invalidate()
        Assert.assertTrue(invalidated)
    }
    
    @Test
    fun testUpdateForced() {
        val fpsRefreshHelper = getFpsRefreshHelper()
        Mockito.`when`(mockNowProvider!!.now).thenReturn(0L)
        fpsRefreshHelper.forceInvalidate()
        Mockito.`when`(mockNowProvider.now).thenReturn(RATE / 2L)
        val invalidated = fpsRefreshHelper.forceInvalidate()
        Assert.assertTrue(invalidated)
    }
    
    private fun getFpsRefreshHelper(): FpsRefreshHelper {
        return FpsRefreshHelper(mMockView, FpsRefreshHelper.FPS_MAX, mockNowProvider!!)
    }
}