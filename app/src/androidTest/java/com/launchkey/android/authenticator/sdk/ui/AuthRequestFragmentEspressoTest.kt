package com.launchkey.android.authenticator.sdk.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.annotation.UiThreadTest
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequest
import com.launchkey.android.authenticator.sdk.core.auth_request_management.ServiceProfile
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorConfig
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthRequestManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeCallbackResult
import com.launchkey.android.authenticator.sdk.ui.test.BaseTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthRequestFragmentEspressoTest : BaseTest(FakeAuthenticatorManager(), FakeAuthRequestManager()) {
    private lateinit var authRequestFragment: AuthRequestFragment
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @UiThreadTest
    @Before
    fun setup() {
        authRequestFragment = AuthRequestFragment()
    }
    
    @Test
    fun authRequestScreenShowsRequestTitle() {
        fakeAManager.expectedIsDeviceLinked = true
        fakeAManager.authenticatorConfig = AuthenticatorConfig.Builder().build()
        fakeARManager.handler = {
            authRequestFragment.activity!!.runOnUiThread { it() }
        }
        fakeARManager.expectedAuthMethodsToVerify = Collections.emptyList<AuthMethod>()
        val mockServiceProfile = Mockito.mock(ServiceProfile::class.java)
        Mockito.`when`(mockServiceProfile.name).thenReturn("some_title1")
        val mockRequest = Mockito.mock(AuthRequest::class.java)
        Mockito.`when`(mockRequest.id).thenReturn("some_id")
        Mockito.`when`(mockRequest.title).thenReturn("some_title2")
        Mockito.`when`(mockRequest.context).thenReturn("some_context")
        Mockito.`when`(mockRequest.createdAtMillis).thenReturn(System.currentTimeMillis())
        Mockito.`when`(mockRequest.expiresAtMillis).thenReturn(System.currentTimeMillis() + 10000)
        Mockito.`when`(mockRequest.serviceProfile).thenReturn(mockServiceProfile)
        fakeARManager.expectedGetAuthRequestEventCallbackResult = FakeCallbackResult.Success(mockRequest)
        launchFragmentInContainer { authRequestFragment }
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withText("some_title2")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun authRequestScreenShowsRequestDetail() {
        fakeAManager.expectedIsDeviceLinked = true
        fakeAManager.authenticatorConfig = AuthenticatorConfig.Builder().build()
        fakeARManager.handler = {
            authRequestFragment.activity!!.runOnUiThread { it() }
        }
        fakeARManager.expectedAuthMethodsToVerify = Collections.emptyList<AuthMethod>()
        val mockServiceProfile = Mockito.mock(ServiceProfile::class.java)
        Mockito.`when`(mockServiceProfile.name).thenReturn("some_title")
        val mockRequest = Mockito.mock(AuthRequest::class.java)
        Mockito.`when`(mockRequest.id).thenReturn("some_id")
        Mockito.`when`(mockRequest.title).thenReturn("12345")
        Mockito.`when`(mockRequest.context).thenReturn("asdfasdf2")
        Mockito.`when`(mockRequest.createdAtMillis).thenReturn(System.currentTimeMillis())
        Mockito.`when`(mockRequest.expiresAtMillis).thenReturn(System.currentTimeMillis() + 10000)
        Mockito.`when`(mockRequest.serviceProfile).thenReturn(mockServiceProfile)
        fakeARManager.expectedGetAuthRequestEventCallbackResult = FakeCallbackResult.Success(mockRequest)
        launchFragmentInContainer { authRequestFragment }
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withText("asdfasdf2")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}