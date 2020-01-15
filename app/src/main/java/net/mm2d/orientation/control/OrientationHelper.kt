/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.SystemSettings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@SuppressLint("StaticFieldLeak")
object OrientationHelper {
    private lateinit var context: Context
    private lateinit var view: View
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: LayoutParams

    val isEnabled: Boolean
        get() = view.parent != null

    @Suppress("DEPRECATION")
    private val type: Int
        get() =
            if (VERSION.SDK_INT >= VERSION_CODES.O) LayoutParams.TYPE_APPLICATION_OVERLAY
            else LayoutParams.TYPE_SYSTEM_ALERT

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        this.context = appContext
        view = View(appContext)
        windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        layoutParams = LayoutParams(
            0, 0, 0, 0,
            type,
            LayoutParams.FLAG_NOT_FOCUSABLE
                or LayoutParams.FLAG_NOT_TOUCHABLE
                or LayoutParams.FLAG_NOT_TOUCH_MODAL
                or LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    fun updateOrientation() {
        ReviewRequest.initializeIfNeed()
        val settings = Settings.get()
        val orientation = settings.orientation.let {
            if (it == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED && settings.useFullSensor) {
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else {
                it
            }
        }
        notifySystemSettingsIfNeed(orientation)
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED &&
            orientation != layoutParams.screenOrientation
        ) {
            settings.orientationChangeCount++
        }
        layoutParams.screenOrientation = orientation
        if (isEnabled) {
            windowManager.updateViewLayout(view, layoutParams)
        } else {
            windowManager.addView(view, layoutParams)
        }
    }

    private fun notifySystemSettingsIfNeed(requestedOrientation: Int) {
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE &&
            requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        ) {
            return
        }
        if (!Settings.get().autoRotateWarning) {
            return
        }
        if (SystemSettings.rotationIsFixed(context)) {
            Toast.makeText(context, R.string.toast_system_settings, Toast.LENGTH_LONG).show()
        }
    }

    fun cancel() {
        if (isEnabled) {
            windowManager.removeViewImmediate(view)
        }
    }
}
