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
class ControlPreferenceRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.CONTROL,
        migrations = listOf(WriteFirstValue())
    )
    private val dataStore: DataStore<Preferences> = context.dataStoreField

    val flow: Flow<ControlPreference> = dataStore.data
        .onErrorResumeEmpty()
        .map {
            ControlPreference(
                shouldNotifySecret = it[NOTIFY_SECRET] ?: false,
                shouldUseBlankIcon = it[USE_BLANK_ICON] ?: false,
            )
        }

    suspend fun updateNotifySecret(secret: Boolean) {
        dataStore.edit {
            it[NOTIFY_SECRET] = secret
        }
    }

    suspend fun updateUseBlankIcon(use: Boolean) {
        dataStore.edit {
            it[USE_BLANK_ICON] = use
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
            Key.Control.DATA_VERSION_INT.intKey()
        private val NOTIFY_SECRET =
            Key.Control.NOTIFY_SECRET_BOOLEAN.booleanKey()
        private val USE_BLANK_ICON =
            Key.Control.USE_BLANK_ICON_BOOLEAN.booleanKey()
    }
}
