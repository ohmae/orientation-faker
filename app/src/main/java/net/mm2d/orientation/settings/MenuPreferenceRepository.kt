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
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuPreferenceRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.MENU,
        migrations = listOf(WriteFirstValue())
    )
    private val dataStore: DataStore<Preferences> = context.dataStoreField

    val flow: Flow<MenuPreference> = dataStore.data
        .onErrorResumeEmpty()
        .map {
            MenuPreference(
                warnSystemRotate = it[WARN_SYSTEM_ROTATE] ?: true,
                nightMode = it[NIGHT_MODE] ?: Default.nightMode,
                shouldShowAllApp = it[SHOW_ALL_APPS] ?: false,
                notificationPermissionRequested = it[NOTIFICATION_PERMISSION_REQUESTED] ?: false
            )
        }

    suspend fun updateWarnSystemRotate(warn: Boolean) {
        dataStore.edit {
            it[WARN_SYSTEM_ROTATE] = warn
        }
    }

    suspend fun updateNightMode(nightMode: Int) {
        dataStore.edit {
            it[NIGHT_MODE] = nightMode
        }
    }

    suspend fun updateShowAllApps(show: Boolean) {
        dataStore.edit {
            it[SHOW_ALL_APPS] = show
        }
    }

    suspend fun updateNotificationPermissionRequested(requested: Boolean) {
        dataStore.edit {
            it[NOTIFICATION_PERMISSION_REQUESTED] = requested
        }
    }

    private class WriteFirstValue : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[DATA_VERSION] != VERSION

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit {
                it[DATA_VERSION] = VERSION
            }

        override suspend fun cleanUp() = Unit
    }

    companion object {
        // 1 : 2022/01/02 : 5.1.0-
        private const val VERSION = 1
        private val DATA_VERSION =
            Key.Menu.DATA_VERSION_INT.intKey()
        private val WARN_SYSTEM_ROTATE =
            Key.Menu.AUTO_ROTATE_WARNING_BOOLEAN.booleanKey()
        private val NIGHT_MODE =
            Key.Menu.NIGHT_MODE_INT.intKey()
        private val SHOW_ALL_APPS =
            Key.Menu.SHOW_ALL_APPS_BOOLEAN.booleanKey()
        private val NOTIFICATION_PERMISSION_REQUESTED =
            Key.Menu.NOTIFICATION_PERMISSION_REQUESTED_BOOLEAN.booleanKey()
    }
}
