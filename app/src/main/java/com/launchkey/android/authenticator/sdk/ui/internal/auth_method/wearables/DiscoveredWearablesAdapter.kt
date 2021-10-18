package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.wearables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.launchkey.android.authenticator.sdk.core.auth_method_management.WearablesManager
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.databinding.ItemBluetoothDeviceDiscoverBinding

class DiscoveredWearablesAdapter(
    private val devices: List<WearablesManager.Wearable>,
    private val onItemClickListener: ((WearablesManager.Wearable) -> Unit)
) : RecyclerView.Adapter<DiscoveredWearablesAdapter.ViewHolder>() {

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
        val item = devices[position]
        val label = if (item.name.trim { it <= ' ' }.isEmpty()) item.id else item.name
        holder.binding.bluetoothTextTitle.text = label
        holder.binding.root.setOnClickListener { onItemClickListener(devices[position]) }
    }

    override fun getItemCount(): Int = devices.size
}