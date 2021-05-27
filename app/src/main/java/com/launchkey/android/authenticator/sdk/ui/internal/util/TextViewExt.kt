package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.widget.TextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal fun TextView.setTextTemporarily(
    lifecycle: Lifecycle,
    temporaryText: String,
    timeInMillis: Long) {
    val initialText = text
    
    val job = CoroutineScope(Dispatchers.Main).launch {
        text = temporaryText
        delay(timeInMillis)
        if (text.toString() == temporaryText) text = initialText
    }
    
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            job.cancel()
        }
    })
}