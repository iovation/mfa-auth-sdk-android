package com.launchkey.android.authenticator.sdk.ui.internal.auth_request.fragment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_method_management.CircleCodeManager
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorConfig
import com.launchkey.android.authenticator.sdk.ui.TestCoroutineRule
import com.launchkey.android.authenticator.sdk.ui.fake.FakeArVerificationFailureResult
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthRequestManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeBiometricManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeCallbackResult
import com.launchkey.android.authenticator.sdk.ui.fake.FakeCircleCodeManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeGeofencesManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeLocationsManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakePINCodeManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeWearablesManager
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify.AuthRequestVerificationViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.verify.AuthRequestVerificationViewModel.VerificationState
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthRequestVerificationViewModelTest {
    private lateinit var viewModel: AuthRequestVerificationViewModel

    private val mockedAuthenticatorConfig = mock<AuthenticatorConfig>()
    private val fakeAuthRequestManager = FakeAuthRequestManager()
    private val fakeAuthenticatorManager = FakeAuthenticatorManager().apply {
        initialize(mockedAuthenticatorConfig)
    }
    private val fakePinCodeManager = FakePINCodeManager(fakeAuthRequestManager)
    private val fakeCircleCodeManager = FakeCircleCodeManager(fakeAuthRequestManager)
    private val fakeBiometricManager = FakeBiometricManager(fakeAuthRequestManager)
    private val fakeWearablesManager = FakeWearablesManager(fakeAuthRequestManager)
    private val fakeLocationsManager = FakeLocationsManager(fakeAuthRequestManager)
    private val fakeGeofencesManager = FakeGeofencesManager(fakeAuthRequestManager)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    
    @Before
    fun setup() {
        viewModel =
            AuthRequestVerificationViewModel(
                fakeAuthRequestManager,
                fakeAuthenticatorManager,
                fakePinCodeManager,
                fakeCircleCodeManager,
                fakeWearablesManager,
                fakeBiometricManager,
                fakeLocationsManager,
                fakeGeofencesManager,
                testCoroutineRule.testCoroutineDispatcher,
                null
            )
    }

    @Test
    fun `test given a conditional geo with PIN & CIRCLE to be verified after, should reset current step to 1 and update amountToVerify from 1 to 2`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.GEOFENCING)
        viewModel.setAuthRequestToBeVerified(mock())
        assertEquals(1, viewModel.currentStep.value)
        assertEquals(1, viewModel.amountToVerify())

        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.PIN_CODE, AuthMethod.CIRCLE_CODE)
        fakeGeofencesManager.expectedAuthMethodRequestVerificationCallback = FakeCallbackResult.Success(false)
        viewModel.verifyGeofences(null)

        val state = viewModel.verificationState.value as VerificationState.VerifyingAuthMethod
        assertEquals(AuthMethod.PIN_CODE, state.authMethod)
        assertEquals(1, viewModel.currentStep.value)
        assertEquals(2, viewModel.amountToVerify())
    }

    @Test
    fun `test given all auth methods are verified, should move to VerifiedAllAuthMethods`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = listOf(AuthMethod.PIN_CODE)
        fakePinCodeManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Success(false)

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyPinCode("", null)

        assertThat(
            viewModel.verificationState.value,
            instanceOf(VerificationState.VerifiedAllAuthMethods::class.java)
        )
    }

    @Test
    fun `test given an auth request that requires an auth method to be verified should move to VerifyingAuthMethod(AUTH_METHOD)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.PIN_CODE)

        viewModel.setAuthRequestToBeVerified(mock())
        val state =
            viewModel.verificationState.value as VerificationState.VerifyingAuthMethod
        assertEquals(AuthMethod.PIN_CODE, state.authMethod)
    }

    @Test
    fun `test given an auth method is successfully verified with more pending, should move to Verifying(NEXT_AUTH_METHOD)`() {
        val firstAuthMethod = AuthMethod.PIN_CODE
        val nextAuthMethod = AuthMethod.CIRCLE_CODE
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(firstAuthMethod, nextAuthMethod)
        fakePinCodeManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Success(false)

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyPinCode("", null)

        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(nextAuthMethod)

        val state = viewModel.verificationState.value as VerificationState.VerifyingAuthMethod
        assertEquals(nextAuthMethod, state.authMethod)
    }

    @Test
    fun `test given the first of 2 auth methods is successfully verified, should update the current step from 1 to 2`() {
        val firstAuthMethod = AuthMethod.PIN_CODE
        val nextAuthMethod = AuthMethod.CIRCLE_CODE
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(firstAuthMethod, nextAuthMethod)
        fakePinCodeManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Success(false)

        viewModel.setAuthRequestToBeVerified(mock())
        assertEquals(1, viewModel.currentStep.value!!)
        viewModel.verifyPinCode("", null)

        assertEquals(2, viewModel.currentStep.value!!)
    }

    @Test
    fun `test when pin verification fails and unlink is triggered, should move to UnlinkTriggered(PIN_CODE, THRESHOLD)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.PIN_CODE)
        fakePinCodeManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), true, false, null))
        val autoUnlinkThreshold = 5

        whenever(mockedAuthenticatorConfig.thresholdAutoUnlink()).thenReturn(autoUnlinkThreshold)

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyPinCode("", null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkTriggered
        assertEquals(autoUnlinkThreshold, state.unlinkThreshold)
        assertEquals(AuthMethod.PIN_CODE, state.failedAuthMethod)
    }

    @Test
    fun `test when pin verification fails and unlink warning is triggered, should move to UnlinkWarningTriggered(PIN_CODE, ATTEMPTS_REMAINING)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.PIN_CODE)
        val attemptsRemaining = 2
        fakePinCodeManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), false, true, attemptsRemaining))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyPinCode("", null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkWarningTriggered
        assertEquals(attemptsRemaining, state.attemptsRemaining)
        assertEquals(AuthMethod.PIN_CODE, state.failedAuthMethod)
    }

    @Test
    fun `test when pin verification fails and was auto sent, should move to AutoFailed(PIN_CODE)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.PIN_CODE)
        fakePinCodeManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(true, mock(), false, false, null))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyPinCode("", null)

        val state = viewModel.verificationState.value as VerificationState.AutoFailed
        assertEquals(AuthMethod.PIN_CODE, state.failedAuthMethod)
    }

    @Test
    fun `test when circle verification fails and unlink is triggered, should move to UnlinkTriggered(CIRCLE_CODE, THRESHOLD)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.CIRCLE_CODE)
        fakeCircleCodeManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), true, false, null))

        val autoUnlinkThreshold = 5

        whenever(mockedAuthenticatorConfig.thresholdAutoUnlink()).thenReturn(autoUnlinkThreshold)

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyCircleCode(listOf(CircleCodeManager.CircleCodeTick.DOWN), null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkTriggered
        assertEquals(autoUnlinkThreshold, state.unlinkThreshold)
        assertEquals(AuthMethod.CIRCLE_CODE, state.failedAuthMethod)
    }

    @Test
    fun `test when circle verification fails and unlink warning is triggered, should move to UnlinkWarningTriggered(CIRCLE_CODE, ATTEMPTS_REMAINING)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.CIRCLE_CODE)
        val attemptsRemaining = 2
        fakeCircleCodeManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), false, true, attemptsRemaining))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyCircleCode(listOf(CircleCodeManager.CircleCodeTick.DOWN), null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkWarningTriggered
        assertEquals(attemptsRemaining, state.attemptsRemaining)
        assertEquals(AuthMethod.CIRCLE_CODE, state.failedAuthMethod)
    }

    @Test
    fun `test when circle verification fails and was auto sent, should move to AutoFailed(CIRCLE_CODE)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.CIRCLE_CODE)
        fakeCircleCodeManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(true, mock(), false, false, null))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyCircleCode(listOf(CircleCodeManager.CircleCodeTick.DOWN), null)

        val state = viewModel.verificationState.value as VerificationState.AutoFailed
        assertEquals(AuthMethod.CIRCLE_CODE, state.failedAuthMethod)
    }

    @Test
    fun `test when biometric verification fails and unlink is triggered, should move to UnlinkTriggered(BIOMETRIC, THRESHOLD)`() {
        val autoUnlinkThreshold = 5

        whenever(mockedAuthenticatorConfig.thresholdAutoUnlink()).thenReturn(autoUnlinkThreshold)
        fakeBiometricManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), true, false, null))
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.BIOMETRIC)

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyBiometric(null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkTriggered
        assertEquals(autoUnlinkThreshold, state.unlinkThreshold)
        assertEquals(AuthMethod.BIOMETRIC, state.failedAuthMethod)
    }

    @Test
    fun `test when biometric verification fails and unlink warning is triggered, should move to UnlinkWarningTriggered(BIOMETRIC, ATTEMPTS_REMAINING)`() {
        val attemptsRemaining = 2

        fakeBiometricManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), false, true, attemptsRemaining))
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.BIOMETRIC)

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyBiometric(null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkWarningTriggered
        assertEquals(attemptsRemaining, state.attemptsRemaining)
        assertEquals(AuthMethod.BIOMETRIC, state.failedAuthMethod)
    }

    @Test
    fun `test when biometric verification fails and was auto sent, should move to AutoFailed(BIOMETRIC)`() {
        fakeBiometricManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(true, mock(), false, false, null))
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.BIOMETRIC)

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyBiometric(null)

        val state = viewModel.verificationState.value as VerificationState.AutoFailed
        assertEquals(AuthMethod.BIOMETRIC, state.failedAuthMethod)
    }

    @Test
    fun `test when locations verification fails and unlink is triggered, should move to UnlinkTriggered(LOCATIONS, THRESHOLD)`() {
        val autoUnlinkThreshold = 5

        whenever(mockedAuthenticatorConfig.thresholdAutoUnlink()).thenReturn(autoUnlinkThreshold)
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.LOCATIONS)
        fakeLocationsManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), true, false, null))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyLocations(null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkTriggered
        assertEquals(autoUnlinkThreshold, state.unlinkThreshold)
        assertEquals(AuthMethod.LOCATIONS, state.failedAuthMethod)
    }

    @Test
    fun `test when locations verification fails and unlink warning is triggered, should move to UnlinkWarningTriggered(LOCATIONS, ATTEMPTS_REMAINING)`() {
        val attemptsRemaining = 2
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.LOCATIONS)
        fakeLocationsManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), false, true, attemptsRemaining))


        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyLocations(null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkWarningTriggered
        assertEquals(attemptsRemaining, state.attemptsRemaining)
        assertEquals(AuthMethod.LOCATIONS, state.failedAuthMethod)
    }

    @Test
    fun `test when locations verification fails and was auto sent, should move to AutoFailed(LOCATIONS)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.LOCATIONS)
        fakeLocationsManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(true, mock(), false, false, null))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyLocations(null)

        val state = viewModel.verificationState.value as VerificationState.AutoFailed
        assertEquals(AuthMethod.LOCATIONS, state.failedAuthMethod)
    }

    @Test
    fun `test when geofences verification fails and unlink is triggered, should move to UnlinkTriggered(GEOFENCING, THRESHOLD)`() {
        val autoUnlinkThreshold = 5
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.LOCATIONS)
        fakeGeofencesManager.expectedAuthMethodRequestVerificationCallback = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), true, false, null))

        whenever(mockedAuthenticatorConfig.thresholdAutoUnlink()).thenReturn(autoUnlinkThreshold)

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyGeofences(null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkTriggered
        assertEquals(autoUnlinkThreshold, state.unlinkThreshold)
        assertEquals(AuthMethod.GEOFENCING, state.failedAuthMethod)
    }

    @Test
    fun `test when geofences verification fails and unlink warning is triggered, should move to UnlinkWarningTriggered(GEOFENCING, ATTEMPTS_REMAINING)`() {
        val attemptsRemaining = 2
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.GEOFENCING)
        fakeGeofencesManager.expectedAuthMethodRequestVerificationCallback = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), false, true, attemptsRemaining))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyGeofences(null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkWarningTriggered
        assertEquals(attemptsRemaining, state.attemptsRemaining)
        assertEquals(AuthMethod.GEOFENCING, state.failedAuthMethod)
    }

    @Test
    fun `test when geofences verification fails and was auto sent, should move to AutoFailed(GEOFENCING)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.GEOFENCING)
        fakeGeofencesManager.expectedAuthMethodRequestVerificationCallback = FakeCallbackResult.Failed(FakeArVerificationFailureResult(true, mock(), false, false, null))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyGeofences(null)

        val state = viewModel.verificationState.value as VerificationState.AutoFailed
        assertEquals(AuthMethod.GEOFENCING, state.failedAuthMethod)
    }

    @Test
    fun `test when wearables verification fails and unlink is triggered, should move to UnlinkTriggered(WEARABLES, THRESHOLD)`() {
        val autoUnlinkThreshold = 5
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.WEARABLES)
        fakeWearablesManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), true, false, null))

        whenever(mockedAuthenticatorConfig.thresholdAutoUnlink()).thenReturn(autoUnlinkThreshold)

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyWearables(null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkTriggered
        assertEquals(autoUnlinkThreshold, state.unlinkThreshold)
        assertEquals(AuthMethod.WEARABLES, state.failedAuthMethod)
    }

    @Test
    fun `test when wearables verification fails and unlink warning is triggered, should move to UnlinkWarningTriggered(WEARABLES, ATTEMPTS_REMAINING)`() {
        val attemptsRemaining = 2
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.WEARABLES)
        fakeWearablesManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(false, mock(), false, true, attemptsRemaining))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyWearables(null)

        val state = viewModel.verificationState.value as VerificationState.UnlinkWarningTriggered
        assertEquals(attemptsRemaining, state.attemptsRemaining)
        assertEquals(AuthMethod.WEARABLES, state.failedAuthMethod)
    }

    @Test
    fun `test when wearables verification fails and was auto sent, should move to AutoFailed(WEARABLES)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = mutableListOf(AuthMethod.WEARABLES)
        fakeWearablesManager.expectedVerifyARAuthMethodVerificationCallbackResult = FakeCallbackResult.Failed(FakeArVerificationFailureResult(true, mock(), false, false, null))

        viewModel.setAuthRequestToBeVerified(mock())
        viewModel.verifyWearables(null)

        val state = viewModel.verificationState.value as VerificationState.AutoFailed
        assertEquals(AuthMethod.WEARABLES, state.failedAuthMethod)
    }
}