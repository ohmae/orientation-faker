package net.mm2d.orientation.settings

import net.mm2d.orientation.control.Orientation

data class OrientationRequest(
    val orientation: Orientation = Orientation.INVALID,
    val timestamp: Long = System.currentTimeMillis(),
)
