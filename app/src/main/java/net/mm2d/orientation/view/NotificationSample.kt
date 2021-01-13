/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.alpha
import net.mm2d.orientation.util.opaque
import net.mm2d.orientation.view.widget.ViewIds

class NotificationSample(view: View) {
    val buttonList: List<ButtonViews> = ViewIds.list.map {
        ButtonViews(
            view.findViewById(it.buttonId),
            view.findViewById(it.iconId),
            view.findViewById(it.labelId),
            view.findViewById(it.shapeId),
        )
    }
    private val base = view.findViewById<View>(R.id.notification)

    init {
        view.findViewById<View>(R.id.remote_views_button_settings).visibility = View.GONE
    }

    fun update() {
        val settings = Settings.get()
        val orientation = OrientationHelper.getOrientation()
        val foreground = settings.foregroundColor
        val background = settings.backgroundColor
        val selectedForeground = settings.foregroundColorSelected
        val selectedBackground = settings.backgroundColorSelected
        val shouldUseIconBackground = settings.shouldUseIconBackground
        val baseColor = if (shouldUseIconBackground) settings.baseColor else background
        base.setBackgroundColor(baseColor)
        val orientationList = settings.orientationList
        orientationList.forEachIndexed { index, value ->
            val button = buttonList[index]
            Orientation.values.find { it.orientation == value }?.let {
                button.icon.setImageResource(it.icon)
                button.label.setText(it.label)
                button.orientation = value
            }
        }
        val iconShape = settings.iconShape
        val selectedIndex = orientationList.indexOf(orientation)
        buttonList.forEachIndexed { index, it ->
            it.shape.setImageResource(iconShape.iconId)
            if (index == selectedIndex) {
                if (shouldUseIconBackground) {
                    it.button.setBackgroundColor(Color.TRANSPARENT)
                    it.shape.isVisible = true
                    it.shape.setImageColor(selectedBackground)
                } else {
                    it.button.setBackgroundColor(selectedBackground)
                    it.shape.isVisible = false
                }
                it.icon.setImageColor(selectedForeground)
                it.label.setTextColor(selectedForeground)
            } else {
                if (shouldUseIconBackground) {
                    it.shape.isVisible = true
                    it.shape.setImageColor(background)
                } else {
                    it.shape.isVisible = false
                }
                it.button.setBackgroundColor(Color.TRANSPARENT)
                it.icon.setImageColor(foreground)
                it.label.setTextColor(foreground)
            }
            it.label.isVisible = !shouldUseIconBackground
            it.button.isVisible = index < orientationList.size
        }
    }

    private fun ImageView.setImageColor(@ColorInt color: Int) {
        setColorFilter(color.opaque())
        imageAlpha = color.alpha()
    }

    class ButtonViews(
        val button: View,
        val icon: ImageView,
        val label: TextView,
        val shape: ImageView,
        var orientation: Int = Orientation.INVALID
    )
}
