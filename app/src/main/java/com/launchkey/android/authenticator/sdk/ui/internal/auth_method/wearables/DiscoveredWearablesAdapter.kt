package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.databinding.ItemBluetoothDeviceDiscoverBinding

class DiscoveredWearablesAdapter(
    private val onItemClickListener: ((WearablesManager.Wearable) -> Unit)
) : ListAdapter<WearablesManager.Wearable, DiscoveredWearablesAdapter.ViewHolder>(DIFF_CALLBACK) {

    private companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WearablesManager.Wearable>() {
            override fun areItemsTheSame(
                oldItem: WearablesManager.Wearable,
                newItem: WearablesManager.Wearable
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: WearablesManager.Wearable,
                newItem: WearablesManager.Wearable
            ): Boolean = oldItem.name == newItem.name &&
                    oldItem.isActive == newItem.isActive &&
                    oldItem.isPendingRemoval == newItem.isPendingRemoval &&
                    oldItem.timeRemainingUntilActivated == newItem.timeRemainingUntilActivated &&
                    oldItem.timeRemainingUntilRemoved == newItem.timeRemainingUntilRemoved

        }
    }

    class ViewHolder(val binding: ItemBluetoothDeviceDiscoverBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBluetoothDeviceDiscoverBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                val listItemsUiProp =
                    AuthenticatorUIManager.instance.config.themeObj().listItems
                root.background = listItemsUiProp.colorBg
                bluetoothTextTitle.setTextColor(listItemsUiProp.colorText)
            })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = currentList[position]
        val label = if (item.name.isBlank()) item.id else item.name
        holder.binding.bluetoothTextTitle.text = label
        holder.binding.root.setOnClickListener { onItemClickListener(currentList[position]) }
    }
}
