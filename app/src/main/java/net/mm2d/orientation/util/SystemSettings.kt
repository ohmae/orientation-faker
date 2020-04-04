/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.System
import android.widget.Toast
import androidx.annotation.RequiresApi
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R

object SystemSettings {
    private const val ACTION_APP_NOTIFICATION_SETTINGS =
        "android.settings.APP_NOTIFICATION_SETTINGS"

    private fun startSystemSettings(activity: Activity, action: String, block: (Intent) -> Unit) {
        try {
            val intent = Intent(action).also {
                it.data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            block(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_could_not_open_setting, Toast.LENGTH_LONG).show()
        }
    }

    private fun startSystemSettings(activity: Activity, action: String) {
        startSystemSettings(activity, action) {
            activity.startActivity(it)
        }
    }

    private fun startSystemSettingsForResult(activity: Activity, action: String, requestCode: Int) {
        startSystemSettings(activity, action) {
            activity.startActivityForResult(it, requestCode)
        }
    }

    fun rotationIsFixed(context: Context): Boolean = try {
        System.getInt(context.contentResolver, System.ACCELEROMETER_ROTATION) == 0
    } catch (ignored: Exception) {
        false
    }

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true
        else Settings.canDrawOverlays(context)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestOverlayPermission(activity: Activity, requestCode: Int) {
        startSystemSettingsForResult(activity, Settings.ACTION_MANAGE_OVERLAY_PERMISSION, requestCode)
    }

    fun startApplicationDetailsSettings(activity: Activity) {
        startSystemSettings(activity, Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    }

    fun startAppNotificationSettings(activity: Activity) {
        try {
            activity.startActivity(Intent(ACTION_APP_NOTIFICATION_SETTINGS).also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.putExtra("app_package", BuildConfig.APPLICATION_ID)
                it.putExtra("app_uid", activity.applicationInfo.uid)
                it.putExtra("android.provider.extra.APP_PACKAGE", BuildConfig.APPLICATION_ID)
            })
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_could_not_open_setting, Toast.LENGTH_LONG).show()
        }
    }
}
