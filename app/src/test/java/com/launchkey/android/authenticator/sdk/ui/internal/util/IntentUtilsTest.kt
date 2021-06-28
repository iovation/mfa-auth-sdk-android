package com.launchkey.android.authenticator.sdk.ui.internal.util;

import android.content.Intent;

import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class IntentUtilsTest {

    @Test
    public void testNullIntent() {
        Assert.assertTrue(IntentUtils.isIntentInvalid(null));
    }

    @Test
    public void testUntrustedIntent() {
        Intent i = new Intent();
        Assert.assertTrue(IntentUtils.isIntentInvalid(i));
    }

    @Test
    public void testTrustedIntent() {
        Intent i = new Intent();
        IntentUtils.addInternalVerification(i);
        Assert.assertFalse(IntentUtils.isIntentInvalid(i));
    }

    @Test
    public void testIntentWithExtrasHasTrustAppended() {
        Intent i = new Intent();
        i.putExtra("asdf", "fdsa");
        IntentUtils.addInternalVerification(i);
        Assert.assertFalse(IntentUtils.isIntentInvalid(i));
        Assert.assertEquals(i.getExtras().getString("asdf"), "fdsa");
    }
}
