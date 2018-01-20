/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker.settings

import android.content.pm.ActivityInfo

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object Maintainer {
    private const val SETTINGS_VERSION = 1

    fun maintain(storage: SettingsStorage) {
        val currentVersion = getSettingsVersion(storage)
        if (currentVersion == SETTINGS_VERSION) {
            return
        }
        if (currentVersion == 0) {
            migrateFrom0(storage)
        }
        storage.writeInt(Key.SETTINGS_VERSION, SETTINGS_VERSION)
    }

    private fun getSettingsVersion(storage: SettingsStorage): Int {
        // バージョン番号を割り振る前の設定値が含まれているかどうかで判断する
        @Suppress("DEPRECATION")
        return if (storage.preferences.contains("mode")) 0
        else storage.readInt(Key.SETTINGS_VERSION, -1)
    }

    private fun migrateFrom0(storage: SettingsStorage) {
        @Suppress("DEPRECATION")
        val resident = storage.preferences.getBoolean("startup", false)
        @Suppress("DEPRECATION")
        val orientation = convertToOrientationInt(storage.preferences.getString("mode", null))
        storage.apply {
            clear()
            writeBoolean(Key.RESIDENT, resident)
            writeInt(Key.ORIENTATION, orientation)
        }
    }

    private fun convertToOrientationInt(orientation: String?): Int {
        return when (orientation) {
            "portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            "landscape" -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            "r_portrait" -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            "r_landscape" -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}
