/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.content.pm.ActivityInfo

enum class Orientation(
    val value: Int
) {
    INVALID(-3),
    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
    REVERSE_PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT),
    REVERSE_LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE),
    UNSPECIFIED(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED),
    FULL_SENSOR(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR),
    SENSOR_PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT),
    SENSOR_LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE),
    SENSOR_LIE_RIGHT(101),
    SENSOR_LIE_LEFT(102),
    SENSOR_HEADSTAND(103),
    ;

    fun usesSensor(): Boolean = when (this) {
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE,
        SENSOR_LIE_RIGHT,
        SENSOR_LIE_LEFT,
        SENSOR_HEADSTAND,
        -> true
        else -> false
    }

    fun isPortrait(): Boolean = when (this) {
        PORTRAIT,
        REVERSE_PORTRAIT,
        -> true
        else -> false
    }

    fun isLandscape(): Boolean = when (this) {
        LANDSCAPE,
        REVERSE_LANDSCAPE,
        -> true
        else -> false
    }

    fun isExperimental(): Boolean = when (this) {
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE,
        SENSOR_LIE_RIGHT,
        SENSOR_LIE_LEFT,
        SENSOR_HEADSTAND,
        -> true
        else -> false
    }

    fun requestsSystemSettings(): Boolean = when (this) {
        REVERSE_PORTRAIT,
        REVERSE_LANDSCAPE,
        SENSOR_PORTRAIT,
        SENSOR_LANDSCAPE,
        SENSOR_LIE_RIGHT,
        SENSOR_LIE_LEFT,
        SENSOR_HEADSTAND,
        -> true
        else -> false
    }

    companion object {
        fun of(value: Int): Orientation =
            values().find { it.value == value } ?: INVALID
    }
}

fun Int.toOrientation(): Orientation = Orientation.of(this)
