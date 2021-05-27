package com.launchkey.android.authenticator.sdk.ui.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.launchkey.android.authenticator.sdk.core.auth_request_management.AuthRequestManager
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorConfig
import com.launchkey.android.authenticator.sdk.core.authentication_management.AuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIConfig
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthRequestManager
import com.launchkey.android.authenticator.sdk.ui.fake.FakeAuthenticatorManager
import com.launchkey.android.authenticator.sdk.ui.internal.util.IntentUtils
import org.junit.After
import org.junit.Before
import org.mockito.Mockito
import java.lang.reflect.Field

open class BaseTest(authenticatorManager: FakeAuthenticatorManager, authRequestManager: FakeAuthRequestManager, authenticatorUIManager: AuthenticatorUIManager? = null) {

    protected lateinit var context: Context

    private val realARManager: AuthRequestManager = AuthRequestManager.instance
    private val realAManager: AuthenticatorManager = AuthenticatorManager.instance
    private val realAUIManager: AuthenticatorUIManager = AuthenticatorUIManager.instance

    protected val fakeARManager: FakeAuthRequestManager
    protected val fakeAManager: FakeAuthenticatorManager
    protected val fakeAUIManager: AuthenticatorUIManager

    init {
        fakeAManager = authenticatorManager
        fakeARManager = authRequestManager
        fakeAUIManager = Mockito.spy(realAUIManager)
    }

    @Before
    fun initApp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        val spyARManager: AuthRequestManager = fakeARManager
        setSpyAuthRequestManager(spyARManager)

        val spyAuthenticatorManager = fakeAManager
        setSpyAuthenticatorManager(spyAuthenticatorManager)

        val spyAuthenticatorUIManager = fakeAUIManager
        setSpyAuthenticatorUIManager(spyAuthenticatorUIManager)

        val spyAuthenticatorManagerConfig = Mockito.spy(AuthenticatorConfig.Builder().build())
        setSpyAuthenticatorManagerConfig(spyAuthenticatorManager, spyAuthenticatorManagerConfig)

        val spyAuthenticatorUIManagerConfig = Mockito.spy(AuthenticatorUIConfig.Builder().build(context))
        setSpyAuthenticatorUIManagerConfig(spyAuthenticatorUIManager, spyAuthenticatorUIManagerConfig)
    }

    @After
    fun resetApp() {
        setSpyAuthenticatorManager(realAManager)
        setSpyAuthenticatorUIManager(realAUIManager)
        setSpyAuthRequestManager(realARManager)
    }

    protected fun <T : Activity> launchActivityWithInternalVerification(clazz: Class<T>) : ActivityScenario<T> {
        val i = Intent(context, clazz)
        IntentUtils.addInternalVerification(i)
        return ActivityScenario.launch(i)
    }

    private fun setSpyAuthRequestManager(authRequestManager: AuthRequestManager?) {
        setField(AuthRequestManager::class.java.getDeclaredField("instance"), authRequestManager)
    }

    private fun setSpyAuthenticatorManager(authenticatorManager: AuthenticatorManager?) {
        setField(AuthenticatorManager::class.java.getDeclaredField("instance"), authenticatorManager)
    }

    private fun setSpyAuthenticatorUIManager(authenticatorUIManager: AuthenticatorUIManager?) {
        setField(AuthenticatorUIManager::class.java.getDeclaredField("sInstance"), authenticatorUIManager)
    }

    private fun <T: Any> setField(field: Field, value: T?) {
        field.isAccessible = true
        field.set(null, value)
        field.isAccessible = false
    }

    private fun setSpyAuthenticatorManagerConfig(realAuthenticatorManager: FakeAuthenticatorManager, authenticatorManagerConfig: AuthenticatorConfig) {
        realAuthenticatorManager.authenticatorConfig = authenticatorManagerConfig
    }

    private fun setSpyAuthenticatorUIManagerConfig(realAuthenticatorUIManager: AuthenticatorUIManager, authenticatorManagerUIConfig: AuthenticatorUIConfig) {
        Mockito.`when`(realAuthenticatorUIManager.config).thenReturn(authenticatorManagerUIConfig)
    }
}