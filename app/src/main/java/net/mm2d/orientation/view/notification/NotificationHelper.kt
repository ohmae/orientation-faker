/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.OrientationIdManager
import net.mm2d.orientation.control.OrientationReceiver
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.view.MainActivity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object NotificationHelper {
    private const val OLD_CHANNEL_ID = "CHANNEL_ID"
    private const val CHANNEL_ID = "CONTROL"
    private const val NOTIFICATION_ID = 10

    @RequiresApi(VERSION_CODES.O)
    private fun createChannel(context: Context) {
        val name = context.getString(R.string.notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).also {
            it.enableLights(false)
            it.enableVibration(false)
        }
        getNotificationManager(context)?.also {
            if (it.getNotificationChannel(OLD_CHANNEL_ID) != null) {
                it.deleteNotificationChannel(OLD_CHANNEL_ID)
            }
            it.createNotificationChannel(channel)
        }
    }

    private fun getNotificationManager(context: Context): NotificationManager? =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    fun startForeground(service: Service) {
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            createChannel(service)
        }
        service.startForeground(NOTIFICATION_ID, makeNotification(service))
    }

    fun stopForeground(service: Service) {
        service.stopForeground(true)
    }

    private fun makeNotification(context: Context): Notification {
        val settings = Settings.get()
        val orientation = settings.orientation
        val visibility =
            if (settings.notifySecret) NotificationCompat.VISIBILITY_SECRET
            else NotificationCompat.VISIBILITY_PUBLIC
        val icon =
            if (settings.shouldUseBlankIconForNotification) R.drawable.ic_blank
            else OrientationIdManager.getIconIdFromOrientation(orientation)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setDefaults(0)
            .setContentTitle(context.getText(R.string.app_name))
            .setVisibility(visibility)
            .setCustomContentView(createRemoteViews(context, orientation))
            .setSmallIcon(icon)
            .setOngoing(true)
            .build()
    }

    private fun createRemoteViews(context: Context, orientation: Int): RemoteViews {
        val settings = Settings.get()
        val foreground = settings.foregroundColor
        val selectedForeground = settings.foregroundColorSelected
        val selectedBackground = settings.backgroundColorSelected
        return RemoteViews(context.packageName, R.layout.notification).also { views ->
            views.setInt(R.id.notification, "setBackgroundColor", settings.backgroundColor)
            views.setTextViewText(
                R.id.title_unspecified,
                context.getText(
                    if (settings.useFullSensor) R.string.force_auto else R.string.unspecified
                )
            )
            OrientationIdManager.list.forEach {
                views.setOnClickPendingIntent(
                    it.viewId,
                    createOrientationIntent(context, it.orientation)
                )
                if (orientation == it.orientation) {
                    views.setInt(it.viewId, "setBackgroundColor", selectedBackground)
                    views.setInt(it.iconViewId, "setColorFilter", selectedForeground)
                    views.setTextColor(it.titleViewId, selectedForeground)
                } else {
                    views.setInt(it.viewId, "setBackgroundColor", Color.TRANSPARENT)
                    views.setInt(it.iconViewId, "setColorFilter", foreground)
                    views.setTextColor(it.titleViewId, foreground)
                }
            }
            views.setOnClickPendingIntent(R.id.button_settings, createActivityIntent(context))
        }
    }

    private fun createOrientationIntent(context: Context, orientation: Int): PendingIntent {
        val intent = Intent(OrientationReceiver.ACTION_ORIENTATION).also {
            it.putExtra(OrientationReceiver.EXTRA_ORIENTATION, orientation)
            it.setClass(context, OrientationReceiver::class.java)
        }
        return PendingIntent.getBroadcast(
            context,
            orientation,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createActivityIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
