/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.SparseIntArray
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StyleableRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.launchkey.android.authenticator.sdk.core.auth_method_management.VerificationFlag
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.AuthMethodFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.auth.BiometricUnrecognizedFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.sensor.BiometricScannerDisabledFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.sensor.BiometricScannerTimeoutFailure
import com.launchkey.android.authenticator.sdk.core.failure.auth_method.user_error.BiometricUnclearFailure
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.SecurityFragment
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.SettingsPanel
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PINCodeRequirement
import com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code.PinCodeRequirementAdapter
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import java.util.*

object UiUtils {
    private const val REQUEST_UI_ANIM_DURATION = android.R.integer.config_shortAnimTime
    fun getBiometricSensorErrorMessage(authMethodFailure: AuthMethodFailure): String {
        return if (authMethodFailure is BiometricScannerTimeoutFailure) {
            authMethodFailure.sensorMessage
        } else if (authMethodFailure is BiometricScannerDisabledFailure) {
            authMethodFailure.sensorMessage
        } else if (authMethodFailure is BiometricUnrecognizedFailure) {
            authMethodFailure.sensorMessage
        } else if (authMethodFailure is BiometricUnclearFailure) {
            authMethodFailure.sensorMessage
        } else {
            "Something went wrong when verifying the Biometric Auth Method"
        }
    }

    fun applyMarshmallowSecureFlagToWindow(a: Activity?) {
        if (a == null) {
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                a.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        } catch (e: Exception) {
        }
    }

    fun dpToPx(context: Context, dp: Float): Int {
        val metrics = context.resources.displayMetrics
        return (dp * (metrics.densityDpi / 160f)).toInt()
    }

    fun toast(activity: Activity?, messageId: Int, isDurationLong: Boolean) {
        if (activity == null || messageId <= 0) {
            return
        }
        var message: String? = null
        try {
            message = activity.getString(messageId)
        } catch (re: RuntimeException) {
            //Intentional fall-through
        }
        toast(activity, message, isDurationLong)
    }

    val defaultDeviceName: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            var result = if (model.startsWith(manufacturer)) String.format("%s", model) else String.format("%s %s", manufacturer, model)
            result = result.trim { it <= ' ' }
            if (result.length > 20) {
                result = String.format("%s %s", result.substring(0, 10), result.substring(result.length - 8))
            }
            return result
        }

    fun toast(context: Context?, message: String?, isDurationLong: Boolean) {
        if (context == null || message == null) {
            return
        }
        val duration = if (isDurationLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context, message, duration).show()
    }

    fun setTextTemporarily(
            lifecycleOwner: LifecycleOwner,
            handler: Handler,
            textView: TextView,
            temporaryText: String,
            howLong: Long) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    lifecycleOwner.lifecycle.removeObserver(this)
                } else if (event == Lifecycle.Event.ON_STOP) {
                    handler.removeCallbacksAndMessages(null)
                }
            }
        })
        val initial = textView.text.toString()
        handler.post { textView.text = temporaryText }
        handler.postDelayed({
            val stillUnchanged = textView.text.toString() == temporaryText
            if (stillUnchanged) {
                textView.text = initial
            }
        }, howLong)
    }

    @JvmOverloads
    fun finishAddingFactorActivity(a: Activity, requestCode: Int? = null) {
        if (requestCode != null) {
            a.setResult(Activity.RESULT_OK, Intent().putExtra(SecurityFragment.REQUEST_CODE, requestCode))
        } else {
            a.setResult(Activity.RESULT_OK)
        }
        a.finish()
    }

    private fun vibrate(context: Context, duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(Vibrator::class.java).vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(duration)
        }
    }

    fun vibratePinCircleFeedback(context: Context) {
        val duration = context.resources.getInteger(R.integer.lk_haptic_feedback_pin_circle).toLong()
        vibrate(context, duration)
    }

    fun wasPermissionGranted(context: Context?, permission: String?): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(context!!, permission!!)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    fun wasPermissionGranted(grantResults: Map<String?, Boolean?>?): Boolean {
        return if (grantResults == null) {
            false
        } else grantResults.size >= 1 && !grantResults.containsValue(false)
    }

    @JvmStatic
    fun areLocationServicesTurnedOn(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ignored: Exception) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ignored: Exception) {
        }
        return gps_enabled && network_enabled
    }

    fun getThemeColor(context: Context, colorAttr: Int): Int {
        return getThemeColor(context, colorAttr, 0)
    }

    private fun getThemeColor(context: Context, colorAttr: Int, themeRef: Int): Int {
        val data = if (themeRef != 0) themeRef else TypedValue().data
        val a = context.obtainStyledAttributes(data, intArrayOf(colorAttr))
        if (!a.hasValue(0)) {
            a.recycle()
            throw Resources.NotFoundException()
        }
        val color = a.getColor(0, -1)
        a.recycle()
        return color
    }

    fun themeStatusBar(a: Activity?) {
        if (a == null) {
            return
        }
        val config = AuthenticatorUIManager.instance.config
        val color = getThemeColor(a, R.attr.authenticatorColorPrimaryDark, config.theme())
        val window = a.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

    private val mTintedDrawables = HashMap<Drawable, TintedDrawable>()
    fun tintDrawable(d: Drawable?, tintColor: Int): Drawable? {
        if (d == null) {
            return d
        }
        if (mTintedDrawables.containsKey(d) && mTintedDrawables[d]!!.tint == tintColor) {
            return mTintedDrawables[d]!!.drawable
        }
        val td = TintedDrawable(d, tintColor)
        mTintedDrawables[d] = td
        return td.drawable
    }

    @JvmOverloads
    fun updateSettingsPanelWithFactorState(p: SettingsPanel?, state: VerificationFlag?, millisUntilToggled: Long = state!!.millisUntilToggled!!, animate: Boolean = true) {
        if (p == null || state == null) {
            return
        }
        val c = p.context.applicationContext
        val timeAgo = TimeAgo(c)
        var verifyExtra = ""
        val verifyLabelRes: Int
        if (state.isPendingToggle) {
            if (millisUntilToggled >= 1000) {
                val timeRemaining = timeAgo.timeAgoWithDiff(millisUntilToggled, false)
                verifyExtra = c.getString(
                        R.string.ioa_sec_panel_verify_extra_format, timeRemaining)
            }
            verifyLabelRes = getNOTStringResFromUserSetState(state)
        } else {
            verifyLabelRes = getStringResFromUserSetState(state)
        }
        val animRes = if ("" == verifyExtra) 0 else R.anim.up_in
        if (state.isPendingToggle) {
            p.isSwitchOn = state.state != VerificationFlag.State.ALWAYS
        } else {
            p.isSwitchOn = state.state == VerificationFlag.State.ALWAYS
        }
        p.setVerifiedWhenText(verifyLabelRes)
        p.setVerifiedExtraText(verifyExtra)
        if (animate) p.playVerifiedExtraTextAnim(animRes)
    }

    fun getStringResFromUserSetState(s: VerificationFlag): Int {
        return getStringResFromUserSetState(s.state)
    }

    fun getStringResFromUserSetState(s: VerificationFlag.State): Int {
        return if (s == VerificationFlag.State.ALWAYS) R.string.ioa_sec_panel_verify_always else R.string.ioa_sec_panel_verify_whenrequired
    }

    @JvmStatic
    fun getNOTStringResFromUserSetState(s: VerificationFlag): Int {
        return if (s.state == VerificationFlag.State.ALWAYS) R.string.ioa_sec_panel_verify_whenrequired else R.string.ioa_sec_panel_verify_always
    }

    private const val EXCEPTION_ATTR_NOT_FOUND_FORMAT = "Theme does not have required attribute (%d)"
    private var attrToDrawableResIdMap: SparseIntArray? = null
    private fun themeContext(context: Context): Context {
        Objects.requireNonNull(context)
        return ContextThemeWrapper(context, AuthenticatorUIManager.instance.config.theme())
    }

    @JvmStatic
    @DrawableRes
    fun getDrawableAttrFromContext(
            context: Context?, attr: Int, fallbackDrawableResId: Int): Int {
        var context = context
        if (context == null) {
            return fallbackDrawableResId
        }
        context = themeContext(context)
        if (attrToDrawableResIdMap == null) {
            attrToDrawableResIdMap = SparseIntArray()
            @StyleableRes val allDrawableAttrs = intArrayOf(
                    R.attr.authenticatorFactorPinIcon,
                    R.attr.authenticatorFactorCircleIcon,
                    R.attr.authenticatorFactorBluetoothIcon,
                    R.attr.authenticatorFactorGeofencingIcon,
                    R.attr.authenticatorFactorFingerprintIcon,
                    R.attr.authenticatorAuthRequestOngoingImageBluetooth,
                    R.attr.authenticatorAuthRequestOngoingImageGeofencing,
                    R.attr.authenticatorAuthRequestOngoingImageFingerprint
            )
            val a = context.obtainStyledAttributes(allDrawableAttrs)
            var drawableResId = -1
            for (i in allDrawableAttrs.indices) {
                if (!a.hasValue(i)) {
                    a.recycle()
                    throw Resources.NotFoundException(String.format(Locale.getDefault(), EXCEPTION_ATTR_NOT_FOUND_FORMAT, i))
                }
                drawableResId = a.getResourceId(i, -1)
                attrToDrawableResIdMap!!.put(allDrawableAttrs[i], drawableResId)
            }
            a.recycle()
        }
        return attrToDrawableResIdMap!![attr, fallbackDrawableResId]
    }

    private var mAttrToColorMap: SparseIntArray? = null
    fun getColorFromTheme(context: Context?, attr: Int, defaultColorRes: Int): Int {
        var context = context
        if (context == null) {
            throw NullPointerException("Context cannot be null")
        }
        if (mAttrToColorMap == null) {
            mAttrToColorMap = SparseIntArray()
            context = themeContext(context)
            val allColorAttrs = intArrayOf(
                    R.attr.authenticatorColorPrimary,
                    R.attr.authenticatorColorPrimaryDark,
                    R.attr.authenticatorColorAccent,
                    R.attr.authenticatorColorBackground,
                    R.attr.authenticatorAuthSliderThumbNormal,
                    R.attr.authenticatorAuthSliderThumbPressed,
                    R.attr.authenticatorAuthSliderTrackLight,
                    R.attr.authenticatorAuthSliderTrackDark,
                    R.attr.authenticatorToolbarBackground,
                    R.attr.authenticatorToolbarItems,
                    R.attr.authenticatorCirclePadHighlightColor,
                    R.attr.authenticatorCirclePadMarksColor,
                    R.attr.authenticatorColorNegativeWidgetBackground,
                    R.attr.authenticatorColorNegativeWidgetText
            )
            val a = context.obtainStyledAttributes(allColorAttrs)
            var color = -1
            for (i in allColorAttrs.indices) {
                if (!a.hasValue(i)) {
                    a.recycle()
                    throw Resources.NotFoundException(String.format(Locale.getDefault(), EXCEPTION_ATTR_NOT_FOUND_FORMAT, i))
                }
                color = a.getColor(i, -1)
                mAttrToColorMap!!.put(allColorAttrs[i], color)
            }
            a.recycle()
        }
        val fallbackColor = ContextCompat.getColor(context, defaultColorRes)
        return mAttrToColorMap!![attr, fallbackColor]
    }

    fun applyThemeToMenu(inflater: MenuInflater?, menu: Menu?) {
        if (inflater == null || menu == null) {
            return
        }
        val helpMenuItems = AuthenticatorUIManager.instance.config.themeObj().helpMenuItems
        if (helpMenuItems.visible) {
            inflater.inflate(R.menu.help, menu)
        }
    }

    /**
     * @return All four views in an array of View elements to ensure correct view ID values.
     */
    fun prepKbaAddCheckHeader(root: View?,
                              headerId: Int,
                              showUpperContainer: Boolean,
                              showBottomRightText: Boolean,
                              widgetId: Int,
                              resources: Resources?,
                              pinCodeRequirements: List<PINCodeRequirement?>): Array<View?> {
        if (root != null && headerId > 0) {
            val header = root.findViewById<View>(headerId)
            if (header != null) {
                // Compare colors and add spacing necessary to balance the views visually-speaking...
                if (resources != null) {
                    balanceWidgetWithSettingsHeader(root, headerId, widgetId, resources)
                }

                // ...then collect all views in the header to return along with the updated visibility flags
                val upperContainer = header.findViewById<View>(R.id.toinclude_method_addcheck_header_layout_upper)
                if (upperContainer != null) {
                    upperContainer.visibility = if (showUpperContainer) View.VISIBLE else View.GONE
                }
                val bottomRightText = header.findViewById<View>(R.id.toinclude_method_addcheck_header_text_bottomright)
                if (bottomRightText != null) {
                    bottomRightText.visibility = if (showBottomRightText) View.VISIBLE else View.GONE
                }
                val requirementsListView: RecyclerView = header.findViewById(R.id.to_include_method_addcheck_header_requirements_list)
                if (requirementsListView != null) {
                    requirementsListView.visibility = if (pinCodeRequirements.isEmpty()) View.GONE else View.VISIBLE
                    requirementsListView.layoutManager = LinearLayoutManager(root.context)
                    val pinCodeRequirementAdapter = PinCodeRequirementAdapter()
                    requirementsListView.adapter = pinCodeRequirementAdapter
                    if (!pinCodeRequirements.isEmpty()) {
                        pinCodeRequirementAdapter.submitList(pinCodeRequirements)
                    }
                }
                return arrayOf(
                        header.findViewById(R.id.toinclude_method_addcheck_header_text_topleft),
                        header.findViewById(R.id.toinclude_method_addcheck_header_switch),
                        header.findViewById(R.id.toinclude_method_addcheck_header_text_bottomleft),
                        bottomRightText,
                        requirementsListView
                )
            }
        }
        return arrayOfNulls(5)
    }

    private fun areOverlappingColorsVisuallyMatching(colorOverlapping: Int, colorBack: Int): Boolean {
        if (colorOverlapping == colorBack) {
            return true
        }
        if (Color.alpha(colorOverlapping) == 0) {
            return true
        }
        val sameRgb = Color.red(colorOverlapping) == Color.red(colorBack) && Color.green(colorOverlapping) == Color.green(colorBack) && Color.blue(colorOverlapping) == Color.blue(colorBack)
        if (sameRgb) {
            val overlappingAlpha = Color.alpha(colorOverlapping)
            val backgroundAlpha = Color.alpha(colorBack)
            val lowerAlphaOnSolid = overlappingAlpha < backgroundAlpha && backgroundAlpha == 255

            // Since more checks can be added later, we don't
            // want to return the result no matter what and thus
            // the suppressed inspection
            if (lowerAlphaOnSolid) {
                return true
            }
        }
        return false
    }

    private fun balanceWidgetWithSettingsHeader(root: View?, headerId: Int, widgetId: Int, resources: Resources?) {
        if (resources == null || root == null || root.background !is ColorDrawable) {
            return
        }
        val header = root.findViewById<View>(headerId)
        if (header == null || header.background !is ColorDrawable) {
            return
        }
        val colorBg = (root.background as ColorDrawable).color
        val colorHeader = (header.background as ColorDrawable).color
        val sameColor = areOverlappingColorsVisuallyMatching(colorHeader, colorBg)
        if (!sameColor) {
            return
        }
        if (widgetId <= 0) {
            return
        }
        val widget = root.findViewById<View>(widgetId) ?: return
        if (widget.layoutParams is RelativeLayout.LayoutParams) {
            val spacingPixel = resources.getDimensionPixelSize(R.dimen.ioa_security_method_settingspanel_spacing_outer_balance)
            val params = widget.layoutParams as RelativeLayout.LayoutParams
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, spacingPixel)
            widget.layoutParams = params
        }
    }

    fun getRequestUiAnimDuration(context: Context?): Long {
        return context?.resources?.getInteger(REQUEST_UI_ANIM_DURATION)?.toLong() ?: 300L
    }

    fun updateToolbarIcon(toolbar: Toolbar?, navButton: NavButton?) {
        if (toolbar == null || navButton == null) {
            return
        }
        toolbar.setNavigationIcon(navButton.getNavigationIconRes())
        toolbar.setNavigationContentDescription(navButton.getContentDescriptionRes())
    }

    private class TintedDrawable(d: Drawable?, var tint: Int) {
        var drawable: Drawable

        init {
            val wrappedDrawable = DrawableCompat.wrap(d!!)
            DrawableCompat.setTint(wrappedDrawable.constantState!!.newDrawable(), tint)
            drawable = wrappedDrawable
        }
    }

    enum class NavButton {
        BACK {
            override fun getNavigationIconRes(): Int {
                return R.drawable.ic_arrow_back_white_24dp
            }

            override fun getContentDescriptionRes(): Int {
                return R.string.ioa_acc_nav_back
            }
        },
        CANCEL {
            override fun getNavigationIconRes(): Int {
                return R.drawable.ic_clear_white_24dp
            }

            override fun getContentDescriptionRes(): Int {
                return R.string.ioa_acc_nav_cancel
            }
        };

        abstract fun getNavigationIconRes() : Int
        abstract fun getContentDescriptionRes() : Int
    }
}