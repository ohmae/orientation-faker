/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import net.mm2d.android.orientationfaker.R

object Orientations {
    data class Entity(
        val orientation: Orientation,
        val icon: Int,
        val label: Int,
        val description: Int
    )

    val entries: List<Entity> = listOf(
        Entity(
            Orientation.PORTRAIT,
            R.drawable.ic_portrait,
            R.string.label_portrait,
            R.string.description_portrait
        ),
        Entity(
            Orientation.LANDSCAPE,
            R.drawable.ic_landscape,
            R.string.label_landscape,
            R.string.description_landscape
        ),
        Entity(
            Orientation.REVERSE_PORTRAIT,
            R.drawable.ic_reverse_portrait,
            R.string.label_reverse_portrait,
            R.string.description_reverse_portrait
        ),
        Entity(
            Orientation.REVERSE_LANDSCAPE,
            R.drawable.ic_reverse_landscape,
            R.string.label_reverse_landscape,
            R.string.description_reverse_landscape
        ),
        Entity(
            Orientation.UNSPECIFIED,
            R.drawable.ic_unspecified,
            R.string.label_unspecified,
            R.string.description_unspecified
        ),
        Entity(
            Orientation.FULL_SENSOR,
            R.drawable.ic_force_sensor,
            R.string.label_force_sensor,
            R.string.description_force_sensor
        ),
        Entity(
            Orientation.SENSOR_PORTRAIT,
            R.drawable.ic_sensor_portrait,
            R.string.label_sensor_portrait,
            R.string.description_sensor_portrait
        ),
        Entity(
            Orientation.SENSOR_LANDSCAPE,
            R.drawable.ic_sensor_landscape,
            R.string.label_sensor_landscape,
            R.string.description_sensor_landscape
        ),
        Entity(
            Orientation.SENSOR_LIE_LEFT,
            R.drawable.ic_sensor_lie_left,
            R.string.label_sensor_lie_left,
            R.string.description_sensor_lie_left
        ),
        Entity(
            Orientation.SENSOR_LIE_RIGHT,
            R.drawable.ic_sensor_lie_right,
            R.string.label_sensor_lie_right,
            R.string.description_sensor_lie_right
        ),
        Entity(
            Orientation.SENSOR_HEADSTAND,
            R.drawable.ic_sensor_headstand,
            R.string.label_sensor_headstand,
            R.string.description_sensor_headstand
        )
    )
}
