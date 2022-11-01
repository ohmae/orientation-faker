package net.mm2d.orientation.util

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.content.pm.PackageManager.ResolveInfoFlags
import android.content.pm.ResolveInfo
import android.os.Build

fun PackageManager.queryIntentActivitiesCompat(intent: Intent, flags: Int): List<ResolveInfo> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, ResolveInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION")
        queryIntentActivities(intent, flags)
    }

fun PackageManager.queryIntentServicesCompat(intent: Intent, flags: Int): List<ResolveInfo> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentServices(intent, ResolveInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION")
        queryIntentServices(intent, flags)
    }

fun PackageManager.resolveActivityCompat(intent: Intent, flags: Int): ResolveInfo? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        resolveActivity(intent, ResolveInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION")
        resolveActivity(intent, flags)
    }

@SuppressLint("QueryPermissionsNeeded")
fun PackageManager.getInstalledPackagesCompat(flags: Int): List<PackageInfo> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getInstalledPackages(PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION")
        getInstalledPackages(flags)
    }
