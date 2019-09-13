/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.review

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.LaunchUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
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

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        settings.reviewCancelCount++

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    companion object {
        private const val TAG = "ReviewDialog"

        fun showDialog(activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            if (fragmentManager.findFragmentByTag(TAG) != null) {
                return
            }
            ReviewDialog().show(fragmentManager, TAG)
        }
    }
}
