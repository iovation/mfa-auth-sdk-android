/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.database.DataSetObserver
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.launchkey.android.authenticator.sdk.ui.databinding.ItemItemlistItemBinding
import com.launchkey.android.authenticator.sdk.ui.internal.dialog.AlertDialogFragment

class ItemListDialogFragment : AlertDialogFragment() {
    private var items: Array<IconItem>? = null
    private val itemListDialogViewModel: ItemListDialogViewModel by viewModels()
    private var listener: DialogInterface.OnClickListener? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (items == null) {
            items = itemListDialogViewModel.items
        } else {
            itemListDialogViewModel.items = items
        }
        setAdapter(IconItemAdapter()) { dialog, which -> listener?.onClick(dialog, which) }
        return super.onCreateDialog(savedInstanceState)
    }
    
    fun setOnClickListener(cancelListener: DialogInterface.OnClickListener?) {
        listener = cancelListener
    }
    
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onClick(dialog, -1)
    }
    
    data class IconItem(
        val iconRes: Int = 0,
        val labelRes: Int = 0,
        val secondaryLabelRes: Int = 0
    )
    
    private inner class IconItemAdapter : ListAdapter {
        override fun areAllItemsEnabled(): Boolean {
            return false
        }
        
        override fun isEnabled(position: Int): Boolean {
            return true
        }
        
        override fun registerDataSetObserver(observer: DataSetObserver) {}
        override fun unregisterDataSetObserver(observer: DataSetObserver) {}
        override fun getCount(): Int {
            return if (itemListDialogViewModel.items == null) 0 else itemListDialogViewModel.items!!.size
        }
        
        override fun getItem(position: Int): IconItem {
            return itemListDialogViewModel.items!![position]
        }
        
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        
        override fun hasStableIds(): Boolean {
            return false
        }
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val convertView = convertView ?: LayoutInflater.from(activity)
                .inflate(R.layout.item_itemlist_item, parent, false)
            
            val itemItemlistItemBinding = ItemItemlistItemBinding.bind(convertView)
            val item = getItem(position)
            val hasDescription: Boolean = item.secondaryLabelRes > 0
            itemItemlistItemBinding.itemlistTextName.setText(item.labelRes)
            itemItemlistItemBinding.itemlistTextDesc.visibility =
                if (hasDescription) View.VISIBLE else View.GONE
            if (hasDescription) {
                itemItemlistItemBinding.itemlistTextDesc.setText(item.secondaryLabelRes)
            }
            itemItemlistItemBinding.itemlistImage.setImageResource(item.iconRes)
            return convertView
        }
        
        override fun getItemViewType(position: Int): Int {
            return 0
        }
        
        override fun getViewTypeCount(): Int {
            return 1
        }
        
        override fun isEmpty(): Boolean {
            return false
        }
    }
    
    class ItemListDialogViewModel : ViewModel() {
        var title: String? = null
        var items: Array<IconItem>? = null
    }
    
    companion object {
        @JvmStatic
        fun show(
            context: Context,
            fm: FragmentManager,
            titleRes: Int,
            items: Array<IconItem>?,
            tag: String? = ItemListDialogFragment::class.java.simpleName,
            itemListener: DialogInterface.OnClickListener?
        ): ItemListDialogFragment {
            val arguments = Bundle()
            arguments.putString(TITLE_ARG, context.getString(titleRes))
            arguments.putString(
                NEGATIVE_BUTTON_TEXT_ARG,
                context.getString(R.string.ioa_generic_cancel)
            )
            return ItemListDialogFragment().apply {
                setOnClickListener(itemListener)
                this.items = items
                show(fm, tag)
            }
        }
    }
}