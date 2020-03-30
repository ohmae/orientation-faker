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

    data class Entity(
        val value: Int,
        val icon: Int,
        val label: Int
    )

    val values: List<Entity> = listOf(
        Entity(
            PORTRAIT,
            R.drawable.ic_portrait,
            R.string.portrait
        ),
        Entity(
            LANDSCAPE,
            R.drawable.ic_landscape,
            R.string.landscape
        ),
        Entity(
            REVERSE_PORTRAIT,
            R.drawable.ic_reverse_portrait,
            R.string.reverse_portrait
        ),
        Entity(
            REVERSE_LANDSCAPE,
            R.drawable.ic_reverse_landscape,
            R.string.reverse_landscape
        ),
        Entity(
            UNSPECIFIED,
            R.drawable.ic_unspecified,
            R.string.unspecified
        ),
        Entity(
            FULL_SENSOR,
            R.drawable.ic_force_sensor,
            R.string.force_sensor
        ),
        Entity(
            SENSOR_PORTRAIT,
            R.drawable.ic_sensor_portrait,
            R.string.sensor_portrait
        ),
        Entity(
            SENSOR_LANDSCAPE,
            R.drawable.ic_sensor_landscape,
            R.string.sensor_landscape
        )
    )

    val portrait: Set<Int> = setOf(
        PORTRAIT,
        REVERSE_PORTRAIT
    )
    val landscape: Set<Int> = setOf(
        LANDSCAPE,
        REVERSE_LANDSCAPE
    )
    val sensor: Set<Int> = setOf(
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE
    )
    val experimental: Set<Int> = setOf(
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE
    )
    val requestSystemSettings: Set<Int> = setOf(
        REVERSE_PORTRAIT,
        REVERSE_LANDSCAPE,
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE
    )
}
