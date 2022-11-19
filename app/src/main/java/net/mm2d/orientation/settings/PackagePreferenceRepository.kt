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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.mm2d.android.orientationfaker.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackagePreferenceRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.PACKAGE,
        migrations = listOf(
            WriteFirstValue(),
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

    private class WriteFirstValue : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[DATA_VERSION] != VERSION

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit {
                it[DATA_VERSION] = VERSION
                it[VERSION_AT_INSTALL] = BuildConfig.VERSION_CODE
            }

        override suspend fun cleanUp() = Unit
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
