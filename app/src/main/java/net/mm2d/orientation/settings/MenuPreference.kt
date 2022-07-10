/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

data class MenuPreference(
    val warnSystemRotate: Boolean,
    val nightMode: Int,
    val shouldShowAllApp: Boolean,
    val notificationPermissionRequested: Boolean,
)
