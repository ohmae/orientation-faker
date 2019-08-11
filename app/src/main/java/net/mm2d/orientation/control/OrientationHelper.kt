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
import net.mm2d.orientation.settings.Settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class OrientationHelper private constructor(context: Context) {
    private val view: View
    private val windowManager: WindowManager
    private val layoutParams: LayoutParams

    val isEnabled: Boolean
        get() = view.parent != null

    @Suppress("DEPRECATION")
    private val type: Int
        get() =
            if (VERSION.SDK_INT >= VERSION_CODES.O) LayoutParams.TYPE_APPLICATION_OVERLAY
            else LayoutParams.TYPE_SYSTEM_ALERT

    init {
        val appContext = context.applicationContext
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
        val settings = Settings.get()
        if (settings.firstUseTime == 0L) {
            settings.firstUseTime = System.currentTimeMillis()
        }
        val orientation = settings.orientation.let {
            if (it == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED && settings.useFullSensor) {
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else {
                it
            }
        }
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

    fun cancel() {
        if (isEnabled) {
            windowManager.removeViewImmediate(view)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: OrientationHelper? = null

        fun getInstance(context: Context): OrientationHelper {
            return instance ?: OrientationHelper(context).also { instance = it }
        }
    }
}
