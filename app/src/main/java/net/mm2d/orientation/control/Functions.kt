/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.FunctionButton.LauncherButton
import net.mm2d.orientation.control.FunctionButton.OrientationButton

object Functions {
    interface Entity {
        val function: FunctionButton
        val icon: Int
        val label: Int
        val description: Int
    }

    data class OrientationEntity(
        override val function: OrientationButton,
        override val icon: Int,
        override val label: Int,
        override val description: Int,
    ) : Entity


    data class FunctionEntity(
        override val function: FunctionButton,
        override val icon: Int,
        override val label: Int,
        override val description: Int,
    ) : Entity

    val orientations: List<OrientationEntity> = listOf(
        OrientationEntity(
            OrientationButton.PORTRAIT,
            R.drawable.ic_portrait,
            R.string.label_portrait,
            R.string.description_portrait,
        ),
        OrientationEntity(
            OrientationButton.LANDSCAPE,
            R.drawable.ic_landscape,
            R.string.label_landscape,
            R.string.description_landscape,
        ),
        OrientationEntity(
            OrientationButton.REVERSE_PORTRAIT,
            R.drawable.ic_reverse_portrait,
            R.string.label_reverse_portrait,
            R.string.description_reverse_portrait,
        ),
        OrientationEntity(
            OrientationButton.REVERSE_LANDSCAPE,
            R.drawable.ic_reverse_landscape,
            R.string.label_reverse_landscape,
            R.string.description_reverse_landscape,
        ),
        OrientationEntity(
            OrientationButton.UNSPECIFIED,
            R.drawable.ic_unspecified,
            R.string.label_unspecified,
            R.string.description_unspecified,
        ),
        OrientationEntity(
            OrientationButton.FULL_SENSOR,
            R.drawable.ic_full_sensor,
            R.string.label_full_sensor,
            R.string.description_full_sensor,
        ),
        OrientationEntity(
            OrientationButton.SENSOR_PORTRAIT,
            R.drawable.ic_sensor_portrait,
            R.string.label_sensor_portrait,
            R.string.description_sensor_portrait,
        ),
        OrientationEntity(
            OrientationButton.SENSOR_LANDSCAPE,
            R.drawable.ic_sensor_landscape,
            R.string.label_sensor_landscape,
            R.string.description_sensor_landscape,
        ),
        OrientationEntity(
            OrientationButton.SENSOR_LIE_LEFT,
            R.drawable.ic_sensor_lie_left,
            R.string.label_sensor_lie_left,
            R.string.description_sensor_lie_left,
        ),
        OrientationEntity(
            OrientationButton.SENSOR_LIE_RIGHT,
            R.drawable.ic_sensor_lie_right,
            R.string.label_sensor_lie_right,
            R.string.description_sensor_lie_right,
        ),
        OrientationEntity(
            OrientationButton.SENSOR_HEADSTAND,
            R.drawable.ic_sensor_headstand,
            R.string.label_sensor_headstand,
            R.string.description_sensor_headstand,
        ),
        OrientationEntity(
            OrientationButton.SENSOR_FULL,
            R.drawable.ic_sensor_full,
            R.string.label_sensor_full,
            R.string.description_sensor_full,
        ),
        OrientationEntity(
            OrientationButton.SENSOR_FORWARD,
            R.drawable.ic_sensor_forward,
            R.string.label_sensor_forward,
            R.string.description_sensor_forward,
        ),
        OrientationEntity(
            OrientationButton.SENSOR_REVERSE,
            R.drawable.ic_sensor_reverse,
            R.string.label_sensor_reverse,
            R.string.description_sensor_reverse,
        ),
    )

    val functions: List<Entity> = orientations +
        FunctionEntity(
            LauncherButton.SETTINGS,
            R.drawable.ic_settings,
            R.string.label_setting,
            R.string.description_settings,
        )

    fun find(orientation: Orientation): Entity? = orientations.find { it.function.orientation == orientation }

    fun find(function: FunctionButton): Entity? = functions.find { it.function == function }
}
