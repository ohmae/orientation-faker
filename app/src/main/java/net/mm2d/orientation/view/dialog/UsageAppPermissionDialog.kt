/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.util.SystemSettings

class UsageAppPermissionDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        return AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_usage_access)
            .setMessage(R.string.dialog_message_usage_access)
            .setPositiveButton(R.string.ok) { _, _ ->
                SystemSettings.startUsageAccessSettings(activity)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        parentFragment?.findNavController()?.popBackStack()
    }

    companion object {
        private const val TAG = "UsageAppPermissionDialog"

        fun show(fragment: Fragment) {
            val manager = fragment.childFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            UsageAppPermissionDialog().show(manager, TAG)
        }
    }
}
