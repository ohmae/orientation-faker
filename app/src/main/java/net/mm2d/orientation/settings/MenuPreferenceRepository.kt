package net.mm2d.orientation.settings

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.mm2d.orientation.settings.Key.Main
import net.mm2d.orientation.settings.Key.Menu

class MenuPreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.MENU,
        migrations = listOf(MigrationFromOldPreference(OldPreference(context)))
    )
    private val dataStore = context.dataStoreField

    val flow: Flow<MenuPreference> = dataStore.data
        .map {
            MenuPreference(
                warnSystemRotateSetting = it[Key.WARN_SYSTEM_ROTATE] ?: true,
                nightMode = it[Key.NIGHT_MODE] ?: Default.nightMode,
                shouldShowAllApp = it[Key.SHOW_ALL_APPS] ?: false,
            )
        }

    suspend fun updateNightMode(nightMode: Int) {
        dataStore.edit {
            it[Key.NIGHT_MODE] = nightMode
        }
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
                    boolean(Main.AUTO_ROTATE_WARNING_BOOLEAN, Key.WARN_SYSTEM_ROTATE)
                    int(Main.NIGHT_MODE_INT, Key.NIGHT_MODE)
                    boolean(Main.SHOW_ALL_APPS_BOOLEAN, Key.SHOW_ALL_APPS)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Main.AUTO_ROTATE_WARNING_BOOLEAN,
                Main.NIGHT_MODE_INT,
                Main.SHOW_ALL_APPS_BOOLEAN,
            )
        }
    }

    private object Key {
        val DATA_VERSION =
            Menu.DATA_VERSION_INT.intKey()
        val WARN_SYSTEM_ROTATE =
            Menu.AUTO_ROTATE_WARNING_BOOLEAN.booleanKey()
        val NIGHT_MODE =
            Menu.NIGHT_MODE_INT.intKey()
        val SHOW_ALL_APPS =
            Menu.SHOW_ALL_APPS_BOOLEAN.booleanKey()
    }

    companion object {
        private const val VERSION = 1
    }
}
