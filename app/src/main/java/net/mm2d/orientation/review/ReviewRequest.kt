/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.review

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import net.mm2d.orientation.control.OrientationHelper
import java.util.concurrent.TimeUnit

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ReviewRequest {
    private const val REVIEW_DELAY = 1000L
    private val UPTIME_MINIMUM = TimeUnit.MINUTES.toMillis(2)
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    fun requestReviewIfNeed(context: Context) {
        if (SystemClock.uptimeMillis() < UPTIME_MINIMUM) {
            return
        }
        handler.postDelayed({
            if (OrientationHelper.getInstance(context).isEnabled) {
                ReviewActivity.startIfNeed(context)
            }
        }, REVIEW_DELAY)
    }
}
