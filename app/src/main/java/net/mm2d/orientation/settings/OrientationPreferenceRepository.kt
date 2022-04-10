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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.toOrientation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrientationPreferenceRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.ORIENTATION,
        migrations = listOf(MigrationFromOldPreference(context))
    )
    private val dataStore: DataStore<Preferences> = context.dataStoreField

    val flow: Flow<OrientationPreference> = dataStore.data
        .onErrorResumeEmpty()
        .map {
            OrientationPreference(
                enabled = it[ENABLED] ?: false,
                orientation = it[ORIENTATION]
                    ?.toOrientation() ?: Orientation.UNSPECIFIED,
                isLandscapeDevice = it[LANDSCAPE_DEVICE] ?: false,
                shouldControlByForegroundApp = it[CONTROL_BY_FOREGROUND_APP] ?: true,
                orientationWhenPowerIsConnected = it[ORIENTATION_WHEN_POWER_IS_CONNECTED]
                    ?.toOrientation() ?: Orientation.INVALID,
            )
        }
    val manuallyOrientationFlow: MutableStateFlow<OrientationRequest> =
        MutableStateFlow(OrientationRequest(Orientation.INVALID))

    suspend fun updateEnabled(enabled: Boolean) {
        dataStore.edit {
            it[ENABLED] = enabled
            if (enabled) {
                val orientation = it[ORIENTATION]
                    ?.toOrientation() ?: Orientation.UNSPECIFIED
                manuallyOrientationFlow.value = OrientationRequest(orientation)
            }
        }
    }

    suspend fun updateOrientation(orientation: Orientation) {
        dataStore.edit {
            it[ORIENTATION] = orientation.value
        }
    }

    suspend fun updateOrientationManually(orientation: Orientation) {
        manuallyOrientationFlow.value = OrientationRequest(orientation)
        dataStore.edit {
            it[ENABLED] = true
            it[ORIENTATION] = orientation.value
        }
    }

    suspend fun updateLandscapeDevice(landscape: Boolean) {
        dataStore.edit {
            it[LANDSCAPE_DEVICE] = landscape
        }
    }

    suspend fun updateControlByForegroundApp(enable: Boolean) {
        dataStore.edit {
            it[CONTROL_BY_FOREGROUND_APP] = enable
        }
    }

    suspend fun updateOrientationWhenPowerIsConnected(orientation: Orientation) {
        dataStore.edit {
            it[ORIENTATION_WHEN_POWER_IS_CONNECTED] = orientation.value
        }
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
                    boolean(Key.Main.RESIDENT_BOOLEAN, ENABLED)
                    int(Key.Main.ORIENTATION_INT, ORIENTATION)
                    boolean(Key.Main.LANDSCAPE_DEVICE_BOOLEAN, LANDSCAPE_DEVICE)
                    boolean(Key.Main.FOREGROUND_PACKAGE_ENABLED_BOOLEAN, CONTROL_BY_FOREGROUND_APP)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Key.Main.RESIDENT_BOOLEAN,
                Key.Main.ORIENTATION_INT,
                Key.Main.LANDSCAPE_DEVICE_BOOLEAN,
                Key.Main.FOREGROUND_PACKAGE_STRING,
                Key.Main.FOREGROUND_PACKAGE_CHECK_TIME_LONG,
                Key.Main.FOREGROUND_PACKAGE_ENABLED_BOOLEAN,
            )
        }
    }

    companion object {
        // 1 : 2022/01/02 : 5.1.0-
        private const val VERSION = 1
        private val DATA_VERSION =
            Key.Orientation.DATA_VERSION_INT.intKey()
        private val ENABLED =
            Key.Orientation.ENABLED_BOOLEAN.booleanKey()
        private val ORIENTATION =
            Key.Orientation.ORIENTATION_INT.intKey()
        private val LANDSCAPE_DEVICE =
            Key.Orientation.LANDSCAPE_DEVICE_BOOLEAN.booleanKey()
        private val CONTROL_BY_FOREGROUND_APP =
            Key.Orientation.CONTROL_BY_FOREGROUND_APP_BOOLEAN.booleanKey()
        private val ORIENTATION_WHEN_POWER_IS_CONNECTED =
            Key.Orientation.ORIENTATION_WHEN_POWER_IS_CONNECTED_INT.intKey()
    }
}
