package com.launchkey.android.authenticator.sdk.ui.internal

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager

class UIAuthSdkInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val appContext = context!!.applicationContext
        AuthenticatorUIManager.init(appContext)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }
}