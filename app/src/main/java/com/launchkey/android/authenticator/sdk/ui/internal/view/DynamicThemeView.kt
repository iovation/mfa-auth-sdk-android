package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager

class DynamicThemeView : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val appBarStaticUiProp = AuthenticatorUIManager.instance.config.themeObj().appBarStatic
        setBackgroundColor(appBarStaticUiProp.colorBg)
    }
}