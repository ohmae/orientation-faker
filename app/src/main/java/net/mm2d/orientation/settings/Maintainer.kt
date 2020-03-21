/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import androidx.preference.PreferenceManager
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.orientation.settings.Key.Main

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object Maintainer {
    // 0 : 1.0.0
    // 1 : 2.0.0 - 2.1.2
    // 2 : 2.2.0 -
    // 3 : 4.0.0 -
    private const val SETTINGS_VERSION = 3

    fun maintain(context: Context, preferences: Preferences<Main>) {
        Main.values().checkSuffix()
        if (preferences.readInt(Main.APP_VERSION_AT_LAST_LAUNCHED_INT, 0) != BuildConfig.VERSION_CODE) {
            preferences.writeInt(Main.APP_VERSION_AT_LAST_LAUNCHED_INT, BuildConfig.VERSION_CODE)
        }
        if (preferences.readInt(Main.PREFERENCES_VERSION_INT, 0) == SETTINGS_VERSION) {
            return
        }
        preferences.writeInt(Main.PREFERENCES_VERSION_INT, SETTINGS_VERSION)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferences.all.isNotEmpty()) {
            if (sharedPreferences.getInt(OldKey.SETTINGS_VERSION.name, 0) == 2) {
                migrateFromVersion2(sharedPreferences, preferences)
                sharedPreferences.edit().clear().apply()
                return
            }
            sharedPreferences.edit().clear().apply()
        }
        if (!preferences.contains(Main.APP_VERSION_AT_INSTALL_INT)) {
            preferences.writeInt(Main.APP_VERSION_AT_INSTALL_INT, BuildConfig.VERSION_CODE)
        }
        writeDefaultValue(preferences)
    }

    private fun writeDefaultValue(preferences: Preferences<Main>) {
        preferences.writeInt(Main.ORIENTATION_INT, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        preferences.writeBoolean(Main.RESIDENT_BOOLEAN, false)
        preferences.writeInt(Main.COLOR_FOREGROUND_INT, Default.color.foreground)
        preferences.writeInt(Main.COLOR_BACKGROUND_INT, Default.color.background)
        preferences.writeInt(Main.COLOR_FOREGROUND_SELECTED_INT, Default.color.foregroundSelected)
        preferences.writeInt(Main.COLOR_BACKGROUND_SELECTED_INT, Default.color.backgroundSelected)
        preferences.writeBoolean(Main.NOTIFY_SECRET_BOOLEAN, false)
        preferences.writeLong(Main.TIME_FIRST_USE_LONG, 0L)
        preferences.writeLong(Main.TIME_FIRST_REVIEW_LONG, 0L)
        preferences.writeInt(Main.COUNT_ORIENTATION_CHANGED_INT, 0)
        preferences.writeInt(Main.COUNT_REVIEW_DIALOG_CANCELED_INT, 0)
        preferences.writeBoolean(Main.REVIEW_REPORTED_BOOLEAN, false)
        preferences.writeBoolean(Main.REVIEW_REVIEWED_BOOLEAN, false)
        preferences.writeBoolean(Main.AUTO_ROTATE_WARNING_BOOLEAN, true)
    }

    private fun migrateFromVersion2(sharedPreferences: SharedPreferences, preferences: Preferences<Main>) {
        Migrator(sharedPreferences, preferences).apply {
            int(OldKey.APP_VERSION_AT_INSTALL, Main.APP_VERSION_AT_INSTALL_INT)
            long(OldKey.REVIEW_INTERVAL_RANDOM_FACTOR, Main.REVIEW_INTERVAL_RANDOM_FACTOR_LONG)
            long(OldKey.TIME_FIRST_USE, Main.TIME_FIRST_USE_LONG)
            long(OldKey.TIME_FIRST_REVIEW, Main.TIME_FIRST_REVIEW_LONG)
            int(OldKey.COUNT_ORIENTATION_CHANGED, Main.COUNT_ORIENTATION_CHANGED_INT)
            int(OldKey.COUNT_REVIEW_DIALOG_CANCELED, Main.COUNT_REVIEW_DIALOG_CANCELED_INT)
            boolean(OldKey.REVIEW_REPORTED, Main.REVIEW_REPORTED_BOOLEAN)
            boolean(OldKey.REVIEW_REVIEWED, Main.REVIEW_REVIEWED_BOOLEAN)
            int(OldKey.ORIENTATION, Main.ORIENTATION_INT)
            boolean(OldKey.RESIDENT, Main.RESIDENT_BOOLEAN)
            int(OldKey.COLOR_FOREGROUND, Main.COLOR_FOREGROUND_INT)
            int(OldKey.COLOR_BACKGROUND, Main.COLOR_BACKGROUND_INT)
            int(OldKey.COLOR_FOREGROUND_SELECTED, Main.COLOR_FOREGROUND_SELECTED_INT)
            int(OldKey.COLOR_BACKGROUND_SELECTED, Main.COLOR_BACKGROUND_SELECTED_INT)
            boolean(OldKey.NOTIFY_SECRET, Main.NOTIFY_SECRET_BOOLEAN)
            boolean(OldKey.USE_FULL_SENSOR, Main.USE_FULL_SENSOR_BOOLEAN)
            boolean(OldKey.AUTO_ROTATE_WARNING, Main.AUTO_ROTATE_WARNING_BOOLEAN)
            boolean(OldKey.USE_BLANK_ICON_FOR_NOTIFICATION, Main.USE_BLANK_ICON_FOR_NOTIFICATION_BOOLEAN)
        }
    }

    private class Migrator(
        private val sharedPreferences: SharedPreferences,
        private val preferences: Preferences<Main>
    ) {
        fun boolean(oldKey: OldKey, key: Main) {
            if (sharedPreferences.contains(oldKey.name)) {
                preferences.writeBoolean(key, sharedPreferences.getBoolean(oldKey.name, false))
            }
        }

        fun int(oldKey: OldKey, key: Main) {
            if (sharedPreferences.contains(oldKey.name)) {
                preferences.writeInt(key, sharedPreferences.getInt(oldKey.name, 0))
            }
        }

        fun long(oldKey: OldKey, key: Main) {
            if (sharedPreferences.contains(oldKey.name)) {
                preferences.writeLong(key, sharedPreferences.getLong(oldKey.name, 0L))
            }
        }
    }
}
