/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.layout_remote_views.*
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.OrientationIdManager
import net.mm2d.orientation.settings.Settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class NotificationSample(activity: Activity) {
    val buttonList: List<ButtonInfo> = OrientationIdManager.list.map {
        ButtonInfo(
            it.orientation,
            activity.findViewById(it.viewId),
            activity.findViewById(it.iconViewId),
            activity.findViewById(it.titleViewId)
        )
    }
    private val background = activity.notification

    init {
        activity.findViewById<View>(R.id.button_settings).visibility = View.GONE
    }

    fun update() {
        val settings = Settings.get()
        val orientation = settings.orientation
        val foreground = settings.foregroundColor
        background.setBackgroundColor(settings.backgroundColor)
        val selectedForeground = settings.foregroundColorSelected
        val selectedBackground = settings.backgroundColorSelected
        buttonList.forEach {
            if (it.orientation == orientation) {
                it.button.setBackgroundColor(selectedBackground)
                it.icon.setColorFilter(selectedForeground)
                it.title.setTextColor(selectedForeground)
            } else {
                it.button.setBackgroundColor(Color.TRANSPARENT)
                it.icon.setColorFilter(foreground)
                it.title.setTextColor(foreground)
            }
        }
        buttonList[0].title.setText(
            if (settings.useFullSensor) R.string.force_auto else R.string.unspecified
        )
    }

    class ButtonInfo(
        val orientation: Int,
        val button: View,
        val icon: ImageView,
        val title: TextView
    )
}
