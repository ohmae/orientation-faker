/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ControlPreference(
    val shouldNotifySecret: Boolean,
    val shouldUseBlankIcon: Boolean,
) : Parcelable
