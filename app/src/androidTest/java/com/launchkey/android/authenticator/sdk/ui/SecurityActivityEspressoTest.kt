package com.launchkey.android.authenticator.sdk.ui

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthRequestManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.test.BaseTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@LargeTest
class SecurityActivityEspressoTest : BaseTest(FakeAuthenticatorManager(), FakeAuthRequestManager()) {
    @Test
    fun securityScreenDefaultShowNoFactorsEnabled() {
        launchActivityWithInternalVerification(SecurityActivity::class.java)
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withText("You have no additional auth methods enabled")).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun securityScreenDefaultShowNoChangesAllowedWhileUnlinked() {
        launchActivityWithInternalVerification(SecurityActivity::class.java)
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withText("No changes can be made while your authenticator is unlinked.")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun securityScreenDoesNotShowNoChangesAllowedWhileUnlinkedWithConfig() {
        AuthenticatorUIManager.instance.run {
            Mockito.`when`(config.areSecurityChangesAllowedWhenUnlinked()).thenReturn(true)
        }
        launchActivityWithInternalVerification(SecurityActivity::class.java)
        Espresso.onView(ViewMatchers.withText("No changes can be made while your authenticator is unlinked.")).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun securityScreenDoesNotAllowAddingPINCodeWhenDisallowed() {
        fakeAManager.expectedIsDeviceLinked = true
        AuthenticatorManager.instance.run {
            Mockito.`when`(config.isMethodAllowed(AuthMethod.PIN_CODE)).thenReturn(false)
        }
        launchActivityWithInternalVerification(SecurityActivity::class.java)
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withId(R.id.security_add)).perform(ViewActions.click())
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withText("PIN Code")).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun securityScreenDoesNotAllowAddingCircleWhenDisallowed() {
        fakeAManager.expectedIsDeviceLinked = true
        AuthenticatorManager.instance.run {
            Mockito.`when`(config.isMethodAllowed(AuthMethod.CIRCLE_CODE)).thenReturn(false)
        }
        launchActivityWithInternalVerification(SecurityActivity::class.java)
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withId(R.id.security_add)).perform(ViewActions.click())
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withText("Circle Code")).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun securityScreenDoesNotAllowAddingLocationsWhenDisallowed() {
        fakeAManager.expectedIsDeviceLinked = true
        AuthenticatorManager.instance.run {
            Mockito.`when`(config.isMethodAllowed(AuthMethod.LOCATIONS)).thenReturn(false)
        }
        launchActivityWithInternalVerification(SecurityActivity::class.java)
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withId(R.id.security_add)).perform(ViewActions.click())
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withText("Geofencing")).check(ViewAssertions.doesNotExist())
    }

//TODO: Can't test these until we find a way to DI Wearables and Biometric Manager so we can mock
//      their isSupported() method
//
//
//    @Test
//    fun securityScreenDoesNotAllowAddingWearablesWhenDisallowed() {
//        AuthenticatorManager.instance.run {
//            Mockito.`when`(isDeviceLinked).thenReturn(true)
//            Mockito.`when`(config.isMethodAllowed(AuthMethod.WEARABLES)).thenReturn(false)
//        }
//        securityActivity = activityRule.activity
//        securityActivity = launchActivityWithInternalVerification(activityRule)
//        securityActivity!!.mSecurityFragment.authMethodManager = SecurityFragment.AuthMethodManager(
//                Mockito.mock(PINCodeManager::class.java),
//                Mockito.mock(CircleCodeManager::class.java),
//                Mockito.mock(LocationsManager::class.java),
//                Mockito.mock(WearablesManager::class.java),
//                Mockito.mock(BiometricManager::class.java))
//        Thread.sleep(1000)
//        Espresso.onView(ViewMatchers.withId(R.id.security_add)).perform(ViewActions.click())
//        Thread.sleep(1000)
//        Espresso.onView(ViewMatchers.withText("Bluetooth Proximity")).check(ViewAssertions.doesNotExist())
//    }
//
//    @Test
//    fun securityScreenDoesNotAllowAddingBiometricWhenDisallowed() {
//        AuthenticatorManager.instance.run {
//            Mockito.`when`(isDeviceLinked).thenReturn(true)
//            Mockito.`when`(config.isMethodAllowed(AuthMethod.BIOMETRIC)).thenReturn(false)
//        }
//        securityActivity = launchActivityWithInternalVerification(activityRule)
//        Thread.sleep(1000)
//        Espresso.onView(ViewMatchers.withId(R.id.security_add)).perform(ViewActions.click())
//        Thread.sleep(1000)
//        Espresso.onView(ViewMatchers.withText("PIN Code")).check(ViewAssertions.doesNotExist())
//    }
}