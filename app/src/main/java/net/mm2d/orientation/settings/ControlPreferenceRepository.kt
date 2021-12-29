/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.mm2d.orientation.control.toOrientation
import net.mm2d.orientation.settings.Key.Control
import net.mm2d.orientation.settings.Key.Main

class ControlPreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.CONTROL,
        migrations = listOf(MigrationFromOldPreference(OldPreference(context)))
    )
    private val dataStore = context.dataStoreField

    val flow: Flow<ControlPreference> = dataStore.data
        .map {
            ControlPreference(
                orientation = it[Key.ORIENTATION].toOrientation(),
                shouldAutoStart = it[Key.AUTO_START] ?: false,
                shouldNotifySecret = it[Key.NOTIFY_SECRET] ?: false,
                shouldControlByForegroundApp = it[Key.CONTROL_BY_FOREGROUND_APP] ?: true,
                isLandscapeDevice = it[Key.LANDSCAPE_DEVICE] ?: false,
                shouldUseBlankIcon = it[Key.USE_BLANK_ICON] ?: false,
            )
        }

    private class MigrationFromOldPreference(
        private val old: OldPreference
    ) : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[Key.DATA_VERSION] != VERSION

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit {
                old.deleteIfTooOld()
                it[Key.DATA_VERSION] = VERSION
                Migrator(old, it).apply {
                    int(Main.ORIENTATION_INT, Key.ORIENTATION)
                    boolean(Main.RESIDENT_BOOLEAN, Key.AUTO_START)
                    boolean(Main.NOTIFY_SECRET_BOOLEAN, Key.NOTIFY_SECRET)
                    boolean(Main.FOREGROUND_PACKAGE_ENABLED_BOOLEAN, Key.CONTROL_BY_FOREGROUND_APP)
                    boolean(Main.LANDSCAPE_DEVICE_BOOLEAN, Key.LANDSCAPE_DEVICE)
                    boolean(Main.USE_BLANK_ICON_FOR_NOTIFICATION_BOOLEAN, Key.USE_BLANK_ICON)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Main.ORIENTATION_INT,
                Main.RESIDENT_BOOLEAN,
                Main.NOTIFY_SECRET_BOOLEAN,
                Main.FOREGROUND_PACKAGE_STRING,
                Main.FOREGROUND_PACKAGE_CHECK_TIME_LONG,
                Main.FOREGROUND_PACKAGE_ENABLED_BOOLEAN,
                Main.LANDSCAPE_DEVICE_BOOLEAN,
                Main.USE_BLANK_ICON_FOR_NOTIFICATION_BOOLEAN,
            )
        }
    }

    private object Key {
        val DATA_VERSION =
            Control.DATA_VERSION_INT.intKey()
        val ORIENTATION =
            Control.ORIENTATION_INT.intKey()
        val AUTO_START =
            Control.AUTO_START_BOOLEAN.booleanKey()
        val NOTIFY_SECRET =
            Control.NOTIFY_SECRET_BOOLEAN.booleanKey()
        val CONTROL_BY_FOREGROUND_APP =
            Control.CONTROL_BY_FOREGROUND_APP_BOOLEAN.booleanKey()
        val LANDSCAPE_DEVICE =
            Control.LANDSCAPE_DEVICE_BOOLEAN.booleanKey()
        val USE_BLANK_ICON =
            Control.USE_BLANK_ICON_BOOLEAN.booleanKey()
    }

    companion object {
        private const val VERSION = 1
    }
}
