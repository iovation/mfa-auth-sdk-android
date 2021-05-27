/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui

import android.content.Context
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIConfig.Builder
import com.launchkey.android.authenticator.sdk.ui.theme.AuthenticatorTheme

/**
 * Object created via [Builder] with all necessary arguments for initializing the
 * Authenticator SDK via [AuthenticatorUIManager.initialize].
 */
class AuthenticatorUIConfig  // Kept private to force builder pattern
private constructor(@param:StyleRes private val themeRes: Int,
                    private val themeObj: AuthenticatorTheme,
                    private val customFontAssetTtf: String?,
                    private val allowSecurityChangesWhenUnlinked: Boolean) {
    /**
     * @return true if End Users can make changes in the Security view even when unlinked.
     */
    fun areSecurityChangesAllowedWhenUnlinked(): Boolean {
        return allowSecurityChangesWhenUnlinked
    }

    /**
     * @return true if it has custom fonts set, false otherwise.
     */
    fun hasCustomFont(): Boolean {
        return customFontAssetTtf != null && !customFontAssetTtf.trim { it <= ' ' }.isEmpty()
    }

    /**
     * Custom font string.
     *
     * @return the String
     */
    fun customFont(): String? {
        return customFontAssetTtf
    }

    /**
     * @return theme applied to the views included in the Authenticator SDK.
     */
    @StyleRes
    fun theme(): Int {
        return themeRes
    }

    fun themeObj(): AuthenticatorTheme {
        return themeObj
    }

    /**
     * Class to build an AuthenticatorConfig object passed to AuthenticatorManager
     * during initialization.
     */
    class Builder
    /**
     * Instantiates a new Builder.
     */
    {
        @StyleRes
        private var themeRes: Int? = null
        private var themeObj: AuthenticatorTheme? = null
        private var customFontAssetTtf: String? = null
        private var allowSecurityChangesWhenUnlinked = DEFAULT_SECURITY_CHANGES_UNLINKED

        /**
         * @param allow true if the End User should be allowed to make changes
         * in the Security view even when unlinked.
         * @return [Builder]
         */
        fun allowSecurityChangesWhenUnlinked(allow: Boolean): Builder {
            allowSecurityChangesWhenUnlinked = allow
            return this
        }

        /**
         * @param assetTtfName name of the TTF file in the assets folder for the custom font to
         * be applied.
         * @return the builder
         */
        fun customFont(assetTtfName: String?): Builder {
            customFontAssetTtf = assetTtfName
            return this
        }

        /**
         * Theme that will be applied to all Views included in the
         * Authenticator SDK.
         * In order to override specific elements of the theme,
         * declare a custom theme setting the default Authenticator
         * theme ("AuthenticatorTheme") as the parent one and specify
         * the individual values.
         * @param themeRes  Reference to the custom theme used to override
         * specific elements.
         * @return          the builder.
         */
        fun theme(@StyleableRes themeRes: Int): Builder {
            this.themeRes = themeRes
            return this
        }

        /**
         * Theme that will be applied to all Views included in the
         * Authenticator SDK at runtime.
         * In order to override specific elements of the theme,
         * rely on [AuthenticatorTheme.Builder] to declare its properties.
         * @param themeObj  Reference to the custom theme used to override
         * specific elements.
         * @return          the builder.
         */
        fun theme(themeObj: AuthenticatorTheme?): Builder {
            this.themeObj = themeObj
            return this
        }

        /**
         * Build the config object
         *
         * @param context Application context that is used internally to theme objects
         * @return the AuthenticatorConfig built object.
         */
        fun build(context: Context?): AuthenticatorUIConfig {
            var themeRes = themeRes
            var themeObj = themeObj
            if (themeRes == null) {
                if (themeObj == null) {
                    themeRes = DEFAULT_THEME_RES
                    themeObj = AuthenticatorTheme.Builder(context!!, DEFAULT_THEME_RES).build()
                } else {
                    themeRes = themeObj.themeRes
                }
            } else {
                if (themeObj == null) {
                    themeObj = AuthenticatorTheme.Builder(context!!, themeRes).build()
                }
            }
            return AuthenticatorUIConfig(themeRes, themeObj, customFontAssetTtf, allowSecurityChangesWhenUnlinked)
        }

        companion object {
            private const val DEFAULT_SECURITY_CHANGES_UNLINKED = false
            private val DEFAULT_THEME_RES = R.style.AuthenticatorTheme
        }
    }
}