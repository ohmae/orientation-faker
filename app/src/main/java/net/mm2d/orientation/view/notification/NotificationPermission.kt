package net.mm2d.orientation.view.notification

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import net.mm2d.orientation.util.PermissionRequestLauncher
import net.mm2d.orientation.util.registerForPermissionRequest

object NotificationPermission {
    @RequiresApi(33)
    private const val PERMISSION = Manifest.permission.POST_NOTIFICATIONS

    fun register(
        fragment: Fragment,
        callback: (granted: Boolean, failedToShowDialog: Boolean) -> Unit
    ): PermissionRequestLauncher =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            fragment.registerForPermissionRequest(PERMISSION, callback)
        } else {
            PermissionRequestLauncher.EMPTY
        }

    fun isGranted(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionChecker.checkSelfPermission(context, PERMISSION) == PermissionChecker.PERMISSION_GRANTED
        } else {
            true
        }
}
