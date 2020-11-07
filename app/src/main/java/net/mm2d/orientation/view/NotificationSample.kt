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
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.view.widget.ViewIds

class NotificationSample(activity: Activity) {
    val buttonList: List<ButtonInfo> = ViewIds.list.map {
        ButtonInfo(
            activity.findViewById(it.buttonId),
            activity.findViewById(it.iconId),
            activity.findViewById(it.titleId),
            activity.findViewById(it.backgroundId),
        )
    }
    private val background = activity.findViewById<View>(R.id.notification)

    init {
        activity.findViewById<View>(R.id.remote_views_button_settings).visibility = View.GONE
    }

    fun update() {
        val settings = Settings.get()
        val orientation = OrientationHelper.getOrientation()
        val foreground = settings.foregroundColor
        background.setBackgroundColor(settings.backgroundColor)
        val selectedForeground = settings.foregroundColorSelected
        val selectedBackground = settings.backgroundColorSelected
        val orientationList = settings.orientationList
        orientationList.forEachIndexed { index, value ->
            val button = buttonList[index]
            Orientation.values.find { it.orientation == value }?.let {
                button.icon.setImageResource(it.icon)
                button.title.setText(it.label)
                button.orientation = value
            }
        }
        val selectedIndex = orientationList.indexOf(orientation)
        val shouldUseRoundBackground = settings.shouldUseRoundBackground
        buttonList.forEachIndexed { index, it ->
            if (index == selectedIndex) {
                if (shouldUseRoundBackground) {
                    it.button.setBackgroundColor(Color.TRANSPARENT)
                    it.background.visibility = View.VISIBLE
                    it.background.setColorFilter(selectedBackground)
                } else {
                    it.button.setBackgroundColor(selectedBackground)
                    it.background.visibility = View.GONE
                }
                it.icon.setColorFilter(selectedForeground)
                it.title.setTextColor(selectedForeground)
            } else {
                it.background.visibility = View.GONE
                it.button.setBackgroundColor(Color.TRANSPARENT)
                it.icon.setColorFilter(foreground)
                it.title.setTextColor(foreground)
            }
            if (shouldUseRoundBackground) {
                it.title.visibility = View.GONE
            } else {
                it.title.visibility = View.VISIBLE
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
        val background: ImageView,
        var orientation: Int = Orientation.INVALID
    )
}
