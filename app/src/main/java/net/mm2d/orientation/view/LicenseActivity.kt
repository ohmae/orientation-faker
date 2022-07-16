/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import net.mm2d.android.orientationfaker.databinding.ActivityLicenseBinding
import net.mm2d.orientation.util.Launcher
import net.mm2d.orientation.util.isNightMode

class LicenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLicenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.webView.settings.let {
            it.setSupportZoom(false)
            it.displayZoomControls = false
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                val mode = if (isNightMode()) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
                WebSettingsCompat.setForceDark(it, mode)
            }
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                WebSettingsCompat.setForceDarkStrategy(it, WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY)
            }
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return true
                return Launcher.openCustomTabs(this@LicenseActivity, uri)
            }
        }
        binding.webView.loadUrl("file:///android_asset/license.html")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LicenseActivity::class.java))
        }
    }
}
