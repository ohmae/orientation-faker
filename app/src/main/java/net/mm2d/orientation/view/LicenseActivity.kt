/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import net.mm2d.android.orientationfaker.databinding.ActivityLicenseBinding
import net.mm2d.orientation.util.Launcher

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class LicenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLicenseBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.webView.settings.setSupportZoom(false)
        binding.webView.settings.displayZoomControls = false
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return true
                return Launcher.openCustomTabs(this@LicenseActivity, uri)
            }
        }
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val query = if (nightMode == Configuration.UI_MODE_NIGHT_YES) "t=dark" else "t=light"
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl("file:///android_asset/license.html?$query")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LicenseActivity::class.java))
        }
    }
}
