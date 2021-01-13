/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService

object Powers {
    fun isInteractive(context: Context): Boolean =
        context.getSystemService<PowerManager>()?.isInteractive == true
}
