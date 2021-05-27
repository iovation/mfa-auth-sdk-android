package com.launchkey.android.authenticator.sdk.ui.internal.auth_method.pin_code

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.ItemPinCodeRequirementBinding

class PinCodeRequirementAdapter :
    ListAdapter<PINCodeRequirement, PinCodeRequirementAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<PINCodeRequirement> =
            object : DiffUtil.ItemCallback<PINCodeRequirement>() {
                override fun areItemsTheSame(
                    oldItem: PINCodeRequirement,
                    newItem: PINCodeRequirement
                ): Boolean {
                    return oldItem.requirement === newItem.requirement
                }

                override fun areContentsTheSame(
                    oldItem: PINCodeRequirement,
                    newItem: PINCodeRequirement
                ): Boolean {
                    return oldItem.requirementTextRes == newItem.requirementTextRes
                }
            }
    }

    class ViewHolder(
        private val requirementTextView: TextView
    ) : RecyclerView.ViewHolder(requirementTextView) {
        fun setRequirementMet(requirementMet: Boolean) {
            requirementTextView.setTextColor(
                ContextCompat.getColor(
                    requirementTextView.context,
                    if (requirementMet) R.color.ioa_ar_authorized
                    else R.color.lk_gray_dark
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPinCodeRequirementBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root,

            )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requirementTextView = holder.itemView as TextView
        val pinCodeRequirement = getItem(position)
        requirementTextView.setText(pinCodeRequirement!!.requirementTextRes)
        requirementTextView.tag = pinCodeRequirement
    }
}