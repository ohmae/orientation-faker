/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.settings.NightModes

class NightModeDialog : DialogFragment() {
    private val modes = listOf(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = requireArguments()
        val requestKey = arguments.getString(KEY_REQUEST_KEY, "")
        var mode = arguments.getInt(KEY_MODE)
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.menu_title_app_theme)
            .setSingleChoiceItems(
                modes.map { getText(NightModes.getTextId(it)) }.toTypedArray(),
                modes.indexOf(mode)
            ) { _, which ->
                mode = modes[which]
            }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.cancel()
                parentFragmentManager.setFragmentResult(
                    requestKey, bundleOf(
                        RESULT_MODE to mode
                    )
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    companion object {
        private const val TAG = "NightModeDialog"
        private const val KEY_REQUEST_KEY = "KEY_REQUEST_KEY"
        private const val KEY_MODE = "KEY_MODE"
        private const val RESULT_MODE = "RESULT_MODE"

        fun registerListener(fragment: Fragment, requestKey: String, listener: (Int) -> Unit) {
            fragment.childFragmentManager
                .setFragmentResultListener(requestKey, fragment) { _, result ->
                    listener(result.getSerializable(RESULT_MODE) as Int)
                }
        }

        fun show(fragment: Fragment, requestKey: String, mode: Int) {
            val manager = fragment.childFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            NightModeDialog().also { dialog ->
                dialog.arguments = bundleOf(
                    KEY_REQUEST_KEY to requestKey,
                    KEY_MODE to mode
                )
            }.show(manager, TAG)
        }
    }
}
