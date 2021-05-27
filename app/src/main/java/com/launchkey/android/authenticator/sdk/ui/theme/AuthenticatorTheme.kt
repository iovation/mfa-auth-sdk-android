/*
 *  Copyright (c) 2018. iovation, LLC. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.launchkey.android.authenticator.sdk.ui.R
import java.util.*

class AuthenticatorTheme internal constructor(val appBar: AppBarUiProp,
                                              val appBarStatic: AppBarStaticBgUiProp,
                                              val bg: BackgroundUiProp,
                                              val bgOverlay: BackgroundOverlayUiProp,
                                              val button: ButtonUiProp,
                                              val buttonNegative: NegativeButtonUiProp,
                                              val pinCode: PinCodeUiProp,
                                              val circleCode: CircleCodeUiProp,
                                              val listItems: ListItemsUiProp,
                                              val listHeaders: ListHeadersUiProp,
                                              val settingsHeaders: SettingsHeadersUiProp,
                                              val helpMenuItems: HelpMenuUiProp,
                                              val methodsSecurityIcons: AuthMethodsSecurityIcons,
                                              val methodsBusyIcons: AuthMethodsBusyIcons,
                                              val authRequestAppBar: AuthRequestAppBar,
                                              val geoFence: GeoFenceUiProp,
                                              val editText: EditTextUiProp,
                                              val expirationTimer: ExpirationTimerProp,
                                              val denialOptions: DenialOptionsUiProp,
                                              val arb: TarbUiProp,
                                              val arbNegative: TarbNegativeUiProp,
                                              val authContentBg: AuthContentBgUiProp,
                                              @ColorInt val authResponseAuthorized: Int,
                                              @ColorInt val authResponseDenied: Int,
                                              @ColorInt val authResponseFailed: Int,
                                              internal @JvmField @StyleRes val themeRes: Int) {
    @Deprecated(message = "Use dedicated fields instead")
    val props: Map<String, List<UiProp>> = Collections.unmodifiableMap(mapOf())

    class Builder @JvmOverloads constructor(context: Context, @StyleRes private val baseThemeReference: Int = R.style.AuthenticatorTheme) {
        private val context: Context = context.applicationContext
        private lateinit var appBar: AppBarUiProp
        private lateinit var appBarStatic: AppBarStaticBgUiProp
        private lateinit var bg: BackgroundUiProp
        private lateinit var bgOverlay: BackgroundOverlayUiProp
        private lateinit var button: ButtonUiProp
        private lateinit var buttonNegative: NegativeButtonUiProp
        private lateinit var pinCode: PinCodeUiProp
        private lateinit var circleCode: CircleCodeUiProp
        private lateinit var listItems: ListItemsUiProp
        private lateinit var listHeaders: ListHeadersUiProp
        private lateinit var settingsHeaders: SettingsHeadersUiProp
        private lateinit var helpMenuItems: HelpMenuUiProp
        private lateinit var methodsSecurityIcons: AuthMethodsSecurityIcons
        private lateinit var methodsBusyIcons: AuthMethodsBusyIcons
        private lateinit var authRequestAppBar: AuthRequestAppBar
        private lateinit var geoFence: GeoFenceUiProp
        private lateinit var editText: EditTextUiProp
        private lateinit var expirationTimer: ExpirationTimerProp
        private lateinit var denialOptions: DenialOptionsUiProp
        private lateinit var arb: TarbUiProp
        private lateinit var arbNegative: TarbNegativeUiProp
        private lateinit var authContentBg: AuthContentBgUiProp
        private var authResponseAuthorized: Int = 0
        private var authResponseDenied: Int = 0
        private var authResponseFailed: Int = 0

        init {
            val wrapper = ContextThemeWrapper(context, baseThemeReference)
            val array = wrapper.obtainStyledAttributes(R.styleable.AuthenticatorTheme)
            // TODO: Check all unused AuthenticatorTheme.Builder methods. Probably means it's new and has no XML counterpart.

            // TODO: Primary, Primary Dark, and Accent colors? Part of the Builder constructor as a requirement?
            val buttonColorText = array.getColor(R.styleable.AuthenticatorTheme_authenticatorColorButtonText, 0)
            val buttonColorBg = array.getDrawable(R.styleable.AuthenticatorTheme_authenticatorColorButtonBackground)!!
            button(buttonColorBg, buttonColorText)
            val buttonNegativeColorText = array.getColor(R.styleable.AuthenticatorTheme_authenticatorColorNegativeWidgetText, 0)
            val buttonNegativeColorBg = array.getDrawable(R.styleable.AuthenticatorTheme_authenticatorColorNegativeWidgetBackground)!!
            buttonNegative(buttonNegativeColorBg, buttonNegativeColorText)
            val bg = array.getDrawable(R.styleable.AuthenticatorTheme_authenticatorColorBackground)!!
            background(bg)
            val bgOverlayColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorColorBackgroundOverlay, 0)
            backgroundOverlay(bgOverlayColor)
            val appBarColorBg = array.getColor(R.styleable.AuthenticatorTheme_authenticatorToolbarBackground, 0)
            val appBarColorItems = array.getColor(R.styleable.AuthenticatorTheme_authenticatorToolbarItems, 0)
            appBar(appBarColorBg, appBarColorItems)
            // TODO: Visibility UiProp variant?
            val pinText = array.getColorStateList(R.styleable.AuthenticatorTheme_authenticatorPinPadButtonOverlayColorSelector)
            val pinBg = array.getDrawable(R.styleable.AuthenticatorTheme_authenticatorPinPadButtonBackgroundDrawableSelector)!!
            pinCode(pinBg, pinText)
            // TODO: Other method variant?
            val circleHighlight = array.getColor(R.styleable.AuthenticatorTheme_authenticatorCirclePadHighlightColor, 0)
            val circleMarks = array.getColor(R.styleable.AuthenticatorTheme_authenticatorCirclePadMarksColor, 0)
            circleCode(circleHighlight, circleMarks)
            val headersVisibility = array.getInt(R.styleable.AuthenticatorTheme_authenticatorHeadersVisibility, View.VISIBLE)
            listHeaders(headersVisibility)
            // TODO: Colors UiProp variant?
            val showHelpMenuItems = array.getBoolean(R.styleable.AuthenticatorTheme_authenticatorShowHelpIcons, true)
            helpMenuItems(showHelpMenuItems)
            val methodsSecurityVisibility = array.getInt(R.styleable.AuthenticatorTheme_authenticatorSecurityListIconsVisibility, View.VISIBLE)
            val methodsSecurityPin = array.getResourceId(R.styleable.AuthenticatorTheme_authenticatorFactorPinIcon, R.drawable.ic_dialpad_black_24dp)
            val methodsSecurityCircle = array.getResourceId(R.styleable.AuthenticatorTheme_authenticatorFactorCircleIcon, R.drawable.ic_settings_backup_restore_black_24dp)
            val methodsSecurityWearable = array.getResourceId(R.styleable.AuthenticatorTheme_authenticatorFactorBluetoothIcon, R.drawable.ic_bluetooth_black_24dp)
            val methodsSecurityGeofencing = array.getResourceId(R.styleable.AuthenticatorTheme_authenticatorFactorGeofencingIcon, R.drawable.ic_place_black_24dp)
            val methodsSecurityFingerprint = array.getResourceId(R.styleable.AuthenticatorTheme_authenticatorFactorFingerprintIcon, R.drawable.ic_fingerprint_black_24dp)
            val methodsSecurityIconColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorFactorListIconColor, 0)
            factorsSecurityIcons(methodsSecurityVisibility, methodsSecurityPin, methodsSecurityCircle, methodsSecurityGeofencing, methodsSecurityWearable, methodsSecurityFingerprint, methodsSecurityIconColor)
            val methodsBusyGeofencing = array.getResourceId(R.styleable.AuthenticatorTheme_authenticatorAuthRequestOngoingImageGeofencing, R.drawable.ic_location_on_black_512px)
            val methodsBusyWearable = array.getResourceId(R.styleable.AuthenticatorTheme_authenticatorAuthRequestOngoingImageBluetooth, R.drawable.ic_bluetooth_searching_black_512px)
            val methodsBusyFingerprint = array.getResourceId(R.styleable.AuthenticatorTheme_authenticatorAuthRequestOngoingImageFingerprint, R.drawable.ic_fingerprint_black_512px)
            factorsBusyIcons(methodsBusyGeofencing, methodsBusyWearable, methodsBusyFingerprint)
            val authRequestAppBarVisibility = array.getInt(R.styleable.AuthenticatorTheme_authenticatorAuthToolbarVisibility, View.VISIBLE)
            authRequestAppBar(authRequestAppBarVisibility)
            val geoFenceColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorMapGeoFenceColor, 0)
            geoFence(geoFenceColor)
            val settingsHeadersBg = array.getDrawable(R.styleable.AuthenticatorTheme_authenticatorSettingsHeadersBackgroundColor)!!
            val settingsHeadersText = array.getColor(R.styleable.AuthenticatorTheme_authenticatorSettingsHeadersTextColor, 0)
            settingsHeaders(settingsHeadersBg, settingsHeadersText)
            val listItemsBg = array.getDrawable(R.styleable.AuthenticatorTheme_authenticatorListItemsBackgroundColor)!!
            val listItemsText = array.getColor(R.styleable.AuthenticatorTheme_authenticatorListItemsTextColor, 0)
            listItems(listItemsBg, listItemsText)
            val editTextHintColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorEditTextHintColor, 0)
            val editTextColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorEditTextInputColor, 0)
            editText(editTextHintColor, editTextColor)
            val expTimerBgColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorExpirationTimerBackgroundColor, 0)
            val expTimerFillColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorExpirationTimerFillColor, 0)
            val expTimerWarnColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorExpirationTimerWarningColor, 0)
            expirationTimer(expTimerBgColor, expTimerFillColor, expTimerWarnColor)
            val denialOptionNormal = array.getColor(R.styleable.AuthenticatorTheme_authenticatorResponseDenialReasonNormalColor, 0)
            val denialOptionChecked = array.getColor(R.styleable.AuthenticatorTheme_authenticatorResponseDenialReasonCheckedColor, 0)
            denialReasons(denialOptionNormal, denialOptionChecked)
            val arbBg = array.getDrawable(R.styleable.AuthenticatorTheme_authenticatorResponseButtonBackground)
            val arbTextColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorResponseButtonTextColor, 0)
            val arbFillColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorResponseButtonFillColor, 0)
            authResponseButton(arbBg, arbTextColor, arbFillColor)
            val arbNegativeBg = array.getDrawable(R.styleable.AuthenticatorTheme_authenticatorResponseButtonNegativeBackground)
            val arbNegativeTextColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorResponseButtonNegativeTextColor, 0)
            val arbNegativeFillColor = array.getColor(R.styleable.AuthenticatorTheme_authenticatorResponseButtonNegativeFillColor, 0)
            authResponseButtonNegative(arbNegativeBg, arbNegativeTextColor, arbNegativeFillColor)
            val colorAuthResponseAuthorized = array.getColor(R.styleable.AuthenticatorTheme_authenticatorResponseAuthorizedColor, 0)
            authResponseAuthorizedColor(colorAuthResponseAuthorized)
            val colorAuthResponseDenied = array.getColor(R.styleable.AuthenticatorTheme_authenticatorResponseDeniedColor, 0)
            authResponseDeniedColor(colorAuthResponseDenied)
            val colorAuthResponseFailed = array.getColor(R.styleable.AuthenticatorTheme_authenticatorResponseFailedColor, 0)
            authResponseFailedColor(colorAuthResponseFailed)
            val colorAuthContentViewBg = array.getColor(R.styleable.AuthenticatorTheme_authenticatorAuthContentViewColorBackground, 0)
            authContentViewBackground(colorAuthContentViewBg)
            array.recycle()
        }

        fun appBar(@ColorInt colorBackground: Int, @ColorInt colorItems: Int): Builder {
            appBar = AppBarUiProp(colorBackground, colorItems)

            // One-time internal prop
            appBarStatic = AppBarStaticBgUiProp(appBar)
            return this
        }

        fun background(drawableBackground: Drawable): Builder {
            bg = BackgroundUiProp(drawableBackground)
            return this
        }

        fun backgroundOverlay(colorBackgroundOverlay: Int): Builder {
            bgOverlay = BackgroundOverlayUiProp(colorBackgroundOverlay)
            return this
        }

        fun button(drawableBackground: Drawable, @ColorInt colorText: Int): Builder {
            button = ButtonUiProp(drawableBackground, colorText)
            return this
        }

        fun button(drawableBackground: Drawable, colorStateListText: ColorStateList?): Builder {
            button = ButtonUiProp(drawableBackground, colorStateListText)
            return this
        }

        fun buttonNegative(drawableBackground: Drawable, colorText: Int): Builder {
            buttonNegative = NegativeButtonUiProp(drawableBackground, colorText)
            return this
        }

        fun buttonNegative(drawableBackground: Drawable,
                           colorStateListText: ColorStateList?): Builder {
            buttonNegative = NegativeButtonUiProp(drawableBackground, colorStateListText)
            return this
        }

        @Deprecated(message = """Auth Slider has been removed. This
          method may be removed in a future release.""")
        fun authSlider(trackUpperColor: Int, trackLowerColor: Int, thumbIdleColor: Int, thumbPressedColor: Int): Builder {
            return this
        }

        @Deprecated(message = """Auth Slider has been removed. This
          method may be removed in a future release.""")
        fun authSlider(trackUpperColor: Int, trackLowerColor: Int, thumbIdleColor: Int, thumbPressedColor: Int, thumbIconTint: Int): Builder {
            return this
        }

        fun pinCode(backgroundDrawable: Drawable, labelColor: Int): Builder {
            pinCode = PinCodeUiProp(backgroundDrawable, labelColor)
            return this
        }

        fun pinCode(backgroundDrawable: Drawable, labelColors: ColorStateList?): Builder {
            pinCode = PinCodeUiProp(backgroundDrawable, labelColors)
            return this
        }

        fun circleCode(colorHighlight: Int, colorMarks: Int): Builder {
            circleCode = CircleCodeUiProp(colorHighlight, colorMarks)
            return this
        }

        fun listItems(colorBackground: Int, colorText: Int): Builder {
            return listItems(ColorDrawable(colorBackground), colorText)
        }

        fun listItems(colorBackground: Drawable, colorText: Int): Builder {
            listItems = ListItemsUiProp(colorBackground, colorText)
            return this
        }

        fun listHeaders(visibility: Int): Builder {
            listHeaders = ListHeadersUiProp(visibility)
            return this
        }

        fun listHeaders(visibility: Int, colorBackground: Int, colorText: Int): Builder {
            listHeaders = ListHeadersUiProp(visibility, colorBackground, colorText)
            return this
        }

        fun settingsHeaders(colorBackground: Int, colorText: Int): Builder {
            return settingsHeaders(ColorDrawable(colorBackground), colorText)
        }

        fun settingsHeaders(colorBackground: Drawable, colorText: Int): Builder {
            settingsHeaders = SettingsHeadersUiProp(colorBackground, colorText)
            return this
        }

        fun helpMenuItems(visible: Boolean): Builder {
            helpMenuItems = HelpMenuUiProp(visible)
            return this
        }

        fun factorsSecurityIcons(iconVisibility: Int): Builder {
            methodsSecurityIcons = AuthMethodsSecurityIcons(iconVisibility)
            return this
        }

        fun factorsSecurityIcons(iconVisibility: Int, iconPinCode: Drawable?, iconCircleCode: Drawable?, iconGeofencing: Drawable?, iconWearable: Drawable?, iconFingerprintScan: Drawable?): Builder {
            methodsSecurityIcons = AuthMethodsSecurityIcons(iconVisibility, iconPinCode, iconCircleCode, iconGeofencing, iconWearable, iconFingerprintScan)
            return this
        }

        fun factorsSecurityIcons(iconVisibility: Int, iconPinCodeRes: Int, iconCircleCodeRes: Int, iconGeofencingRes: Int, iconWearableRes: Int, iconFingerprintScanRes: Int): Builder {
            methodsSecurityIcons = AuthMethodsSecurityIcons(iconVisibility, iconPinCodeRes, iconCircleCodeRes, iconGeofencingRes, iconWearableRes, iconFingerprintScanRes)
            return this
        }

        fun factorsSecurityIcons(iconVisibility: Int, iconPinCodeRes: Int, iconCircleCodeRes: Int, iconGeofencingRes: Int, iconWearableRes: Int, iconFingerprintScanRes: Int, iconColor: Int): Builder {
            methodsSecurityIcons = AuthMethodsSecurityIcons(iconVisibility, iconPinCodeRes, iconCircleCodeRes, iconGeofencingRes, iconWearableRes, iconFingerprintScanRes, iconColor)
            return this
        }

        fun factorsBusyIcons(drawableGeofencing: Drawable?, drawableWearable: Drawable?, drawableFingerprintScan: Drawable?): Builder {
            methodsBusyIcons = AuthMethodsBusyIcons(drawableGeofencing, drawableWearable, drawableFingerprintScan)
            return this
        }

        fun factorsBusyIcons(drawableGeofencingRes: Int, drawableWearableRes: Int, drawableFingerprintScanRes: Int): Builder {
            methodsBusyIcons = AuthMethodsBusyIcons(drawableGeofencingRes, drawableWearableRes, drawableFingerprintScanRes)
            return this
        }

        fun authRequestAppBar(visibility: Int): Builder {
            authRequestAppBar = AuthRequestAppBar(visibility)
            return this
        }

        fun geoFence(colorFence: Int): Builder {
            geoFence = GeoFenceUiProp(colorFence)
            return this
        }

        fun editText(colorTextHint: Int, colorText: Int): Builder {
            editText = EditTextUiProp(colorTextHint, colorText)
            return this
        }

        fun expirationTimer(colorBackground: Int, colorFill: Int, colorWarning: Int): Builder {
            expirationTimer = ExpirationTimerProp(colorBackground, colorFill, colorWarning)
            return this
        }

        fun denialReasons(colorNormal: Int, colorChecked: Int): Builder {
            denialOptions = DenialOptionsUiProp(colorNormal, colorChecked)
            return this
        }

        fun authResponseButton(backgroundRes: Int, textColorRes: Int, fillColorRes: Int): Builder {
            arb = TarbUiProp(backgroundRes, textColorRes, fillColorRes)
            return this
        }

        fun authResponseButton(background: Drawable?, textColor: Int, fillColor: Int): Builder {
            arb = TarbUiProp(background, textColor, fillColor)
            return this
        }

        fun authResponseButtonNegative(backgroundRes: Int, textColorRes: Int, fillColorRes: Int): Builder {
            arbNegative = TarbNegativeUiProp(backgroundRes, textColorRes, fillColorRes)
            return this
        }

        fun authResponseButtonNegative(background: Drawable?, textColor: Int, fillColor: Int): Builder {
            arbNegative = TarbNegativeUiProp(background, textColor, fillColor)
            return this
        }

        fun authContentViewBackground(color: Int): Builder {
            authContentBg = AuthContentBgUiProp(color)
            return this
        }

        fun authResponseAuthorizedColor(color: Int): Builder {
            authResponseAuthorized = color
            return this
        }

        fun authResponseDeniedColor(color: Int): Builder {
            authResponseDenied = color
            return this
        }

        fun authResponseFailedColor(color: Int): Builder {
            authResponseFailed = color
            return this
        }

        fun build(): AuthenticatorTheme {
            // Help Menu Items are set separately after instantiation of AuthenticatorTheme
            // Auth Methods Security Icons are set separately after instantiation of AuthenticatorTheme
            // Auth Methods Busy Icons are set separately after instantiation of AuthenticatorTheme
            // Auth Request App Bar is set separately after instantiation of AuthenticatorTheme
            // Geo-fence is set separately after instantiation of AuthenticatorTheme
            return AuthenticatorTheme(appBar,
                    appBarStatic,
                    bg,
                    bgOverlay,
                    button,
                    buttonNegative,
                    pinCode,
                    circleCode,
                    listItems,
                    listHeaders,
                    settingsHeaders,
                    helpMenuItems,
                    methodsSecurityIcons,
                    methodsBusyIcons,
                    authRequestAppBar,
                    geoFence,
                    editText,
                    expirationTimer,
                    denialOptions,
                    arb,
                    arbNegative,
                    authContentBg,
                    authResponseAuthorized,
                    authResponseDenied,
                    authResponseFailed,
                    baseThemeReference)
        }
    }
}