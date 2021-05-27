package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.content.Intent
import android.os.Bundle

object IntentUtils {
    private const val INTERNAL_INTENT_VERIFICATION_KEY = "QTgcenPQI0"
    private const val INTERNAL_INTENT_VERIFICATION_VAL = "WjH3L5UJkw"
    @JvmStatic
    fun addInternalVerification(i: Intent?) {
        if (i == null) {
            return
        }
        val b = if (i.extras != null) i.extras else Bundle()
        b!!.putString(
                INTERNAL_INTENT_VERIFICATION_KEY,
                INTERNAL_INTENT_VERIFICATION_VAL)
        i.putExtras(b)
    }

    @JvmStatic
    fun isIntentInvalid(i: Intent?): Boolean {
        val trusted = i != null && i.extras != null && INTERNAL_INTENT_VERIFICATION_VAL ==
                i.extras!!.getString(INTERNAL_INTENT_VERIFICATION_KEY)
        return !trusted
    }
}