/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.OrientationIdManager
import net.mm2d.orientation.control.OrientationReceiver
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.view.MainActivity

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object RemoteViewsCreator {
    fun create(context: Context, orientation: Int, notification: Boolean): RemoteViews {
        val settings = Settings.get()
        val foreground = settings.foregroundColor
        val selectedForeground = settings.foregroundColorSelected
        val selectedBackground = settings.backgroundColorSelected
        val layout = if (notification) R.layout.notification else R.layout.widget
        return RemoteViews(context.packageName, layout).also { views ->
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
            views.setInt(R.id.button_settings, "setBackgroundColor", Color.TRANSPARENT)
            views.setInt(R.id.icon_settings, "setColorFilter", foreground)
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
