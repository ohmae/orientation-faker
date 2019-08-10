/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import net.mm2d.android.orientationfaker.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object OverlayPermissionHelper {
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true
        else Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermissionIfNeed(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(activity)) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_RAM_LOW)
        ) {
            AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_title_permission)
                .setMessage(R.string.dialog_message_permission)
                .setPositiveButton(R.string.dialog_button_open_settings) { _, _ ->
                    requestOverlayPermission(activity, requestCode)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    activity.finish()
                }
                .setOnCancelListener { activity.finish() }
                .show()
        }
        requestOverlayPermission(activity, requestCode)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestOverlayPermission(activity: Activity, requestCode: Int) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_could_not_open_setting, Toast.LENGTH_LONG)
                .show()
        }
    }
}
