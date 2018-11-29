/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_license.*
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.util.LaunchUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class LicenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setTitle(R.string.license)
            setDisplayHomeAsUpEnabled(true)
        }
        webView.settings.setSupportZoom(false)
        webView.settings.displayZoomControls = false
        webView.webViewClient = object : WebViewClient() {
            @Suppress("OverridingDeprecatedMember")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url ?: return true
                LaunchUtils.openCustomTabs(this@LicenseActivity, url)
                return true
            }

            @Suppress("DEPRECATION")
            @RequiresApi(VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return shouldOverrideUrlLoading(view, request?.url?.toString())
            }
        }
        webView.loadUrl("file:///android_asset/license.html")
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
