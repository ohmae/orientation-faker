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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.LayoutReviewBinding
import net.mm2d.orientation.util.Launcher

@AndroidEntryPoint
class ReviewDialog : DialogFragment() {
    private val viewModel: ReviewDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val parent = activity.window.decorView as ViewGroup
        val view = LayoutReviewBinding.inflate(activity.layoutInflater, parent, false).root
        return AlertDialog.Builder(activity)
            .setIcon(R.drawable.ic_launcher)
            .setTitle(R.string.app_name)
            .setView(view)
            .setPositiveButton(R.string.dialog_button_review) { _, _ ->
                Launcher.openGooglePlay(activity)
                viewModel.onReview()
            }
            .setNeutralButton(R.string.dialog_button_send_mail) { _, _ ->
                Launcher.sendMailToDeveloper(activity)
                viewModel.onReport()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewModel.onCancel()
    }

    companion object {
        private const val TAG = "ReviewDialog"

        fun show(fragment: Fragment): Boolean {
            val manager = fragment.childFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return false
            ReviewDialog().show(manager, TAG)
            return true
        }
    }
}
