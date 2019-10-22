/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.review

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.Settings
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ReviewRequest {
    private const val ORIENTATION_CHANGE_COUNT = 5
    private val INTERVAL_FIRST_REVIEW = TimeUnit.DAYS.toMillis(21)
    private val INTERVAL_SECOND_REVIEW = TimeUnit.DAYS.toMillis(42)
    private val INTERVAL_RANDOM_RANGE = TimeUnit.DAYS.toMillis(14)

    fun initializeIfNeed() {
        val settings = Settings.get()
        if (settings.firstUseTime == 0L) {
            settings.firstUseTime = System.currentTimeMillis()
            settings.reviewIntervalRandomFactor = Random.nextLong(INTERVAL_RANDOM_RANGE)
        }
    }

    fun requestReviewIfNeed(activity: FragmentActivity) {
        if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            return
        }
        val settings = Settings.get()
        if (settings.reported || settings.reviewed) {
            return
        }
        if (settings.reviewCancelCount >= 2) {
            return
        }
        if (settings.orientationChangeCount < ORIENTATION_CHANGE_COUNT) {
            return
        }
        val now = System.currentTimeMillis()
        if (settings.reviewCancelCount == 0 &&
            now - settings.firstUseTime < INTERVAL_FIRST_REVIEW + settings.reviewIntervalRandomFactor
        ) {
            return
        }
        if (settings.reviewCancelCount == 1 &&
            now - settings.firstReviewTime < INTERVAL_SECOND_REVIEW
        ) {
            return
        }
        if (settings.reviewCancelCount == 0) {
            settings.firstReviewTime = now
        }
        if (OrientationHelper.isEnabled) {
            ReviewDialog.showDialog(activity)
        }
    }
}
