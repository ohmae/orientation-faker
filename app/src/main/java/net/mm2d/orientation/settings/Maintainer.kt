/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import android.os.Build
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.settings.Key.Main

internal object Maintainer {
    // 0 : 2013/04/21 : 1.0.0
    // 1 : 2018/01/14 : 2.0.0 - 2.1.2
    // 2 : 2018/12/16 : 2.2.0 -
    // 3 : 2020/03/28 : 4.0.0 -
    // 4 : 2020/12/05 : 4.7.0-
    // 5 : 2021/09/19 : 5.0.0-
    private const val SETTINGS_VERSION = 5

    fun maintain(context: Context, preferences: Preferences<Main>) {
        Main.values().checkSuffix()
        if (preferences.readInt(Main.APP_VERSION_AT_LAST_LAUNCHED_INT, 0) != BuildConfig.VERSION_CODE) {
            preferences.writeInt(Main.APP_VERSION_AT_LAST_LAUNCHED_INT, BuildConfig.VERSION_CODE)
        }
        val settingsVersion = preferences.readInt(Main.PREFERENCES_VERSION_INT, 0)
        if (settingsVersion == SETTINGS_VERSION) {
            return
        }
        preferences.writeInt(Main.PREFERENCES_VERSION_INT, SETTINGS_VERSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.deleteSharedPreferences(context.packageName + "_preferences")
        }
        if (settingsVersion == 4) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                preferences.writeInt(Main.COLOR_BASE_INT, Default.color.base)
                preferences.writeBoolean(Main.USE_ROUND_BACKGROUND_BOOLEAN, true)
                preferences.writeBoolean(Main.SHOW_SETTINGS_ON_NOTIFICATION_BOOLEAN, false)
            }
            return
        }
        if (settingsVersion == 3) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                preferences.writeInt(Main.COLOR_BASE_INT, Default.color.base)
                preferences.writeBoolean(Main.USE_ROUND_BACKGROUND_BOOLEAN, true)
                preferences.writeBoolean(Main.SHOW_SETTINGS_ON_NOTIFICATION_BOOLEAN, false)
                return
            }
            if (preferences.readBoolean(Main.USE_ROUND_BACKGROUND_BOOLEAN, false) &&
                preferences.contains(Main.COLOR_BACKGROUND_INT)
            ) {
                val bg = preferences.readInt(Main.COLOR_BACKGROUND_INT, Default.color.background)
                preferences.writeInt(Main.COLOR_BASE_INT, bg)
            }
            return
        }
        if (!preferences.contains(Main.APP_VERSION_AT_INSTALL_INT)) {
            preferences.writeInt(Main.APP_VERSION_AT_INSTALL_INT, BuildConfig.VERSION_CODE)
        }
        writeDefaultValue(preferences)
    }

    private fun writeDefaultValue(preferences: Preferences<Main>) {
        preferences.writeLong(Main.TIME_FIRST_USE_LONG, 0L)
        preferences.writeLong(Main.TIME_FIRST_REVIEW_LONG, 0L)
        preferences.writeInt(Main.COUNT_ORIENTATION_CHANGED_INT, 0)
        preferences.writeInt(Main.COUNT_REVIEW_DIALOG_CANCELED_INT, 0)
        preferences.writeBoolean(Main.REVIEW_REPORTED_BOOLEAN, false)
        preferences.writeBoolean(Main.REVIEW_REVIEWED_BOOLEAN, false)
        preferences.writeInt(Main.ORIENTATION_INT, Orientation.UNSPECIFIED.value)
        preferences.writeBoolean(Main.RESIDENT_BOOLEAN, false)
        preferences.writeInt(Main.COLOR_FOREGROUND_INT, Default.color.foreground)
        preferences.writeInt(Main.COLOR_BACKGROUND_INT, Default.color.background)
        preferences.writeInt(Main.COLOR_FOREGROUND_SELECTED_INT, Default.color.foregroundSelected)
        preferences.writeInt(Main.COLOR_BACKGROUND_SELECTED_INT, Default.color.backgroundSelected)
        preferences.writeBoolean(Main.NOTIFY_SECRET_BOOLEAN, false)
        preferences.writeBoolean(Main.AUTO_ROTATE_WARNING_BOOLEAN, true)
        preferences.writeBoolean(Main.USE_BLANK_ICON_FOR_NOTIFICATION_BOOLEAN, false)
        preferences.writeString(Main.ORIENTATION_LIST_STRING, Default.orientationList.joinToString(","))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            preferences.writeInt(Main.COLOR_BASE_INT, Default.color.base)
            preferences.writeBoolean(Main.USE_ROUND_BACKGROUND_BOOLEAN, true)
            preferences.writeBoolean(Main.SHOW_SETTINGS_ON_NOTIFICATION_BOOLEAN, false)
        } else {
            preferences.writeBoolean(Main.USE_ROUND_BACKGROUND_BOOLEAN, false)
            preferences.writeBoolean(Main.SHOW_SETTINGS_ON_NOTIFICATION_BOOLEAN, true)
        }
    }
}
