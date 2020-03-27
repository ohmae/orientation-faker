package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.orientationfaker.R

class ResetButtonDialog : DialogFragment() {
    interface Callback {
        fun resetOrientation()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(context!!)
            .setTitle(R.string.dialog_title_reset_orientation)
            .setMessage(R.string.dialog_message_reset_orientation)
            .setPositiveButton(R.string.ok) { _, _ ->
                (activity as? Callback)?.resetOrientation()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    companion object {
        private const val TAG = "ResetOrientationDialog"

        fun show(activity: FragmentActivity) {
            val fragmentManager = activity.supportFragmentManager
            if (fragmentManager.findFragmentByTag(TAG) != null) {
                return
            }
            ResetButtonDialog()
                .show(fragmentManager, TAG)
        }
    }
}
