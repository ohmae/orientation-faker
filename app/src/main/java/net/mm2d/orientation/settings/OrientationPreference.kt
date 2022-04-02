package net.mm2d.orientation.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.mm2d.orientation.control.Orientation

@Parcelize
data class OrientationPreference(
    val enabled: Boolean,
    val orientation: Orientation,
    val isLandscapeDevice: Boolean,
    val shouldControlByForegroundApp: Boolean,
    val orientationWhenPowerIsConnected: Orientation,
) : Parcelable
