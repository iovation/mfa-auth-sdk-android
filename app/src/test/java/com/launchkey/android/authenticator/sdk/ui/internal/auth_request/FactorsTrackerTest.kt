package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import org.junit.Assert
import org.junit.Test
import java.util.*

class FactorsTrackerTest {
    @Test
    fun testTrackerNoFactors() {
        // All zeroes if no auth methods are verified
        val tracker = getTracker()
        Assert.assertEquals(0, tracker.currentForUi.toLong())
        Assert.assertEquals(0, tracker.countForUi.toLong())
    }
    
    @Test
    fun testTrackerNone() {
        val tracker = getTracker(
            AuthMethod.PIN_CODE
        )
        tracker.setVerified(AuthMethod.PIN_CODE)
        Assert.assertNull(tracker.currentId)
        Assert.assertEquals(1, tracker.countForUi.toLong())
    }
    
    @Test
    fun testTrackerVerifiedFlags1() {
        val tracker = getTracker(
            AuthMethod.CIRCLE_CODE,
            AuthMethod.PIN_CODE
        )
        tracker.setVerified(AuthMethod.CIRCLE_CODE)
        tracker.setVerified(AuthMethod.PIN_CODE)
        Assert.assertEquals(true, tracker.isVerified(AuthMethod.CIRCLE_CODE))
        Assert.assertEquals(true, tracker.isVerified(AuthMethod.PIN_CODE))
    }
    
    @Test
    fun testTrackerVerifiedFlags2() {
        val tracker = getTracker(
            AuthMethod.WEARABLES,
            AuthMethod.PIN_CODE,
            AuthMethod.BIOMETRIC
        )
        tracker.setVerified(AuthMethod.WEARABLES)
        tracker.setVerified(AuthMethod.PIN_CODE)
        Assert.assertEquals(true, tracker.isVerified(AuthMethod.WEARABLES))
        Assert.assertEquals(true, tracker.isVerified(AuthMethod.PIN_CODE))
        Assert.assertEquals(false, tracker.isVerified(AuthMethod.BIOMETRIC))
    }
    
    @Test
    fun testTrackerVerifiedFlags3() {
        val tracker = getTracker(
            AuthMethod.WEARABLES,
            AuthMethod.PIN_CODE,
            AuthMethod.BIOMETRIC
        )
        Assert.assertEquals(false, tracker.isVerified(AuthMethod.WEARABLES))
        Assert.assertEquals(false, tracker.isVerified(AuthMethod.PIN_CODE))
        Assert.assertEquals(false, tracker.isVerified(AuthMethod.BIOMETRIC))
    }
    
    @Test
    fun testTrackerOneFactor() {
        val tracker = getTracker(
            AuthMethod.CIRCLE_CODE,
            AuthMethod.PIN_CODE
        )
        Assert.assertEquals(1, tracker.currentForUi.toLong())
        tracker.setVerified(AuthMethod.CIRCLE_CODE)
        Assert.assertEquals(2, tracker.currentForUi.toLong())
        Assert.assertEquals(2, tracker.countForUi.toLong())
    }
    
    @Test
    fun testTrackerTwoFactors() {
        val tracker = getTracker(
            AuthMethod.CIRCLE_CODE,
            AuthMethod.BIOMETRIC
        )
        tracker.setVerified(AuthMethod.CIRCLE_CODE)
        Assert.assertEquals(AuthMethod.BIOMETRIC, tracker.currentId)
        Assert.assertEquals(2, tracker.currentForUi.toLong())
        Assert.assertEquals(2, tracker.countForUi.toLong())
    }
    
    @Test
    fun testTrackerTwoVerifiedOnePending() {
        val tracker = getTracker(
            AuthMethod.LOCATIONS,
            AuthMethod.CIRCLE_CODE,
            AuthMethod.BIOMETRIC
        )
        tracker.setVerified(AuthMethod.LOCATIONS)
        tracker.setVerified(AuthMethod.CIRCLE_CODE)
        Assert.assertEquals(AuthMethod.BIOMETRIC, tracker.currentId)
        Assert.assertEquals(3, tracker.currentForUi.toLong())
        Assert.assertEquals(3, tracker.countForUi.toLong())
    }
    
    @Test
    fun testTrackerThreeVerifiedNonePending() {
        val tracker = getTracker(
            AuthMethod.GEOFENCING,
            AuthMethod.CIRCLE_CODE,
            AuthMethod.BIOMETRIC
        )
        tracker.setVerified(AuthMethod.GEOFENCING)
        tracker.setVerified(AuthMethod.CIRCLE_CODE)
        tracker.setVerified(AuthMethod.BIOMETRIC)
        Assert.assertNull(tracker.currentId)
        Assert.assertEquals(3, tracker.currentForUi.toLong())
        Assert.assertEquals(3, tracker.countForUi.toLong())
    }
    
    @Test
    fun testTrackerTwoVerifiedNonePending() {
        val tracker = getTracker(
            AuthMethod.CIRCLE_CODE,
            AuthMethod.PIN_CODE,
            AuthMethod.GEOFENCING
        )
        tracker.setVerified(AuthMethod.CIRCLE_CODE)
        tracker.setVerified(AuthMethod.PIN_CODE)
        tracker.setVerified(AuthMethod.GEOFENCING)
        Assert.assertNull(tracker.currentId)
        Assert.assertEquals(3, tracker.currentForUi.toLong())
        Assert.assertEquals(3, tracker.countForUi.toLong())
    }
    
    @Test
    fun testTrackerGeofencing() {
        val tracker = getTracker(
            AuthMethod.GEOFENCING,
            AuthMethod.LOCATIONS
        )
        Assert.assertEquals(1, tracker.currentForUi.toLong())
        Assert.assertEquals(1, tracker.countForUi.toLong())
        Assert.assertEquals(AuthMethod.GEOFENCING, tracker.currentId)
        tracker.setVerified(AuthMethod.GEOFENCING)
        Assert.assertEquals(AuthMethod.LOCATIONS, tracker.currentId)
        tracker.setVerified(AuthMethod.LOCATIONS)
        Assert.assertNull(tracker.currentId)
    }
    
    @Test
    fun testTrackerGeofencingAsOne() {
        val tracker = getTracker(
            AuthMethod.GEOFENCING,
            AuthMethod.LOCATIONS,
            AuthMethod.BIOMETRIC
        )
        Assert.assertEquals(1, tracker.currentForUi.toLong())
        Assert.assertEquals(2, tracker.countForUi.toLong())
        Assert.assertEquals(AuthMethod.GEOFENCING, tracker.currentId)
        tracker.setVerified(AuthMethod.GEOFENCING)
        Assert.assertEquals(AuthMethod.LOCATIONS, tracker.currentId)
        tracker.setVerified(AuthMethod.LOCATIONS)
        Assert.assertEquals(AuthMethod.BIOMETRIC, tracker.currentId)
        Assert.assertEquals(2, tracker.currentForUi.toLong())
    }
    
    @Test
    fun testTrackerAllUserSet() {
        val tracker = getTracker(
            AuthMethod.WEARABLES,
            AuthMethod.LOCATIONS,
            AuthMethod.CIRCLE_CODE,
            AuthMethod.PIN_CODE,
            AuthMethod.BIOMETRIC
        )
        tracker.setVerified(AuthMethod.WEARABLES)
        tracker.setVerified(AuthMethod.LOCATIONS)
        tracker.setVerified(AuthMethod.CIRCLE_CODE)
        Assert.assertEquals(4, tracker.currentForUi.toLong())
        Assert.assertEquals(5, tracker.countForUi.toLong())
        Assert.assertEquals(AuthMethod.PIN_CODE, tracker.currentId)
    }
    
    @Test
    fun testTrackerAllUserSet2() {
        val tracker = getTracker(
            AuthMethod.WEARABLES,
            AuthMethod.LOCATIONS,
            AuthMethod.CIRCLE_CODE,
            AuthMethod.PIN_CODE
        )
        tracker.setVerified(AuthMethod.WEARABLES)
        tracker.setVerified(AuthMethod.LOCATIONS)
        Assert.assertEquals(3, tracker.currentForUi.toLong())
        Assert.assertEquals(4, tracker.countForUi.toLong())
        Assert.assertEquals(AuthMethod.CIRCLE_CODE, tracker.currentId)
    }
    
    @Test
    fun testTrackerAllUserAndServiceSet() {
        val tracker = getTracker(
            AuthMethod.WEARABLES,
            AuthMethod.GEOFENCING,
            AuthMethod.LOCATIONS,
            AuthMethod.CIRCLE_CODE,
            AuthMethod.PIN_CODE,
            AuthMethod.BIOMETRIC
        )
        tracker.setVerified(AuthMethod.WEARABLES)
        Assert.assertEquals(2, tracker.currentForUi.toLong())
        tracker.setVerified(AuthMethod.GEOFENCING)
        Assert.assertEquals(2, tracker.currentForUi.toLong())
        Assert.assertEquals(AuthMethod.LOCATIONS, tracker.currentId)
        tracker.setVerified(AuthMethod.LOCATIONS)
        Assert.assertEquals(3, tracker.currentForUi.toLong())
        Assert.assertEquals(5, tracker.countForUi.toLong())
        Assert.assertEquals(AuthMethod.CIRCLE_CODE, tracker.currentId)
    }
    
    @Test
    fun testTrackerAllUserAndServiceSet2() {
        val tracker = getTracker(
            AuthMethod.WEARABLES,
            AuthMethod.GEOFENCING,
            AuthMethod.LOCATIONS,
            AuthMethod.CIRCLE_CODE,
            AuthMethod.PIN_CODE
        )
        tracker.setVerified(AuthMethod.WEARABLES)
        Assert.assertEquals(2, tracker.currentForUi.toLong())
        tracker.setVerified(AuthMethod.GEOFENCING)
        Assert.assertEquals(2, tracker.currentForUi.toLong())
        Assert.assertEquals(AuthMethod.LOCATIONS, tracker.currentId)
        tracker.setVerified(AuthMethod.LOCATIONS)
        Assert.assertEquals(3, tracker.currentForUi.toLong())
        Assert.assertEquals(4, tracker.countForUi.toLong())
        Assert.assertEquals(AuthMethod.CIRCLE_CODE, tracker.currentId)
        tracker.setVerified(AuthMethod.CIRCLE_CODE)
        Assert.assertEquals(AuthMethod.PIN_CODE, tracker.currentId)
    }
    
    private fun getTracker(vararg authMethods: AuthMethod): FactorsTracker {
        return FactorsTracker(Arrays.asList(*authMethods))
    }
}