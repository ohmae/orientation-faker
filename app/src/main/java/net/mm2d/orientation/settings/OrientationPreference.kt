package net.mm2d.orientation.settings

import net.mm2d.orientation.control.Orientation

data class OrientationPreference(
    val enabled: Boolean,
    val orientation: Orientation,
    val isLandscapeDevice: Boolean,
    val shouldControlByForegroundApp: Boolean,
    val orientationWhenPowerIsConnected: Orientation,
)
