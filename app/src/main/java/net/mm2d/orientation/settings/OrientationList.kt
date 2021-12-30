/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.toOrientation

object OrientationList {
    const val MAX = 6
    const val MIN = 1
    private const val DELIMITER = ","

    fun toString(list: List<Orientation>): String =
        list.map { it.value }.joinToString(DELIMITER)

    fun toList(string: String?): List<Orientation> =
        (string ?: "").split(DELIMITER)
            .mapNotNull { it.toIntOrNull() }
            .map { it.toOrientation() }
}
