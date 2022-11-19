/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import android.os.Build
import net.mm2d.android.orientationfaker.BuildConfig
import java.io.File

class OldPreference(
    private val context: Context
) {
    fun deleteIfEmpty() {
        deleteSharedPreferences(BuildConfig.APPLICATION_ID + ".Main")
        deleteSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences")
        deleteSharedPreferences(BuildConfig.APPLICATION_ID + ".b")
    }

    private fun makeSharedPreferenceFile(name: String): File {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        return File(prefsDir, "$name.xml")
    }

    private fun deleteSharedPreferences(name: String) {
        if (Build.VERSION.SDK_INT >= 24) {
            context.deleteSharedPreferences(name)
        } else {
            val prefsFile = makeSharedPreferenceFile(name)
            val prefsBackup = File(prefsFile.path + ".bak")
            prefsFile.delete()
            prefsBackup.delete()
        }
    }
}
