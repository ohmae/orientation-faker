/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_main.*
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.control.OverlayPermissionHelper
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.LaunchUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainActivity : AppCompatActivity() {
    private val settings by lazy {
        Settings.get()
    }
    private val orientationHelper by lazy {
        OrientationHelper.getInstance(this)
    }
    private val handler = Handler(Looper.getMainLooper())
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setStatusDescription()
            notificationSample.update()
        }
    }
    private lateinit var notificationSample: NotificationSample

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = getString(R.string.app_name)
        notificationSample = NotificationSample(this)
        status.setOnClickListener { toggleStatus() }
        resident.setOnClickListener { toggleResident() }
        version_description.text = makeVersionInfo()
        setStatusDescription()
        setResidentCheckBox()
        setUpOrientationIcons()
        UpdateRouter.register(receiver)
        customize.setOnClickListener { CustomizeActivity.start(this) }
        if (!OverlayPermissionHelper.canDrawOverlays(this)) {
            MainService.stop(this)
        } else if (settings.shouldResident()) {
            MainService.start(this)
        }
        checkPermission()
    }

    private fun checkPermission() {
        OverlayPermissionHelper.requestOverlayPermissionIfNeed(this, REQUEST_CODE)
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
        UpdateRouter.unregister(receiver)
    }

    override fun onResume() {
        super.onResume()
        notificationSample.update()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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

    private fun setUpOrientationIcons() {
        notificationSample.buttonList.forEach { view ->
            view.button.setOnClickListener { setOrientation(view.orientation) }
        }
        notificationSample.update()
    }

    private fun toggleStatus() {
        if (orientationHelper.isEnabled) {
            MainService.stop(this)
            if (settings.shouldResident()) {
                settings.setResident(false)
                setResidentCheckBox()
            }
        } else {
            MainService.start(this)
        }
    }

    private fun setStatusDescription() {
        val enabled = orientationHelper.isEnabled
        status_switch.isChecked = enabled
        status_description.setText(if (enabled) R.string.status_running else R.string.status_waiting)
    }

    private fun toggleResident() {
        settings.setResident(!settings.shouldResident())
        setResidentCheckBox()
        if (settings.shouldResident() && !orientationHelper.isEnabled) {
            MainService.start(this)
        }
    }

    private fun setResidentCheckBox() {
        resident_switch.isChecked = settings.shouldResident()
    }

    private fun setOrientation(orientation: Int) {
        settings.orientation = orientation
        notificationSample.update()
        if (orientationHelper.isEnabled) {
            MainService.start(this)
        }
    }

    private fun makeVersionInfo(): String {
        return BuildConfig.VERSION_NAME +
                if (BuildConfig.DEBUG)
                    " # " + DateFormat.format("yyyy/M/d kk:mm:ss", BuildConfig.BUILD_TIME)
                else ""
    }

    companion object {
        private const val REQUEST_CODE = 101
    }
}
