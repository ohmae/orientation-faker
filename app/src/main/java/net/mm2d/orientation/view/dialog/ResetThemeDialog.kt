/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.util.parentViewModels

class ResetThemeDialog : DialogFragment() {
    private val viewModel: ResetThemeDialogViewModel by parentViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_reset_theme)
            .setMessage(R.string.dialog_message_reset_theme)
            .setPositiveButton(R.string.ok) { _, _ ->
                viewModel.postReset()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    companion object {
        private const val TAG = "ResetThemeDialog"

        fun show(fragment: Fragment) {
            val manager = fragment.childFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            ResetThemeDialog().show(manager, TAG)
        }
    }
}
