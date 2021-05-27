package com.launchkey.android.authenticator.sdk.ui.internal.auth_method

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.launchkey.android.authenticator.sdk.core.auth_method_management.AuthMethod
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.ItemSecurityBinding
import com.launchkey.android.authenticator.sdk.ui.internal.common.SecurityItem
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.theme.AuthenticatorTheme

class SecurityItemAdapter(
    private val authenticatorTheme: AuthenticatorTheme,
    private val timeAgo: TimeAgo,
    private val onAuthMethodSettingsClicked: (AuthMethod) -> Unit
) : ListAdapter<SecurityItem, SecurityItemAdapter.ViewHolder>(DIFF_CALLBACK) {
    
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SecurityItem>() {
            override fun areItemsTheSame(oldItem: SecurityItem, newItem: SecurityItem): Boolean =
                oldItem.type == newItem.type
            
            override fun areContentsTheSame(oldItem: SecurityItem, newItem: SecurityItem): Boolean =
                oldItem.verificationFlag?.state == newItem.verificationFlag?.state
                    && oldItem.contentDescriptionRes == newItem.contentDescriptionRes
                    && oldItem.helpMessageRes == newItem.helpMessageRes
                    && oldItem.titleRes == newItem.titleRes
            
        }
    }
    
    class ViewHolder(val binding: ItemSecurityBinding) : RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSecurityBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = currentList[position]
        val itemSecurityBinding: ItemSecurityBinding = holder.binding
        applyThemeToSecurityItem(itemSecurityBinding, item.type)
        itemSecurityBinding.securityTitle.setText(item.titleRes)
        val verificationFlag = item.verificationFlag
        val context = holder.itemView.context
        val checkWhen = context.getString(
            UiUtils.getStringResFromUserSetState(verificationFlag!!)
        )
        itemSecurityBinding.securityVerifiedwhen.text = checkWhen
        itemSecurityBinding.securitySettings.tag = position
        itemSecurityBinding.securitySettings.setOnClickListener { onAuthMethodSettingsClicked(item.type) }
        itemSecurityBinding.securitySettings.contentDescription =
            context.getString(item.contentDescriptionRes)
        itemSecurityBinding.securitySettings.alpha = 1.0f
        val listItemsUiProp = AuthenticatorUIManager.instance.config.themeObj().listItems
        itemSecurityBinding.root.background = listItemsUiProp.colorBg
        itemSecurityBinding.securityTitle.setTextColor(listItemsUiProp.colorText)
        itemSecurityBinding.securityVerifiedwhen.setTextColor(listItemsUiProp.colorText)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)
        else {
            val item = currentList[position]
            val verificationFlag = item.verificationFlag!!
            val context = holder.itemView.context
            val remainingMillis = payloads.last() as Long
            if (remainingMillis < 1000) {
                holder.binding.securityVerifiedwhen.setText(
                    UiUtils.getNOTStringResFromUserSetState(verificationFlag)
                )
                return
            }
            val remaining = timeAgo.timeAgoWithDiff(remainingMillis, false)
            val checkWhenExtra =
                context.getString(UiUtils.getStringResFromUserSetState(verificationFlag)) + String.format(
                    " %s",
                    context.getString(
                        R.string.ioa_sec_currentlychecking_extra_format,
                        remaining
                    )
                )
            holder.binding.securityVerifiedwhen.text = checkWhenExtra
        }
    }
    
    fun notifyTimerUpdate(item: SecurityItem, remainingMillis: Long) {
        val indexOfItem = currentList.indexOfFirst { it == item }
        if (indexOfItem < 0) return
        notifyItemChanged(indexOfItem, remainingMillis)
    }
    
    private fun applyThemeToSecurityItem(
        itemSecurityBinding: ItemSecurityBinding,
        securityItemType: AuthMethod
    ) {
        val authMethodsSecurityIcons =
            authenticatorTheme.methodsSecurityIcons
        val icon = itemSecurityBinding.securityIcon
        icon.visibility = authMethodsSecurityIcons.iconVisibility
        if (authMethodsSecurityIcons.colorIcon != null) {
            icon.setColorFilter(authMethodsSecurityIcons.colorIcon!!)
        }
        val resProvided = authMethodsSecurityIcons.resProvided
        if (!resProvided) {
            icon.imageTintList = null
        }
        when (securityItemType) {
            AuthMethod.PIN_CODE -> if (resProvided) {
                icon.setImageResource(authMethodsSecurityIcons.iconPinCodeRes!!)
            } else if (authMethodsSecurityIcons.iconPinCode != null) {
                icon.setImageDrawable(authMethodsSecurityIcons.iconPinCode)
            }
            AuthMethod.CIRCLE_CODE -> if (resProvided) {
                icon.setImageResource(authMethodsSecurityIcons.iconCircleCodeRes!!)
            } else if (authMethodsSecurityIcons.iconCircleCode != null) {
                icon.setImageDrawable(authMethodsSecurityIcons.iconCircleCode)
            }
            AuthMethod.LOCATIONS -> if (resProvided) {
                icon.setImageResource(authMethodsSecurityIcons.iconGeofencingRes!!)
            } else if (authMethodsSecurityIcons.iconGeofencing != null) {
                icon.setImageDrawable(authMethodsSecurityIcons.iconGeofencing)
            }
            AuthMethod.WEARABLES -> if (resProvided) {
                icon.setImageResource(authMethodsSecurityIcons.iconWearableRes!!)
            } else if (authMethodsSecurityIcons.iconWearable != null) {
                icon.setImageDrawable(authMethodsSecurityIcons.iconWearable)
            }
            AuthMethod.BIOMETRIC -> if (resProvided) {
                icon.setImageResource(authMethodsSecurityIcons.iconFingerprintScanRes!!)
            } else if (authMethodsSecurityIcons.iconFingerprintScan != null) {
                icon.setImageDrawable(authMethodsSecurityIcons.iconFingerprintScan)
            }
            else -> throw IllegalArgumentException("Invalid AuthMethod")
        }
    }
}