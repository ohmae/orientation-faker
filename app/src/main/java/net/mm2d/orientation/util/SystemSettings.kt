/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.content.Context
import android.provider.Settings.System

object SystemSettings {
    fun rotationIsFixed(context: Context): Boolean = try {
        System.getInt(context.contentResolver, System.ACCELEROMETER_ROTATION) == 0
    } catch (ignored: Exception) {
        false
    }
}
