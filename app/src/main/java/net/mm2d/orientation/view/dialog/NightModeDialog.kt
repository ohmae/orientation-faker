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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.settings.NightModes
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.parentViewModels

class NightModeDialog : DialogFragment() {
    private val modes = listOf(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
    )
    private var mode: Int = Settings.get().nightMode
    private val viewModel: NightModeDialogViewModel by parentViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.menu_title_app_theme)
            .setSingleChoiceItems(
                modes.map { getText(NightModes.getTextId(it)) }.toTypedArray(),
                modes.indexOf(mode)
            ) { _, which ->
                mode = modes[which]
            }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.cancel()
                viewModel.postMode(mode)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    companion object {
        private const val TAG = "NightModeDialog"

        fun show(fragment: Fragment) {
            val manager = fragment.childFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            NightModeDialog().show(manager, TAG)
        }
    }
}
