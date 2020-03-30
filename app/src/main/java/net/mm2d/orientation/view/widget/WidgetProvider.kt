/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.Settings

/**
 * Implementation of App Widget functionality.
 */
class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val orientation =
            if (OrientationHelper.isEnabled) {
                Settings.get().orientation
            } else {
                Orientation.INVALID
            }
        appWidgetIds.forEach {
            updateAppWidget(context, appWidgetManager, it, orientation)
        }
    }

    companion object {
        fun start(context: Context) {
            update(context, Settings.get().orientation)
        }

        fun stop(context: Context) {
            update(context, Orientation.INVALID)
        }

        private fun update(context: Context, orientation: Int) {
            val widgetManager: AppWidgetManager =
                context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
            widgetManager.getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))?.forEach {
                updateAppWidget(context, widgetManager, it, orientation)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, id: Int, orientation: Int) {
            val views = RemoteViewsCreator.create(context, orientation, false)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
