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
import android.view.View
import android.widget.RemoteViews
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.OrientationReceiver
import net.mm2d.orientation.control.Orientations
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
            val orientationList = settings.orientationList
            orientationList.forEachIndexed { index, value ->
                val button = ViewIds.list[index]
                Orientations.values.find { it.value == value }?.let {
                    views.setImageViewResource(button.iconViewId, it.icon)
                    views.setTextViewText(button.titleViewId, context.getText(it.label))
                    views.setOnClickPendingIntent(button.viewId, createOrientationIntent(context, it.value))
                }
            }
            val selectedIndex = orientationList.indexOf(orientation)
            ViewIds.list.forEachIndexed { index, it ->
                if (index == selectedIndex) {
                    views.setInt(it.viewId, "setBackgroundColor", selectedBackground)
                    views.setInt(it.iconViewId, "setColorFilter", selectedForeground)
                    views.setTextColor(it.titleViewId, selectedForeground)
                } else {
                    views.setInt(it.viewId, "setBackgroundColor", Color.TRANSPARENT)
                    views.setInt(it.iconViewId, "setColorFilter", foreground)
                    views.setTextColor(it.titleViewId, foreground)
                }
                if (index < orientationList.size) {
                    views.setViewVisibility(it.viewId, View.VISIBLE)
                } else {
                    views.setViewVisibility(it.viewId, View.GONE)
                }
            }
            views.setInt(R.id.remote_views_button_settings, "setBackgroundColor", Color.TRANSPARENT)
            views.setInt(R.id.remote_views_icon_settings, "setColorFilter", foreground)
            views.setOnClickPendingIntent(R.id.remote_views_button_settings, createActivityIntent(context))
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
