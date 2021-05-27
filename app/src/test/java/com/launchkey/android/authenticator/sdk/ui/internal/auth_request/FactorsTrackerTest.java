package com.launchkey.android.authenticator.sdk.ui.internal.auth_request;

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class FactorsTrackerTest {
    @Test
    public void testTrackerNoFactors() {
        // All zeroes if no auth methods are verified
        FactorsTracker tracker = getTracker();
        assertEquals(0, tracker.getCurrentForUi());
        assertEquals(0, tracker.getCountForUi());
    }

    @Test
    public void testTrackerNone() {
        FactorsTracker tracker = getTracker(
                AuthMethod.PIN_CODE);

        tracker.setVerified(AuthMethod.PIN_CODE);

        assertNull(tracker.getCurrentId());
        assertEquals(1, tracker.getCountForUi());
    }

    @Test
    public void testTrackerVerifiedFlags1() {
        FactorsTracker tracker = getTracker(
                AuthMethod.CIRCLE_CODE,
                AuthMethod.PIN_CODE);

        tracker.setVerified(AuthMethod.CIRCLE_CODE);
        tracker.setVerified(AuthMethod.PIN_CODE);

        assertEquals(true, tracker.isVerified(AuthMethod.CIRCLE_CODE));
        assertEquals(true, tracker.isVerified(AuthMethod.PIN_CODE));
    }

    @Test
    public void testTrackerVerifiedFlags2() {

        FactorsTracker tracker = getTracker(
                AuthMethod.WEARABLES,
                AuthMethod.PIN_CODE,
                AuthMethod.BIOMETRIC);

        tracker.setVerified(AuthMethod.WEARABLES);
        tracker.setVerified(AuthMethod.PIN_CODE);

        assertEquals(true, tracker.isVerified(AuthMethod.WEARABLES));
        assertEquals(true, tracker.isVerified(AuthMethod.PIN_CODE));
        assertEquals(false, tracker.isVerified(AuthMethod.BIOMETRIC));
    }

    @Test
    public void testTrackerVerifiedFlags3() {

        FactorsTracker tracker = getTracker(
                AuthMethod.WEARABLES,
                AuthMethod.PIN_CODE,
                AuthMethod.BIOMETRIC);

        assertEquals(false, tracker.isVerified(AuthMethod.WEARABLES));
        assertEquals(false, tracker.isVerified(AuthMethod.PIN_CODE));
        assertEquals(false, tracker.isVerified(AuthMethod.BIOMETRIC));
    }

    @Test
    public void testTrackerOneFactor() {

        FactorsTracker tracker = getTracker(
                AuthMethod.CIRCLE_CODE,
                AuthMethod.PIN_CODE);

        assertEquals(1, tracker.getCurrentForUi());

        tracker.setVerified(AuthMethod.CIRCLE_CODE);

        assertEquals(2, tracker.getCurrentForUi());
        assertEquals(2, tracker.getCountForUi());
    }

    @Test
    public void testTrackerTwoFactors() {

        FactorsTracker tracker = getTracker(
                AuthMethod.CIRCLE_CODE,
                AuthMethod.BIOMETRIC);

        tracker.setVerified(AuthMethod.CIRCLE_CODE);

        assertEquals(AuthMethod.BIOMETRIC, tracker.getCurrentId());
        assertEquals(2, tracker.getCurrentForUi());
        assertEquals(2, tracker.getCountForUi());
    }

    @Test
    public void testTrackerTwoVerifiedOnePending() {

        FactorsTracker tracker = getTracker(
                AuthMethod.LOCATIONS,
                AuthMethod.CIRCLE_CODE,
                AuthMethod.BIOMETRIC);

        tracker.setVerified(AuthMethod.LOCATIONS);
        tracker.setVerified(AuthMethod.CIRCLE_CODE);

        assertEquals(AuthMethod.BIOMETRIC, tracker.getCurrentId());
        assertEquals(3, tracker.getCurrentForUi());
        assertEquals(3, tracker.getCountForUi());
    }

    @Test
    public void testTrackerThreeVerifiedNonePending() {

        FactorsTracker tracker = getTracker(
                AuthMethod.GEOFENCING,
                AuthMethod.CIRCLE_CODE,
                AuthMethod.BIOMETRIC);

        tracker.setVerified(AuthMethod.GEOFENCING);
        tracker.setVerified(AuthMethod.CIRCLE_CODE);
        tracker.setVerified(AuthMethod.BIOMETRIC);

        assertNull(tracker.getCurrentId());
        assertEquals(3, tracker.getCurrentForUi());
        assertEquals(3, tracker.getCountForUi());
    }

    @Test
    public void testTrackerTwoVerifiedNonePending() {

        FactorsTracker tracker = getTracker(
                AuthMethod.CIRCLE_CODE,
                AuthMethod.PIN_CODE,
                AuthMethod.GEOFENCING);

        tracker.setVerified(AuthMethod.CIRCLE_CODE);
        tracker.setVerified(AuthMethod.PIN_CODE);
        tracker.setVerified(AuthMethod.GEOFENCING);

        assertNull(tracker.getCurrentId());
        assertEquals(3, tracker.getCurrentForUi());
        assertEquals(3, tracker.getCountForUi());
    }

    @Test
    public void testTrackerGeofencing() {

        FactorsTracker tracker = getTracker(
                AuthMethod.GEOFENCING,
                AuthMethod.LOCATIONS);

        assertEquals(1, tracker.getCurrentForUi());
        assertEquals(1, tracker.getCountForUi());
        assertEquals(AuthMethod.GEOFENCING, tracker.getCurrentId());

        tracker.setVerified(AuthMethod.GEOFENCING);
        assertEquals(AuthMethod.LOCATIONS, tracker.getCurrentId());

        tracker.setVerified(AuthMethod.LOCATIONS);

        assertNull(tracker.getCurrentId());
    }

    @Test
    public void testTrackerGeofencingAsOne() {

        FactorsTracker tracker = getTracker(
                AuthMethod.GEOFENCING,
                AuthMethod.LOCATIONS,
                AuthMethod.BIOMETRIC);

        assertEquals(1, tracker.getCurrentForUi());
        assertEquals(2, tracker.getCountForUi());
        assertEquals(AuthMethod.GEOFENCING, tracker.getCurrentId());

        tracker.setVerified(AuthMethod.GEOFENCING);

        assertEquals(AuthMethod.LOCATIONS, tracker.getCurrentId());

        tracker.setVerified(AuthMethod.LOCATIONS);

        assertEquals(AuthMethod.BIOMETRIC, tracker.getCurrentId());
        assertEquals(2, tracker.getCurrentForUi());
    }

    @Test
    public void testTrackerAllUserSet() {

        FactorsTracker tracker = getTracker(
                AuthMethod.WEARABLES,
                AuthMethod.LOCATIONS,
                AuthMethod.CIRCLE_CODE,
                AuthMethod.PIN_CODE,
                AuthMethod.BIOMETRIC);

        tracker.setVerified(AuthMethod.WEARABLES);
        tracker.setVerified(AuthMethod.LOCATIONS);
        tracker.setVerified(AuthMethod.CIRCLE_CODE);

        assertEquals(4, tracker.getCurrentForUi());
        assertEquals(5, tracker.getCountForUi());
        assertEquals(AuthMethod.PIN_CODE, tracker.getCurrentId());
    }

    @Test
    public void testTrackerAllUserSet2() {

        FactorsTracker tracker = getTracker(
                AuthMethod.WEARABLES,
                AuthMethod.LOCATIONS,
                AuthMethod.CIRCLE_CODE,
                AuthMethod.PIN_CODE);

        tracker.setVerified(AuthMethod.WEARABLES);
        tracker.setVerified(AuthMethod.LOCATIONS);

        assertEquals(3, tracker.getCurrentForUi());
        assertEquals(4, tracker.getCountForUi());
        assertEquals(AuthMethod.CIRCLE_CODE, tracker.getCurrentId());
    }

    @Test
    public void testTrackerAllUserAndServiceSet() {

        FactorsTracker tracker = getTracker(
                AuthMethod.WEARABLES,
                AuthMethod.GEOFENCING,
                AuthMethod.LOCATIONS,
                AuthMethod.CIRCLE_CODE,
                AuthMethod.PIN_CODE,
                AuthMethod.BIOMETRIC);

        tracker.setVerified(AuthMethod.WEARABLES);

        assertEquals(2, tracker.getCurrentForUi());

        tracker.setVerified(AuthMethod.GEOFENCING);

        assertEquals(2, tracker.getCurrentForUi());
        assertEquals(AuthMethod.LOCATIONS, tracker.getCurrentId());

        tracker.setVerified(AuthMethod.LOCATIONS);

        assertEquals(3, tracker.getCurrentForUi());
        assertEquals(5, tracker.getCountForUi());
        assertEquals(AuthMethod.CIRCLE_CODE, tracker.getCurrentId());
    }

    @Test
    public void testTrackerAllUserAndServiceSet2() {

        FactorsTracker tracker = getTracker(
                AuthMethod.WEARABLES,
                AuthMethod.GEOFENCING,
                AuthMethod.LOCATIONS,
                AuthMethod.CIRCLE_CODE,
                AuthMethod.PIN_CODE);

        tracker.setVerified(AuthMethod.WEARABLES);

        assertEquals(2, tracker.getCurrentForUi());

        tracker.setVerified(AuthMethod.GEOFENCING);

        assertEquals(2, tracker.getCurrentForUi());

        assertEquals(AuthMethod.LOCATIONS, tracker.getCurrentId());

        tracker.setVerified(AuthMethod.LOCATIONS);

        assertEquals(3, tracker.getCurrentForUi());
        assertEquals(4, tracker.getCountForUi());
        assertEquals(AuthMethod.CIRCLE_CODE, tracker.getCurrentId());

        tracker.setVerified(AuthMethod.CIRCLE_CODE);

        assertEquals(AuthMethod.PIN_CODE, tracker.getCurrentId());
    }

    private FactorsTracker getTracker(AuthMethod... authMethods) {
        return new FactorsTracker(Arrays.asList(authMethods));
    }
}
