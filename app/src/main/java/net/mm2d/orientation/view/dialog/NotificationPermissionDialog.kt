package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R

@RequiresApi(Build.VERSION_CODES.O)
class NotificationPermissionDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_notification_permission_title)
            .setMessage(R.string.dialog_notification_permission_message)
            .setPositiveButton(R.string.app_info) { _, _ ->
                startAppInfo()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .create()

    private fun startAppInfo() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "PermissionDialog"

        fun show(fragment: Fragment) {
            val manager = fragment.childFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            NotificationPermissionDialog().show(manager, TAG)
        }
    }
}
