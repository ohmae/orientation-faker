/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.review

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.take
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.service.MainService
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.view.dialog.ReviewDialog
import java.util.concurrent.TimeUnit

object ReviewRequest {
    private const val ORIENTATION_CHANGE_COUNT = 10
    private val INTERVAL_FIRST_REVIEW = TimeUnit.DAYS.toMillis(21)
    private val INTERVAL_SECOND_REVIEW = TimeUnit.DAYS.toMillis(42)

    suspend fun updateOrientation(orientation: Orientation) {
        val reviewPreferenceRepository = PreferenceRepository.get().reviewPreferenceRepository
        reviewPreferenceRepository.updateFirstUseTimeIfZero(System.currentTimeMillis())
        if (orientation != Orientation.UNSPECIFIED) {
            reviewPreferenceRepository.inclementOrientationChangeCount()
        }
    }

    fun requestReviewIfNeed(fragment: Fragment) {
        if (!MainService.isStarted) {
            return
        }
        if (fragment.lifecycle.currentState != Lifecycle.State.RESUMED) {
            return
        }
        fragment.lifecycleScope.launchWhenResumed {
            val reviewPreferenceRepository = PreferenceRepository.get().reviewPreferenceRepository
            reviewPreferenceRepository.flow
                .take(1)
                .collect {
                    if (it.reported || it.reviewed) {
                        return@collect
                    }
                    if (it.cancelCount >= 0) {
                        return@collect
                    }
                    if (it.orientationChangeCount < ORIENTATION_CHANGE_COUNT) {
                        return@collect
                    }
                    val now = System.currentTimeMillis()
                    if (it.cancelCount == 0 &&
                        now - it.firstUseTime < INTERVAL_FIRST_REVIEW + it.intervalRandomFactor
                    ) {
                        return@collect
                    }
                    if (it.cancelCount == 1 &&
                        now - it.firstReviewTime < INTERVAL_SECOND_REVIEW
                    ) {
                        return@collect
                    }
                    if (ReviewDialog.show(fragment) && it.cancelCount == 0) {
                        reviewPreferenceRepository.updateFirstReviewTime(now)
                    }
                }
        }
    }
}
