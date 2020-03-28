/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle.State
import kotlinx.android.synthetic.main.layout_main.*
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.control.OverlayPermissionHelper
import net.mm2d.orientation.event.EventObserver
import net.mm2d.orientation.event.EventRouter
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.LaunchUtils
import net.mm2d.orientation.util.SystemSettings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainActivity : AppCompatActivity() {
    private val settings by lazy {
        Settings.get()
    }
    private val handler = Handler(Looper.getMainLooper())
    private val checkSystemSettingsTask = Runnable { checkSystemSettings() }
    private val eventObserver: EventObserver = EventRouter.createUpdateObserver()
    private lateinit var notificationSample: NotificationSample

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = getString(R.string.app_name)
        setUpViews()
        eventObserver.subscribe {
            applyStatus()
            notificationSample.update()
        }
        if (!OverlayPermissionHelper.canDrawOverlays(this)) {
            MainService.stop(this)
        } else {
            if (Settings.get().shouldAutoStart()) {
                MainService.start(this)
            }
        }
        checkPermission()
    }

    private fun checkPermission() {
        OverlayPermissionHelper.requestOverlayPermissionIfNeed(
            this,
            REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            handler.postDelayed({ checkPermission() }, 1000)
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        eventObserver.unsubscribe()
    }

    override fun onResume() {
        super.onResume()
        notificationSample.update()
        handler.removeCallbacks(checkSystemSettingsTask)
        handler.post(checkSystemSettingsTask)
        applyStatus()
        applyAutoStart()
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
            caution.visibility = View.GONE
            return
        }
        caution.visibility = if (SystemSettings.rotationIsFixed(this)) View.VISIBLE else View.GONE
        handler.postDelayed(checkSystemSettingsTask, CHECK_INTERVAL)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.license -> LicenseActivity.start(this)
            R.id.source_code -> LaunchUtils.openSourceCode(this)
            R.id.privacy_policy -> LaunchUtils.openPrivacyPolicy(this)
            R.id.play_store -> LaunchUtils.openGooglePlay(this)
        }
        return true
    }

    private fun setUpViews() {
        notificationSample = NotificationSample(this)
        status.setOnClickListener { toggleStatus() }
        auto_start.setOnClickListener { toggleAutoStart() }
        detailed_setting.setOnClickListener { DetailedSettingsActivity.start(this) }
        version_description.text = makeVersionInfo()
        setUpOrientationIcons()
    }

    private fun setUpOrientationIcons() {
        notificationSample.buttonList.forEach { view ->
            view.button.setOnClickListener { updateOrientation(view.orientation) }
        }
    }

    private fun toggleStatus() {
        if (OrientationHelper.isEnabled) {
            MainService.stop(this)
            if (settings.shouldAutoStart()) {
                settings.setAutoStart(false)
                applyAutoStart()
            }
        } else {
            MainService.start(this)
        }
    }

    private fun applyStatus() {
        if (OrientationHelper.isEnabled) {
            status_button.setText(R.string.status_stop)
            status_button.setBackgroundResource(R.drawable.bg_stop_button)
            status_description.setText(R.string.status_running)
        } else {
            status_button.setText(R.string.status_start)
            status_button.setBackgroundResource(R.drawable.bg_start_button)
            status_description.setText(R.string.status_waiting)
        }
        ReviewRequest.requestReviewIfNeed(this)
    }

    private fun toggleAutoStart() {
        settings.setAutoStart(!settings.shouldAutoStart())
        applyAutoStart()
        if (settings.shouldAutoStart() && !OrientationHelper.isEnabled) {
            MainService.start(this)
        }
    }

    private fun applyAutoStart() {
        auto_start.isChecked = settings.shouldAutoStart()
    }

    private fun updateOrientation(orientation: Int) {
        settings.orientation = orientation
        notificationSample.update()
        MainService.update(this)
    }

    private fun makeVersionInfo(): String {
        return BuildConfig.VERSION_NAME +
            if (BuildConfig.DEBUG)
                " # " + DateFormat.format("yyyy/M/d kk:mm:ss", BuildConfig.BUILD_TIME)
            else ""
    }

    companion object {
        private const val REQUEST_CODE = 101
        private const val CHECK_INTERVAL = 5000L
    }
}
