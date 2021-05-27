package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.locations

import com.launchkey.android.authenticator.sdk.core.auth_method_management.LocationsManager
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.ItemAdapter

class LocationItem(
        val location: LocationsManager.StoredLocation,
        override val now: Long = System.currentTimeMillis()
) : ItemAdapter.Item<LocationsManager.StoredLocation> {

    override val item: LocationsManager.StoredLocation
        get() = location
    override val name: String
        get() = location.name
    override val isPendingRemoval: Boolean
        get() = location.isPendingRemoval
    override val isActive: Boolean
        get() = location.isActive
    override val timeRemainingUntilRemoved: Long
        get() = location.timeRemainingUntilRemoved
    override val timeRemainingUntilActivated: Long
        get() = location.timeRemainingUntilActivated

}