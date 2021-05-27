package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager

class DynamicThemeEditText: AppCompatEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val editTextUiProp = AuthenticatorUIManager.instance.config.themeObj().editText
        setHintTextColor(editTextUiProp.colorHint)
        setTextColor(editTextUiProp.colorText)
    }
}