package com.launchkey.android.authenticator.sdk.ui.internal.auth_request.fragment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequestResponse
import com.launchkey.android.authenticator.sdk.core.auth_request_management.DenialReason
import com.launchkey.android.authenticator.sdk.core.auth_request_management.ServiceProfile
import com.launchkey.android.authenticator.sdk.core.exception.AuthRequestCancelledException
import com.launchkey.android.authenticator.sdk.core.failure.Failure
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.auth.PINCodeWrongFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_request.config.AuthRequestConfigFailure
import com.launchkey.android.authenticator.sdk.ui.TestCoroutineRule
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthRequestManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeCallbackResult
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestFragmentViewModel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestFragmentViewModel.AuthRequestState
import com.launchkey.android.authenticator.sdk.ui.internal.auth_request.AuthRequestFragmentViewModel.FetchState
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.Mockito.`when` as whenever

class AuthRequestFragmentViewModelTest {
    private lateinit var fragmentViewModel: AuthRequestFragmentViewModel
    private val fakeAuthRequestManager: FakeAuthRequestManager = spy(FakeAuthRequestManager())
    private val mockedObserver: Observer<AuthRequestState> =
        mock(Observer::class.java) as Observer<AuthRequestState>
    
    private val fakeAuthRequest = object : AuthRequest {
        private var denialReasons: List<DenialReason> = emptyList()
        
        override fun getId(): String = ""
        override fun hasContext(): Boolean = true
        override fun getContext(): String = ""
        override fun getTitle(): String = ""
        override fun getExpiresAtMillis(): Long = 0L
        override fun getCreatedAtMillis(): Long = 0L
        override fun getServiceProfile(): ServiceProfile = object : ServiceProfile {
            override fun hasName(): Boolean = true
            override fun getName(): String = ""
        }
        
        override fun getDenialReasons(): List<DenialReason> = denialReasons
        fun setDenialReasons(reasons: List<DenialReason>) {
            denialReasons = reasons
        }
    }
    
    private val fakeAuthRequestResponse = object : AuthRequestResponse {
        var expectedResult: AuthRequestResponse.Result = AuthRequestResponse.Result.APPROVED
        var expectedFailure: Failure? = null
        
        override fun getResult(): AuthRequestResponse.Result = expectedResult
        override fun getFailure(): Failure? = expectedFailure
    }
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    
    @Before
    fun setup() {
        fakeAuthRequestManager.expectedGetAuthRequestEventCallbackResult = FakeCallbackResult.Success(fakeAuthRequest)
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Success(fakeAuthRequestResponse)
        fragmentViewModel =
            AuthRequestFragmentViewModel(
                fakeAuthRequestManager,
                testCoroutineRule.testCoroutineDispatcher,
                null
            )
        fragmentViewModel.authRequestState.observeForever(mockedObserver)
    }
    
    @After
    fun teardown() {
        fragmentViewModel.authRequestState.removeObserver(mockedObserver)
    }
    
    @Test
    fun `test when ViewModel is cleared should unregister for events`() {
        fragmentViewModel.onCleared()
        fakeAuthRequestManager.thenUnregisterForEventsWasCalled()
    }
    
    @Test
    fun `test when accepting an auth request that should auto send should move to Sending`() {
        fakeAuthRequestManager.expectedAcceptAndSendResult = true
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Success(mock(AuthRequestResponse::class.java).also {
            whenever(it.result).thenReturn(AuthRequestResponse.Result.FAILED)
            whenever(it.failure).thenReturn(mock(AuthRequestConfigFailure::class.java))
        })
        fragmentViewModel.acceptAuthRequest()
        assert(fragmentViewModel.authRequestState.value is AuthRequestState.Sending)
    }
    
    @Test
    fun `test when accepting an auth request that should not auto send should move to Accepted`() {
        fakeAuthRequestManager.expectedAcceptAndSendResult = false
        fragmentViewModel.acceptAuthRequest()
        assert(fragmentViewModel.authRequestState.value is AuthRequestState.Accepted)
    }
    
    @Test
    fun `test when failing to check for auth request should move to Failed`() {
        fakeAuthRequestManager.expectedGetAuthRequestEventCallbackResult = FakeCallbackResult.Failed(mock(Exception::class.java))
        fragmentViewModel.checkForAuthRequest()
        assert(fragmentViewModel.fetchState.value is FetchState.Failed)
    }
    
    @Test
    fun `test given a null auth request when checking request should move to ReceivedEmpty state`() {
        fakeAuthRequestManager.expectedGetAuthRequestEventCallbackResult = FakeCallbackResult.Success(null)
        fragmentViewModel.checkForAuthRequest()
        assert(fragmentViewModel.fetchState.value is FetchState.FetchedEmptyAuthRequest)
    }
    
    @Test
    fun `test given a valid auth request when checking should move to FetchedAuthRequest`() {
        assertThat(fragmentViewModel.fetchState.value, instanceOf(FetchState.FetchedAuthRequest::class.java))
    }
    
    @Test
    fun `test given a valid auth request and current auth request is null when checking should move to FetchedAuthRequest`() {
        fakeAuthRequestManager.expectedGetAuthRequestEventCallbackResult = FakeCallbackResult.Success(fakeAuthRequest)
        assertThat(fragmentViewModel.fetchState.value, instanceOf(FetchState.FetchedAuthRequest::class.java))
    }
    
    @Test
    fun `test given a valid auth request and current auth request is different when checking should move to FetchedNewerAuthRequest`() {
        val newAuthRequest = object : AuthRequest {
            override fun getId(): String = "new"
            override fun hasContext(): Boolean = fakeAuthRequest.hasContext()
            override fun getContext(): String = fakeAuthRequest.context
            override fun getTitle(): String = fakeAuthRequest.title
            override fun getExpiresAtMillis(): Long = fakeAuthRequest.expiresAtMillis
            override fun getCreatedAtMillis(): Long = fakeAuthRequest.createdAtMillis
            override fun getServiceProfile(): ServiceProfile = fakeAuthRequest.serviceProfile
            override fun getDenialReasons(): List<DenialReason> = fakeAuthRequest.denialReasons
        }
        
        fakeAuthRequestManager.expectedGetAuthRequestEventCallbackResult = FakeCallbackResult.Success(newAuthRequest)
        fragmentViewModel.checkForAuthRequest()
        
        assert(fragmentViewModel.fetchState.value is FetchState.FetchedNewerAuthRequest)
    }
    
    @Test
    fun `test given an auth request with no denial reasons when denying should move to Responded state`() {
        fakeAuthRequest.denialReasons = emptyList()
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Success(fakeAuthRequestResponse)
        fragmentViewModel.denyAuthRequest()
        fakeAuthRequestManager.thenDenyAndSendWasCalledWith(fakeAuthRequest, null)
        
        assertThat(fragmentViewModel.authRequestState.value, instanceOf(AuthRequestState.Responded::class.java))
    }
    
    @Test
    fun `test given an auth request with denial reasons when denying should move to Denying state`() {
        fakeAuthRequest.denialReasons = listOf(object : DenialReason {
            override fun getId(): String = ""
            override fun getMessage(): String = ""
        })
        
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Success(fakeAuthRequestResponse)
        fragmentViewModel.denyAuthRequest()
        assertThat(fragmentViewModel.authRequestState.value, instanceOf(AuthRequestState.Denying::class.java))
    }
    
    @Test
    fun `test given an auth request and denial reason when denying should move to Sending and should call denyAndSend`() {
        val mockDenialReason = mock(DenialReason::class.java)
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Success(mock(AuthRequestResponse::class.java).also {
            whenever(it.result).thenReturn(AuthRequestResponse.Result.DENIED)
        })
        fragmentViewModel.denyAuthRequestWithDenialReason(fakeAuthRequest, mockDenialReason)
        fakeAuthRequestManager.thenDenyAndSendWasCalledWith(fakeAuthRequest, mockDenialReason)
        verify(mockedObserver).onChanged(any(AuthRequestState.Sending::class.java))
    }
    
    @Test
    fun `test given an auth request when sending should move to Sending and should call send`() {
        fragmentViewModel.sendAuthRequest()
        verify(mockedObserver).onChanged(any(AuthRequestState.Sending::class.java))
    }
    
    @Test
    fun `test calling setAuthRequestExpired should move to AuthRequestFailed)`() {
        fragmentViewModel.setAuthRequestExpired()
        assert(fragmentViewModel.authRequestState.value is AuthRequestState.AuthRequestFailed)
    }
    
    @Test
    fun `test given an auth request with no auth methods to verify when starting to verify auth methods should move to Responded(true)`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = emptyList()
        fragmentViewModel.startVerifyingAuthMethods()
        assertTrue((fragmentViewModel.authRequestState.value as AuthRequestState.Responded).isAuthorized!!)
    }
    
    @Test
    fun `test given an auth request with an auth method to verify when starting to verify auth methods should move to Verifying`() {
        fakeAuthRequestManager.expectedAuthMethodsToVerify = listOf(AuthMethod.PIN_CODE)
        fragmentViewModel.startVerifyingAuthMethods()
        assert(fragmentViewModel.authRequestState.value is AuthRequestState.Verifying)
    }
    
    @Test
    fun `test given a successful push event occurs should move to PushReceived`() {
        fakeAuthRequestManager.expectedAuthRequestPushReceivedEventResult = FakeCallbackResult.Success(null)
        fakeAuthRequestManager.simulatePushReceived()
        assert(fragmentViewModel.fetchState.value is FetchState.PushReceived)
    }
    
    @Test
    fun `test given a failed push event should move to Failed`() {
        fakeAuthRequestManager.expectedAuthRequestPushReceivedEventResult = FakeCallbackResult.Failed(mock(Exception::class.java))
        fakeAuthRequestManager.simulatePushReceived()
        assert(fragmentViewModel.fetchState.value is FetchState.Failed)
    }
    
    @Test
    fun `test given an authorized auth response should move to Responded(true)`() {
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Success(mock(AuthRequestResponse::class.java).also {
            whenever(it.result).thenReturn(AuthRequestResponse.Result.APPROVED)
        })
        fakeAuthRequestManager.send(fakeAuthRequest)
        assertTrue((fragmentViewModel.authRequestState.value as AuthRequestState.Responded).isAuthorized!!)
    }
    
    @Test
    fun `test given denied auth response should move to Responded(false)`() {
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Success(mock(AuthRequestResponse::class.java).also {
            whenever(it.result).thenReturn(AuthRequestResponse.Result.DENIED)
        })
        fakeAuthRequestManager.send(fakeAuthRequest)
        
        assertFalse((fragmentViewModel.authRequestState.value as AuthRequestState.Responded).isAuthorized!!)
    }
    
    @Test
    fun `test given an failed response should move to AuthRequestFailed`() {
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Success(mock(AuthRequestResponse::class.java).also {
            whenever(it.result).thenReturn(AuthRequestResponse.Result.FAILED)
            whenever(it.failure).thenReturn(mock(PINCodeWrongFailure::class.java))
        })
        fakeAuthRequestManager.send(fakeAuthRequest)
        
        assert(fragmentViewModel.authRequestState.value is AuthRequestState.AuthRequestFailed)
    }
    
    @Test
    fun `test given a canceled auth request response should move to AuthRequestFailed`() {
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Failed(mock(AuthRequestCancelledException::class.java))
        fakeAuthRequestManager.send(fakeAuthRequest)
        assert(fragmentViewModel.authRequestState.value is AuthRequestState.AuthRequestFailed)
    }
    
    @Test
    fun `test given a failed response with a non canceled exception should move to Failed`() {
        fakeAuthRequestManager.expectedAuthRequestResponseEventResult = FakeCallbackResult.Failed(mock(Exception::class.java))
        fakeAuthRequestManager.send(fakeAuthRequest)
        assert(fragmentViewModel.authRequestState.value is AuthRequestState.Failed)
    }
}