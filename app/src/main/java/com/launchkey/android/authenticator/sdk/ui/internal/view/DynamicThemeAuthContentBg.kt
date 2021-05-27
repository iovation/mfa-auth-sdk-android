package com.launchkey.android.authenticator.sdk.ui.internal.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager

class DynamicThemeAuthContentBgFrameLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setAuthContentBackground(this)
    }
}

class DynamicThemeAuthContentBgLinearLayout : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setAuthContentBackground(this)
    }
}

class DynamicThemeAuthContentBgGroup : Group {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setAuthContentBackground(this)
    }
}

class DynamicThemeAuthContentBgView : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setAuthContentBackground(this)
    }
}

class DynamicThemeAuthContentBgScrollView : ScrollView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setAuthContentBackground(this)
    }
}

private fun setAuthContentBackground(view: View) {
    val authContentBg = AuthenticatorUIManager.instance.config.themeObj().authContentBg
    view.background = ColorDrawable(authContentBg.colorBg)
}