/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.util.SystemSettings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object OverlayPermissionHelper {
    fun requestOverlayPermissionIfNeed(activity: Activity, requestCode: Int) {
        if (SystemSettings.canDrawOverlays(activity)) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            !activity.packageManager.hasSystemFeature(PackageManager.FEATURE_RAM_LOW)
        ) {
            SystemSettings.requestOverlayPermission(activity, requestCode)
            return
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_permission)
            .setMessage(R.string.dialog_message_permission)
            .setPositiveButton(R.string.dialog_button_open_settings) { _, _ ->
                SystemSettings.requestOverlayPermission(activity, requestCode)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                activity.finish()
            }
            .setOnCancelListener { activity.finish() }
            .show()
    }
}
