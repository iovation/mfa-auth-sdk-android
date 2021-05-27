package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager

class DynamicThemeListHeadersTextView : AppCompatTextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val listHeaders = AuthenticatorUIManager.instance.config.themeObj().listHeaders

        visibility = listHeaders.visibility
        if (listHeaders.colorBg != 0) {
            setBackgroundColor(listHeaders.colorBg)
        }
        if (listHeaders.colorText != 0) {
            setTextColor(listHeaders.colorText)
        }
    }
}