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
import net.mm2d.orientation.control.Orientations
import net.mm2d.orientation.settings.DesignPreference
import net.mm2d.orientation.settings.OrientationPreference
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

    fun update(orientation: OrientationPreference, design: DesignPreference) {
        val baseColor = if (design.iconize) design.base ?: 0 else design.background
        base.setBackgroundColor(baseColor)
        val orientationList = design.orientations
        orientationList.forEachIndexed { index, value ->
            val button = buttonList[index]
            Orientations.find(value)?.let {
                button.icon.setImageResource(it.icon)
                button.label.setText(it.label)
                button.orientation = value
            }
        }
        val iconShape = design.shape
        val selectedIndex = orientationList.indexOf(orientation.orientation)
        buttonList.forEachIndexed { index, it ->
            it.shape.setImageResource(iconShape.iconId)
            if (index == selectedIndex) {
                if (design.iconize) {
                    it.button.setBackgroundColor(Color.TRANSPARENT)
                    it.shape.isVisible = true
                    it.shape.setImageColor(design.backgroundSelected)
                } else {
                    it.button.setBackgroundColor(design.backgroundSelected)
                    it.shape.isVisible = false
                }
                it.icon.setImageColor(design.foregroundSelected)
                it.label.setTextColor(design.foregroundSelected)
            } else {
                if (design.iconize) {
                    it.shape.isVisible = true
                    it.shape.setImageColor(design.background)
                } else {
                    it.shape.isVisible = false
                }
                it.button.setBackgroundColor(Color.TRANSPARENT)
                it.icon.setImageColor(design.foreground)
                it.label.setTextColor(design.foreground)
            }
            it.label.isVisible = !design.iconize
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
        var orientation: Orientation = Orientation.INVALID
    )
}
