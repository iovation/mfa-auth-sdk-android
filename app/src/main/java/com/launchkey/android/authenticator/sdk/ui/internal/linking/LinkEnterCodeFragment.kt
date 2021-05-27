/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.linking

import android.content.Context
import android.os.Bundle
import android.text.*
import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentLinkEntercodeBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding
import java.util.*

class LinkEnterCodeFragment : BaseAppCompatFragment(R.layout.fragment_link_entercode) {
    companion object {
        private const val REGEX_ALPHANUMERIC = "[a-zA-Z0-9]+"
        private const val ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }

    private var codeMaxLength: Int = 0
    private val linkViewModel: LinkViewModel by activityViewModels()
    private val binding: FragmentLinkEntercodeBinding by viewBinding(FragmentLinkEntercodeBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    override fun onResume() {
        super.onResume()
        focusAndStartCode()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun setupUi() {
        codeMaxLength = resources.getInteger(R.integer.lk_link_code_length)
        val filters = arrayOf(
                AllCaps(),
                LengthFilter(codeMaxLength),
                LinkingCodeFilter(ALLOWED_CHARS)
        )
        binding.linkEntercodeCode.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        binding.linkEntercodeCode.filters = filters
        binding.linkEntercodeCode.addTextChangedListener {
            binding.pairEntercodeButtonDone.visibility = if (isInputProperLength()) View.VISIBLE else View.INVISIBLE
        }
        binding.linkEntercodeCode.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (isInputProperLength()) {
                    donePressed()
                    return@OnEditorActionListener true
                }
            }
            false
        })
        binding.pairEntercodeButtonDone.setOnClickListener {
            donePressed()
        }
        linkViewModel.state.observe(viewLifecycleOwner, Observer { state ->
            if (state is LinkViewModel.State.Failed) {
                clearCode()
                showKeyboard(binding.linkEntercodeCode)
                // Activity will automatically display the right message
            }
        })
    }

    private fun donePressed() {
        hideKeyboard()
        obtainCode()
    }

    private fun isInputProperLength(): Boolean {
        return binding.linkEntercodeCode.text.toString().trim { it <= ' ' }.length == codeMaxLength
    }

    private fun obtainCode() {
        onCodeObtained(binding.linkEntercodeCode.text.toString().trim { it <= ' ' })
    }

    private fun onCodeObtained(code: String) {
        if (code.matches(Regex(REGEX_ALPHANUMERIC))) {
            linkViewModel.codeReady(code.toLowerCase(Locale.US))
        } else {
            clearCode()
            focusAndStartCode()
            //following error message should not be shown since the input filters invalid characters
            linkViewModel.exception(InvalidRegexLinkingException())
        }
    }

    private fun focusAndStartCode() {
        binding.linkEntercodeCode.requestFocus()
        showKeyboard(binding.linkEntercodeCode)
    }

    private fun clearCode() {
        binding.linkEntercodeCode.setText("")
    }

    private fun showKeyboard(view: View) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }

    private fun hideKeyboard() {
        val focused = requireActivity().currentFocus
        if (focused != null) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(focused.windowToken, 0)
        }
    }

    private class LinkingCodeFilter internal constructor(private val allowedCharacters: String) : InputFilter {
        private fun isCharacterAllowed(character: Char): Boolean {
            return allowedCharacters.contains(character.toString())
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
            var changed = false
            val sb = StringBuilder()
            var character: Char
            for (i in start until end) {
                character = source[i]
                if (isCharacterAllowed(character)) {
                    sb.append(character)
                } else if (!changed) {
                    changed = true
                }
            }
            return sb.toString()
        }

    }
}