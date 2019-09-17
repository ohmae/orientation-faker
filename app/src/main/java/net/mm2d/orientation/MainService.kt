/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.IBinder
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.control.OverlayPermissionHelper
import net.mm2d.orientation.notification.NotificationHelper

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MainService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationHelper.startForeground(this)
        if (shouldStop(intent)) {
            stop()
            return START_NOT_STICKY
        }
        start()
        return START_STICKY
    }

    private fun shouldStop(intent: Intent?): Boolean {
        if (!OverlayPermissionHelper.canDrawOverlays(this)) {
            return true
        }
        return intent != null && intent.action == ACTION_STOP
    }

    private fun start() {
        OrientationHelper.updateOrientation()
        UpdateRouter.send()
    }

    private fun stop() {
        NotificationHelper.stopForeground(this)
        OrientationHelper.cancel()
        UpdateRouter.send()
        stopSelf()
    }

    companion object {
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"

        fun start(context: Context) {
            startService(context, ACTION_START)
        }

        fun stop(context: Context) {
            if (!OrientationHelper.isEnabled) {
                return
            }
            startService(context, ACTION_STOP)
        }

        private fun startService(context: Context, action: String) {
            val intent = makeIntent(context, action)
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
