package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager

class DynamicThemeNegativeButton: DynamicThemeButton {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val buttonUiProp = AuthenticatorUIManager.instance.config.themeObj().buttonNegative
        background = buttonUiProp.drawableBg
        val colorStateListText = buttonUiProp.colorStateListText
        if (colorStateListText != null) {
            setTextColor(colorStateListText)
        } else {
            setTextColor(buttonUiProp.colorText)
        }
    }
}