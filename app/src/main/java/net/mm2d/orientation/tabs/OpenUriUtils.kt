/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.tabs

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import net.mm2d.orientation.util.queryIntentActivitiesCompat
import net.mm2d.orientation.util.resolveActivityCompat

object OpenUriUtils {
    private var defaultBrowserPackage: String? = null
    private var browserPackages: Set<String>? = null

    fun getBrowserPackages(context: Context): Set<String> {
        browserPackages?.let {
            return it
        }
        return getBrowserPackagesInner(context).also {
            browserPackages = it
        }
    }

    private fun getBrowserPackagesInner(context: Context): Set<String> {
        val flags = PackageManager.MATCH_ALL or PackageManager.MATCH_DEFAULT_ONLY
        return context.packageManager
            .queryIntentActivitiesCompat(makeBrowserTestIntent(), flags)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    fun getDefaultBrowserPackage(context: Context): String? {
        defaultBrowserPackage?.let {
            return it
        }
        return getDefaultBrowserPackageInner(context)?.also {
            defaultBrowserPackage = it
        }
    }

    private fun getDefaultBrowserPackageInner(context: Context): String? {
        val packageName = context.packageManager
            .resolveActivityCompat(makeBrowserTestIntent(), PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo
            ?.packageName
            ?: return null
        return if (getBrowserPackages(context).contains(packageName)) {
            packageName
        } else null
    }

    private fun makeBrowseIntent(uri: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(uri)).also {
            it.addCategory(Intent.CATEGORY_BROWSABLE)
        }

    private fun makeBrowserTestIntent(): Intent =
        makeBrowseIntent("http://www.example.com/")
}
