package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.ItemAdapter

class WearableItem(
        val wearable: WearablesManager.Wearable,
        override val now: Long = System.currentTimeMillis()
) : ItemAdapter.Item<WearablesManager.Wearable> {

    override val item: WearablesManager.Wearable
        get() = wearable
    override val name: String
        get() = wearable.name
    override val isPendingRemoval: Boolean
        get() = wearable.isPendingRemoval
    override val isActive: Boolean
        get() = wearable.isActive
    override val timeRemainingUntilRemoved: Long
        get() = wearable.timeRemainingUntilRemoved
    override val timeRemainingUntilActivated: Long
        get() = wearable.timeRemainingUntilActivated

}