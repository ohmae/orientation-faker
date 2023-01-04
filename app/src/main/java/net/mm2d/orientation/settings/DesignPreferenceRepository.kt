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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.mm2d.orientation.control.FunctionButton
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DesignPreferenceRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val defaults: Default,
) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.DESIGN,
        migrations = listOf(WriteFirstValue())
    )
    private val dataStore: DataStore<Preferences> = context.dataStoreField

    val flow: Flow<DesignPreference> = dataStore.data
        .onErrorResumeEmpty()
        .map {
            val overS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            DesignPreference(
                foreground = it[FOREGROUND] ?: defaults.color.foreground,
                background = it[BACKGROUND] ?: defaults.color.background,
                foregroundSelected = it[FOREGROUND_SELECTED] ?: defaults.color.foregroundSelected,
                backgroundSelected = it[BACKGROUND_SELECTED] ?: defaults.color.backgroundSelected,
                base = it[BASE] ?: defaults.color.base,
                iconize = it[ICONIZE] ?: overS,
                shape = IconShape.of(it[SHAPE]),
                shouldShowSettings = it[SHOW_SETTINGS] ?: !overS,
                functions = FunctionButton
                    .run { it[FUNCTION_BUTTONS].toFunctionButtons() }
                    .ifEmpty { Default.functions },
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
                it[BASE] = it[BACKGROUND] ?: defaults.color.background
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

    suspend fun updateFunctions(functions: List<FunctionButton>) {
        dataStore.edit {
            it[FUNCTION_BUTTONS] = FunctionButton.run { functions.toSerializedString() }
        }
    }

    private class WriteFirstValue : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[DATA_VERSION] != VERSION

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit {
                if (it[DATA_VERSION] == 1) {
                    it[FUNCTION_BUTTONS] = FunctionButton.run {
                        @Suppress("DEPRECATION")
                        it[ORIENTATION_LIST].migrateFromOrientations()
                    }
                }
                it[DATA_VERSION] = VERSION
            }

        override suspend fun cleanUp() = Unit
    }

    companion object {
        // 1 : 2022/01/02 : 5.1.0-
        // 2 : 2023/01/0X : 6.0.0-
        private const val VERSION = 2
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
        private val FUNCTION_BUTTONS =
            Key.Design.FUNCTION_BUTTONS_STRING.stringKey()

        @Deprecated("removed: 6.0.0")
        private val ORIENTATION_LIST =
            @Suppress("DEPRECATION")
            Key.Design.ORIENTATION_LIST_STRING.stringKey()
    }
}
