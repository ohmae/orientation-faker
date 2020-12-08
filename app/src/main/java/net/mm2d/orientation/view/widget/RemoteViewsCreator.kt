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
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationReceiver
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.alpha
import net.mm2d.orientation.util.opaque
import net.mm2d.orientation.util.shouldUseWhiteForeground
import net.mm2d.orientation.view.MainActivity
import net.mm2d.orientation.view.widget.ViewIds.ViewId

object RemoteViewsCreator {
    fun create(context: Context, orientation: Int): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification).also { views ->
            val settings = Settings.get()
            val fgColor = settings.foregroundColor
            val bgColor = settings.backgroundColor
            val selFgColor = settings.foregroundColorSelected
            val selBgColor = settings.backgroundColorSelected
            val shouldUseIconBackground = settings.shouldUseIconBackground
            val baseColor = if (shouldUseIconBackground) settings.baseColor else bgColor
            views.helper(R.id.notification).setBackgroundColor(baseColor)
            val orientationList = settings.orientationList
            orientationList.forEachIndexed { index, value ->
                val button = ViewIds.list[index]
                Orientation.values.find { it.orientation == value }?.let {
                    val helpers = RemoteViewHelpers(views, button)
                    helpers.icon.setImageResource(it.icon)
                    helpers.label.setText(it.label)
                    helpers.button.setOnClickPendingIntent(createOrientationIntent(context, it.orientation))
                }
            }
            val iconShape = settings.iconShape
            val selectedIndex = orientationList.indexOf(orientation)
            ViewIds.list.forEachIndexed { index, it ->
                val helpers = RemoteViewHelpers(views, it)
                helpers.shape.setImageResource(iconShape.iconId)
                if (index == selectedIndex) {
                    if (shouldUseIconBackground) {
                        helpers.button.setBackgroundColor(Color.TRANSPARENT)
                        helpers.shape.setVisible(true)
                        helpers.shape.setImageColor(selBgColor)
                    } else {
                        helpers.button.setBackgroundColor(selBgColor)
                        helpers.shape.setVisible(false)
                    }
                    helpers.icon.setImageColor(selFgColor)
                    helpers.label.setTextColor(selFgColor)
                } else {
                    if (shouldUseIconBackground) {
                        helpers.shape.setVisible(true)
                        helpers.shape.setImageColor(bgColor)
                    } else {
                        helpers.shape.setVisible(false)
                    }
                    helpers.button.setBackgroundColor(Color.TRANSPARENT)
                    helpers.icon.setImageColor(fgColor)
                    helpers.label.setTextColor(fgColor)
                }
                helpers.label.setVisible(!shouldUseIconBackground)
                helpers.button.setVisible(index < orientationList.size)
            }
            val whiteForeground = baseColor.shouldUseWhiteForeground()
            val settingsColor = if (shouldUseIconBackground) {
                if (whiteForeground) Color.WHITE else Color.BLACK
            } else fgColor
            views.helper(R.id.remote_views_icon_settings).setImageColor(settingsColor)
            views.helper(R.id.remote_views_button_settings).also {
                it.setBackgroundColor(Color.TRANSPARENT)
                it.setOnClickPendingIntent(createActivityIntent(context))
            }
        }

    private class RemoteViewHelpers(views: RemoteViews, viewId: ViewId) {
        val button = views.helper(viewId.buttonId)
        val icon = views.helper(viewId.iconId)
        val label = views.helper(viewId.labelId)
        val shape = views.helper(viewId.shapeId)
    }

    private class RemoteViewHelper(
        private val views: RemoteViews,
        @IdRes private val id: Int
    ) {
        fun setVisible(visible: Boolean) {
            views.setViewVisibility(id, if (visible) View.VISIBLE else View.GONE)
        }

        fun setImageResource(@DrawableRes resourceId: Int) {
            views.setImageViewResource(id, resourceId)
        }

        fun setText(@StringRes text: Int) {
            views.setInt(id, "setText", text)
        }

        fun setTextColor(@ColorInt color: Int) {
            views.setTextColor(id, color)
        }

        fun setImageColor(@ColorInt color: Int) {
            views.setInt(id, "setColorFilter", color.opaque())
            views.setInt(id, "setImageAlpha", color.alpha())
        }

        fun setBackgroundColor(@ColorInt color: Int) {
            views.setInt(id, "setBackgroundColor", color)
        }

        fun setOnClickPendingIntent(pendingIntent: PendingIntent) {
            views.setOnClickPendingIntent(id, pendingIntent)
        }
    }

    private fun RemoteViews.helper(@IdRes id: Int): RemoteViewHelper =
        RemoteViewHelper(this, id)

    private fun createOrientationIntent(context: Context, orientation: Int): PendingIntent {
        val intent = Intent(OrientationReceiver.ACTION_ORIENTATION).also {
            it.putExtra(OrientationReceiver.EXTRA_ORIENTATION, orientation)
            it.setClass(context, OrientationReceiver::class.java)
        }
        return PendingIntent.getBroadcast(
            context,
            orientation + 1000,
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
