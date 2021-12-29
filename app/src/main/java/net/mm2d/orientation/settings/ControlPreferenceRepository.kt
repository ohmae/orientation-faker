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

class ControlPreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.CONTROL,
        migrations = listOf(MigrationFromOldPreference(OldPreference(context)))
    )
    private val dataStore = context.dataStoreField

    val flow: Flow<ControlPreference> = dataStore.data
        .map {
            ControlPreference(
                shouldNotifySecret = it[NOTIFY_SECRET] ?: false,
                shouldUseBlankIcon = it[USE_BLANK_ICON] ?: false,
                shouldControlByForegroundApp = it[CONTROL_BY_FOREGROUND_APP] ?: true,
            )
        }

    private class MigrationFromOldPreference(
        private val old: OldPreference
    ) : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[DATA_VERSION] != VERSION

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit {
                old.deleteIfTooOld()
                it[DATA_VERSION] = VERSION
                Migrator(old, it).apply {
                    boolean(Key.Main.NOTIFY_SECRET_BOOLEAN, NOTIFY_SECRET)
                    boolean(Key.Main.USE_BLANK_ICON_FOR_NOTIFICATION_BOOLEAN, USE_BLANK_ICON)
                    boolean(Key.Main.FOREGROUND_PACKAGE_ENABLED_BOOLEAN, CONTROL_BY_FOREGROUND_APP)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Key.Main.ORIENTATION_INT,
                Key.Main.NOTIFY_SECRET_BOOLEAN,
                Key.Main.USE_BLANK_ICON_FOR_NOTIFICATION_BOOLEAN,
                Key.Main.FOREGROUND_PACKAGE_STRING,
                Key.Main.FOREGROUND_PACKAGE_CHECK_TIME_LONG,
                Key.Main.FOREGROUND_PACKAGE_ENABLED_BOOLEAN,
            )
        }
    }

    companion object {
        private const val VERSION = 1
        private val DATA_VERSION =
            Key.Control.DATA_VERSION_INT.intKey()
        private val NOTIFY_SECRET =
            Key.Control.NOTIFY_SECRET_BOOLEAN.booleanKey()
        private val USE_BLANK_ICON =
            Key.Control.USE_BLANK_ICON_BOOLEAN.booleanKey()
        private val CONTROL_BY_FOREGROUND_APP =
            Key.Control.CONTROL_BY_FOREGROUND_APP_BOOLEAN.booleanKey()
    }
}
