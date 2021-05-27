/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.snackbar.Snackbar
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentListDevicesBinding
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.CoreExceptionToMessageConverter
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding

class DevicesFragment :
    BaseAppCompatFragment(R.layout.fragment_list_devices),
    OnRefreshListener {
    private lateinit var deviceAdapter: DeviceAdapter
    private val devicesViewModel by viewModels<DevicesViewModel> { defaultViewModelProviderFactory }
    private val binding: FragmentListDevicesBinding by viewBinding(FragmentListDevicesBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeObservers()
    }

    override fun onRefresh() {
        devicesViewModel.refreshDevices()
    }

    private fun setupUi() {
        binding.devicesSwiperefresh.setOnRefreshListener(this)
        binding.devicesSwiperefresh.setColorSchemeColors(
            UiUtils.getColorFromTheme(
                activity,
                R.attr.authenticatorColorAccent,
                R.color.lk_accent
            )
        )

        deviceAdapter = DeviceAdapter {
            devicesViewModel.unlinkDevice(it)
        }

        with(binding.devicesRecyclerView) {
            adapter = deviceAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun showSnackbar(m: String) {
        Snackbar.make(binding.devicesSwiperefresh, m, Snackbar.LENGTH_LONG).show()
    }

    private fun updateDevicesRecyclerViewVisibility(showList: Boolean) {
        if (showList) {
            binding.devicesEmpty.visibility = View.INVISIBLE
            binding.devicesRecyclerView.visibility = View.VISIBLE
        } else {
            binding.devicesEmpty.visibility = View.VISIBLE
            binding.devicesRecyclerView.visibility = View.INVISIBLE
        }
    }

    private fun subscribeObservers() {
        devicesViewModel.state.observe(viewLifecycleOwner, { state ->
            binding.devicesSwiperefresh.isRefreshing = state is DevicesViewModel.State.Loading
            when (state) {
                is DevicesViewModel.State.GetDevicesSuccess -> {
                    deviceAdapter.submitList(state.devices)
                    updateDevicesRecyclerViewVisibility(state.devices.isNotEmpty())
                }
                is DevicesViewModel.State.UnlinkDeviceSuccess -> {
                    val unlinkedDevice = state.unlinkedDevice
                    val message =
                        getString(
                            R.string.ioa_misc_devices_deviceunlinked_format,
                            unlinkedDevice.name
                        )
                    showSnackbar(message)
                    if (unlinkedDevice.isCurrent) {
                        deviceAdapter.submitList(emptyList())
                        updateDevicesRecyclerViewVisibility(false)
                    } else {
                        deviceAdapter.submitList(
                            deviceAdapter.currentList.filter { it != unlinkedDevice }
                        )
                    }
                }
                is DevicesViewModel.State.Failed -> {
                    showSnackbar(
                        CoreExceptionToMessageConverter.convert(
                            state.failure,
                            requireContext()
                        )
                    )
                }
                else -> Unit
            }
        })
    }
}