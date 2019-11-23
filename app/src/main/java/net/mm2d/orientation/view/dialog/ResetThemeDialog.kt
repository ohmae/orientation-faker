/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.orientationfaker.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ResetThemeDialog : DialogFragment() {
    interface Callback {
        fun resetTheme()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(context!!)
            .setTitle(R.string.dialog_title_reset_theme)
            .setMessage(R.string.dialog_message_reset_theme)
            .setPositiveButton(R.string.ok) { _, _ ->
                (activity as? Callback)?.resetTheme()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    companion object {
        private const val TAG = "ResetThemeDialog"

        fun show(activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            if (fragmentManager.findFragmentByTag(TAG) != null) {
                return
            }
            ResetThemeDialog()
                .show(fragmentManager, TAG)
        }
    }
}
