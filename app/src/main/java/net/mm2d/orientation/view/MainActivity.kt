/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle.State
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.ActivityMainBinding
import net.mm2d.orientation.event.EventRouter
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.service.MainController
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.settings.NightModes
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.Launcher
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.Updater
import net.mm2d.orientation.view.dialog.NightModeDialog
import net.mm2d.orientation.view.dialog.OverlayPermissionDialog

class MainActivity : AppCompatActivity(), NightModeDialog.Callback {
    private val settings by lazy {
        Settings.get()
    }
    private val handler = Handler(Looper.getMainLooper())
    private val checkSystemSettingsTask = Runnable { checkSystemSettings() }
    private lateinit var notificationSample: NotificationSample
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.app_name)
        setUpViews()
        EventRouter.observeUpdate(this) {
            applyStatus()
            notificationSample.update()
        }
        if (!SystemSettings.canDrawOverlays(this)) {
            MainController.stop()
        } else {
            if (Settings.get().shouldAutoStart()) {
                MainController.start()
            }
            Updater.startUpdateIfAvailable(this)
        }
    }

    @SuppressLint("NewApi")
    override fun onPostResume() {
        super.onPostResume()
        if (!SystemSettings.canDrawOverlays(this)) {
            OverlayPermissionDialog.show(this)
        }
    }

    override fun onResume() {
        super.onResume()
        notificationSample.update()
        handler.removeCallbacks(checkSystemSettingsTask)
        handler.post(checkSystemSettingsTask)
        applyStatus()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(checkSystemSettingsTask)
    }

    private fun checkSystemSettings() {
        if (lifecycle.currentState != State.RESUMED) {
            return
        }
        if (!settings.autoRotateWarning) {
            binding.content.caution.visibility = View.GONE
            return
        }
        binding.content.caution.visibility =
            if (SystemSettings.rotationIsFixed(this)) View.VISIBLE else View.GONE
        handler.postDelayed(checkSystemSettingsTask, CHECK_INTERVAL)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.license -> LicenseActivity.start(this)
            R.id.source_code -> Launcher.openSourceCode(this)
            R.id.privacy_policy -> Launcher.openPrivacyPolicy(this)
            R.id.mail_to_developer -> Launcher.sendMailToDeveloper(this)
            R.id.play_store -> Launcher.openGooglePlay(this)
        }
        return true
    }

    private fun setUpViews() {
        notificationSample = NotificationSample(this)
        binding.content.status.setOnClickListener { toggleStatus() }
        binding.content.detailedSetting.setOnClickListener { DetailedSettingsActivity.start(this) }
        binding.content.versionDescription.text = BuildConfig.VERSION_NAME
        setUpOrientationIcons()
        binding.content.eachApp.setOnClickListener { EachAppActivity.start(this) }
        setUpNightMode()
    }

    private fun setUpOrientationIcons() {
        notificationSample.buttonList.forEach { view ->
            view.button.setOnClickListener {
                updateOrientation(view.orientation)
                if (!MainService.isStarted && SystemSettings.canDrawOverlays(this)) {
                    MainController.start()
                    settings.setAutoStart(true)
                }
            }
        }
    }

    private fun setUpNightMode() {
        binding.content.nightMode.setOnClickListener {
            NightModeDialog.show(this)
        }
        applyNightMode()
    }

    private fun applyNightMode() {
        binding.content.nightModeDescription.setText(NightModes.getTextId(settings.nightMode))
    }

    override fun onSelectNightMode(mode: Int) {
        if (settings.nightMode == mode) return
        settings.nightMode = mode
        applyNightMode()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    @SuppressLint("NewApi")
    private fun toggleStatus() {
        if (MainService.isStarted) {
            MainController.stop()
            settings.setAutoStart(false)
        } else {
            if (SystemSettings.canDrawOverlays(this)) {
                MainController.start()
                settings.setAutoStart(true)
            } else {
                OverlayPermissionDialog.show(this)
            }
        }
    }

    private fun applyStatus() {
        if (MainService.isStarted) {
            binding.content.statusButton.setText(R.string.button_status_stop)
            binding.content.statusButton.setBackgroundResource(R.drawable.bg_stop_button)
            binding.content.statusDescription.setText(R.string.menu_description_status_running)
        } else {
            binding.content.statusButton.setText(R.string.button_status_start)
            binding.content.statusButton.setBackgroundResource(R.drawable.bg_start_button)
            binding.content.statusDescription.setText(R.string.menu_description_status_waiting)
        }
        ReviewRequest.requestReviewIfNeed(this)
    }

    private fun updateOrientation(orientation: Int) {
        settings.orientation = orientation
        notificationSample.update()
        MainController.update()
    }

    companion object {
        private const val CHECK_INTERVAL: Long = 5000L
    }
}
