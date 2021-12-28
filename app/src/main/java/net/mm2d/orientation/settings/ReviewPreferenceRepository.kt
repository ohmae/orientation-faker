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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.mm2d.orientation.settings.Key.Main
import net.mm2d.orientation.settings.Key.Review
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ReviewPreferenceRepository(context: Context) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.REVIEW,
        migrations = listOf(MigrationFromOldPreference(OldPreference(context)))
    )
    private val dataStore = context.dataStoreField

    val flow: Flow<ReviewPreference> = dataStore.data
        .map {
            ReviewPreference(
                intervalRandomFactor = it[Key.INTERVAL_RANDOM_FACTOR] ?: 0L,
                firstUseTime = it[Key.FIRST_USE_TIME] ?: 0L,
                firstReviewTime = it[Key.FIRST_REVIEW_TIME] ?: 0L,
                orientationChangeCount = it[Key.ORIENTATION_CHANGE_COUNT] ?: 0,
                cancelCount = it[Key.CANCEL_COUNT] ?: 0,
                reviewed = it[Key.REVIEWED] ?: false,
                reported = it[Key.REPORTED] ?: false,
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
                    long(
                        Main.REVIEW_INTERVAL_RANDOM_FACTOR_LONG,
                        Key.INTERVAL_RANDOM_FACTOR,
                        Random.nextLong(INTERVAL_RANDOM_RANGE)
                    )
                    long(Main.TIME_FIRST_USE_LONG, Key.FIRST_USE_TIME)
                    long(Main.TIME_FIRST_REVIEW_LONG, Key.FIRST_REVIEW_TIME)
                    int(Main.COUNT_ORIENTATION_CHANGED_INT, Key.ORIENTATION_CHANGE_COUNT)
                    int(Main.COUNT_REVIEW_DIALOG_CANCELED_INT, Key.CANCEL_COUNT)
                    boolean(Main.REVIEW_REPORTED_BOOLEAN, Key.REPORTED)
                    boolean(Main.REVIEW_REVIEWED_BOOLEAN, Key.REVIEWED)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Main.REVIEW_INTERVAL_RANDOM_FACTOR_LONG,
                Main.TIME_FIRST_USE_LONG,
                Main.TIME_FIRST_REVIEW_LONG,
                Main.COUNT_ORIENTATION_CHANGED_INT,
                Main.COUNT_REVIEW_DIALOG_CANCELED_INT,
                Main.REVIEW_REPORTED_BOOLEAN,
                Main.REVIEW_REVIEWED_BOOLEAN,
            )
        }
    }

    private object Key {
        val DATA_VERSION =
            Review.DATA_VERSION_INT.intKey()
        val INTERVAL_RANDOM_FACTOR =
            Review.INTERVAL_RANDOM_FACTOR_LONG.longKey()
        val FIRST_USE_TIME =
            Review.FIRST_USE_TIME_LONG.longKey()
        val FIRST_REVIEW_TIME =
            Review.FIRST_REVIEW_TIME_LONG.longKey()
        val ORIENTATION_CHANGE_COUNT =
            Review.ORIENTATION_CHANGE_COUNT_INT.intKey()
        val CANCEL_COUNT =
            Review.CANCEL_COUNT_INT.intKey()
        val REPORTED =
            Review.REPORTED_BOOLEAN.booleanKey()
        val REVIEWED =
            Review.REVIEWED_BOOLEAN.booleanKey()
    }

    companion object {
        private const val VERSION = 1
        private val INTERVAL_RANDOM_RANGE = TimeUnit.DAYS.toMillis(14)
    }
}
