package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
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
        activity?.finish()
    }

    companion object {
        private const val TAG = "UsageAppPermissionDialog"

        fun showDialog(activity: FragmentActivity) {
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            UsageAppPermissionDialog().show(manager, TAG)
        }
    }
}
