package com.launchkey.android.authenticator.sdk.ui

import android.content.Context
import android.content.Intent
import com.launchkey.android.authenticator.sdk.ui.internal.linking.LinkActivity
import com.launchkey.android.authenticator.sdk.ui.internal.util.IntentUtils.addInternalVerification
import com.launchkey.android.authenticator.sdk.ui.internal.util.TypefaceUtil.overrideFont

class AuthenticatorUIManager private constructor(context: Context) {
    /**
     * @return the [AuthenticatorUIConfig] object passed during initialization.
     */
    var config: AuthenticatorUIConfig
        private set
    private val context: Context

    /**
     * Method to initialize the Authenticator SDK.
     * @param config Config object built via
     * [AuthenticatorUIConfig.Builder]
     * with required and configurable properties.
     */
    fun initialize(config: AuthenticatorUIConfig) {
        this.config = config

        // Override font
        if (config.hasCustomFont()) {
            overrideFont(context, "NORMAL", config.customFont()!!)
            overrideFont(context, "DEFAULT", config.customFont()!!)
            overrideFont(context, "SERIF", config.customFont()!!)
            overrideFont(context, "MONOSPACE", config.customFont()!!)
            overrideFont(context, "SANS_SERIF", config.customFont()!!)
            overrideFont(context, "SANS", config.customFont()!!)
        }
    }

    /**
     * Method that will start the Security Activity to have the End User
     * set their own Security Factors.
     *
     * @param context Context used to bring up the Activity.
     */
    fun startSecurityActivity(context: Context) {
        val security = Intent(context, SecurityActivity::class.java)
        addInternalVerification(security)
        context.startActivity(security)
    }

    /**
     * Method that will start the default Activity/View to scan a QR code
     * containing a valid linking code and then prompt the End User to provide
     * a custom name for the device.
     * It will notify all
     * event callbacks registered for when the current device is linked.
     *
     * @param context   Context used to bring up the Activity.
     * @param sdkKey    SDK Key used for linking.
     */
    fun startLinkingActivity(context: Context, sdkKey: String) {
        startLinkingActivity(context, LINKING_METHOD_SCAN, sdkKey)
    }

    /**
     * Method that will start the default Activity/View obtain a valid linking
     * code and then prompt the End User to provide
     * a custom name for the device. It could either bring up a QR code scanner or
     * an input field to have the End User enter the linking code instead.
     * It will notify all
     * event callbacks registered for when the current device is linked.
     *
     * @param context       Context used to bring up the Activity.
     * @param linkingMethod Value to define the means to obtain a valid linking code.
     * [.LINKING_METHOD_MANUAL] or [.LINKING_METHOD_SCAN].
     * @param sdkKey        SDK Key used for linking.
     */
    fun startLinkingActivity(context: Context, linkingMethod: Int, sdkKey: String) {
        val linkOption: Int
        linkOption = when (linkingMethod) {
            LINKING_METHOD_SCAN -> LinkActivity.TYPE_SCAN_QR
            LINKING_METHOD_MANUAL -> LinkActivity.TYPE_ENTER_CODE
            else -> LinkActivity.TYPE_SCAN_QR
        }
        val pairIntent = Intent(context, LinkActivity::class.java)
        pairIntent.putExtra(LinkActivity.EXTRA_TYPE, linkOption)
        pairIntent.putExtra(LinkActivity.EXTRA_SDK_KEY, sdkKey)
        addInternalVerification(pairIntent)
        context.startActivity(pairIntent)
    }

    companion object {
        const val LINKING_METHOD_MANUAL = 1
        const val LINKING_METHOD_SCAN = 2
        @Volatile
        private var __instance: AuthenticatorUIManager? = null

        /**
         * @return the singleton instance of [AuthenticatorUIManager].
         */
        @JvmStatic
        val instance: AuthenticatorUIManager
            get() = __instance!!

        @Synchronized
        fun init(appContext: Context) {
            check(__instance == null) { "Cannot initialize this class more than once." }
            if (__instance == null) {
                synchronized(AuthenticatorUIManager::class.java) {
                    if (__instance == null) {
                        __instance = AuthenticatorUIManager(appContext)
                    }
                }
            }
        }
    }

    // Private because singleton
    init {
        this.context = context.applicationContext
        config = AuthenticatorUIConfig.Builder().build(this.context)
    }
}