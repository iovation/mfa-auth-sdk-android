/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */

//package com.launchkey.android.authenticator.sdk.ui.internal.view;
//
//import android.os.Build;
//
//import com.launchkey.android.authenticator.sdk.AuthenticatorConfig;
//import com.launchkey.android.authenticator.sdk.AuthenticatorManager;
//import com.launchkey.android.authenticator.sdk.core.auth.AuthRequest;
//import com.launchkey.android.authenticator.sdk.core.auth.AuthRequestManager;
//import com.launchkey.android.authenticator.sdk.internal.tools.utils.TimingCounter;
//import com.launchkey.android.authenticator.sdk.ui.mock.MockHandler;
//
//import org.junit.Before;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//
//import static org.mockito.Mockito.when;
//
//@RunWith(MockitoJUnitRunner.class)
//public class IntRequestPresenterTest {
//    @Mock private IntRequestContract.View mockRequestView;
//    @Mock private com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager mMockManager;
//    @Mock private AuthenticatorManager mMockUiManager;
//    @Mock private AuthRequestManager mockRequestManager;
//    @Mock private com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorConfig mockConfig;
//    @Mock private AuthenticatorConfig mockUiConfig;
//    private MockHandler mockHandler = MockHandler.createMockHandler();
//    @Mock private AuthRequest mockRequest;
//    @Mock private TimingCounter.NowProvider mockNowProvider;
//    @Captor private ArgumentCaptor eventsCaptor;
//
//    private IntRequestPresenter presenter;
//
//    private void useReflectionToSetApiAbove23() {
//        try {
//            Field field = Build.VERSION.class.getField("SDK_INT");
//            field.setAccessible(true);
//
//            Field modifiersField = Field.class.getDeclaredField("modifiers");
//            modifiersField.setAccessible(true);
//            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//
//            field.set(null, 24);
//        } catch (Exception ignored) {}
//    }
//
//    @Before
//    public void setup() {
//        // Doing this so that check for mock location doesn't result in a system call
//        useReflectionToSetApiAbove23();
//
//        when(mockRequest.getExpiresAtMillis()).thenReturn(100L);
//
//        when(mMockUiManager.getConfig()).thenReturn(mockUiConfig);
//
//        presenter = new IntRequestPresenter(
//                mockRequestView,
//                mMockManager,
//                mockRequestManager,
//                mockHandler,
//                mockNowProvider);
//    }

//    @Test
//    public void testRequestReceivedAndShown() {
//        PolicyResult mockResult = Mockito.mock(PolicyResult.class);
//        when(mockResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.TRUE);
//        when(mockResult.getPendingFactors()).thenReturn(new ArrayList<Factor>());
//        when(mockPolicyProcessor.getPolicyResult(any(AuthPolicy.class))).thenReturn(mockResult);
//        sendRequestToAuthRequestManagerWithPolicyResult(Mockito.mock(AuthPolicy.class));
//        ArgumentCaptor<Boolean> requiresAuthMethodVerifCaptor = ArgumentCaptor.forClass(Boolean.class);
//        verify(mockRequestView).updateInfo(nullable(String.class), nullable(String.class), requiresAuthMethodVerifCaptor.capture());
//        assertFalse(requiresAuthMethodVerifCaptor.getValue());
//    }
//
//    @Test
//    public void testRequestReceivedAndNoErrorShownEvenThoughItWillFail() {
//        PolicyResult mockResult = Mockito.mock(PolicyResult.class);
//        when(mockResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.FALSE);
//        when(mockPolicyProcessor.getPolicyResult(any(AuthPolicy.class))).thenReturn(mockResult);
//        sendRequestToAuthRequestManagerWithPolicyResult(Mockito.mock(AuthPolicy.class));
//        verify(mockRequestView, times(0)).showError(any(AuthRequestError.class));
//    }
//
//    @Test
//    public void testRequestReceivedAndAnyErrorShown() {
//        PolicyResult mockResult = Mockito.mock(PolicyResult.class);
//        when(mockResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.FALSE);
//        when(mockResult.getErrorFlag()).thenReturn(AuthRequestError.Config.AMOUNT);
//        when(mockPolicyProcessor.getPolicyResult(any(AuthPolicy.class))).thenReturn(mockResult);
//        sendRequestToAuthRequestManagerWithPolicyResult(Mockito.mock(AuthPolicy.class));
//        presenter.setUserReady();
//        verify(mockRequestView, times(1)).showError(any(AuthRequestError.class));
//    }
//
//    @Test
//    public void testRequestAmountReceivedAndUserDenied() {
//        PolicyResult mockResult = Mockito.mock(PolicyResult.class);
//        when(mockResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.TRUE);
//        when(mockResult.getPendingFactors()).thenReturn(new ArrayList<Factor>());
//        when(mockPolicyProcessor.getPolicyResult(any(AuthPolicy.class))).thenReturn(mockResult);
//        sendRequestToAuthRequestManagerWithPolicyResult(Mockito.mock(AuthPolicy.class));
//        presenter.setUserDenied();
//        verify(mockRequestView, times(1)).showResponding();
//    }
//
//    @Test
//    public void testRequestAmountReceivedAndUserDeniedWithDenialOptions() {
//        PolicyResult mockResult = Mockito.mock(PolicyResult.class);
//        when(mockResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.TRUE);
//        when(mockResult.getPendingFactors()).thenReturn(new ArrayList<Factor>());
//        when(mockPolicyProcessor.getPolicyResult(any(AuthPolicy.class))).thenReturn(mockResult);
//        sendRequestToAuthRequestManagerWithPolicyResult(Mockito.mock(AuthPolicy.class));
//        when(mockRequest.getDenialReasons()).thenReturn(new DenialReason[] {
//                new DenialReason("0", "Nope", false),
//                new DenialReason("1", "somefraudheremmk", true)
//        });
//        when(mockRequest.hasDenialReasons()).thenReturn(true);
//        presenter.setUserDenied();
//        verify(mockRequestView, times(1)).showDenialReasons(any(DenialReason[].class));
//        presenter.setUserDenialReason(new DenialReason("0", "Nope", false));
//        verify(mockRequestView, times(1)).showResponding();
//    }
//
//    @Test
//    public void testDynamicRequestReceivedAndFlowCorrectInsideOfFence() {
//        // Given
//        Fence insideThisFence = Mockito.mock(Fence.class);
//        when(insideThisFence.isLocationInside(any(Location.class))).thenReturn(true);
//        ConditionalPolicy mockConditionalPolicy = Mockito.mock(ConditionalPolicy.class);
//        when(mockConditionalPolicy.getServiceFences()).thenReturn(Arrays.asList(insideThisFence));
//
//        PolicyResult mockConditionalResult = Mockito.mock(PolicyResult.class);
//        when(mockConditionalResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.CONDITIONAL);
//        when(mockConditionalResult.getPendingFactors()).thenReturn(Arrays.asList(Factor.GEOFENCING_CONDITIONAL));
//
//        PolicyResult mockSimpleResult = Mockito.mock(PolicyResult.class);
//        when(mockSimpleResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.TRUE);
//        when(mockSimpleResult.getPendingFactors()).thenReturn(new ArrayList<Factor>());
//
//        when(mockPolicyProcessor.getPolicyResult(nullable(AuthPolicy.class))).thenReturn(mockConditionalResult, mockSimpleResult);
//
//        // When
//        sendRequestToAuthRequestManagerWithPolicyResult(mockConditionalPolicy);
//        when(mockLocationTracker.getCheckCode()).thenReturn(LocationTracker.CHECK_OK);
//        when(mockLocationTracker.stop()).thenReturn(Mockito.mock(Location.class));
//        presenter.setPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, true);
//        presenter.setUserReady();
//        mockHandler.executeRunnablesInOrder();
//
//        // Then
//        verify(mockRequestView, times(1)).showCheckingGeofencing();
//        verify(mockRequestView, times(0)).showError(nullable(AuthRequestError.class));
//        verify(mockRequestView, times(0)).updateAuthMethodProgress(anyInt(), anyInt());
//        verify(mockRequestView, times(1)).showResponding();
//    }
//
//    @Test
//    public void testDynamicRequestReceivedAndFlowCorrectOutsideOfFence() {
//        // Given
//        Fence outsideThisFence = Mockito.mock(Fence.class);
//        when(outsideThisFence.isLocationInside(any(Location.class))).thenReturn(false);
//
//        ConditionalPolicy mockConditionalPolicy = Mockito.mock(ConditionalPolicy.class);
//        when(mockConditionalPolicy.getServiceFences()).thenReturn(Arrays.asList(outsideThisFence));
//
//        PolicyResult mockConditionalResult = Mockito.mock(PolicyResult.class);
//        when(mockConditionalResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.CONDITIONAL);
//        when(mockConditionalResult.getPendingFactors()).thenReturn(Arrays.asList(Factor.GEOFENCING_CONDITIONAL));
//
//        PolicyResult mockSimpleResult = Mockito.mock(PolicyResult.class);
//        when(mockSimpleResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.TRUE);
//        when(mockSimpleResult.getPendingFactors()).thenReturn(new ArrayList<Factor>());
//
//        when(mockPolicyProcessor.getPolicyResult(nullable(AuthPolicy.class))).thenReturn(mockConditionalResult, mockSimpleResult);
//
//        // When
//        sendRequestToAuthRequestManagerWithPolicyResult(mockConditionalPolicy);
//        when(mockLocationTracker.getCheckCode()).thenReturn(LocationTracker.CHECK_OK);
//        when(mockLocationTracker.stop()).thenReturn(Mockito.mock(Location.class));
//        presenter.setPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, true);
//        presenter.setUserReady();
//        mockHandler.executeRunnablesInOrder();
//
//        // Then
//        verify(mockRequestView, times(1)).showCheckingGeofencing();
//        verify(mockRequestView, times(0)).showError(nullable(AuthRequestError.class));
//        verify(mockRequestView, times(0)).updateAuthMethodProgress(anyInt(), anyInt());
//        verify(mockRequestView, times(1)).showResponding();
//    }
//
//    @Test
//    public void testNestedDynamicRequestReceivedAndFlowCorrect() {
//        // Given
//        Fence outsideThisFence = Mockito.mock(Fence.class);
//        when(outsideThisFence.isLocationInside(any(Location.class))).thenReturn(false);
//
//        Fence insideThisFence = Mockito.mock(Fence.class);
//        when(insideThisFence.isLocationInside(any(Location.class))).thenReturn(true);
//
//        ConditionalPolicy mockConditionalPolicy1 = Mockito.mock(ConditionalPolicy.class);
//        when(mockConditionalPolicy1.getServiceFences()).thenReturn(Arrays.asList(outsideThisFence));
//
//        ConditionalPolicy mockConditionalPolicy2 = Mockito.mock(ConditionalPolicy.class);
//        when(mockConditionalPolicy2.getServiceFences()).thenReturn(Arrays.asList(insideThisFence));
//
//        when(mockConditionalPolicy1.getOutsidePolicy()).thenReturn(mockConditionalPolicy2);
//
//        PolicyResult mockConditionalResult1 = Mockito.mock(PolicyResult.class);
//        when(mockConditionalResult1.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.CONDITIONAL);
//        when(mockConditionalResult1.getPendingFactors()).thenReturn(Arrays.asList(Factor.GEOFENCING_CONDITIONAL));
//
//        PolicyResult mockConditionalResult2 = Mockito.mock(PolicyResult.class);
//        when(mockConditionalResult2.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.CONDITIONAL);
//        when(mockConditionalResult2.getPendingFactors()).thenReturn(Arrays.asList(Factor.GEOFENCING_CONDITIONAL));
//
//        PolicyResult mockSimpleResult = Mockito.mock(PolicyResult.class);
//        when(mockSimpleResult.isFulfilled()).thenReturn(PolicyResult.PolicyResultFulfilled.TRUE);
//        when(mockSimpleResult.getPendingFactors()).thenReturn(new ArrayList<Factor>());
//
//        when(mockPolicyProcessor.getPolicyResult(nullable(AuthPolicy.class))).thenReturn(mockConditionalResult1, mockConditionalResult2, mockSimpleResult);
//
//        // When
//        sendRequestToAuthRequestManagerWithPolicyResult(mockConditionalPolicy1);
//        when(mockLocationTracker.getCheckCode()).thenReturn(LocationTracker.CHECK_OK);
//        when(mockLocationTracker.stop()).thenReturn(Mockito.mock(Location.class));
//        presenter.setPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, true);
//        presenter.setUserReady();
//        mockHandler.executeRunnablesInOrder();
//
//        // Then
//        verify(mockRequestView, times(1)).showCheckingGeofencing();
//        verify(mockRequestView, times(0)).showError(nullable(AuthRequestError.class));
//        verify(mockRequestView, times(0)).updateAuthMethodProgress(anyInt(), anyInt());
//        verify(mockRequestView, times(1)).showResponding();
//    }
//
//    private void sendRequestToAuthRequestManagerWithPolicyResult(AuthPolicy policy) {
//        presenter.setVisible(true);
//        when(mockRequestManager.hasPending()).thenReturn(true);
//        when(mockRequestManager.getPendingAuthRequest()).thenReturn(mockRequest);
//        when(mockRequest.getPolicy()).thenReturn(policy);
//        when(mockStorage.getOffsetCachedApiTime()).thenReturn(new StorageOperationResult<>(100L, 0, 0));
//
//        verify(mockRequestManager).registerForEvents((AuthRequestManager.BaseManagerEventCallback[])eventsCaptor.capture());
//        List<Object> callbacks = eventsCaptor.getAllValues();
//        for (Object callback : callbacks) {
//            if (callback instanceof GetAuthRequestEventCallback) {
//                final AuthResponseButton mockView = Mockito.mock(AuthResponseButton.class);
//                IntRequestContract2.View.AuthMethodProvider<AuthResponseButton, View.OnClickListener> mockProvider = new IntRequestContract2.View.AuthMethodProvider<AuthResponseButton, View.OnClickListener>() {
//                    @Override
//                    public AuthResponseButton getView() {
//                        return mockView;
//                    }
//                    @Override
//                    public View.OnClickListener getListener() {
//                        return null;
//                    }
//                };
//                when(mockRequestView.updateInfo(nullable(String.class), nullable(String.class), anyBoolean())).thenReturn(mockProvider);
//                ((GetAuthRequestEventCallback)callback).onEventResult(true, null, null);
//                break;
//            }
//        }
//    }
//}