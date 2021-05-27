package com.launchkey.android.authenticator.sdk.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.launchkey.android.authenticator.sdk.core.authentication_management.Session
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.databinding.ItemSessionBinding
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils

class SessionsAdapter(private val timeAgo: TimeAgo) :
    RecyclerView.Adapter<SessionsAdapter.ViewHolder>() {
    private val sessions: MutableList<Session> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSessionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                val listItemsUiProp = AuthenticatorUIManager.instance.config.themeObj().listItems
                root.background = listItemsUiProp.colorBg
                sessionTextTime.setTextColor(listItemsUiProp.colorText)
                sessionTextTitle.setTextColor(listItemsUiProp.colorText)
            }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        val binding = holder.binding
        Glide.with(binding.sessionImage)
            .load(session.iconUrl)
            .transition(DrawableTransitionOptions().dontTransition()).into(binding.sessionImage)
        binding.sessionTextTitle.text = session.name
        binding.sessionTextTime.text = timeAgo.timeAgo(session.createdAtMillis, false)
    }

    override fun getItemCount(): Int {
        return sessions.size
    }

    fun submitItems(newSessions: List<Session>) {
        sessions.clear()
        sessions.addAll(newSessions)
        notifyDataSetChanged()
    }

    fun removeSession(session: Session) {
        val indexOfSession = sessions.indexOf(session)

        if (indexOfSession < 0 || indexOfSession > sessions.size - 1) {
            return
        }

        sessions.removeAt(indexOfSession)
        notifyItemRemoved(indexOfSession)
    }

    fun getSession(position: Int): Session {
        return sessions[position]
    }

    class ViewHolder(val binding: ItemSessionBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}