package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.content.Intent
import com.launchkey.android.authenticator.sdk.ui.internal.util.IntentUtils.addInternalVerification
import com.launchkey.android.authenticator.sdk.ui.internal.util.IntentUtils.isIntentInvalid
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
class IntentUtilsTest {
    @Test
    fun testNullIntent() {
        Assert.assertTrue(isIntentInvalid(null))
    }
    
    @Test
    fun testUntrustedIntent() {
        val i = Intent()
        Assert.assertTrue(isIntentInvalid(i))
    }
    
    @Test
    fun testTrustedIntent() {
        val i = Intent()
        addInternalVerification(i)
        Assert.assertFalse(isIntentInvalid(i))
    }
    
    @Test
    fun testIntentWithExtrasHasTrustAppended() {
        val i = Intent()
        i.putExtra("asdf", "fdsa")
        addInternalVerification(i)
        Assert.assertFalse(isIntentInvalid(i))
        Assert.assertEquals(i.extras!!.getString("asdf"), "fdsa")
    }
}