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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.mm2d.orientation.settings.Key.Design
import net.mm2d.orientation.settings.Key.Main

class DesignPreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.DESIGN,
        migrations = listOf(MigrationFromOldPreference(OldPreference(context)))
    )
    private val dataStore = context.dataStoreField

    val flow: Flow<DesignPreference> = dataStore.data
        .map {
            DesignPreference(
                foreground = it[Key.FOREGROUND] ?: Default.color.foreground,
                background = it[Key.BACKGROUND] ?: Default.color.background,
                foregroundSelected = it[Key.FOREGROUND_SELECTED] ?: Default.color.foregroundSelected,
                backgroundSelected = it[Key.BACKGROUND_SELECTED] ?: Default.color.backgroundSelected,
                base = it[Key.BASE] ?: getDefaultBaseColor(),
                iconize = it[Key.ICONIZE] ?: false,
                shape = IconShape.of(it[Key.SHAPE]),
                shouldShowSettings = it[Key.SHOW_SETTINGS] ?: true,
                orientations = OrientationList.toList(it[Key.ORIENTATION_LIST]).let {
                    if (it.isEmpty()) Default.orientationList else it
                },
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
                    int(Main.COLOR_FOREGROUND_INT, Key.FOREGROUND)
                    int(Main.COLOR_BACKGROUND_INT, Key.BACKGROUND)
                    int(Main.COLOR_FOREGROUND_SELECTED_INT, Key.FOREGROUND_SELECTED)
                    int(Main.COLOR_BACKGROUND_SELECTED_INT, Key.BACKGROUND_SELECTED)
                    int(Main.COLOR_BASE_INT, Key.BASE)
                    boolean(Main.USE_ROUND_BACKGROUND_BOOLEAN, Key.ICONIZE)
                    string(Main.ICON_SHAPE_STRING, Key.SHAPE)

                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Main.COLOR_FOREGROUND_INT,
                Main.COLOR_BACKGROUND_INT,
                Main.COLOR_FOREGROUND_SELECTED_INT,
                Main.COLOR_BACKGROUND_SELECTED_INT,
                Main.COLOR_BASE_INT,
                Main.USE_ROUND_BACKGROUND_BOOLEAN,
                Main.ICON_SHAPE_STRING,
                Main.SHOW_SETTINGS_ON_NOTIFICATION_BOOLEAN,
                Main.ORIENTATION_LIST_STRING,
            )
        }
    }

    private object Key {
        val DATA_VERSION =
            Design.DATA_VERSION_INT.intKey()
        val FOREGROUND =
            Design.FOREGROUND_INT.intKey()
        val BACKGROUND =
            Design.BACKGROUND_INT.intKey()
        val FOREGROUND_SELECTED =
            Design.FOREGROUND_SELECTED_INT.intKey()
        val BACKGROUND_SELECTED =
            Design.BACKGROUND_SELECTED_INT.intKey()
        val BASE =
            Design.BASE_INT.intKey()
        val ICONIZE =
            Design.ICONIZE_BOOLEAN.booleanKey()
        val SHAPE =
            Design.SHAPE_STRING.stringKey()
        val SHOW_SETTINGS =
            Design.SHOW_SETTINGS_BOOLEAN.booleanKey()
        val ORIENTATION_LIST =
            Design.ORIENTATION_LIST_STRING.stringKey()
    }

    companion object {
        private const val VERSION = 1

        private fun getDefaultBaseColor(): Int? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Default.color.base else null
    }
}
