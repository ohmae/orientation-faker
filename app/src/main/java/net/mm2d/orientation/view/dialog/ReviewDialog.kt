/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.LayoutReviewBinding
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.Launcher

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ReviewDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val parent = activity.window.decorView as ViewGroup
        val view = LayoutReviewBinding.inflate(activity.layoutInflater, parent, false).root
        val settings = Settings.get()
        return AlertDialog.Builder(activity)
            .setIcon(R.drawable.ic_launcher)
            .setTitle(R.string.app_name)
            .setView(view)
            .setPositiveButton(R.string.dialog_button_review) { _, _ ->
                Launcher.openGooglePlay(activity)
                settings.reviewed = true
            }
            .setNeutralButton(R.string.dialog_button_send_mail) { _, _ ->
                Launcher.sendMailReport(activity)
                settings.reported = true
            }
            .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Settings.get().reviewCancelCount++
    }

    companion object {
        private const val TAG = "ReviewDialog"

        fun showDialog(activity: FragmentActivity) {
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            ReviewDialog().show(manager, TAG)
        }
    }
}
