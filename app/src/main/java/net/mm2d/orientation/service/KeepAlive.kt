/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.service

import net.mm2d.orientation.settings.Settings

object KeepAlive {
    fun ensureResident() {
        if (Settings.get().shouldAutoStart()) {
            MainController.start()
        }
    }
}
