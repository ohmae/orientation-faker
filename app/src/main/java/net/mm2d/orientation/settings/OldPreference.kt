/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.orientation.settings.Key.Main
import java.io.File

class OldPreference(
    private val context: Context
) {
    private val fileName = BuildConfig.APPLICATION_ID + "." + Main.FILE_NAME
    private val sharedPreferences: SharedPreferences?

    init {
        sharedPreferences = if (makeSharedPreferenceFile(fileName).exists()) {
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        } else null
    }

    fun deleteIfEmpty() {
        sharedPreferences?.let {
            val keys = sharedPreferences.all.keys
            if (keys.size == 0 || keys.size == 1 && keys.contains(Main.PREFERENCES_VERSION_INT.name)) {
                deleteSharedPreferences(fileName)
            }
        }
    }

    fun deleteOldSharedPreferences() {
        deleteSharedPreferences(context.packageName + "_preferences")
        // 2020/11/2 4.6.0
        deleteSharedPreferences(context.packageName + ".b")
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

    // 0 : 2013/04/21 : 1.0.0
    // 1 : 2018/01/14 : 2.0.0 - 2.1.2
    // 2 : 2018/12/16 : 2.2.0 -
    // 3 : 2020/03/28 : 4.0.0 -
    // 4 : 2020/12/05 : 4.7.0-
    // 5 : 2021/09/19 : 5.0.0-
    fun deleteIfTooOld() {
        getInt(Main.PREFERENCES_VERSION_INT)?.let { version ->
            if (version <= 3) {
                clear()
                deleteSharedPreferences(fileName)
            }
        }
    }

    fun remove(vararg keys: Main) {
        sharedPreferences?.edit(true) {
            keys.forEach { remove(it.name) }
        }
    }

    fun clear() {
        sharedPreferences?.edit(true) {
            clear()
        }
    }

    fun getBoolean(key: Main): Boolean? =
        if (sharedPreferences?.contains(key.name) == true)
            sharedPreferences.getBoolean(key.name, false)
        else null

    fun getInt(key: Main): Int? =
        if (sharedPreferences?.contains(key.name) == true)
            sharedPreferences.getInt(key.name, 0)
        else null

    fun getLong(key: Main): Long? =
        if (sharedPreferences?.contains(key.name) == true)
            sharedPreferences.getLong(key.name, 0L)
        else null

    fun getString(key: Main): String? =
        if (sharedPreferences?.contains(key.name) == true)
            sharedPreferences.getString(key.name, null)
        else null
}
