/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import net.mm2d.orientation.control.Orientation

data class OrientationRequest(
    val orientation: Orientation = Orientation.INVALID,
    val timestamp: Long = System.currentTimeMillis(),
)
