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
import net.mm2d.orientation.settings.Key.Main
import net.mm2d.orientation.settings.Key.Package

class PackagePreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.PACKAGE,
        migrations = listOf(
            MigrationFromOldPreference(OldPreference(context)),
            MigrationForUpdate(),
        )
    )
    private val dataStore = context.dataStoreField

    val flow: Flow<PackagePreference> = dataStore.data
        .map {
            PackagePreference(
                versionAtInstall = it[Key.VERSION_AT_INSTALL] ?: 0,
                versionAtLastLaunched = it[Key.VERSION_AT_LAST_LAUNCHED] ?: 0,
                versionBeforeUpdate = it[Key.VERSION_BEFORE_UPDATE] ?: 0
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
                    int(Main.APP_VERSION_AT_INSTALL_INT, Key.VERSION_AT_INSTALL, BuildConfig.VERSION_CODE)
                    int(Main.APP_VERSION_AT_LAST_LAUNCHED_INT, Key.VERSION_AT_LAST_LAUNCHED)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Main.APP_VERSION_AT_INSTALL_INT,
                Main.APP_VERSION_AT_LAST_LAUNCHED_INT,
            )
            old.deleteDefaultSharedPreferences()
        }
    }

    private class MigrationForUpdate : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[Key.VERSION_AT_LAST_LAUNCHED] != BuildConfig.VERSION_CODE

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit { preferences ->
                preferences[Key.VERSION_AT_LAST_LAUNCHED]?.let {
                    preferences[Key.VERSION_BEFORE_UPDATE] = it
                }
                preferences[Key.VERSION_AT_LAST_LAUNCHED] = BuildConfig.VERSION_CODE
            }

        override suspend fun cleanUp() = Unit
    }

    private object Key {
        val DATA_VERSION =
            Package.DATA_VERSION_INT.intKey()
        val VERSION_AT_INSTALL =
            Package.VERSION_AT_INSTALL_INT.intKey()
        val VERSION_AT_LAST_LAUNCHED =
            Package.VERSION_AT_LAST_LAUNCHED_INT.intKey()
        val VERSION_BEFORE_UPDATE =
            Package.VERSION_BEFORE_UPDATE_INT.intKey()
    }

    companion object {
        private const val VERSION = 1
    }
}
