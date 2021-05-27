package com.launchkey.android.authenticator.sdk.ui.fake

import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure

data class FakeVerificationFailureResult(val failure: AuthMethodFailure,
                                         val unlinkTriggered: Boolean,
                                         val unlinkWarningTriggered: Boolean,
                                         val attemptsRemaining: Int? = null)