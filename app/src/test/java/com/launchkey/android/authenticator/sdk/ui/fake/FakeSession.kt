package com.launchkey.android.authenticator.sdk.ui.fake

import com.launchkey.android.authenticator.sdk.core.authentication_management.Session

class FakeSession(private val name: String,
                  private val id: String,
                  private val createdAtMillis: Long,
                  private val iconUrl: String) : Session {
    override fun getName(): String = name
    override fun getId(): String = id
    override fun getCreatedAtMillis(): Long = createdAtMillis
    override fun getIconUrl(): String = iconUrl
}