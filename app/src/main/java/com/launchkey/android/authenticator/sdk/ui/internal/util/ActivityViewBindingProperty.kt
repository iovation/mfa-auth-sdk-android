package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.view.View
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ActivityViewBindingProperty<T : ViewBinding>(private val viewBinder: (View) -> T, private val layoutId: Int) : ReadOnlyProperty<FragmentActivity, T> {
    private var binding: T? = null
    @MainThread
    override fun getValue(thisRef: FragmentActivity, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        thisRef.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                this@ActivityViewBindingProperty.binding = null
            }
        })
        return viewBinder(thisRef.window.decorView.findViewById(layoutId)!!).also { this.binding = it }
    }
}

inline fun <reified T : ViewBinding> FragmentActivity.viewBinding(noinline viewBinder: (View) -> T, layoutId: Int): ReadOnlyProperty<FragmentActivity, T> {
    return ActivityViewBindingProperty(viewBinder, layoutId)
}