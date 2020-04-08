/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.app.Activity
import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.System
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R

object SystemSettings {
    private const val ACTION_APP_NOTIFICATION_SETTINGS =
        "android.settings.APP_NOTIFICATION_SETTINGS"

    private fun startSystemSettings(activity: Activity, action: String, withPackage: Boolean = true) {
        try {
            val intent = Intent(action).also {
                if (withPackage) {
                    it.data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                }
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_could_not_open_setting, Toast.LENGTH_LONG).show()
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
    fun requestOverlayPermission(activity: Activity) {
        startSystemSettings(activity, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
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

    fun startUsageAccessSettings(activity: Activity) {
        val canSpecifyPackage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        startSystemSettings(activity, Settings.ACTION_USAGE_ACCESS_SETTINGS, canSpecifyPackage)
    }

    fun hasUsageAccessPermission(context: Context) =
        try {
            checkOpNoThrow(
                context,
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                BuildConfig.APPLICATION_ID
            ) == AppOpsManager.MODE_ALLOWED
        } catch (ignored: Exception) {
            false
        }

    @Suppress("SameParameterValue", "DEPRECATION")
    private fun checkOpNoThrow(context: Context, op: String, uid: Int, packageName: String): Int? =
        context.getSystemService<AppOpsManager>()?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.unsafeCheckOpNoThrow(op, uid, packageName)
            } else {
                it.checkOpNoThrow(op, uid, packageName)
            }
        }
}
