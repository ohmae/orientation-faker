/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.annotation.SuppressLint
import android.app.Activity
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
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.installStatus
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import kotlinx.android.synthetic.main.layout_main.*
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.event.EventObserver
import net.mm2d.orientation.event.EventRouter
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.LaunchUtils
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.view.dialog.OverlayPermissionDialog

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainActivity : AppCompatActivity(), InstallStateUpdatedListener {
    private val settings by lazy {
        Settings.get()
    }
    private val appUpdateManager by lazy {
        AppUpdateManagerFactory.create(applicationContext)
    }
    private var appUpdateListenerRegistered: Boolean = false
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
        if (!SystemSettings.canDrawOverlays(this)) {
            MainService.stop(this)
        } else {
            if (Settings.get().shouldAutoStart()) {
                MainService.start(this)
            }
            checkUpdate()
        }
    }

    @SuppressLint("NewApi")
    override fun onPostResume() {
        super.onPostResume()
        if (!SystemSettings.canDrawOverlays(this)) {
            OverlayPermissionDialog.showDialog(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        eventObserver.unsubscribe()
        unregisterAppUpdateListener()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                registerAppUpdateListener()
                showUpdateStatus()
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun registerAppUpdateListener() {
        if (!appUpdateListenerRegistered) {
            appUpdateManager.registerListener(this)
            appUpdateListenerRegistered = true
        }
    }

    private fun unregisterAppUpdateListener() {
        if (appUpdateListenerRegistered) {
            appUpdateManager.unregisterListener(this)
            appUpdateListenerRegistered = false
        }
    }

    private fun checkUpdate() {
        val activity = this
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus == InstallStatus.DOWNLOADED) {
                showUpdateButton()
            } else if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.clientVersionStalenessDays.let { it != null && it >= DAYS_FOR_UPDATE }
            ) {
                if (info.isFlexibleUpdateAllowed) {
                    appUpdateManager.startUpdateFlowForResult(
                        info, AppUpdateType.FLEXIBLE, activity, UPDATE_REQUEST_CODE
                    )
                } else if (info.isImmediateUpdateAllowed) {
                    val options = AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                    appUpdateManager.startUpdateFlow(info, activity, options)
                }
            }
        }
    }

    private fun showUpdateStatus() {
        update_snack_bar.visibility = View.VISIBLE
        update_button.isEnabled = false
        update_title.setText(R.string.status_update_preparing)
    }

    private fun showUpdateButton() {
        update_snack_bar.visibility = View.VISIBLE
        update_button.isEnabled = true
        update_title.setText(R.string.status_update_downloaded)
        update_snack_bar.setOnClickListener {
            appUpdateManager.completeUpdate()
        }
    }

    override fun onStateUpdate(state: InstallState) {
        when (state.installStatus()) {
            InstallStatus.PENDING -> {
            }
            InstallStatus.DOWNLOADING -> {
                update_title.setText(R.string.status_update_downloading)
            }
            InstallStatus.DOWNLOADED -> {
                showUpdateButton()
            }
            else -> {
                update_snack_bar.visibility = View.GONE
            }
        }
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
        each_app.setOnClickListener { EachAppActivity.start(this) }
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
            status_button.setText(R.string.button_status_stop)
            status_button.setBackgroundResource(R.drawable.bg_stop_button)
            status_description.setText(R.string.menu_description_status_running)
        } else {
            status_button.setText(R.string.button_status_start)
            status_button.setBackgroundResource(R.drawable.bg_start_button)
            status_description.setText(R.string.menu_description_status_waiting)
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
        private const val CHECK_INTERVAL: Long = 5000L
        private const val DAYS_FOR_UPDATE: Int = 2
        private const val UPDATE_REQUEST_CODE: Int = 100
    }
}
