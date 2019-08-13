/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.review

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.mm2d.log.Logger
import net.mm2d.orientation.settings.Settings
import java.util.concurrent.TimeUnit

class ReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReviewDialog.showDialog(this)
    }

    companion object {
        private const val ORIENTATION_CHANGE_COUNT = 10
        private val INTERVAL_FIRST_REVIEW = TimeUnit.DAYS.toMillis(20)
        private val INTERVAL_SECOND_REVIEW = TimeUnit.DAYS.toMillis(40)

        fun startIfNeed(context: Context) {
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
            start(context)
        }

        private fun start(context: Context) {
            try {
                val intent = Intent(context, ReviewActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    it.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Logger.w(e)
            }
        }
    }
}
