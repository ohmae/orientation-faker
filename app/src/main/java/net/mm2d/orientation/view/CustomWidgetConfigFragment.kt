/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.FragmentCustomWidgetConfigBinding
import net.mm2d.color.chooser.ColorChooserDialog
import net.mm2d.orientation.control.FunctionButton
import net.mm2d.orientation.control.Functions
import net.mm2d.orientation.room.WidgetSettingEntity
import net.mm2d.orientation.util.alpha
import net.mm2d.orientation.util.autoCleared
import net.mm2d.orientation.util.opaque
import net.mm2d.orientation.view.dialog.IconShapeDialog
import net.mm2d.orientation.view.dialog.ResetThemeDialog
import net.mm2d.orientation.view.widget.CustomWidgetProvider
import net.mm2d.orientation.view.widget.config.DragItemAdapter
import net.mm2d.orientation.view.widget.config.InvisibleDragHelper
import net.mm2d.orientation.view.widget.config.VisibleDragHelper

@AndroidEntryPoint
class CustomWidgetConfigFragment : Fragment(R.layout.fragment_custom_widget_config) {
    private var binding: FragmentCustomWidgetConfigBinding by autoCleared()
    private val args: CustomWidgetConfigFragmentArgs by navArgs()
    private val viewModel: CustomWidgetConfigViewModel by viewModels()
    private var visibleAdapter: DragItemAdapter? = null
    private var invisibleAdapter: DragItemAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCustomWidgetConfigBinding.bind(view)

        binding.visibleItems.itemAnimator?.moveDuration = 100L
        binding.invisibleItems.itemAnimator?.moveDuration = 100L
        val activity = requireActivity()
        viewModel.getParam(args.appWidgetId).observe(viewLifecycleOwner) {
            binding.iconShapeIcon.setImageResource(it.shape.iconId)
            binding.iconShapeTitle.setText(it.shape.textId)
            binding.sampleForeground.setImageColor(it.foreground)
            binding.foreground.setOnClickListener { _ ->
                ColorChooserDialog.show(
                    this, REQUEST_KEY_FOREGROUND, it.foreground, true
                )
            }
            binding.sampleBackground.setImageColor(it.background)
            binding.background.setOnClickListener { _ ->
                ColorChooserDialog.show(
                    this, REQUEST_KEY_BACKGROUND, it.background, true
                )
            }
            binding.sampleForegroundSelected.setImageColor(it.foregroundSelected)
            binding.foregroundSelected.setOnClickListener { _ ->
                ColorChooserDialog.show(
                    this, REQUEST_KEY_FOREGROUND_SELECTED, it.foregroundSelected, true
                )
            }
            binding.sampleBackgroundSelected.setImageColor(it.backgroundSelected)
            binding.backgroundSelected.setOnClickListener { _ ->
                ColorChooserDialog.show(
                    this, REQUEST_KEY_BACKGROUND_SELECTED, it.backgroundSelected, true
                )
            }
            binding.sampleBase.setImageColor(it.base)
            binding.base.setOnClickListener { _ ->
                ColorChooserDialog.show(
                    this, REQUEST_KEY_BASE, it.base, true
                )
            }
            binding.visibleItems.setBackgroundColor(it.base)
            binding.invisibleItems.setBackgroundColor(it.base)

            val visibleAdapter = visibleAdapter ?: createVisibleAdapter(activity, it)
            val invisibleAdapter = invisibleAdapter ?: createInvisibleAdapter(activity, it)
            visibleAdapter.updateWidgetSetting(it)
            invisibleAdapter.updateWidgetSetting(it)
        }

        setUpViews()
        registerDialogListener()
    }

    override fun onStop() {
        super.onStop()
        visibleAdapter?.let { adapter ->
            viewModel.updateFunctions(adapter.currentList.map { it.function })
            CustomWidgetProvider.update(requireContext(), args.appWidgetId)
        }
    }

    private fun createVisibleAdapter(activity: Activity, widgetSetting: WidgetSettingEntity): DragItemAdapter =
        DragItemAdapter(activity, widgetSetting, VisibleDragHelper()).also { adapter ->
            visibleAdapter = adapter
            binding.visibleItems.adapter = adapter
            widgetSetting.functions
                .mapNotNull { Functions.find(it) }
                .let { adapter.submitList(it) }
        }

    private fun createInvisibleAdapter(activity: Activity, widgetSetting: WidgetSettingEntity): DragItemAdapter =
        DragItemAdapter(activity, widgetSetting, InvisibleDragHelper()).also { adapter ->
            invisibleAdapter = adapter
            binding.invisibleItems.adapter = adapter
            FunctionButton.all()
                .subtract(widgetSetting.functions.toSet())
                .mapNotNull { Functions.find(it) }
                .let { adapter.submitList(it) }
        }

    private fun ImageView.setImageColor(@ColorInt color: Int) {
        setColorFilter(color.opaque())
        imageAlpha = color.alpha()
    }

    private fun setUpViews() {
        binding.resetTheme.setOnClickListener {
            ResetThemeDialog.show(this, REQUEST_KEY_RESET_THEME)
        }
        binding.iconShape.setOnClickListener {
            IconShapeDialog.show(this, REQUEST_KEY_SHAPE)
        }
    }

    private fun registerDialogListener() {
        ColorChooserDialog.registerListener(this, REQUEST_KEY_FOREGROUND, {
            viewModel.updateForeground(it)
        }, null)
        ColorChooserDialog.registerListener(this, REQUEST_KEY_BACKGROUND, {
            viewModel.updateBackground(it)
        }, null)
        ColorChooserDialog.registerListener(this, REQUEST_KEY_FOREGROUND_SELECTED, {
            viewModel.updateForegroundSelected(it)
        }, null)
        ColorChooserDialog.registerListener(this, REQUEST_KEY_BACKGROUND_SELECTED, {
            viewModel.updateBackgroundSelected(it)
        }, null)
        ColorChooserDialog.registerListener(this, REQUEST_KEY_BASE, {
            viewModel.updateBase(it)
        }, null)
        ResetThemeDialog.registerListener(this, REQUEST_KEY_RESET_THEME) {
            viewModel.resetTheme()
        }
        IconShapeDialog.registerListener(this, REQUEST_KEY_SHAPE) {
            viewModel.updateShape(it)
        }
    }

    companion object {
        private const val PREFIX = "CustomWidgetConfigFragment."
        private const val REQUEST_KEY_FOREGROUND = PREFIX + "REQUEST_KEY_FOREGROUND"
        private const val REQUEST_KEY_BACKGROUND = PREFIX + "REQUEST_KEY_BACKGROUND"
        private const val REQUEST_KEY_FOREGROUND_SELECTED = PREFIX + "REQUEST_KEY_FOREGROUND_SELECTED"
        private const val REQUEST_KEY_BACKGROUND_SELECTED = PREFIX + "REQUEST_KEY_BACKGROUND_SELECTED"
        private const val REQUEST_KEY_BASE = PREFIX + "REQUEST_KEY_BASE"
        private const val REQUEST_KEY_SHAPE = PREFIX + "REQUEST_KEY_SHAPE"
        private const val REQUEST_KEY_RESET_THEME = PREFIX + "REQUEST_KEY_RESET_THEME"

        fun create(activity: FragmentActivity): CustomWidgetConfigFragment =
            CustomWidgetConfigFragment().also {
                it.arguments = CustomWidgetConfigFragmentArgs(
                    appWidgetId = activity.intent.getIntExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID
                    )
                ).toBundle()
            }
    }
}
