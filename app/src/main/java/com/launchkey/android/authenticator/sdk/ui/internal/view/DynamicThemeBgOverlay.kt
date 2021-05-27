package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager

class DynamicThemeTextView : AppCompatTextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val backgroundOverlayUiProp = AuthenticatorUIManager.instance.config.themeObj().bgOverlay
        setTextColor(backgroundOverlayUiProp.colorBgOverlay)
    }
}

class DynamicThemeImageView : AppCompatImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val backgroundOverlayUiProp = AuthenticatorUIManager.instance.config.themeObj().bgOverlay
        setColorFilter(backgroundOverlayUiProp.colorBgOverlay)
    }
}

class DynamicThemeFittingTextView : FittingTextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val backgroundOverlayUiProp = AuthenticatorUIManager.instance.config.themeObj().bgOverlay
        setTextColor(backgroundOverlayUiProp.colorBgOverlay)
    }
}