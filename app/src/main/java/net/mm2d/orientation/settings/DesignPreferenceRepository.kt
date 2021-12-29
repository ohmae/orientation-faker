/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.mm2d.orientation.control.Orientation

class DesignPreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.DESIGN,
        migrations = listOf(MigrationFromOldPreference(OldPreference(context)))
    )
    private val dataStore = context.dataStoreField

    val flow: Flow<DesignPreference> = dataStore.data
        .map {
            DesignPreference(
                foreground = it[FOREGROUND] ?: Default.color.foreground,
                background = it[BACKGROUND] ?: Default.color.background,
                foregroundSelected = it[FOREGROUND_SELECTED] ?: Default.color.foregroundSelected,
                backgroundSelected = it[BACKGROUND_SELECTED] ?: Default.color.backgroundSelected,
                base = it[BASE] ?: getDefaultBaseColor(),
                iconize = it[ICONIZE] ?: false,
                shape = IconShape.of(it[SHAPE]),
                shouldShowSettings = it[SHOW_SETTINGS] ?: true,
                orientations = OrientationList.toList(it[ORIENTATION_LIST]).let { list ->
                    if (list.isEmpty()) Default.orientationList else list
                },
            )
        }

    suspend fun updateForeground(color: Int) {
        dataStore.edit {
            it[FOREGROUND] = color
        }
    }

    suspend fun updateBackground(color: Int) {
        dataStore.edit {
            it[BACKGROUND] = color
        }
    }

    suspend fun updateForegroundSelected(color: Int) {
        dataStore.edit {
            it[FOREGROUND_SELECTED] = color
        }
    }

    suspend fun updateBackgroundSelected(color: Int) {
        dataStore.edit {
            it[BACKGROUND_SELECTED] = color
        }
    }

    suspend fun updateBase(color: Int) {
        dataStore.edit {
            it[BASE] = color
        }
    }

    suspend fun resetTheme() {
        dataStore.edit {
            it.remove(FOREGROUND)
            it.remove(BACKGROUND)
            it.remove(FOREGROUND_SELECTED)
            it.remove(BACKGROUND_SELECTED)
            it.remove(BASE)
        }
    }

    suspend fun updateIconize(iconize: Boolean) {
        dataStore.edit {
            it[ICONIZE] = iconize
            if (iconize && it[BASE] == null) {
                it[BASE] = it[BACKGROUND] ?: Default.color.background
            }
        }
    }

    suspend fun updateShape(shape: IconShape) {
        dataStore.edit {
            it[SHAPE] = shape.name
        }
    }

    suspend fun updateShowSettings(show: Boolean) {
        dataStore.edit {
            it[SHOW_SETTINGS] = show
        }
    }

    suspend fun updateOrientations(orientations: List<Orientation>) {
        dataStore.edit {
            it[ORIENTATION_LIST] = OrientationList.toString(orientations)
        }
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
                    int(Key.Main.COLOR_FOREGROUND_INT, FOREGROUND)
                    int(Key.Main.COLOR_BACKGROUND_INT, BACKGROUND)
                    int(Key.Main.COLOR_FOREGROUND_SELECTED_INT, FOREGROUND_SELECTED)
                    int(Key.Main.COLOR_BACKGROUND_SELECTED_INT, BACKGROUND_SELECTED)
                    int(Key.Main.COLOR_BASE_INT, BASE)
                    boolean(Key.Main.USE_ROUND_BACKGROUND_BOOLEAN, ICONIZE)
                    string(Key.Main.ICON_SHAPE_STRING, SHAPE)

                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Key.Main.COLOR_FOREGROUND_INT,
                Key.Main.COLOR_BACKGROUND_INT,
                Key.Main.COLOR_FOREGROUND_SELECTED_INT,
                Key.Main.COLOR_BACKGROUND_SELECTED_INT,
                Key.Main.COLOR_BASE_INT,
                Key.Main.USE_ROUND_BACKGROUND_BOOLEAN,
                Key.Main.ICON_SHAPE_STRING,
                Key.Main.SHOW_SETTINGS_ON_NOTIFICATION_BOOLEAN,
                Key.Main.ORIENTATION_LIST_STRING,
            )
        }
    }

    companion object {
        private const val VERSION = 1
        private val DATA_VERSION =
            Key.Design.DATA_VERSION_INT.intKey()
        private val FOREGROUND =
            Key.Design.FOREGROUND_INT.intKey()
        private val BACKGROUND =
            Key.Design.BACKGROUND_INT.intKey()
        private val FOREGROUND_SELECTED =
            Key.Design.FOREGROUND_SELECTED_INT.intKey()
        private val BACKGROUND_SELECTED =
            Key.Design.BACKGROUND_SELECTED_INT.intKey()
        private val BASE =
            Key.Design.BASE_INT.intKey()
        private val ICONIZE =
            Key.Design.ICONIZE_BOOLEAN.booleanKey()
        private val SHAPE =
            Key.Design.SHAPE_STRING.stringKey()
        private val SHOW_SETTINGS =
            Key.Design.SHOW_SETTINGS_BOOLEAN.booleanKey()
        private val ORIENTATION_LIST =
            Key.Design.ORIENTATION_LIST_STRING.stringKey()

        private fun getDefaultBaseColor(): Int? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Default.color.base else null
    }
}
