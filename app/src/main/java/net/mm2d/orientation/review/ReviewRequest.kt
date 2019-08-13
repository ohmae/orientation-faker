/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.review

import androidx.fragment.app.FragmentActivity
import net.mm2d.orientation.control.OrientationHelper
import net.mm2d.orientation.settings.Settings
import java.util.concurrent.TimeUnit

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ReviewRequest {
    private const val ORIENTATION_CHANGE_COUNT = 10
    private val INTERVAL_FIRST_REVIEW = TimeUnit.DAYS.toMillis(20)
    private val INTERVAL_SECOND_REVIEW = TimeUnit.DAYS.toMillis(40)

    fun requestReviewIfNeed(activity: FragmentActivity) {
        val settings = Settings.get()
        if (settings.reported || settings.reviewed) {
            return
        }
        if (settings.orientationChangeCount < ORIENTATION_CHANGE_COUNT) {
            return
        }
        if (settings.reviewCancelCount >= 2) {
            return
        }
        if (settings.reviewCancelCount == 0 &&
            System.currentTimeMillis() - settings.firstUseTime < INTERVAL_FIRST_REVIEW
        ) {
            return
        }
        if (settings.reviewCancelCount == 1 &&
            System.currentTimeMillis() - settings.firstReviewTime < INTERVAL_SECOND_REVIEW
        ) {
            return
        }
        if (settings.reviewCancelCount == 0) {
            settings.firstReviewTime = System.currentTimeMillis()
        }
        if (OrientationHelper.getInstance(activity).isEnabled) {
            ReviewDialog.showDialog(activity)
        }
    }
}
