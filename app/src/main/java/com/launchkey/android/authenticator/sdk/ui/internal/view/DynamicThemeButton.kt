package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager

open class DynamicThemeButton: AppCompatButton {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val buttonUiProp = AuthenticatorUIManager.instance.config.themeObj().button
        background = buttonUiProp.drawableBg
        val colorStateListText = buttonUiProp.colorStateListText
        if (colorStateListText != null) {
            setTextColor(colorStateListText)
        } else {
            setTextColor(buttonUiProp.colorText)
        }
    }
}