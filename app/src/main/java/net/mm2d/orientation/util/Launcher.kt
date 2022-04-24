/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ShareCompat
import net.mm2d.android.orientationfaker.BuildConfig
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.tabs.CustomTabsHelper

object Launcher {
    private const val PACKAGE_NAME = "net.mm2d.android.orientationfaker"
    private const val PRIVACY_POLICY_URL =
        "https://ohmae.github.io/app/orientation-faker/privacy-policy.html"
    private const val GITHUB_URL =
        "https://github.com/ohmae/orientation-faker/"
    private const val EMAIL_ADDRESS = "ryo@mm2d.net"

    private fun openUri(context: Context, uri: String): Boolean = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        context.startActivity(intent)
        true
    }.getOrNull() ?: false

    private fun openCustomTabs(context: Context, uri: String): Boolean =
        openCustomTabs(context, Uri.parse(uri))

    fun openCustomTabs(context: Context, uri: Uri): Boolean = runCatching {
        val scheme =
            if (context.isNightMode()) CustomTabsIntent.COLOR_SCHEME_DARK
            else CustomTabsIntent.COLOR_SCHEME_LIGHT
        val params = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(context.resolveColor(R.attr.colorPrimary))
            .build()
        val intent = CustomTabsIntent.Builder(CustomTabsHelper.session)
            .setShowTitle(true)
            .setColorScheme(scheme)
            .setDefaultColorSchemeParams(params)
            .build()
        intent.intent.setPackage(CustomTabsHelper.packageNameToBind)
        intent.launchUrl(context, uri)
        true
    }.getOrNull() ?: false


    private fun openGooglePlay(context: Context, packageName: String): Boolean =
        openUri(context, "market://details?id=$packageName") ||
            openCustomTabs(context, "https://play.google.com/store/apps/details?id=$packageName")

    fun openGooglePlay(context: Context): Boolean =
        openGooglePlay(context, PACKAGE_NAME)

    fun openPrivacyPolicy(context: Context) {
        openCustomTabs(context, PRIVACY_POLICY_URL)
    }

    fun openSourceCode(context: Context) {
        openCustomTabs(context, GITHUB_URL)
    }

    fun sendMailToDeveloper(context: Context) {
        runCatching {
            val intent = Intent(Intent.ACTION_SENDTO).also {
                it.data = Uri.parse("mailto:")
                it.putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_ADDRESS))
                it.putExtra(
                    Intent.EXTRA_SUBJECT,
                    context.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME + " (${Build.MODEL}, Android ${VERSION.RELEASE})"
                )
            }
            context.startActivity(intent)
        }
    }

    fun shareThisApp(context: Activity) {
        val url = "https://play.google.com/store/apps/details?id=$PACKAGE_NAME"
        val hashTag = context.getString(R.string.app_hash_tag)
        ShareCompat.IntentBuilder(context)
            .setText(" $hashTag $url")
            .setType("text/plain")
            .startChooser()
    }
}
