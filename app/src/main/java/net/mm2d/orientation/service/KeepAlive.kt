/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.service

import android.content.Context
import net.mm2d.orientation.settings.Settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object KeepAlive {
    fun ensureResident(context: Context) {
        Settings.doOnGet {
            if (it.shouldAutoStart()) {
                MainService.start(context)
            }
        }
    }
}
