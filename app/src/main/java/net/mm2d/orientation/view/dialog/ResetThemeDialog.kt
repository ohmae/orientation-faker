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
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import net.mm2d.android.orientationfaker.R

class ResetThemeDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_reset_theme)
            .setMessage(R.string.dialog_message_reset_theme)
            .setPositiveButton(R.string.ok) { _, _ ->
                val requestKey = requireArguments().getString(KEY_REQUEST_KEY, "")
                parentFragmentManager.setFragmentResult(requestKey, bundleOf())
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    companion object {
        private const val TAG = "ResetThemeDialog"
        private const val KEY_REQUEST_KEY = "KEY_REQUEST_KEY"

        fun registerListener(fragment: Fragment, requestKey: String, listener: () -> Unit) {
            fragment.childFragmentManager
                .setFragmentResultListener(requestKey, fragment) { _, _ ->
                    listener()
                }
        }

        fun show(fragment: Fragment, requestKey: String) {
            val manager = fragment.childFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            ResetThemeDialog().also { dialog ->
                dialog.arguments = bundleOf(
                    KEY_REQUEST_KEY to requestKey
                )
            }.show(manager, TAG)
        }
    }
}
