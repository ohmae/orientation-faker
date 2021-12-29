package net.mm2d.orientation.settings

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.mm2d.orientation.control.toOrientation

class OrientationPreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.ORIENTATION,
        migrations = listOf(MigrationFromOldPreference(OldPreference(context)))
    )
    private val dataStore = context.dataStoreField

    val flow: Flow<OrientationPreference> = dataStore.data
        .map {
            OrientationPreference(
                enabled = it[ENABLED] ?: false,
                orientation = it[ORIENTATION].toOrientation(),
                isLandscapeDevice = it[LANDSCAPE_DEVICE] ?: false,
            )
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
                    boolean(Key.Main.RESIDENT_BOOLEAN, ENABLED)
                    int(Key.Main.ORIENTATION_INT, ORIENTATION)
                    boolean(Key.Main.LANDSCAPE_DEVICE_BOOLEAN, LANDSCAPE_DEVICE)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Key.Main.RESIDENT_BOOLEAN,
                Key.Main.ORIENTATION_INT,
                Key.Main.LANDSCAPE_DEVICE_BOOLEAN,
            )
        }
    }

    companion object {
        private const val VERSION = 1
        private val DATA_VERSION =
            Key.Orientation.DATA_VERSION_INT.intKey()
        private val ENABLED =
            Key.Orientation.ENABLED_BOOLEAN.booleanKey()
        private val ORIENTATION =
            Key.Orientation.ORIENTATION_INT.intKey()
        private val LANDSCAPE_DEVICE =
            Key.Orientation.LANDSCAPE_DEVICE_BOOLEAN.booleanKey()
    }
}
