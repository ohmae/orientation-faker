/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.review

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.orientationfaker.R
import net.mm2d.log.Logger
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.LaunchUtils
import java.util.concurrent.TimeUnit

class ReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showDialog(this)
    }

    class ReviewDialog : DialogFragment() {
        private lateinit var settings: Settings
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            settings = Settings.get()
            val activity = activity ?: throw IllegalStateException()
            val parent = activity.window.decorView as ViewGroup
            val view = activity.layoutInflater
                .inflate(R.layout.layout_review, parent, false)
            return AlertDialog.Builder(activity)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(R.string.app_name)
                .setView(view)
                .setPositiveButton(R.string.dialog_button_review) { _, _ ->
                    LaunchUtils.openGooglePlay(activity)
                    settings.reviewed = true
                }
                .setNeutralButton(R.string.dialog_button_send_mail) { _, _ ->
                    LaunchUtils.sendMailReport(activity)
                    settings.reported = true
                }
                .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                .create()
        }

        override fun onCancel(dialog: DialogInterface?) {
            super.onCancel(dialog)
            settings.reviewCancelCount++

        }

        override fun onDismiss(dialog: DialogInterface?) {
            super.onDismiss(dialog)
            activity?.finish()
        }
    }

    companion object {
        private const val TAG = "ReviewDialog"
        private const val ORIENTATION_CHANGE_COUNT = 10
        private val INTERVAL_FIRST_REVIEW = TimeUnit.DAYS.toMillis(20)
        private val INTERVAL_SECOND_REVIEW = TimeUnit.DAYS.toMillis(40)

        private fun showDialog(activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager ?: return
            if (fragmentManager.findFragmentByTag(TAG) != null) {
                return
            }
            ReviewDialog().show(fragmentManager, TAG)
        }

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
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Logger.w(e)
            }
        }
    }
}
