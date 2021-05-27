/*
 * Copyright (c) 2016. LaunchKey, Inc. All rights reserved.
 */
package com.launchkey.android.authenticator.sdk.ui.fragment

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.snackbar.Snackbar
import com.launchkey.android.authenticator.sdk.ui.R
import com.launchkey.android.authenticator.sdk.ui.adapter.SessionsAdapter
import com.launchkey.android.authenticator.sdk.ui.databinding.FragmentListSessionsBinding
import com.launchkey.android.authenticator.sdk.ui.internal.common.TimeAgo
import com.launchkey.android.authenticator.sdk.ui.internal.util.BaseAppCompatFragment
import com.launchkey.android.authenticator.sdk.ui.internal.util.CoreExceptionToMessageConverter
import com.launchkey.android.authenticator.sdk.ui.internal.util.UiUtils
import com.launchkey.android.authenticator.sdk.ui.internal.util.viewBinding
import java.util.*

class SessionsFragment :
    BaseAppCompatFragment(R.layout.fragment_list_sessions),
    OnRefreshListener {
    private lateinit var sessionsAdapter: SessionsAdapter
    private val binding: FragmentListSessionsBinding by viewBinding(FragmentListSessionsBinding::bind)
    private val sessionsViewModel: SessionsViewModel by viewModels { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeObservers()
    }

    override fun onRefresh() {
        sessionsViewModel.refreshSessions()
    }

    private fun setupUi() {
        binding.sessionsSwiperefresh.setOnRefreshListener(this)
        binding.sessionsSwiperefresh.setColorSchemeColors(
            UiUtils.getColorFromTheme(activity, R.attr.authenticatorColorAccent, R.color.lk_accent)
        )

        // Sessions List
        val deleteIconSize = resources.getDimensionPixelSize(R.dimen.lk_session_delete_icon_size)
        val deleteIconDrawable = Objects.requireNonNull(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_clear_black_24dp
            )
        )

        val deleteIcon: Drawable = BitmapDrawable(
            resources,
            Bitmap.createScaledBitmap(
                (deleteIconDrawable as BitmapDrawable).bitmap,
                deleteIconSize,
                deleteIconSize,
                true
            )
        )

        deleteIcon.alpha = 255 / 2
        val background = ColorDrawable(
            ContextCompat.getColor(
                requireContext(),
                R.color.lk_authorization_back_background
            )
        )

        sessionsAdapter = SessionsAdapter(TimeAgo(requireContext()))
        val deleteSwipedItemCallback =
            SwipeSimpleCallback(deleteIcon, background) { itemAdapterPosition ->
                sessionsAdapter.notifyItemChanged(itemAdapterPosition)
                sessionsViewModel.endSession(sessionsAdapter.getSession(itemAdapterPosition))
            }

        with(binding.sessionsList) {
            adapter = sessionsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            ItemTouchHelper(deleteSwipedItemCallback).attachToRecyclerView(this)
        }
    }

    private fun subscribeObservers() {
        sessionsViewModel.sessionState.observe(viewLifecycleOwner) { state ->
            binding.sessionsSwiperefresh.isRefreshing =
                state is SessionsViewModel.SessionState.Loading

            when (state) {
                is SessionsViewModel.SessionState.Failed -> {
                    val failureMessage = CoreExceptionToMessageConverter.convert(
                        state.failure,
                        requireContext()
                    )

                    Snackbar.make(binding.root, failureMessage, Snackbar.LENGTH_LONG).show()
                }
                is SessionsViewModel.SessionState.GetSessionsSuccess -> {
                    sessionsAdapter.submitItems(state.sessions)
                    binding.sessionsEmpty.visibility =
                        if (state.sessions.isEmpty()) View.VISIBLE else View.GONE
                }
                is SessionsViewModel.SessionState.EndSessionSuccess -> {
                    sessionsAdapter.removeSession(state.session)
                    binding.sessionsEmpty.visibility =
                        if (sessionsAdapter.itemCount == 0) View.VISIBLE else View.GONE
                }
                else -> Unit
            }
        }
    }

    class SwipeSimpleCallback(
        private val iconToReveal: Drawable,
        private val backgroundToReveal: ColorDrawable,
        private val onItemSwiped: (swipedItemAdapterPosition: Int) -> Unit
    ) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onItemSwiped(viewHolder.adapterPosition)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
            val itemView = viewHolder.itemView
            val backgroundCornerOffset = 0
            val deleteIconHorizontalMargin = (itemView.height - iconToReveal.intrinsicHeight) / 2
            val deleteIconTopMargin =
                itemView.top + (itemView.height - iconToReveal.intrinsicHeight) / 2
            val deleteIconBottomMargin = deleteIconTopMargin + iconToReveal.intrinsicHeight
            when {
                dX > 0 -> { // Swiping to the right
                    val deleteIconRight = deleteIconHorizontalMargin + iconToReveal.intrinsicWidth
                    iconToReveal.setBounds(
                        deleteIconHorizontalMargin,
                        deleteIconTopMargin,
                        deleteIconRight,
                        deleteIconBottomMargin
                    )
                    backgroundToReveal.setBounds(
                        itemView.left, itemView.top,
                        itemView.left + dX.toInt() + backgroundCornerOffset,
                        itemView.bottom
                    )
                }
                dX < 0 -> { // Swiping to the left
                    val deleteIconLeft =
                        itemView.right - deleteIconHorizontalMargin - iconToReveal.intrinsicWidth
                    val deleteIconRight = itemView.right - deleteIconHorizontalMargin
                    iconToReveal.setBounds(
                        deleteIconLeft,
                        deleteIconTopMargin,
                        deleteIconRight,
                        deleteIconBottomMargin
                    )
                    backgroundToReveal.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom
                    )
                }
                else -> { // view is unSwiped
                    backgroundToReveal.setBounds(0, 0, 0, 0)
                }
            }
            backgroundToReveal.draw(c)
            iconToReveal.draw(c)
        }
    }
}