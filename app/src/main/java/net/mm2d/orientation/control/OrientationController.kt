/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.core.content.getSystemService

class OrientationController(context: Context) {
    private val view: View
    private val windowManager: WindowManager

    init {
        val ctx = createContext(context)
        view = View(ctx)
        windowManager = ctx.getSystemService()!!
    }

    private fun createContext(context: Context): Context {
        val display = context.getSystemService<DisplayManager>()!!
            .getDisplay(Display.DEFAULT_DISPLAY)
        val displayContext = context.createDisplayContext(display)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            displayContext.createWindowContext(LayoutParams.TYPE_APPLICATION_OVERLAY, null)
        } else {
            displayContext
        }
    }

    private val layoutParams: LayoutParams = LayoutParams(
        0, 0, 0, 0,
        type,
        LayoutParams.FLAG_NOT_FOCUSABLE
            or LayoutParams.FLAG_NOT_TOUCHABLE
            or LayoutParams.FLAG_NOT_TOUCH_MODAL
            or LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).also {
        it.screenOrientation = Orientation.UNSPECIFIED.value
    }

    var isEnabled: Boolean = false
        private set

    val orientation: Orientation
        get() = layoutParams.screenOrientation.toOrientation()

    private var attached: Boolean = false

    @Suppress("DEPRECATION")
    private val type: Int
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                LayoutParams.TYPE_SYSTEM_ALERT
            }

    fun setOrientation(orientation: Orientation) {
        isEnabled = true
        if (layoutParams.screenOrientation == orientation.value) return
        layoutParams.screenOrientation = orientation.value
        runCatching {
            if (orientation == Orientation.UNSPECIFIED) {
                if (attached) {
                    windowManager.removeView(view)
                    attached = false
                }
            } else {
                if (attached) {
                    windowManager.updateViewLayout(view, layoutParams)
                } else {
                    windowManager.addView(view, layoutParams)
                    attached = true
                }
            }
        }
    }

    fun stop() {
        isEnabled = false
        layoutParams.screenOrientation = Orientation.UNSPECIFIED.value
        if (attached) {
            runCatching {
                windowManager.removeViewImmediate(view)
            }
            attached = false
        }
    }
}
