/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R
import net.mm2d.log.Logger
import net.mm2d.orientation.tabs.CustomTabsHelper

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object LaunchUtils {
    private const val PACKAGE_NAME = "net.mm2d.android.orientationfaker"
    private const val PRIVACY_POLICY_URL =
        "https://github.com/ohmae/orientation-faker/blob/develop/PRIVACY-POLICY.md"
    private const val GITHUB_URL =
        "https://github.com/ohmae/orientation-faker/"
    private const val EMAIL_ADDRESS = "ryo@mm2d.net"

    private fun openUri(context: Context, uri: String): Boolean {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Logger.w(e)
            return false
        }
        return true
    }

    fun openCustomTabs(context: Context, uri: String): Boolean =
        openCustomTabs(context, Uri.parse(uri))

    fun openCustomTabs(context: Context, uri: Uri): Boolean {
        try {
            val intent = CustomTabsIntent.Builder(CustomTabsHelper.session)
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(context, R.color.primary))
                .build()
            intent.intent.setPackage(CustomTabsHelper.packageNameToBind)
            intent.launchUrl(context, uri)
        } catch (e: ActivityNotFoundException) {
            Logger.w(e)
            return false
        }
        return true
    }

    private fun openGooglePlay(context: Context, packageName: String): Boolean {
        return openUri(context, "market://details?id=$packageName") ||
            openCustomTabs(context, "https://play.google.com/store/apps/details?id=$packageName")
    }

    fun openGooglePlay(context: Context): Boolean =
        openGooglePlay(context, PACKAGE_NAME)

    fun openPrivacyPolicy(context: Context) {
        openCustomTabs(context, PRIVACY_POLICY_URL)
    }

    fun openSourceCode(context: Context) {
        openCustomTabs(context, GITHUB_URL)
    }

    fun sendMailReport(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).also {
                it.data = Uri.parse("mailto:$EMAIL_ADDRESS")
                it.putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_ADDRESS))
                it.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                it.putExtra(Intent.EXTRA_TEXT, makeSystemInformation(context))
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Logger.w(e)
        }
    }

    private fun makeSystemInformation(context: Context): String = StringBuilder()
        .append("----------system----------\n")
        .append("APP: ")
        .append(context.getString(R.string.app_name))
        .append(" ")
        .append(BuildConfig.VERSION_NAME)
        .append("/")
        .append(BuildConfig.VERSION_CODE)
        .append("\n")
        .append("DEVICE: ")
        .append(Build.FINGERPRINT)
        .append("\n")
        .toString()
}
