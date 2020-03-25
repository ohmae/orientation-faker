package net.mm2d.orientation.control

import android.content.pm.ActivityInfo
import net.mm2d.android.orientationfaker.R

object Orientations {
    const val SCREEN_ORIENTATION_INVALID: Int = -3

    data class Orientation(
        val value: Int,
        val icon: Int,
        val label: Int
    )

    val values: List<Orientation> = listOf(
        Orientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
            R.drawable.ic_portrait,
            R.string.portrait
        ),
        Orientation(
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
            R.drawable.ic_landscape,
            R.string.landscape
        ),
        Orientation(
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
            R.drawable.ic_reverse_portrait,
            R.string.reverse_portrait
        ),
        Orientation(
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            R.drawable.ic_reverse_landscape,
            R.string.reverse_landscape
        ),
        Orientation(
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
            R.drawable.ic_unspecified,
            R.string.unspecified
        ),
        Orientation(
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR,
            R.drawable.ic_force_sensor,
            R.string.force_sensor
        ),
        Orientation(
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
            R.drawable.ic_sensor_portrait,
            R.string.sensor_portrait
        ),
        Orientation(
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            R.drawable.ic_sensor_landscape,
            R.string.sensor_landscape
        )
    )
}
