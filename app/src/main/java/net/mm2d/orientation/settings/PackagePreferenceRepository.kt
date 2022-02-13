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
import net.mm2d.android.orientationfaker.BuildConfig

class PackagePreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.PACKAGE,
        migrations = listOf(
            MigrationFromOldPreference(context),
            MigrationForUpdate(),
        )
    )
    private val dataStore: DataStore<Preferences> = context.dataStoreField

    val flow: Flow<PackagePreference> = dataStore.data
        .onErrorResumeEmpty()
        .map {
            PackagePreference(
                versionAtInstall = it[VERSION_AT_INSTALL] ?: 0,
                versionAtLastLaunched = it[VERSION_AT_LAST_LAUNCHED] ?: 0,
                versionBeforeUpdate = it[VERSION_BEFORE_UPDATE] ?: 0
            )
        }

    private class MigrationFromOldPreference(
        private val context: Context
    ) : DataMigration<Preferences> {
        private val old: OldPreference by lazy { OldPreference(context) }

        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[DATA_VERSION] != VERSION

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit {
                old.deleteIfTooOld()
                it[DATA_VERSION] = VERSION
                Migrator(old, it).apply {
                    int(Key.Main.APP_VERSION_AT_INSTALL_INT, VERSION_AT_INSTALL, BuildConfig.VERSION_CODE)
                    int(Key.Main.APP_VERSION_AT_LAST_LAUNCHED_INT, VERSION_AT_LAST_LAUNCHED)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Key.Main.APP_VERSION_AT_INSTALL_INT,
                Key.Main.APP_VERSION_AT_LAST_LAUNCHED_INT,
            )
            old.deleteOldSharedPreferences()
        }
    }

    private class MigrationForUpdate : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[VERSION_AT_LAST_LAUNCHED] != BuildConfig.VERSION_CODE

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit { preferences ->
                preferences[VERSION_AT_LAST_LAUNCHED]?.let {
                    preferences[VERSION_BEFORE_UPDATE] = it
                }
                preferences[VERSION_AT_LAST_LAUNCHED] = BuildConfig.VERSION_CODE
            }

        override suspend fun cleanUp() = Unit
    }

    companion object {
        // 1 : 2022/01/02 : 5.1.0-
        private const val VERSION = 1
        private val DATA_VERSION =
            Key.Package.DATA_VERSION_INT.intKey()
        private val VERSION_AT_INSTALL =
            Key.Package.VERSION_AT_INSTALL_INT.intKey()
        private val VERSION_AT_LAST_LAUNCHED =
            Key.Package.VERSION_AT_LAST_LAUNCHED_INT.intKey()
        private val VERSION_BEFORE_UPDATE =
            Key.Package.VERSION_BEFORE_UPDATE_INT.intKey()
    }
}
