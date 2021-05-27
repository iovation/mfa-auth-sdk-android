package com.launchkey.android.authenticator.sdk.ui.internal.util

import androidx.fragment.app.Fragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentBundleArgumentProperty<T>(private val key: String) : ReadOnlyProperty<Fragment, T> {
    private var argument: T? = null
    
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if (argument == null) {
            argument = (thisRef.requireArguments().get(key) as? T)
                ?: throw IllegalArgumentException("Fragment $thisRef does not have the argument with key: $key")
        }
        
        return argument!!
    }
}

inline fun <reified T> Fragment.bundleArgument(key: String): ReadOnlyProperty<Fragment, T> {
    return FragmentBundleArgumentProperty(key)
}