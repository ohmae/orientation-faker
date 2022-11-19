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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ReviewPreferenceRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val Context.dataStoreField: DataStore<Preferences> by preferences(
        file = DataStoreFile.REVIEW,
        migrations = listOf(WriteFirstValue())
    )
    private val dataStore: DataStore<Preferences> = context.dataStoreField

    val flow: Flow<ReviewPreference> = dataStore.data
        .onErrorResumeEmpty()
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

    suspend fun updateFirstUseTimeIfZero(time: Long) {
        dataStore.edit {
            if ((it[FIRST_USE_TIME] ?: 0L) == 0L) {
                it[FIRST_USE_TIME] = time
            }
        }
    }

    suspend fun updateFirstReviewTime(time: Long) {
        dataStore.edit {
            it[FIRST_REVIEW_TIME] = time
        }
    }

    suspend fun inclementOrientationChangeCount() {
        dataStore.edit {
            val count = it[ORIENTATION_CHANGE_COUNT] ?: 0
            it[ORIENTATION_CHANGE_COUNT] = count + 1
        }
    }

    suspend fun inclementCancelCount() {
        dataStore.edit {
            val count = it[CANCEL_COUNT] ?: 0
            it[CANCEL_COUNT] = count + 1
        }
    }

    suspend fun updateReviewed(reviewed: Boolean) {
        dataStore.edit {
            it[REVIEWED] = reviewed
        }
    }

    suspend fun updateReported(reported: Boolean) {
        dataStore.edit {
            it[REPORTED] = reported
        }
    }

    private class WriteFirstValue : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            currentData[DATA_VERSION] != VERSION

        override suspend fun migrate(currentData: Preferences): Preferences =
            currentData.edit {
                it[DATA_VERSION] = VERSION
                it[INTERVAL_RANDOM_FACTOR] = Random.nextLong(INTERVAL_RANDOM_RANGE)
            }

        override suspend fun cleanUp() = Unit
    }

    companion object {
        // 1 : 2022/01/02 : 5.1.0-
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
