/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import net.mm2d.android.orientationfaker.BuildConfig

object OldPreference {
    fun deleteAll(context: Context) {
        context.deleteSharedPreferences(BuildConfig.APPLICATION_ID + ".Main")
        context.deleteSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences")
        context.deleteSharedPreferences(BuildConfig.APPLICATION_ID + ".b")
    }
}
