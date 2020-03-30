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
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.view.widget.ViewIds

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class NotificationSample(activity: Activity) {
    val buttonList: List<ButtonInfo> = ViewIds.list.map {
        ButtonInfo(
            activity.findViewById(it.viewId),
            activity.findViewById(it.iconViewId),
            activity.findViewById(it.titleViewId)
        )
    }
    private val background = activity.notification

    init {
        activity.findViewById<View>(R.id.remote_views_button_settings).visibility = View.GONE
    }

    fun update() {
        val settings = Settings.get()
        val orientation = settings.orientation
        val foreground = settings.foregroundColor
        background.setBackgroundColor(settings.backgroundColor)
        val selectedForeground = settings.foregroundColorSelected
        val selectedBackground = settings.backgroundColorSelected
        val orientationList = settings.orientationList
        orientationList.forEachIndexed { index, value ->
            val button = buttonList[index]
            Orientation.values.find { it.value == value }?.let {
                button.icon.setImageResource(it.icon)
                button.title.setText(it.label)
                button.orientation = value
            }
        }
        val selectedIndex = orientationList.indexOf(orientation)
        buttonList.forEachIndexed { index, it ->
            if (index == selectedIndex) {
                it.button.setBackgroundColor(selectedBackground)
                it.icon.setColorFilter(selectedForeground)
                it.title.setTextColor(selectedForeground)
            } else {
                it.button.setBackgroundColor(Color.TRANSPARENT)
                it.icon.setColorFilter(foreground)
                it.title.setTextColor(foreground)
            }
            if (index < orientationList.size) {
                it.button.visibility = View.VISIBLE
            } else {
                it.button.visibility = View.GONE
            }
        }
    }

    class ButtonInfo(
        val button: View,
        val icon: ImageView,
        val title: TextView,
        var orientation: Int = Orientation.INVALID
    )
}
