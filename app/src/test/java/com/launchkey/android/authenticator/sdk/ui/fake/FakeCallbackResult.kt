package com.launchkey.android.authenticator.sdk.ui.fake

import java.lang.Exception

sealed class FakeCallbackResult<T>(val result: T) {
    class Success<T>(result: T): FakeCallbackResult<T>(result)
    class Failed<T>(result: T): FakeCallbackResult<T>(result)
}