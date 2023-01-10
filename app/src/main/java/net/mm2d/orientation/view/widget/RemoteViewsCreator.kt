/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget

import android.app.PendingIntent
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.Functions
import net.mm2d.orientation.control.PendingIntentCreator
import net.mm2d.orientation.control.mapOrientation
import net.mm2d.orientation.settings.DesignPreference
import net.mm2d.orientation.settings.OrientationPreference
import net.mm2d.orientation.util.alpha
import net.mm2d.orientation.util.opaque
import net.mm2d.orientation.view.widget.ViewIds.ViewId

object RemoteViewsCreator {
    fun create(
        context: Context,
        orientation: OrientationPreference,
        design: DesignPreference,
    ): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification).also { views ->
            views.helper(R.id.notification).setBackgroundColor(design.base)
            design.functions.forEachIndexed { index, function ->
                val button = ViewIds.notification[index]
                val helpers = RemoteViewHelpers(views, button)
                helpers.button.setOnClickPendingIntent(PendingIntentCreator.function(context, function))
                Functions.find(function)?.let { helpers.icon.setImageResource(it.icon) }
            }
            val selectedIndex = design.functions.mapOrientation().indexOf(orientation.orientation)
            ViewIds.notification.forEachIndexed { index, it ->
                val helpers = RemoteViewHelpers(views, it)
                helpers.shape.setImageResource(design.shape.iconId)
                if (index == selectedIndex) {
                    helpers.shape.setImageColor(design.backgroundSelected)
                    helpers.icon.setImageColor(design.foregroundSelected)
                } else {
                    helpers.shape.setImageColor(design.background)
                    helpers.icon.setImageColor(design.foreground)
                }
                helpers.button.setVisible(index < design.functions.size)
            }
        }

    private class RemoteViewHelpers(views: RemoteViews, viewId: ViewId) {
        val button = views.helper(viewId.buttonId)
        val icon = views.helper(viewId.iconId)
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
