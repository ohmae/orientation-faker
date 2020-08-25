package net.mm2d.orientation.control

import android.content.pm.ActivityInfo
import net.mm2d.android.orientationfaker.R

object Orientation {
    const val INVALID: Int = -3
    const val PORTRAIT: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    const val LANDSCAPE: Int = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    const val REVERSE_PORTRAIT: Int = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
    const val REVERSE_LANDSCAPE: Int = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
    const val UNSPECIFIED: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    const val FULL_SENSOR: Int = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    const val SENSOR_PORTRAIT: Int = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    const val SENSOR_LANDSCAPE: Int = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    const val SENSOR_LIE_RIGHT: Int = 101
    const val SENSOR_LIE_LEFT: Int = 102
    const val SENSOR_HEADSTAND: Int = 103

    data class Entity(
        val orientation: Int,
        val icon: Int,
        val label: Int,
        val description: Int
    )

    val values: List<Entity> = listOf(
        Entity(
            PORTRAIT,
            R.drawable.ic_portrait,
            R.string.label_portrait,
            R.string.description_portrait
        ),
        Entity(
            LANDSCAPE,
            R.drawable.ic_landscape,
            R.string.label_landscape,
            R.string.description_landscape
        ),
        Entity(
            REVERSE_PORTRAIT,
            R.drawable.ic_reverse_portrait,
            R.string.label_reverse_portrait,
            R.string.description_reverse_portrait
        ),
        Entity(
            REVERSE_LANDSCAPE,
            R.drawable.ic_reverse_landscape,
            R.string.label_reverse_landscape,
            R.string.description_reverse_landscape
        ),
        Entity(
            UNSPECIFIED,
            R.drawable.ic_unspecified,
            R.string.label_unspecified,
            R.string.description_unspecified
        ),
        Entity(
            FULL_SENSOR,
            R.drawable.ic_force_sensor,
            R.string.label_force_sensor,
            R.string.description_force_sensor
        ),
        Entity(
            SENSOR_PORTRAIT,
            R.drawable.ic_sensor_portrait,
            R.string.label_sensor_portrait,
            R.string.description_sensor_portrait
        ),
        Entity(
            SENSOR_LANDSCAPE,
            R.drawable.ic_sensor_landscape,
            R.string.label_sensor_landscape,
            R.string.description_sensor_landscape
        ),
        Entity(
            SENSOR_LIE_LEFT,
            R.drawable.ic_sensor_lie_left,
            R.string.label_sensor_lie_left,
            R.string.description_sensor_lie_left
        ),
        Entity(
            SENSOR_LIE_RIGHT,
            R.drawable.ic_sensor_lie_right,
            R.string.label_sensor_lie_right,
            R.string.description_sensor_lie_right
        ),
        Entity(
            SENSOR_HEADSTAND,
            R.drawable.ic_sensor_headstand,
            R.string.label_sensor_headstand,
            R.string.description_sensor_headstand
        )
    )

    val portrait: Set<Int> = setOf(
        PORTRAIT,
        REVERSE_PORTRAIT,
    )
    val landscape: Set<Int> = setOf(
        LANDSCAPE,
        REVERSE_LANDSCAPE,
    )
    val sensor: Set<Int> = setOf(
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE,
        SENSOR_LIE_RIGHT,
        SENSOR_LIE_LEFT,
        SENSOR_HEADSTAND,
    )
    val experimental: Set<Int> = setOf(
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE,
        SENSOR_LIE_RIGHT,
        SENSOR_LIE_LEFT,
        SENSOR_HEADSTAND,
    )
    val requestSystemSettings: Set<Int> = setOf(
        REVERSE_PORTRAIT,
        REVERSE_LANDSCAPE,
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE,
        SENSOR_LIE_RIGHT,
        SENSOR_LIE_LEFT,
        SENSOR_HEADSTAND,
    )
}
