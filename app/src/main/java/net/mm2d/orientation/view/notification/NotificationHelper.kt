/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.view.widget.RemoteViewsCreator

object NotificationHelper {
    private const val OLD_CHANNEL_ID = "CHANNEL_ID"
    private const val CHANNEL_ID = "CONTROL"
    private const val NOTIFICATION_ID = 10

    fun createChannel(context: Context) {
        if (VERSION.SDK_INT < VERSION_CODES.O) {
            return
        }
        val name = context.getString(R.string.notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).also {
            it.enableLights(false)
            it.enableVibration(false)
        }
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(OLD_CHANNEL_ID) != null) {
            manager.deleteNotificationChannel(OLD_CHANNEL_ID)
        }
        manager.createNotificationChannel(channel)
    }

    fun startForegroundEmpty(service: Service) {
        service.startForeground(NOTIFICATION_ID, makeEmptyNotification(service))
    }

    fun startForeground(service: Service) {
        service.startForeground(NOTIFICATION_ID, makeNotification(service))
    }

    fun stopForeground(service: Service) {
        service.stopForeground(true)
    }

    private fun makeEmptyNotification(context: Context): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setDefaults(0)
            .setCustomContentView(RemoteViews(context.packageName, R.layout.empty_notification))
            .setSmallIcon(R.drawable.ic_blank)
            .build()

    private fun makeNotification(context: Context): Notification {
        val orientation = OrientationHelper.getOrientation()
        val settings = Settings.get()
        val visibility =
            if (settings.notifySecret) NotificationCompat.VISIBILITY_SECRET
            else NotificationCompat.VISIBILITY_PUBLIC
        val icon =
            if (settings.shouldUseBlankIconForNotification) R.drawable.ic_blank
            else Orientation.values.find { it.orientation == orientation }?.icon ?: R.drawable.ic_blank
        val views = RemoteViewsCreator.create(context, orientation)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setDefaults(0)
            .setContentTitle(context.getText(R.string.app_name))
            .setVisibility(visibility)
            .setOngoing(true)
            .setCustomContentView(views)
            .setSmallIcon(icon)
            .setNotificationSilent(settings.notifySecret)
            .build()
    }

    private fun NotificationCompat.Builder.setNotificationSilent(silent: Boolean): NotificationCompat.Builder = apply {
        if (silent) setNotificationSilent()
    }
}
