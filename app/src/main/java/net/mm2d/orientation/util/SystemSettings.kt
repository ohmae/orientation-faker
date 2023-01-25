/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.provider.Settings.System
import androidx.core.content.getSystemService
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R

object SystemSettings {
    private fun startSystemSettings(activity: Activity, action: String, withPackage: Boolean = true) {
        if (!withPackage) {
            startSystemSettingsWithoutPackage(activity, action)
            return
        }
        try {
            val intent = Intent(action).also {
                it.data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            startSystemSettingsWithoutPackage(activity, action)
        }
    }

    private fun startSystemSettingsWithoutPackage(activity: Activity, action: String) {
        try {
            val intent = Intent(action).also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            Toaster.showLong(activity, R.string.toast_could_not_open_setting)
        }
    }

    fun rotationIsFixed(context: Context): Boolean = runCatching {
        System.getInt(context.contentResolver, System.ACCELEROMETER_ROTATION) == 0
    }.getOrNull() ?: false

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun requestOverlayPermission(activity: Activity) {
        startSystemSettings(activity, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    }

    fun startApplicationDetailsSettings(activity: Activity) {
        startSystemSettings(activity, Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    }

    fun startAppNotificationSettings(activity: Activity) {
        try {
            activity.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.putExtra("app_package", BuildConfig.APPLICATION_ID)
                it.putExtra("app_uid", activity.applicationInfo.uid)
                it.putExtra("android.provider.extra.APP_PACKAGE", BuildConfig.APPLICATION_ID)
            })
        } catch (e: Exception) {
            Toaster.showLong(activity, R.string.toast_could_not_open_setting)
        }
    }

    fun startUsageAccessSettings(activity: Activity) {
        val canSpecifyPackage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        startSystemSettings(activity, Settings.ACTION_USAGE_ACCESS_SETTINGS, canSpecifyPackage)
    }

    fun hasUsageAccessPermission(context: Context) =
        runCatching {
            checkOpNoThrow(
                context,
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                BuildConfig.APPLICATION_ID
            ) == AppOpsManager.MODE_ALLOWED
        }.getOrNull() ?: false

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
