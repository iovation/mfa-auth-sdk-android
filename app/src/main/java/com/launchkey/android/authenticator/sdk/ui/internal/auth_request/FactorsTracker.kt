package com.launchkey.android.authenticator.sdk.ui.internal.auth_request

import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import java.util.*

class FactorsTracker(authMethodIds: List<AuthMethod>?) {
    private val all: Set<AuthMethod> = if (authMethodIds == null) LinkedHashSet() else LinkedHashSet(authMethodIds)
    private val verified: MutableSet<AuthMethod> = HashSet()
    private val currentPending: Iterator<AuthMethod> = all.iterator()
    var currentId: AuthMethod? = if (currentPending.hasNext()) currentPending.next() else null
        private set
    
    operator fun contains(authMethodId: AuthMethod): Boolean {
        return all.contains(authMethodId)
    }
    
    // Geofencing is considered one single type. Decrease by one if both are being verified
    val countForUi: Int
        get() {
            var count = all.size
            
            // Geofencing is considered one single type. Decrease by one if both are being verified
            if (contains(AuthMethod.LOCATIONS) && contains(AuthMethod.GEOFENCING)) {
                count--
            }
            return count
        }
    
    val currentForUi: Int
        get() {
            // Handles 0 factors and all factors verified cases
            if (all.size == verified.size) {
                return all.size
            }
            
            // Start off with amount of 1 instead of a zero-based count/index.
            var amountVerified = verified.size + 1
            
            // End User-set and Service-provided geo-fences are considered the same factor.
            // Decrease by one to offset that "extra" factor when
            // both are to be verified AND at least one has been verified
            // (when none has been verified, it would decrease count of any previous factor verified)
            if (contains(AuthMethod.LOCATIONS)
                && contains(AuthMethod.GEOFENCING)) {
                val localVerified = verified.contains(AuthMethod.LOCATIONS)
                val remoteVerified = verified.contains(AuthMethod.GEOFENCING)
                if (localVerified || remoteVerified) {
                    amountVerified--
                }
            }
            return amountVerified
        }
    
    fun setVerified(authMethod: AuthMethod) {
        if (currentId === authMethod) {
            verified.add(authMethod)
            currentId = if (currentPending.hasNext()) currentPending.next() else null
        }
    }
    
    fun isVerified(authMethod: AuthMethod): Boolean {
        return verified.contains(authMethod)
    }
}