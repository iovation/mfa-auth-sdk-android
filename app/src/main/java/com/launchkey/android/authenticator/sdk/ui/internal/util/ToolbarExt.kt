package com.launchkey.android.authenticator.sdk.ui.internal.util

import androidx.appcompat.widget.Toolbar

internal fun Toolbar.setNavigationButton(navButton: UiUtils.NavButton) {
    setNavigationIcon(navButton.getNavigationIconRes())
    setNavigationContentDescription(navButton.getContentDescriptionRes())
}