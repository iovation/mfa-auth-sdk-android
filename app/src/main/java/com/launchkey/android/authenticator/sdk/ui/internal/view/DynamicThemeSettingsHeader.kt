package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.internal.util.findAllText

class DynamicThemeSettingsHeaderLinearLayout : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setSettingsHeaders(this)
    }
}

class DynamicThemeSettingsHeaderFrameLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setSettingsHeaders(this)
    }
}

private fun setSettingsHeaders(viewGroup: ViewGroup) {
    val settingsHeaders = AuthenticatorUIManager.instance.config.themeObj().settingsHeaders
    val bg = settingsHeaders.colorBackground
    val textColor = settingsHeaders.colorText
    viewGroup.background = bg
    viewGroup.findAllText { textView ->
        if (textView !is Button) {
            textView.setTextColor(textColor)
        }
    }
}