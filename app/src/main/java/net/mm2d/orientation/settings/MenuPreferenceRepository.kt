package net.mm2d.orientation.settings

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MenuPreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.MENU,
        migrations = listOf(MigrationFromOldPreference(OldPreference(context)))
    )
    private val dataStore = context.dataStoreField

    val flow: Flow<MenuPreference> = dataStore.data
        .map {
            MenuPreference(
                warnSystemRotate = it[WARN_SYSTEM_ROTATE] ?: true,
                nightMode = it[NIGHT_MODE] ?: Default.nightMode,
                shouldShowAllApp = it[SHOW_ALL_APPS] ?: false,
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
                    boolean(Key.Main.AUTO_ROTATE_WARNING_BOOLEAN, WARN_SYSTEM_ROTATE)
                    int(Key.Main.NIGHT_MODE_INT, NIGHT_MODE)
                    boolean(Key.Main.SHOW_ALL_APPS_BOOLEAN, SHOW_ALL_APPS)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Key.Main.AUTO_ROTATE_WARNING_BOOLEAN,
                Key.Main.NIGHT_MODE_INT,
                Key.Main.SHOW_ALL_APPS_BOOLEAN,
            )
        }
    }

    companion object {
        // 1 : 2022/01/XX : 5.1.0-
        private const val VERSION = 1
        private val DATA_VERSION =
            Key.Menu.DATA_VERSION_INT.intKey()
        private val WARN_SYSTEM_ROTATE =
            Key.Menu.AUTO_ROTATE_WARNING_BOOLEAN.booleanKey()
        private val NIGHT_MODE =
            Key.Menu.NIGHT_MODE_INT.intKey()
        private val SHOW_ALL_APPS =
            Key.Menu.SHOW_ALL_APPS_BOOLEAN.booleanKey()
    }
}
