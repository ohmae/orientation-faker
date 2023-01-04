/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget

import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.Functions
import net.mm2d.orientation.control.PendingIntentCreator
import net.mm2d.orientation.control.mapOrientation
import net.mm2d.orientation.settings.DesignPreference
import net.mm2d.orientation.settings.OrientationPreference
import net.mm2d.orientation.util.alpha
import net.mm2d.orientation.util.opaque
import net.mm2d.orientation.util.shouldUseWhiteForeground
import net.mm2d.orientation.view.widget.ViewIds.ViewId

object RemoteViewsCreator {
    fun create(
        context: Context,
        orientation: OrientationPreference,
        design: DesignPreference,
        forWidget: Boolean = false
    ): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification).also { views ->
            val baseColor = if (design.iconize) design.base ?: 0 else design.background
            views.helper(R.id.notification).setBackgroundColor(baseColor)
            design.functions.forEachIndexed { index, value ->
                val button = ViewIds.list[index]
                Functions.find(value)?.let {
                    val helpers = RemoteViewHelpers(views, button)
                    helpers.icon.setImageResource(it.icon)
                    helpers.label.setText(it.label)
                    helpers.button.setOnClickPendingIntent(PendingIntentCreator.function(context, it.function))
                }
            }
            val selectedIndex = design.functions.mapOrientation().indexOf(orientation.orientation)
            ViewIds.list.forEachIndexed { index, it ->
                val helpers = RemoteViewHelpers(views, it)
                helpers.shape.setImageResource(design.shape.iconId)
                if (index == selectedIndex) {
                    if (design.iconize) {
                        helpers.button.setBackgroundColor(Color.TRANSPARENT)
                        helpers.shape.setVisible(true)
                        helpers.shape.setImageColor(design.backgroundSelected)
                    } else {
                        helpers.button.setBackgroundColor(design.backgroundSelected)
                        helpers.shape.setVisible(false)
                    }
                    helpers.icon.setImageColor(design.foregroundSelected)
                    helpers.label.setTextColor(design.foregroundSelected)
                } else {
                    if (design.iconize) {
                        helpers.shape.setVisible(true)
                        helpers.shape.setImageColor(design.background)
                    } else {
                        helpers.shape.setVisible(false)
                    }
                    helpers.button.setBackgroundColor(Color.TRANSPARENT)
                    helpers.icon.setImageColor(design.foreground)
                    helpers.label.setTextColor(design.foreground)
                }
                helpers.label.setVisible(!design.iconize)
                helpers.button.setVisible(index < design.functions.size)
            }
            val whiteForeground = baseColor.shouldUseWhiteForeground()
            val settingsColor = if (design.iconize) {
                if (whiteForeground) Color.WHITE else Color.BLACK
            } else design.foreground
            views.helper(R.id.remote_views_icon_settings).setImageColor(settingsColor)
            views.helper(R.id.remote_views_button_settings).also {
                it.setBackgroundColor(Color.TRANSPARENT)
                it.setOnClickPendingIntent(PendingIntentCreator.activity(context))
                it.setVisible(!forWidget && design.shouldShowSettings)
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
}
