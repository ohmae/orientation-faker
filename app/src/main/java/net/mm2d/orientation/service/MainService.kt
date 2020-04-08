/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.IBinder
import android.widget.Toast
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.ForegroundPackageChecker
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.event.EventRouter
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.view.notification.NotificationHelper
import net.mm2d.orientation.view.widget.WidgetProvider

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainService : Service() {
    private var checker: ForegroundPackageChecker? = null

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (shouldStop(intent)) {
            stop()
            return START_NOT_STICKY
        }
        start()
        return START_STICKY
    }

    private fun shouldStop(intent: Intent?): Boolean {
        if (!SystemSettings.canDrawOverlays(this)) {
            return true
        }
        return intent != null && intent.action == ACTION_STOP
    }

    private fun start() {
        OrientationHelper.update(Settings.get().orientation)
        NotificationHelper.startForeground(this)
        EventRouter.notifyUpdate()
        startForegroundChecker()
    }

    private fun stop() {
        OrientationHelper.cancel()
        NotificationHelper.startForeground(this)
        NotificationHelper.stopForeground(this)
        EventRouter.notifyUpdate()
        stopForegroundChecker()
        stopSelf()
    }

    private fun startForegroundChecker() {
        if (checker != null) return
        if (ForegroundPackageSettings.isEmpty()) return
        if (SystemSettings.hasUsageAccessPermission(this)) {
            Toast.makeText(this, R.string.toast_no_permission_to_usage_access, Toast.LENGTH_LONG).show()
            return
        }
        checker = ForegroundPackageChecker(this, ::onUpdateForegroundPackage).also {
            it.start()
        }
    }

    private fun stopForegroundChecker() {
        checker?.destroy()
        checker = null
    }

    private fun onUpdateForegroundPackage(packageName: String) {
        val orientation = ForegroundPackageSettings.get(packageName).let {
            if (it == Orientation.INVALID) Settings.get().orientation else it
        }
        if (!OrientationHelper.isEnabled ||
            OrientationHelper.getOrientation() == orientation
        ) {
            return
        }
        OrientationHelper.update(orientation)
        NotificationHelper.startForeground(this)
        EventRouter.notifyUpdate()
    }

    companion object {
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"

        fun update(context: Context) {
            if (OrientationHelper.isEnabled) {
                start(context)
            } else {
                WidgetProvider.start(context)
            }
        }

        fun start(context: Context) {
            WidgetProvider.start(context)
            startService(
                context,
                ACTION_START
            )
        }

        fun stop(context: Context) {
            WidgetProvider.stop(context)
            if (!OrientationHelper.isEnabled) {
                return
            }
            startService(
                context,
                ACTION_STOP
            )
        }

        private fun startService(context: Context, action: String) {
            val intent =
                makeIntent(context, action)
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        private fun makeIntent(context: Context, action: String) =
            Intent(context, MainService::class.java).also {
                it.action = action
            }
    }
}
