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
                intervalRandomFactor = it[INTERVAL_RANDOM_FACTOR] ?: 0L,
                firstUseTime = it[FIRST_USE_TIME] ?: 0L,
                firstReviewTime = it[FIRST_REVIEW_TIME] ?: 0L,
                orientationChangeCount = it[ORIENTATION_CHANGE_COUNT] ?: 0,
                cancelCount = it[CANCEL_COUNT] ?: 0,
                reviewed = it[REVIEWED] ?: false,
                reported = it[REPORTED] ?: false,
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
                    long(
                        Key.Main.REVIEW_INTERVAL_RANDOM_FACTOR_LONG,
                        INTERVAL_RANDOM_FACTOR,
                        Random.nextLong(INTERVAL_RANDOM_RANGE)
                    )
                    long(Key.Main.TIME_FIRST_USE_LONG, FIRST_USE_TIME)
                    long(Key.Main.TIME_FIRST_REVIEW_LONG, FIRST_REVIEW_TIME)
                    int(Key.Main.COUNT_ORIENTATION_CHANGED_INT, ORIENTATION_CHANGE_COUNT)
                    int(Key.Main.COUNT_REVIEW_DIALOG_CANCELED_INT, CANCEL_COUNT)
                    boolean(Key.Main.REVIEW_REPORTED_BOOLEAN, REPORTED)
                    boolean(Key.Main.REVIEW_REVIEWED_BOOLEAN, REVIEWED)
                }
            }

        override suspend fun cleanUp() {
            old.remove(
                Key.Main.REVIEW_INTERVAL_RANDOM_FACTOR_LONG,
                Key.Main.TIME_FIRST_USE_LONG,
                Key.Main.TIME_FIRST_REVIEW_LONG,
                Key.Main.COUNT_ORIENTATION_CHANGED_INT,
                Key.Main.COUNT_REVIEW_DIALOG_CANCELED_INT,
                Key.Main.REVIEW_REPORTED_BOOLEAN,
                Key.Main.REVIEW_REVIEWED_BOOLEAN,
            )
        }
    }

    companion object {
        private const val VERSION = 1
        private val DATA_VERSION =
            Key.Review.DATA_VERSION_INT.intKey()
        private val INTERVAL_RANDOM_FACTOR =
            Key.Review.INTERVAL_RANDOM_FACTOR_LONG.longKey()
        private val FIRST_USE_TIME =
            Key.Review.FIRST_USE_TIME_LONG.longKey()
        private val FIRST_REVIEW_TIME =
            Key.Review.FIRST_REVIEW_TIME_LONG.longKey()
        private val ORIENTATION_CHANGE_COUNT =
            Key.Review.ORIENTATION_CHANGE_COUNT_INT.intKey()
        private val CANCEL_COUNT =
            Key.Review.CANCEL_COUNT_INT.intKey()
        private val REPORTED =
            Key.Review.REPORTED_BOOLEAN.booleanKey()
        private val REVIEWED =
            Key.Review.REVIEWED_BOOLEAN.booleanKey()

        private val INTERVAL_RANDOM_RANGE = TimeUnit.DAYS.toMillis(14)
    }
}
