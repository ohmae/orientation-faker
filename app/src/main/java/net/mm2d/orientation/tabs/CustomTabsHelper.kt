/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.tabs

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@SuppressLint("StaticFieldLeak")
object CustomTabsHelper : CustomTabsServiceConnection() {
    private val PREFERRED_PACKAGES = listOf(
        "com.android.chrome", // Chrome
        "org.mozilla.firefox", // Firefox
        "com.microsoft.emmx", // Microsoft Edge
        "com.yandex.browser", //Yandex Browser
        "com.sec.android.app.sbrowser", // Samsung Internet Browser
        "com.kiwibrowser.browser", // Kiwi Browser
        "com.brave.browser", // Brave Browser
        "com.chrome.beta", // Chrome Beta
        "com.chrome.dev", // Chrome Dev
        "com.chrome.canary", // Chrome Canary
        "org.mozilla.firefox_beta", // Firefox Beta
        "org.mozilla.fennec_aurora", // Firefox Nightly
        "org.mozilla.focus", // Firefox Focus
        "com.yandex.browser.beta", // Yandex Browser (beta)
        "com.yandex.browser.alpha", // Yandex Browser (alpha)
        "com.google.android.apps.chrome" // Chrome Local
    )
    private const val ACTION_CUSTOM_TABS_CONNECTION =
        "android.support.customtabs.action.CustomTabsService"
    var packageNameToBind: String? = null
        private set

    private fun findPackageNameToUse(context: Context): String? {
        packageNameToBind = findPackageNameToUseInner(context)
        return packageNameToBind
    }

    private fun findPackageNameToUseInner(context: Context): String? {
        val browsers = OpenUriUtils.getBrowserPackages(context)
        val candidate = context.packageManager
            .queryIntentServices(Intent(ACTION_CUSTOM_TABS_CONNECTION), 0)
            .mapNotNull { it.serviceInfo?.packageName }
            .filter { browsers.contains(it) }
        if (candidate.isEmpty()) {
            return null
        }
        if (candidate.size == 1) {
            return candidate[0]
        }
        OpenUriUtils.getDefaultBrowserPackage(context).let {
            if (candidate.contains(it)) {
                return it
            }
        }
        return PREFERRED_PACKAGES.find { candidate.contains(it) }
    }

    private lateinit var context: Context
    private var bound: Boolean = false
    var session: CustomTabsSession? = null
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    internal fun bind() {
        if (!bound) {
            val packageName = findPackageNameToUse(context) ?: return
            bound = CustomTabsClient.bindCustomTabsService(context, packageName, this)
        }
    }

    internal fun unbind() {
        if (bound) {
            context.unbindService(this)
            bound = false
            session = null
        }
    }

    override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
        client.warmup(0)
        session = client.newSession(CustomTabsCallback())
    }

    override fun onServiceDisconnected(name: ComponentName) {
        session = null
    }
}
