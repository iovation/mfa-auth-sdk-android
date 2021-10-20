package com.launchkey.android.authenticator.sdk.ui.internal.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.DialogFragmentSetnameBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class SetNameDialogFragment : AlertDialogFragment() {
    private var hint: String? = null
    private var defaultTextValue: String? = null
    private var setNameListener: SetNameListener? = null
    private val binding: DialogFragmentSetnameBinding by viewBinding(DialogFragmentSetnameBinding::bind)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogFragmentSetnameBinding.inflate(
            LayoutInflater.from(requireActivity()), null, false
        )
        val arguments = arguments
        if (arguments != null) {
            hint = arguments.getString(HINT_ARG)
            defaultTextValue = arguments.getString(DEFAULT_TEXT_ARG)
        }
        val editText = binding.setnameEditName
        if (hint != null) {
            binding.setnameTilName.hint = hint
        }
        if (defaultTextValue != null) {
            editText.setText(defaultTextValue)
        }
        editText.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (dialog != null && dialog is AlertDialog) {
                    val positiveButton =
                        (dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton?.performClick()
                    return@OnEditorActionListener true
                }
            }
            false
        })

        return binding.root
    }

    // If you're wondering why we do this it's because AlertDialogs dismiss themselves whenever any
    // button is clicked on, including the positive one, we're preventing that from happening by
    // doing this hack
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog is AlertDialog) {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener { processInput(setNameListener) }
        }
    }

    private fun processInput(setNameListener: SetNameListener?) {
        val editText = binding.setnameEditName
        if (setNameListener != null) {
            val cleanedName: String = editText.text.toString().trim()
            setNameListener.onNameSet(this, cleanedName)
        }
    }

    fun setErrorMessage(errorMessage: String?) {
        val textInputLayout = binding.setnameTilName
        textInputLayout.isErrorEnabled = errorMessage != null
        textInputLayout.error = errorMessage
    }

    fun setPositiveButtonClickListener(setNameListener: SetNameListener?) {
        this.setNameListener = setNameListener
    }

    fun interface SetNameListener {
        /**
         * @param dialog  Reference to the dialog making use
         * of the implemented validator.
         * @param name    Name entered by the user.
         */
        fun onNameSet(dialog: SetNameDialogFragment?, name: String?)
    }

    companion object {
        private const val HINT_ARG = "hint"
        private const val DEFAULT_TEXT_ARG = "default_text"

        @Deprecated(message = "Use alternative show method with corresponding viewmodel")
        @JvmStatic
        fun show(
            context: Context,
            fm: FragmentManager,
            titleResourceId: Int,
            hintRes: Int,
            positiveButtonTextRes: Int,
            defaultValue: String?,
            onNameSet: SetNameListener?,
            onCancel: DialogInterface.OnCancelListener?,
            cancellable: Boolean
        ): SetNameDialogFragment {

            val arguments = Bundle()
            arguments.putString(TITLE_ARG, context.getString(titleResourceId))
            arguments.putString(POSITIVE_BUTTON_ARG, context.getString(positiveButtonTextRes))
            arguments.putString(
                NEGATIVE_BUTTON_TEXT_ARG,
                context.getString(R.string.ioa_generic_cancel)
            )
            arguments.putString(HINT_ARG, context.getString(hintRes))
            arguments.putString(DEFAULT_TEXT_ARG, defaultValue)
            arguments.putBoolean(CANCELLABLE_ARG, cancellable)
            val dialog = SetNameDialogFragment()
            dialog.arguments = arguments
            dialog.setPositiveButtonClickListener(onNameSet)
            dialog.setCancelListener(onCancel)
            dialog.show(fm, SetNameDialogFragment::class.java.simpleName)
            return dialog
        }

        @JvmStatic
        fun show(
            context: Context,
            fragmentManager: FragmentManager,
            titleResourceId: Int,
            hintRes: Int,
            positiveButtonTextRes: Int,
            defaultValue: String?,
            tag: String = SetNameDialogFragment::class.java.simpleName
        ): SetNameDialogFragment = SetNameDialogFragment().apply {
            arguments = Bundle().apply {
                putString(TITLE_ARG, context.getString(titleResourceId))
                putString(POSITIVE_BUTTON_ARG, context.getString(positiveButtonTextRes))
                putString(NEGATIVE_BUTTON_TEXT_ARG, context.getString(R.string.ioa_generic_cancel))
                putString(HINT_ARG, context.getString(hintRes))
                putString(DEFAULT_TEXT_ARG, defaultValue)
                putBoolean(CANCELLABLE_ARG, false)
            }
            show(fragmentManager, tag)
        }
    }
}