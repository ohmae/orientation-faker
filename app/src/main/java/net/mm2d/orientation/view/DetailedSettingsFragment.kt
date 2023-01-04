/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.gridlayout.widget.GridLayout
import androidx.transition.TransitionManager
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.FragmentDetailedSettingsBinding
import net.mm2d.color.chooser.ColorChooserDialog
import net.mm2d.orientation.control.FunctionButton
import net.mm2d.orientation.control.Functions
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.mapOrientation
import net.mm2d.orientation.settings.Default
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.Toaster
import net.mm2d.orientation.util.alpha
import net.mm2d.orientation.util.autoCleared
import net.mm2d.orientation.util.opaque
import net.mm2d.orientation.view.dialog.IconShapeDialog
import net.mm2d.orientation.view.dialog.OrientationHelpDialog
import net.mm2d.orientation.view.dialog.OrientationSelectDialog
import net.mm2d.orientation.view.dialog.ResetLayoutDialog
import net.mm2d.orientation.view.dialog.ResetThemeDialog
import net.mm2d.orientation.view.view.CheckItemView
import net.mm2d.orientation.view.view.SwitchMenuView

@AndroidEntryPoint
class DetailedSettingsFragment : Fragment(R.layout.fragment_detailed_settings) {
    private lateinit var notificationSample: NotificationSample
    private lateinit var checkList: List<CheckItemView>
    private val functions: MutableList<FunctionButton> = mutableListOf()
    private var binding: FragmentDetailedSettingsBinding by autoCleared()
    private val viewModel: DetailedSettingsFragmentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDetailedSettingsBinding.bind(view)
        setUpViews()
        viewModel.menu.observe(viewLifecycleOwner) {
            binding.content.autoRotateWarning.isChecked = it.warnSystemRotate
        }
        viewModel.sample.observe(viewLifecycleOwner) { (orientation, design) ->
            TransitionManager.beginDelayedTransition(binding.notificationSample)
            notificationSample.update(orientation, design)
        }
        viewModel.control.observe(viewLifecycleOwner) {
            binding.content.notificationPrivacy.isChecked = it.shouldNotifySecret
            binding.content.useBlankIconForNotification.isChecked = it.shouldUseBlankIcon
        }
        val backgroundDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.bg_sample)
        binding.content.sampleForeground.background = backgroundDrawable
        binding.content.sampleBackground.background = backgroundDrawable
        binding.content.sampleForegroundSelected.background = backgroundDrawable
        binding.content.sampleBackgroundSelected.background = backgroundDrawable
        binding.content.sampleBase.background = backgroundDrawable
        viewModel.design.observe(viewLifecycleOwner) { design ->
            binding.content.sampleForeground.setImageColor(design.foreground)
            binding.content.foreground.setOnClickListener {
                ColorChooserDialog.show(this, REQUEST_KEY_FOREGROUND, design.foreground, true)
            }
            binding.content.sampleBackground.setImageColor(design.background)
            binding.content.background.setOnClickListener {
                ColorChooserDialog.show(this, REQUEST_KEY_BACKGROUND, design.background, true)
            }
            binding.content.sampleForegroundSelected.setImageColor(design.foregroundSelected)
            binding.content.foregroundSelected.setOnClickListener {
                ColorChooserDialog.show(this, REQUEST_KEY_FOREGROUND_SELECTED, design.foregroundSelected, true)
            }
            binding.content.sampleBackgroundSelected.setImageColor(design.backgroundSelected)
            binding.content.backgroundSelected.setOnClickListener {
                ColorChooserDialog.show(this, REQUEST_KEY_BACKGROUND_SELECTED, design.backgroundSelected, true)
            }
            binding.content.sampleBase.setImageColor(design.base ?: 0)
            binding.content.base.setOnClickListener {
                ColorChooserDialog.show(this, REQUEST_KEY_BASE, design.base ?: 0, true)
            }
            if (binding.content.base.isVisible != design.iconize) {
                TransitionManager.beginDelayedTransition(binding.content.colorSettings)
                binding.content.base.isVisible = design.iconize
            }
            binding.content.useIconBackground.isChecked = design.iconize

            if (design.iconize) {
                binding.content.iconShape.isEnabled = true
                binding.content.iconShape.alpha = 1.0f
            } else {
                binding.content.iconShape.isEnabled = false
                binding.content.iconShape.alpha = 0.5f
            }
            binding.content.iconShapeIcon.setImageResource(design.shape.iconId)
            binding.content.iconShapeDescription.setText(design.shape.textId)
            checkList.forEach { view ->
                view.isChecked = design.functions
                    .contains(view.funciton)
            }
            val experimental = design.functions
                .mapOrientation()
                .any { it.isExperimental() }
            if (binding.content.caution.isVisible != experimental) {
                TransitionManager.beginDelayedTransition(binding.content.contentsContainer)
                binding.content.caution.isVisible = experimental
            }
            functions.clear()
            functions.addAll(design.functions)
            binding.content.showSettingsOnNotification.isChecked = design.shouldShowSettings
        }
        viewModel.orientation.observe(viewLifecycleOwner) {
            if (it.orientationWhenPowerIsConnected != Orientation.INVALID) {
                Functions.find(it.orientationWhenPowerIsConnected)?.let { entity ->
                    binding.content.pluggedOrientationIcon.setImageResource(entity.icon)
                    binding.content.pluggedOrientationName.setText(entity.label)
                }
            } else {
                binding.content.pluggedOrientationIcon.setImageResource(0)
                binding.content.pluggedOrientationName.text = ""
            }
        }
        registerDialogListener()
    }

    override fun onStop() {
        super.onStop()
        viewModel.adjustOrientation()
    }

    private fun setUpViews() {
        notificationSample = NotificationSample(binding.notificationSample)
        setUpSample()
        setUpLayoutSelector()
        setUpUseIconBackground()
        setUpIconShape()
        setUpUseBlankIcon()
        setUpSettingsOnNotification()
        setUpOrientationWhenPowerIsConnected()
        setUpAutoRotateWarning()
        setUpNotificationPrivacy()
        setUpSystemSetting()
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
        ResetLayoutDialog.registerListener(this, REQUEST_KEY_RESET_LAYOUT) {
            resetLayout()
        }
        IconShapeDialog.registerListener(this, REQUEST_KEY_SHAPE) {
            viewModel.updateShape(it)
        }
        OrientationSelectDialog.registerListener(this, REQUEST_KEY_ORIENTATION) {
            viewModel.updateOrientationWhenPowerIsConnected(it)
        }
    }

    private fun setUpSample() {
        binding.content.resetTheme.setOnClickListener {
            ResetThemeDialog.show(this, REQUEST_KEY_RESET_THEME)
        }
        notificationSample.buttonList.forEach { view ->
            view.button.setOnClickListener {
                view.function.orientation?.let {
                    viewModel.updateOrientation(it)
                }
            }
        }
    }

    private fun ImageView.setImageColor(@ColorInt color: Int) {
        setColorFilter(color.opaque())
        imageAlpha = color.alpha()
    }

    private fun setUpLayoutSelector() {
        checkList = Functions.functions.map { function ->
            CheckItemView(requireContext()).also { view ->
                view.funciton = function.function
                view.setIcon(function.icon)
                view.setText(function.label)
                view.setOnClickListener {
                    onClickCheckItem(view)
                }
            }
        }
        checkList.forEachIndexed { index, view ->
            val params = GridLayout.LayoutParams(
                GridLayout.spec(index / 4),
                GridLayout.spec(index % 4, 1f)
            ).also {
                it.width = 0
                it.height = resources.getDimensionPixelSize(R.dimen.customize_height)
            }
            binding.content.checkHolder.addView(view, params)
        }
        binding.content.resetLayout.setOnClickListener { ResetLayoutDialog.show(this, REQUEST_KEY_RESET_LAYOUT) }
        binding.content.helpLayout.setOnClickListener { OrientationHelpDialog.show(this) }
    }

    private fun onClickCheckItem(view: CheckItemView) {
        if (view.isChecked) {
            if (functions.size <= FunctionButton.MIN) {
                Toaster.showLong(requireContext(), R.string.toast_select_item_min)
            } else {
                functions.remove(view.funciton)
                view.isChecked = false
                viewModel.updateFunctions(functions)
            }
        } else {
            if (functions.size >= FunctionButton.MAX) {
                Toaster.showLong(requireContext(), R.string.toast_select_item_max)
            } else {
                functions.add(view.funciton)
                view.isChecked = true
                viewModel.updateFunctions(functions)
            }
        }
    }

    private fun resetLayout() {
        viewModel.updateFunctions(Default.functions)
    }

    private fun setUpUseIconBackground() {
        binding.content.useIconBackground.setOnClickListener {
            viewModel.updateIconize(!binding.content.useIconBackground.isChecked)
        }
    }

    private fun setUpIconShape() {
        binding.content.iconShape.setOnClickListener {
            IconShapeDialog.show(this, REQUEST_KEY_SHAPE)
        }
    }

    private fun setUpUseBlankIcon() {
        binding.content.useBlankIconForNotification.setOnClickListener {
            viewModel.updateUseBlankIcon(!(it as SwitchMenuView).isChecked)
        }
    }

    private fun setUpSettingsOnNotification() {
        binding.content.showSettingsOnNotification.setOnClickListener {
            viewModel.updateShowSettings(!(it as SwitchMenuView).isChecked)
        }
    }

    private fun setUpOrientationWhenPowerIsConnected() {
        binding.content.pluggedOrientation.setOnClickListener {
            OrientationSelectDialog.show(this, REQUEST_KEY_ORIENTATION)
        }
    }

    private fun setUpAutoRotateWarning() {
        binding.content.autoRotateWarning.setOnClickListener {
            viewModel.updateWarnSystemRotate(!(it as SwitchMenuView).isChecked)
        }
    }

    private fun setUpNotificationPrivacy() {
        binding.content.notificationPrivacy.setOnClickListener {
            viewModel.updateNotifySecret(!(it as SwitchMenuView).isChecked)
        }
    }

    private fun setUpSystemSetting() {
        binding.content.systemApp.setOnClickListener {
            SystemSettings.startApplicationDetailsSettings(requireActivity())
        }
        binding.content.systemNotification.setOnClickListener {
            SystemSettings.startAppNotificationSettings(requireActivity())
        }
    }

    companion object {
        private const val PREFIX = "DetailedSettingsFragment."
        private const val REQUEST_KEY_FOREGROUND = PREFIX + "REQUEST_KEY_FOREGROUND"
        private const val REQUEST_KEY_BACKGROUND = PREFIX + "REQUEST_KEY_BACKGROUND"
        private const val REQUEST_KEY_FOREGROUND_SELECTED = PREFIX + "REQUEST_KEY_FOREGROUND_SELECTED"
        private const val REQUEST_KEY_BACKGROUND_SELECTED = PREFIX + "REQUEST_KEY_BACKGROUND_SELECTED"
        private const val REQUEST_KEY_BASE = PREFIX + "REQUEST_KEY_BASE"
        private const val REQUEST_KEY_SHAPE = PREFIX + "REQUEST_KEY_SHAPE"
        private const val REQUEST_KEY_RESET_LAYOUT = PREFIX + "REQUEST_KEY_RESET_LAYOUT"
        private const val REQUEST_KEY_RESET_THEME = PREFIX + "REQUEST_KEY_RESET_THEME"
        private const val REQUEST_KEY_ORIENTATION = PREFIX + "REQUEST_KEY_ORIENTATION"
    }
}
