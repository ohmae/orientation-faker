package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.util.isResumed

class ResetLayoutDialog : DialogFragment() {
    interface Callback {
        fun resetLayout()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_reset_layout)
            .setMessage(R.string.dialog_message_reset_layout)
            .setPositiveButton(R.string.ok) { _, _ ->
                (activity as? Callback)?.resetLayout()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    companion object {
        private const val TAG = "ResetLayoutDialog"

        fun show(activity: FragmentActivity) {
            if (activity.isFinishing || !activity.isResumed()) {
                return
            }
            activity.supportFragmentManager.also {
                if (it.findFragmentByTag(TAG) == null) {
                    ResetLayoutDialog().show(it, TAG)
                }
            }
        }
    }
}
