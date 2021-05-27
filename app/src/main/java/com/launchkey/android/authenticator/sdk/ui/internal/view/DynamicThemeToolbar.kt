package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.findAllText

class DynamicThemeToolbar : Toolbar {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val appBarUiProp = AuthenticatorUIManager.instance.config.themeObj().appBar
        setBackgroundColor(appBarUiProp.backgroundColor)
        postDelayed({
            val textFinder = { textView: TextView -> textView.setTextColor(appBarUiProp.colorItems) }
            findAllText(textFinder)

            if (navigationIcon != null) {
                navigationIcon = UiUtils.tintDrawable(navigationIcon!!.constantState!!.newDrawable(), appBarUiProp.colorItems)
            }
            if (overflowIcon != null) {
                overflowIcon = UiUtils.tintDrawable(overflowIcon!!.constantState!!.newDrawable(), appBarUiProp.colorItems)
            }
            if (menu != null) {
                val menu = menu
                for (i in 0 until menu.size()) {
                    val item = menu.getItem(i)
                    if (item != null && item.icon != null) {
                        item.icon = UiUtils.tintDrawable(item.icon, appBarUiProp.colorItems)
                    }
                }
            }
        }, 20)
    }
}