package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.isResumed

class OverlayPermissionDialog : DialogFragment() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val message = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            !activity.packageManager.hasSystemFeature(PackageManager.FEATURE_RAM_LOW)
        ) {
            R.string.dialog_message_overlay_permission
        } else {
            R.string.dialog_message_overlay_permission_go
        }
        return AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_overlay_permission)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_button_open_settings) { _, _ ->
                SystemSettings.requestOverlayPermission(activity)
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
        private const val TAG = "OverlayPermissionDialog"

        @RequiresApi(Build.VERSION_CODES.M)
        fun showDialog(activity: FragmentActivity) {
            if (activity.isFinishing || !activity.isResumed()) {
                return
            }
            activity.supportFragmentManager.also {
                if (it.findFragmentByTag(TAG) == null) {
                    OverlayPermissionDialog().show(it, TAG)
                }
            }
        }
    }
}
