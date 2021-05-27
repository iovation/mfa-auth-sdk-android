package com.launchkey.android.authenticator.sdk.ui.internal.util

import com.launchkey.android.authenticator.sdk.core.util.Disposable
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Job

fun <T> Disposable.disposeWhenCancelled(continuation: CancellableContinuation<T>) {
    continuation.invokeOnCancellation {
        dispose()
    }
}

fun Disposable.disposeWhenCompleted(job: Job) {
    job.invokeOnCompletion {
        dispose()
    }
}
