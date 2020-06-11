/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import androidx.gridlayout.widget.GridLayout.spec
import kotlinx.android.synthetic.main.layout_detailed_settings.*
import net.mm2d.android.orientationfaker.R
import net.mm2d.color.chooser.ColorChooserDialog
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.event.EventObserver
import net.mm2d.orientation.event.EventRouter
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.settings.Default
import net.mm2d.orientation.settings.OrientationList
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.view.dialog.OrientationHelpDialog
import net.mm2d.orientation.view.dialog.ResetLayoutDialog
import net.mm2d.orientation.view.dialog.ResetThemeDialog
import net.mm2d.orientation.view.view.CheckItemView

class DetailedSettingsActivity : AppCompatActivity(),
    ResetThemeDialog.Callback,
    ResetLayoutDialog.Callback,
    ColorChooserDialog.Callback {
    private val settings by lazy {
        Settings.get()
    }
    private val eventObserver: EventObserver = EventRouter.createUpdateObserver()
    private lateinit var notificationSample: NotificationSample
    private lateinit var checkList: List<CheckItemView>
    private lateinit var orientationListStart: List<Int>
    private val orientationList: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpViews()
        eventObserver.subscribe { notificationSample.update() }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventObserver.unsubscribe()
        if (!orientationList.contains(settings.orientation)) {
            settings.orientation = orientationList[0]
            MainService.update(this)
            if (!OrientationHelper.isEnabled) {
                EventRouter.notifyUpdate()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (orientationListStart != orientationList) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        notificationSample.update()
        applyLayoutSelection()
        applyUseBlankIcon()
        applyAutoRotateWarning()
        applyNotificationPrivacy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpViews() {
        notificationSample = NotificationSample(this)
        setUpSample()
        setUpLayoutSelector()
        setUpUseBlankIcon()
        setUpAutoRotateWarning()
        setUpNotificationPrivacy()
        setUpSystemSetting()
    }

    private fun setUpSample() {
        sample_foreground.setColorFilter(settings.foregroundColor)
        sample_background.setColorFilter(settings.backgroundColor)
        sample_foreground_selected.setColorFilter(settings.foregroundColorSelected)
        sample_background_selected.setColorFilter(settings.backgroundColorSelected)
        foreground.setOnClickListener {
            ColorChooserDialog.show(this, it.id, settings.foregroundColor)
        }
        background.setOnClickListener {
            ColorChooserDialog.show(this, it.id, settings.backgroundColor)
        }
        foreground_selected.setOnClickListener {
            ColorChooserDialog.show(this, it.id, settings.foregroundColorSelected)
        }
        background_selected.setOnClickListener {
            ColorChooserDialog.show(this, it.id, settings.backgroundColorSelected)
        }
        reset_theme.setOnClickListener { ResetThemeDialog.show(this) }
        setUpOrientationIcons()
    }

    private fun setUpOrientationIcons() {
        notificationSample.buttonList.forEach { view ->
            view.button.setOnClickListener { updateOrientation(view.orientation) }
        }
    }

    private fun updateOrientation(orientation: Int) {
        settings.orientation = orientation
        notificationSample.update()
        MainService.update(this)
    }

    override fun onColorChooserResult(requestCode: Int, resultCode: Int, color: Int) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            R.id.foreground -> {
                settings.foregroundColor = color
                sample_foreground.setColorFilter(color)
            }
            R.id.background -> {
                settings.backgroundColor = color
                sample_background.setColorFilter(color)
            }
            R.id.foreground_selected -> {
                settings.foregroundColorSelected = color
                sample_foreground_selected.setColorFilter(color)
            }
            R.id.background_selected -> {
                settings.backgroundColorSelected = color
                sample_background_selected.setColorFilter(color)
            }
        }
        notificationSample.update()
        MainService.update(this)
    }

    override fun resetTheme() {
        settings.resetTheme()
        sample_foreground.setColorFilter(settings.foregroundColor)
        sample_background.setColorFilter(settings.backgroundColor)
        sample_foreground_selected.setColorFilter(settings.foregroundColorSelected)
        sample_background_selected.setColorFilter(settings.backgroundColorSelected)
        notificationSample.update()
        MainService.update(this)
    }

    private fun setUpLayoutSelector() {
        orientationListStart = settings.orientationList
        orientationList.addAll(orientationListStart)

        checkList = Orientation.values.map { orientation ->
            CheckItemView(this).also { view ->
                view.orientation = orientation.orientation
                view.setIcon(orientation.icon)
                view.setText(orientation.label)
                view.setOnClickListener {
                    onClickCheckItem(view)
                    updateCaution()
                }
            }
        }
        checkList.forEachIndexed { index, view ->
            val params = GridLayout.LayoutParams(
                spec(index / 4),
                spec(index % 4, 1f)
            ).also {
                it.width = 0
                it.height = resources.getDimensionPixelSize(R.dimen.customize_height)
            }
            check_holder.addView(view, params)
        }
        applyLayoutSelection()
        reset_layout.setOnClickListener { ResetLayoutDialog.show(this) }
        help_layout.setOnClickListener { OrientationHelpDialog.show(this) }
        updateCaution()
    }

    private fun updateCaution() {
        if (orientationList.any { Orientation.experimental.contains(it) }) {
            caution.visibility = View.VISIBLE
        } else {
            caution.visibility = View.GONE
        }
    }

    private fun onClickCheckItem(view: CheckItemView) {
        if (view.isChecked) {
            if (orientationList.size <= OrientationList.MIN) {
                Toast.makeText(this, R.string.toast_select_item_min, Toast.LENGTH_LONG).show()
            } else {
                orientationList.remove(view.orientation)
                view.isChecked = false
                updateLayoutSelector()
            }
        } else {
            if (orientationList.size >= OrientationList.MAX) {
                Toast.makeText(this, R.string.toast_select_item_max, Toast.LENGTH_LONG).show()
            } else {
                orientationList.add(view.orientation)
                view.isChecked = true
                updateLayoutSelector()
            }
        }
    }

    private fun updateLayoutSelector() {
        settings.orientationList = orientationList
        notificationSample.update()
        MainService.update(this)
    }

    override fun resetLayout() {
        orientationList.clear()
        orientationList.addAll(Default.orientationList)
        applyLayoutSelection()
        updateLayoutSelector()
        updateCaution()
    }

    private fun applyLayoutSelection() {
        checkList.forEach { view ->
            view.isChecked = orientationList.contains(view.orientation)
        }
    }

    private fun setUpUseBlankIcon() {
        use_blank_icon_for_notification.setOnClickListener { toggleUseBlankIcon() }
    }

    private fun applyUseBlankIcon() {
        use_blank_icon_for_notification.isChecked = settings.shouldUseBlankIconForNotification
    }

    private fun toggleUseBlankIcon() {
        settings.shouldUseBlankIconForNotification = !settings.shouldUseBlankIconForNotification
        applyUseBlankIcon()
        MainService.update(this)
    }

    private fun setUpAutoRotateWarning() {
        auto_rotate_warning.setOnClickListener { toggleAutoRotateWarning() }
    }

    private fun applyAutoRotateWarning() {
        auto_rotate_warning.isChecked = settings.autoRotateWarning
    }

    private fun toggleAutoRotateWarning() {
        settings.autoRotateWarning = !settings.autoRotateWarning
        applyAutoRotateWarning()
    }

    private fun setUpNotificationPrivacy() {
        notification_privacy.setOnClickListener { toggleNotificationPrivacy() }
    }

    private fun applyNotificationPrivacy() {
        notification_privacy.isChecked = settings.notifySecret
    }

    private fun toggleNotificationPrivacy() {
        settings.notifySecret = !settings.notifySecret
        applyNotificationPrivacy()
        MainService.update(this)
    }

    private fun setUpSystemSetting() {
        system_app.setOnClickListener {
            SystemSettings.startApplicationDetailsSettings(this)
        }
        system_notification.setOnClickListener {
            SystemSettings.startAppNotificationSettings(this)
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, DetailedSettingsActivity::class.java))
        }
    }
}
