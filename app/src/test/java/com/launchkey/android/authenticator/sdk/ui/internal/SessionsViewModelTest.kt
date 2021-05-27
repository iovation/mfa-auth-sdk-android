package com.launchkey.android.authenticator.sdk.ui.internal

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.launchkey.android.authenticator.sdk.core.authentication_management.Session
import com.launchkey.android.authenticator.sdk.ui.TestCoroutineRule
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeCallbackResult
import com.launchkey.android.authenticator.sdk.ui.fragment.SessionsViewModel
import com.launchkey.android.authenticator.sdk.ui.fragment.SessionsViewModel.SessionState
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class SessionsViewModelTest {
    private val fakeAuthenticatorManager = Mockito.spy(FakeAuthenticatorManager())
    private val sessionsViewModel by lazy {
        SessionsViewModel(fakeAuthenticatorManager, Dispatchers.Unconfined, null)
    }

    private val viewModelState: SessionState
        get() = sessionsViewModel.sessionState.value!!

    private val validSession = object : Session {
        override fun getId(): String = ""

        override fun getName(): String = id

        override fun getIconUrl(): String = id

        override fun getCreatedAtMillis(): Long = 0
    }

    @get:Rule
    var testCoroutineRule = TestCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        fakeAuthenticatorManager.expectedGetSessionsEventCallbackResult =
            FakeCallbackResult.Success(ArrayList<Session>())
    }

    @Test
    fun `given a successful getSessions call, when refreshing sessions, then session state should be GetSessionsSuccess`() =
        testCoroutineRule.runBlockingTest {
            fakeAuthenticatorManager.expectedGetSessionsEventCallbackResult =
                FakeCallbackResult.Success(listOf<Session>())

            sessionsViewModel.refreshSessions()
            assertTrue(viewModelState is SessionState.GetSessionsSuccess)
        }

    @Test
    fun `given a failed getSessions call, when refreshing session, then session state should be Failed`() =
        testCoroutineRule.runBlockingTest {
            fakeAuthenticatorManager.expectedGetSessionsEventCallbackResult =
                FakeCallbackResult.Failed(Exception())

            sessionsViewModel.refreshSessions()
            assertTrue(viewModelState is SessionState.Failed)
        }

    @Test
    fun `given a successful endSession call, when ending a single session, then session state should be EndSessionSuccess`() =
        testCoroutineRule.runBlockingTest {
            fakeAuthenticatorManager.expectedEndSessionEventCallbackResult =
                FakeCallbackResult.Success(validSession)

            sessionsViewModel.endSession(validSession)
            assertTrue(viewModelState is SessionState.EndSessionSuccess)
        }

    @Test
    fun `given a failed endSession call, when ending a single session, then session state should be Failed`() =
        testCoroutineRule.runBlockingTest {
            fakeAuthenticatorManager.expectedEndSessionEventCallbackResult =
                FakeCallbackResult.Failed(Exception())

            sessionsViewModel.endSession(validSession)
            assertTrue(viewModelState is SessionState.Failed)
        }
}