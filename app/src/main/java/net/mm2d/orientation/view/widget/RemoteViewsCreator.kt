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
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationReceiver
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.shouldUseWhiteForeground
import net.mm2d.orientation.view.MainActivity

object RemoteViewsCreator {
    fun create(context: Context, orientation: Int): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification).also { views ->
            val settings = Settings.get()
            val foreground = settings.foregroundColor
            val background = settings.backgroundColor
            val selectedForeground = settings.foregroundColorSelected
            val selectedBackground = settings.backgroundColorSelected
            val shouldUseIconBackground = settings.shouldUseIconBackground
            val baseColor = if (shouldUseIconBackground) settings.baseColor else background
            views.setInt(R.id.notification, "setBackgroundColor", baseColor)
            val orientationList = settings.orientationList
            orientationList.forEachIndexed { index, value ->
                val button = ViewIds.list[index]
                Orientation.values.find { it.orientation == value }?.let {
                    views.setImageViewResource(button.iconId, it.icon)
                    views.setTextViewText(button.titleId, context.getText(it.label))
                    views.setOnClickPendingIntent(button.buttonId, createOrientationIntent(context, it.orientation))
                }
            }
            val iconShape = settings.iconShape
            val selectedIndex = orientationList.indexOf(orientation)
            ViewIds.list.forEachIndexed { index, it ->
                views.setImageViewResource(it.backgroundId, iconShape.iconId)
                if (index == selectedIndex) {
                    if (shouldUseIconBackground) {
                        views.setInt(it.buttonId, "setBackgroundColor", Color.TRANSPARENT)
                        views.setViewVisibility(it.backgroundId, View.VISIBLE)
                        views.setInt(it.backgroundId, "setColorFilter", selectedBackground)
                    } else {
                        views.setInt(it.buttonId, "setBackgroundColor", selectedBackground)
                        views.setViewVisibility(it.backgroundId, View.GONE)
                    }
                    views.setInt(it.iconId, "setColorFilter", selectedForeground)
                    views.setTextColor(it.titleId, selectedForeground)
                } else {
                    if (shouldUseIconBackground) {
                        views.setViewVisibility(it.backgroundId, View.VISIBLE)
                        views.setInt(it.backgroundId, "setColorFilter", background)
                    } else {
                        views.setViewVisibility(it.backgroundId, View.GONE)
                    }
                    views.setInt(it.buttonId, "setBackgroundColor", Color.TRANSPARENT)
                    views.setInt(it.iconId, "setColorFilter", foreground)
                    views.setTextColor(it.titleId, foreground)
                }
                if (shouldUseIconBackground) {
                    views.setViewVisibility(it.titleId, View.GONE)
                } else {
                    views.setViewVisibility(it.titleId, View.VISIBLE)
                }
                if (index < orientationList.size) {
                    views.setViewVisibility(it.buttonId, View.VISIBLE)
                } else {
                    views.setViewVisibility(it.buttonId, View.GONE)
                }
            }
            views.setInt(R.id.remote_views_button_settings, "setBackgroundColor", Color.TRANSPARENT)
            val whiteForeground = baseColor.shouldUseWhiteForeground()
            val settingsColor = if (shouldUseIconBackground) {
                if (whiteForeground) Color.WHITE else Color.BLACK
            } else foreground
            views.setInt(R.id.remote_views_icon_settings, "setColorFilter", settingsColor)
            views.setOnClickPendingIntent(R.id.remote_views_button_settings, createActivityIntent(context))
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
