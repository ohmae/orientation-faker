/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_detailed_settings.*
import net.mm2d.android.orientationfaker.R
import net.mm2d.color.chooser.ColorChooserDialog
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.Settings

class DetailedSettingsActivity
    : AppCompatActivity(), ResetThemeDialog.Callback, ColorChooserDialog.Callback {
    private val settings by lazy {
        Settings.get()
    }
    private val orientationHelper by lazy {
        OrientationHelper.getInstance(this)
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            notificationSample.update()
        }
    }
    private lateinit var notificationSample: NotificationSample

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        notificationSample = NotificationSample(this)
        setUpSample()
        setUpOrientationIcons()
        setUpNotificationPrivacy()
        UpdateRouter.register(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        UpdateRouter.unregister(receiver)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpOrientationIcons() {
        notificationSample.buttonList.forEach { view ->
            view.button.setOnClickListener { setOrientation(view.orientation) }
        }
        notificationSample.update()
    }

    private fun setOrientation(orientation: Int) {
        settings.orientation = orientation
        notificationSample.update()
        if (orientationHelper.isEnabled) {
            MainService.start(this)
        }
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
        reset.setOnClickListener { ResetThemeDialog.show(this) }
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
        if (orientationHelper.isEnabled) {
            MainService.start(this)
        }
    }

    override fun resetTheme() {
        settings.resetTheme()
        sample_foreground.setColorFilter(settings.foregroundColor)
        sample_background.setColorFilter(settings.backgroundColor)
        sample_foreground_selected.setColorFilter(settings.foregroundColorSelected)
        sample_background_selected.setColorFilter(settings.backgroundColorSelected)
        notificationSample.update()
        if (orientationHelper.isEnabled) {
            MainService.start(this)
        }
    }

    private fun setUpNotificationPrivacy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            notification_privacy.visibility = View.GONE
        } else {
            notification_privacy.setOnClickListener { toggleNotificationPrivacy() }
            applyNotificationPrivacy()
        }
    }

    private fun applyNotificationPrivacy() {
        val notifySecret = settings.notifySecret
        notification_privacy_switch.isChecked = notifySecret
        notification_privacy_description.setText(
            if (notifySecret) R.string.notification_privacy_on
            else R.string.notification_privacy_off
        )
    }

    private fun toggleNotificationPrivacy() {
        settings.notifySecret = !settings.notifySecret
        applyNotificationPrivacy()
        if (orientationHelper.isEnabled) {
            MainService.start(this)
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, DetailedSettingsActivity::class.java))
        }
    }
}
