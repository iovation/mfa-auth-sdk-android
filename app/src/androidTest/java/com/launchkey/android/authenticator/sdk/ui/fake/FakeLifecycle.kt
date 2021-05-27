package com.launchkey.android.authenticator.sdk.ui.fake

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver

class FakeLifecycle() : Lifecycle() {
    val observers: MutableList<LifecycleObserver> = mutableListOf()
    var state: State = State.INITIALIZED
    var event: Event? = null
        set(value) {
            field = value
            for (observer in observers) {
                (observer as LifecycleEventObserver).onStateChanged({ this }, event!!)
            }
        }

    override fun addObserver(observer: LifecycleObserver) {
        observers.add(observer)
    }

    override fun removeObserver(observer: LifecycleObserver) {
        observers.remove(observer);
    }

    override fun getCurrentState(): State {
        return state!!
    }

}