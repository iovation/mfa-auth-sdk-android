package com.launchkey.android.authenticator.sdk.ui.internal.auth_method

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.ItemGeofencingGeofenceBinding
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import com.launchkey.android.authenticator.sdk.ui.theme.AuthenticatorTheme

class ItemAdapter<T>(
        private val authenticatorTheme: AuthenticatorTheme,
        private val timeAgo: TimeAgo,
        @StringRes private val contentDescriptionId: Int,
        @DrawableRes private val imageIconId: Int,
        private val onItemRemoveClicked: (Item<T>) -> Unit
) : ListAdapter<ItemAdapter.Item<T>, ItemAdapter.ViewHolder>(
    getDiffCallback()
) {
    companion object {
        private fun <T> getDiffCallback() : DiffUtil.ItemCallback<Item<T>>{
            return DIFF_CALLBACK as DiffUtil.ItemCallback<Item<T>>
        }
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<Item<*>>() {
                override fun areItemsTheSame(
                        oldItem: Item<*>,
                        newItem: Item<*>
                ): Boolean = oldItem.item == newItem.item
                
                override fun areContentsTheSame(
                        oldItem: Item<*>,
                        newItem: Item<*>
                ): Boolean =
                    oldItem.pendingState == newItem.pendingState
            }
    }
    
    class ViewHolder(binding: ItemGeofencingGeofenceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val icon = binding.itemImageIcon
        val deleteButton = binding.itemImagebuttonDelete
        val title = binding.itemTextTitle
        val titleExtra = binding.itemTextExtra
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemGeofencingGeofenceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                val listItemsUiProp = authenticatorTheme.listItems
                root.background = listItemsUiProp.colorBg
                itemImageIcon.setImageResource(imageIconId)
                itemTextTitle.setTextColor(listItemsUiProp.colorText)
                itemTextExtra.setTextColor(listItemsUiProp.colorText)
            }
        
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = currentList[position]
        val contentDescription = context.getString(
            contentDescriptionId,
            item.name
        )
        
        holder.title.text = item.name
        holder.titleExtra.text = context.getString(R.string.ioa_sec_passivefactor_active)
        with(holder.deleteButton) {
            setOnClickListener { onItemRemoveClicked(item) }
            this.contentDescription = contentDescription
        }
        
        when (item.pendingState) {
            is ItemPendingState.PendingActivation, ItemPendingState.NotPending -> {
                holder.icon.alpha = 1f
                holder.deleteButton.setImageResource(R.drawable.ic_delete_black_24dp)
            }
            is ItemPendingState.PendingRemoval -> {
                holder.icon.alpha = 0.4f
                holder.deleteButton.setImageResource(R.drawable.ic_undo_black_24dp)
            }
        }
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val currentItem = currentList[position]
            val context = holder.itemView.context
            
            // payload is timeRemainingInMillis
            val timeRemaining = payloads.last() as Long
            if (timeRemaining > 0) {
                val timeDiff = timeAgo.timeAgoWithDiff(timeRemaining, false)
                holder.titleExtra.text =
                    context.getString(
                        if (currentItem.isPendingRemoval)
                            R.string.ioa_sec_passivefactor_removedin
                        else
                            R.string.ioa_sec_passivefactor_activein,
                        timeDiff
                    )
                holder.icon.alpha = 0.4f
                if (currentItem.isPendingRemoval) {
                    holder.deleteButton.setImageResource(R.drawable.ic_undo_black_24dp)
                } else {
                    holder.deleteButton.setImageResource(R.drawable.ic_delete_black_24dp)
                }
            } else {
                // make active
                holder.titleExtra.text =
                    context.getString(R.string.ioa_sec_passivefactor_active)
                holder.icon.alpha = 1f
                holder.deleteButton.setImageResource(R.drawable.ic_delete_black_24dp)
            }
        }
    }
    
    fun notifyTimerFinished(item: Item<T>) {
        if (item.isPendingRemoval) {
            val foundItem = currentList.find { it == item }
            submitList(currentList - foundItem)
        } else if (!item.isActive) {
            // make active
            val indexOfItem = currentList.indexOfFirst { it == item }
            notifyItemChanged(indexOfItem, 0L)
        }
    }
    
    fun notifyTimerUpdate(item: Item<T>, remainingMillis: Long) {
        val indexOfItem = currentList.indexOfFirst { it == item }
        if (indexOfItem < 0) return
        
        notifyItemChanged(indexOfItem, remainingMillis)
    }

    interface Item<T> {
        val item: T
        val name: String
        val isPendingRemoval: Boolean
        val isActive: Boolean
        val now: Long
        val timeRemainingUntilRemoved: Long
        val timeRemainingUntilActivated: Long
        val pendingState: ItemPendingState
            get() = when {
                isPendingRemoval -> ItemPendingState.PendingRemoval(
                        now + timeRemainingUntilRemoved * 1000
                )
                !isActive -> ItemPendingState.PendingActivation(
                        now + timeRemainingUntilActivated * 1000
                )
                else -> ItemPendingState.NotPending
            }
    }

    sealed class ItemPendingState {
        object NotPending : ItemPendingState()
        data class PendingRemoval(val removedAtTimeInMillis: Long) : ItemPendingState()
        data class PendingActivation(val activatedAtTimeInMillis: Long) : ItemPendingState()
    }
}