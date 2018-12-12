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
import android.text.TextUtils
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
            return Service.START_NOT_STICKY
        }
        start()
        return Service.START_STICKY
    }

    private fun shouldStop(intent: Intent?): Boolean {
        if (!OverlayPermissionHelper.canDrawOverlays(this)) {
            return true
        }
        return intent != null && TextUtils.equals(intent.action, ACTION_STOP)
    }

    private fun start() {
        OrientationHelper.getInstance(this)
            .updateOrientation()
        MainActivity.notifyUpdate(this)
    }

    private fun stop() {
        NotificationHelper.stopForeground(this)
        OrientationHelper.getInstance(this)
            .cancel()
        MainActivity.notifyUpdate(this)
        stopSelf()
    }

    companion object {
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"

        fun start(context: Context) {
            startService(context, Intent(context, MainService::class.java).apply {
                action = ACTION_START
            })
        }

        fun stop(context: Context) {
            if (!OrientationHelper.getInstance(context).isEnabled) {
                return
            }
            startService(context, Intent(context, MainService::class.java).apply {
                action = ACTION_STOP
            })
        }

        private fun startService(context: Context, intent: Intent) {
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
