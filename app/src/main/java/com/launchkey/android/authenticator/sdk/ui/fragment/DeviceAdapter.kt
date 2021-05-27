package com.launchkey.android.authenticator.sdk.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.launchkey.android.authenticator.sdk.core.authentication_management.Device
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val onUnlinkDeviceClicked: (Device) -> Unit
) : ListAdapter<Device, DeviceAdapter.ViewHolder>(DIFF_UTIL) {

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<Device>() {
            override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean =
                oldItem.uuid == newItem.uuid

            override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean =
                oldItem.equals(newItem)

        }
    }

    class ViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).apply {
                val listItemsUiProp = AuthenticatorUIManager.instance.config.themeObj().listItems
                root.background = listItemsUiProp.colorBg
                deviceTextCurrentdevice.setTextColor(listItemsUiProp.colorText)
                deviceTextName.setTextColor(listItemsUiProp.colorText)
            }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = currentList[position]
        holder.binding.deviceTextName.text = device.name
        holder.binding.deviceTextCurrentdevice.visibility =
            if (device.isCurrent) View.VISIBLE else View.GONE
        holder.binding.deviceButtonUnlink.setOnClickListener { onUnlinkDeviceClicked(device) }
    }
}