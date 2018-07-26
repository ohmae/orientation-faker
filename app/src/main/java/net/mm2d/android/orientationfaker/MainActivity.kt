/*
 * Copyright (c) 2014 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.layout_main.*
import net.mm2d.android.orientationfaker.orientation.OrientationHelper
import net.mm2d.android.orientationfaker.orientation.OrientationIdManager
import net.mm2d.android.orientationfaker.orientation.OverlayPermissionHelper
import net.mm2d.android.orientationfaker.settings.Settings
import net.mm2d.log.Log
import java.util.*

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
    private val buttonList = ArrayList<Pair<Int, View>>()
    private val handler = Handler(Looper.getMainLooper())
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setStatusDescription()
            setOrientationIcon()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.subtitle = makeVersionInfo()

        status.setOnClickListener { toggleStatus() }
        resident.setOnClickListener { toggleResident() }
        setStatusDescription()
        setResidentCheckBox()
        setUpOrientationIcons()
        registerReceiver()
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
        unregisterReceiver()
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, IntentFilter(ACTION_UPDATE))
    }

    private fun unregisterReceiver() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(receiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.license -> startActivity(Intent(this, LicenseActivity::class.java))
            R.id.play_store -> openGooglePlay(this)
        }
        return true
    }

    private fun setUpOrientationIcons() {
        OrientationIdManager.list.forEach {
            val orientation = it.orientation
            val button = findViewById<View>(it.viewId)
            buttonList.add(Pair(orientation, button))
            button.setOnClickListener { setOrientation(orientation) }
        }
        setOrientationIcon()
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
        statusSwitch.isChecked = enabled
        statusDescription.setText(if (enabled) R.string.status_running else R.string.status_waiting)
    }

    private fun toggleResident() {
        settings.setResident(!settings.shouldResident())
        setResidentCheckBox()
        if (settings.shouldResident() && !orientationHelper.isEnabled) {
            MainService.start(this)
        }
    }

    private fun setResidentCheckBox() {
        residentCheckBox.isChecked = settings.shouldResident()
    }

    private fun setOrientation(orientation: Int) {
        settings.orientation = orientation
        setOrientationIcon()
        if (orientationHelper.isEnabled) {
            MainService.start(this)
        }
    }

    private fun setOrientationIcon() {
        val orientation = settings.orientation
        for (pair in buttonList) {
            pair.second?.run {
                setBackgroundResource(if (orientation == pair.first) R.drawable.bg_icon_selected else R.drawable.bg_icon)
            }
        }
    }

    private fun makeVersionInfo(): String {
        return "Ver." + BuildConfig.VERSION_NAME +
                if (BuildConfig.DEBUG) " # " + DateFormat.format("yyyy/M/d kk:mm:ss", BuildConfig.BUILD_TIME)
                else ""
    }

    companion object {
        private const val PACKAGE_NAME = "net.mm2d.android.orientationfaker"
        private const val ACTION_UPDATE = "ACTION_UPDATE"
        private const val REQUEST_CODE = 101

        fun notifyUpdate(context: Context) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(Intent(ACTION_UPDATE))
        }

        private fun openUri(context: Context, uri: String?): Boolean {
            if (TextUtils.isEmpty(uri)) {
                return false
            }
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.w(e)
                return false
            }
            return true
        }

        private fun openGooglePlay(context: Context, packageName: String): Boolean {
            return openUri(context, "market://details?id=$packageName") ||
                    openUri(context, "https://play.google.com/store/apps/details?id=$packageName")
        }

        private fun openGooglePlay(context: Context): Boolean {
            return openGooglePlay(context, PACKAGE_NAME)
        }
    }
}
