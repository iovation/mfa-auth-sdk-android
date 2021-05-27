package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import android.util.SparseIntArray
import androidx.annotation.StringRes
import com.launchkey.android.authenticator.sdk.ui.R
import java.util.*

class PINCodeRequirement internal constructor(@field:StringRes @get:StringRes
                                              @param:StringRes val requirementTextRes: Int, requirement: Requirement) {
    val requirement: Requirement

    interface Requirement {
        fun isValid(secret: List<Int>): Boolean
    }

    companion object {
        // Prevents 123321 from not counting as a sequence break// The two numbers are more than one apart// Determine whether the sequence is increasing, decreasing, or neither// A sequence of length 1 would meet the requirements of this method (though not the AAL2
        // requirement, we expect this to be handled elsewhere)
        // If there's at least two elements continue
// All that is needed is a single digit that occurred only once
        // Too lazy to come up with something more elegant
        // This checks that if a digit is repeated that it is not adjacent to its last occurrence
        val pinCodeRequirements: List<PINCodeRequirement>
            get() = Arrays.asList(
                    PINCodeRequirement(R.string.ioa_sec_pin_add_length_too_short_requirement, object : Requirement {
                        override fun isValid(secret: List<Int>): Boolean {
                            return secret.size >= 8
                        }
                    }),
                    PINCodeRequirement(R.string.ioa_sec_pin_add_repeated_characters_only_not_allowed_requirement, object : Requirement {
                        override fun isValid(secret: List<Int>): Boolean {
                            val arrMap = SparseIntArray(10)
                            for (i in secret.indices) {
                                // Too lazy to come up with something more elegant
                                // This checks that if a digit is repeated that it is not adjacent to its last occurrence
                                if (i > 0 && secret[i - 1] != secret[i] && arrMap[secret[i], Int.MIN_VALUE] != Int.MIN_VALUE) {
                                    return true
                                }
                                arrMap.put(secret[i], arrMap[secret[i], 0] + 1)
                            }
                            for (i in 0 until arrMap.size()) {
                                // All that is needed is a single digit that occurred only once
                                if (arrMap.valueAt(i) == 1) {
                                    return true
                                }
                            }
                            return false
                        }
                    }),
                    PINCodeRequirement(R.string.ioa_sec_pin_add_characters_incrementing_or_decrementing_requirement, object : Requirement {
                        override fun isValid(secret: List<Int>): Boolean {
                            // A sequence of length 1 would meet the requirements of this method (though not the AAL2
                            // requirement, we expect this to be handled elsewhere)
                            var sequenceBreakDetected = secret.size < 2
                            // If there's at least two elements continue
                            if (!sequenceBreakDetected) {
                                // Determine whether the sequence is increasing, decreasing, or neither
                                var sequenceDirection = secret[1] - secret[0]
                                for (i in 2 until secret.size) {
                                    val currentVal = secret[i]
                                    val previousVal = secret[i - 1]
                                    val newSequenceDirection = currentVal - previousVal
                                    if (newSequenceDirection > 1 || newSequenceDirection < -1) {
                                        // The two numbers are more than one apart
                                        return true
                                    } else if (sequenceDirection > 0 && newSequenceDirection < 0 || sequenceDirection < 0 && newSequenceDirection > 0) {
                                        sequenceBreakDetected = true
                                        break
                                    } else if (newSequenceDirection != 0) {
                                        // Prevents 123321 from not counting as a sequence break
                                        sequenceDirection = newSequenceDirection
                                    }
                                }
                            }
                            return sequenceBreakDetected
                        }
                    })
            )
    }

    init {
        this.requirement = Objects.requireNonNull(requirement)
    }
}