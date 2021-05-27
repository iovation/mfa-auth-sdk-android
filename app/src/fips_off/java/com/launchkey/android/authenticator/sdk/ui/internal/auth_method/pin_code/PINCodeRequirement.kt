package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import androidx.annotation.StringRes
import java.util.*

class PINCodeRequirement internal constructor(@field:StringRes @get:StringRes
                                              @param:StringRes val requirementTextRes: Int, requirement: Requirement) {
    val requirement: Requirement

    interface Requirement {
        fun isValid(secret: List<Int?>): Boolean
    }

    companion object {
        // In the event that we want to display this in the non-fips sdk in the future
//        return Arrays.asList(
//                new PINCodeRequirement(R.string.ioa_sec_pin_add_length_too_short_requirement, new Requirement() {
//                    @Override
//                    public boolean isValid(@NonNull final List<Integer> secret) {
//                        return secret.size() >= 4;
//                    }
//                }));
        val pinCodeRequirements: List<PINCodeRequirement>
            get() = ArrayList()
        // In the event that we want to display this in the non-fips sdk in the future
//        return Arrays.asList(
//                new PINCodeRequirement(R.string.ioa_sec_pin_add_length_too_short_requirement, new Requirement() {
//                    @Override
//                    public boolean isValid(@NonNull final List<Integer> secret) {
//                        return secret.size() >= 4;
//                    }
//                }));
    }

    init {
        this.requirement = Objects.requireNonNull(requirement)
    }
}