package com.launchkey.android.authenticator.sdk.ui.internal.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T> {
    /**
     * Creates a MutableLiveData initialized with the given `value`.
     *
     * @param value initial value
     */
    constructor(value: T) : super(value) {}

    /**
     * Creates a MutableLiveData with no value assigned to it.
     */
    constructor() : super() {}

    private val map: MutableMap<Observer<*>, AtomicBoolean> = HashMap()
    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        owner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    map.remove(observer)
                    owner.lifecycle.removeObserver(this)
                }
            }
        })
        map[observer] = AtomicBoolean(false)
        // Observe the internal MutableLiveData
        super.observe(owner, { t ->
            val pending = map[observer]
            if (pending != null && pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        for ((_, value) in map) {
            value.set(true)
        }
        super.setValue(t)
    }
}