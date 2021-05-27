package com.launchkey.android.authenticator.sdk.ui.internal.util

import androidx.fragment.app.FragmentActivity
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ActivityIntentExtraProperty<T>(private val key: String) :
    ReadOnlyProperty<FragmentActivity, T> {
    var extra: T? = null
    
    override fun getValue(thisRef: FragmentActivity, property: KProperty<*>): T {
        if (extra == null) {
            extra = thisRef.intent.extras?.get(key) as? T
                ?: throw IllegalArgumentException("Activity $thisRef does not have the argument with key: $key")
        }
        
        return extra!!
    }
}

inline fun <reified T> FragmentActivity.intentExtra(key: String): ReadOnlyProperty<FragmentActivity, T> {
    return ActivityIntentExtraProperty(key)
}