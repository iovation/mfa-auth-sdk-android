/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.internal.linking

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.abhi.barcode.frag.libv2.BarcodeFragment
import com.abhi.barcode.frag.libv2.IScanResultHandler
import com.abhi.barcode.frag.libv2.ScanResult
import com.google.zxing.BarcodeFormat
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentLinkScancodeBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding
import java.util.*

class LinkScanCodeFragment : BaseAppCompatFragment(R.layout.fragment_link_scancode), IScanResultHandler {
    companion object {
        private const val SCANNER_RESTART = 1500L
    }

    private lateinit var scanner: BarcodeFragment
    private val handler = Handler(Looper.getMainLooper())
    private val linkViewModel: LinkViewModel by activityViewModels()
    private val binding: FragmentLinkScancodeBinding by viewBinding(FragmentLinkScancodeBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    override fun onResume() {
        super.onResume()
        setScannerVisibility(true)
    }

    private fun setupUi() {
        scanner = childFragmentManager.findFragmentById(R.id.pair_scancode_camera) as BarcodeFragment
        scanner.setDecodeFor(EnumSet.of(BarcodeFormat.QR_CODE))
        scanner.scanResultHandler = this
        linkViewModel.state.observe(viewLifecycleOwner, Observer { state ->
            if (state is LinkViewModel.State.Failed) {
                setScannerVisibility(true)
                scanner.restart()
                // Activity will automatically display the right message
            }
        })
    }

    private fun setScannerVisibility(scannerVisible: Boolean) {
        binding.pairScancodeCover.visibility = View.VISIBLE
        if (scannerVisible) {
            val anim = AnimationUtils.loadAnimation(activity, R.anim.alpha_out)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    binding.pairScancodeCover.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            binding.pairScancodeCover.startAnimation(anim)
        }
    }

    override fun scanResult(scanResult: ScanResult) {
        val code = if (scanResult.rawResult.text == null) null else scanResult.rawResult.text.trim { it <= ' ' }
        if (code != null && code.isNotEmpty()) {
            onCodeObtained(code)
        } else {
            UiUtils.toast(activity, R.string.ioa_link_scancode_error_retry, true)
            handler.postDelayed({
                setScannerVisibility(true)
                scanner.restart()
            }, SCANNER_RESTART)
        }
    }

    private fun onCodeObtained(code: String) {
        setScannerVisibility(false)
        linkViewModel.codeReady(code)
    }
}