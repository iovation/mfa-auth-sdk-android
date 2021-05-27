package com.launchkey.android.authenticator.sdk.ui.internal.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.ListAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.launchkey.android.authenticator.sdk.ui.AuthenticatorUIManager
import com.launchkey.android.authenticator.sdk.ui.R

open class AlertDialogFragment : DialogFragment() {
    companion object {
        const val TITLE_ARG = "title"
        const val MESSAGE_ARG = "message"
        const val POSITIVE_BUTTON_ARG = "positive_button"
        const val NEGATIVE_BUTTON_TEXT_ARG = "negative_button_text"
        const val TAG_ARG = "tag"
        const val CANCELLABLE_ARG = "cancellable"
        private fun newThemedAlertDialogBuilder(context: Context): AlertDialog.Builder {
            val theme = AuthenticatorUIManager.instance.config.theme()
            val a = context.obtainStyledAttributes(
                    theme, intArrayOf(R.attr.alertDialogTheme))
            val alertDialogTheme = a.getResourceId(0, R.style.AuthenticatorAlertDialogStyle)
            a.recycle()
            return AlertDialog.Builder(context, alertDialogTheme)
        }
    }

    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var negativeButtonClickListener: DialogInterface.OnClickListener? = null
    private var cancelListener: DialogInterface.OnCancelListener? = null
    private var adapter: ListAdapter? = null
    private var adapterListener: DialogInterface.OnClickListener? = null
    protected val dialogFragmentViewModel: DialogFragmentViewModel by lazy {
        val arguments = arguments
        val tag = if (arguments != null) {
            if (arguments.containsKey(TAG_ARG)) arguments.getString(TAG_ARG)!! else javaClass.simpleName
        } else {
            javaClass.simpleName
        }
        val parentFragment = parentFragment
        if (parentFragment != null) {
            ViewModelProvider(requireParentFragment()).get(tag, DialogFragmentViewModel::class.java)
        } else {
            ViewModelProvider(requireActivity()).get(tag, DialogFragmentViewModel::class.java)
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title: String?
        val message: String?
        val positiveButtonText: String?
        val negativeButtonText: String?
        val arguments = arguments
        if (arguments != null) {
            title = arguments.getString(TITLE_ARG)
            message = arguments.getString(MESSAGE_ARG)
            positiveButtonText = arguments.getString(POSITIVE_BUTTON_ARG)
            negativeButtonText = arguments.getString(NEGATIVE_BUTTON_TEXT_ARG)
        } else {
            title = null
            message = null
            positiveButtonText = null
            negativeButtonText = null
        }
        val accessibilityManager = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val includeTitle = (!accessibilityManager.isEnabled || !accessibilityManager.isTouchExplorationEnabled)
        val alertDialogBuilder = newThemedAlertDialogBuilder(requireContext())
        if (includeTitle && title != null) {
            alertDialogBuilder.setTitle(title)
        }
        if (message != null) {
            alertDialogBuilder.setMessage(message)
        }
        if (positiveButtonText != null) {
            alertDialogBuilder.setPositiveButton(
                    positiveButtonText
            ) { dialog, which ->
                if (positiveButtonClickListener != null) {
                    positiveButtonClickListener!!.onClick(dialog, which)
                }
                dialogFragmentViewModel.changeState(DialogFragmentViewModel.State.Gone)
            }
        }
        if (negativeButtonText != null) {
            alertDialogBuilder.setNegativeButton(
                    negativeButtonText
            ) { dialog, which ->
                if (negativeButtonClickListener != null) {
                    negativeButtonClickListener!!.onClick(dialog, which)
                }
                dialogFragmentViewModel.changeState(DialogFragmentViewModel.State.Gone)
            }
        }
        if (adapter != null) {
            alertDialogBuilder.setAdapter(adapter) { dialog, which ->
                if (adapterListener != null) {
                    adapterListener!!.onClick(dialog, which)
                }
                dialogFragmentViewModel.changeState(DialogFragmentViewModel.State.Gone)
            }
        }
        val alertDialog = alertDialogBuilder.create()
        val messageView = alertDialog.findViewById<TextView>(android.R.id.message)
        if (messageView != null) {
            messageView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        }
        return alertDialog
    }

    // Unfortunately adding an AlertDialog instead of a Dialog messes up this system since
    // AlertDialog's have their own view already, setContentView does not work and we do this
    // instead if a derived class of AlertDialogFragment overrided onCreateView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (dialog as AlertDialog?)!!.setView(getView())
    }
    
    override fun onResume() {
        super.onResume()
        dialogFragmentViewModel.changeState(DialogFragmentViewModel.State.Shown)
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val layoutInflater = super.onGetLayoutInflater(savedInstanceState)
        // They tell you not to call Dialog::setCancelable directly on the Dialog in the
        // DialogFragment docs but you also can't call DialogFragment::setCancelable on the
        // DialogFragment until after DialogFragment::onCreateDialog. So we're just gunna do it here
        // for now.
        val cancellable: Boolean?
        val arguments = arguments
        cancellable = if (arguments != null && arguments.containsKey(CANCELLABLE_ARG)) {
            arguments.getBoolean(CANCELLABLE_ARG)
        } else {
            null
        }
        if (cancellable != null) {
            isCancelable = cancellable
        }
        return layoutInflater
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        val cancelListener = cancelListener
        cancelListener?.onCancel(dialog)
        dialogFragmentViewModel.changeState(DialogFragmentViewModel.State.Gone)
    }

    fun setPositiveButtonClickListener(positiveButtonClickListener: DialogInterface.OnClickListener?) {
        this.positiveButtonClickListener = positiveButtonClickListener
    }

    fun setNegativeButtonClickListener(negativeButtonClickListener: DialogInterface.OnClickListener?) {
        this.negativeButtonClickListener = negativeButtonClickListener
    }

    fun setCancelListener(cancelListener: DialogInterface.OnCancelListener?) {
        this.cancelListener = cancelListener
    }

    fun setAdapter(adapter: ListAdapter?, adapterListener: DialogInterface.OnClickListener?) {
        this.adapter = adapter
        this.adapterListener = adapterListener
    }

    class Builder {
        private var title: String? = null
        private var message: String? = null
        private var positiveButtonText: String? = null
        private var negativeButtonText: String? = null
        private var tag: String? = null
        private var cancellable: Boolean? = null
        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setPositiveButtonText(positiveButtonText: String): Builder {
            this.positiveButtonText = positiveButtonText
            return this
        }

        fun setNegativeButtonText(negativeButtonText: String): Builder {
            this.negativeButtonText = negativeButtonText
            return this
        }

        fun setTag(tag: String?): Builder {
            this.tag = tag
            return this
        }

        fun setCancellable(cancellable: Boolean): Builder {
            this.cancellable = cancellable
            return this
        }

        fun build(): AlertDialogFragment {
            val alertDialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            if (title != null) bundle.putString(TITLE_ARG, title)
            if (message != null) bundle.putString(MESSAGE_ARG, message)
            if (positiveButtonText != null) bundle.putString(POSITIVE_BUTTON_ARG, positiveButtonText)
            if (negativeButtonText != null) bundle.putString(NEGATIVE_BUTTON_TEXT_ARG, negativeButtonText)
            if (tag != null) bundle.putString(TAG_ARG, tag)
            if (cancellable != null) bundle.putBoolean(CANCELLABLE_ARG, cancellable!!)
            alertDialogFragment.arguments = bundle
            return alertDialogFragment
        }
    }
}