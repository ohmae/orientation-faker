package net.mm2d.orientation.control

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.core.content.getSystemService

class OrientationController(context: Context) {
    private val view: View = View(context)
    private val windowManager: WindowManager = context.getSystemService()!!
    private val layoutParams: LayoutParams = LayoutParams(
        0, 0, 0, 0,
        type,
        LayoutParams.FLAG_NOT_FOCUSABLE
            or LayoutParams.FLAG_NOT_TOUCHABLE
            or LayoutParams.FLAG_NOT_TOUCH_MODAL
            or LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).also {
        it.screenOrientation = Orientation.UNSPECIFIED
    }
    var isEnabled: Boolean = false
        private set

    val orientation: Int
        get() = layoutParams.screenOrientation

    @Suppress("DEPRECATION")
    private val type: Int
        get() =
            if (VERSION.SDK_INT >= VERSION_CODES.O) LayoutParams.TYPE_APPLICATION_OVERLAY
            else LayoutParams.TYPE_SYSTEM_ALERT

    fun setOrientation(orientation: Int) {
        if (layoutParams.screenOrientation == orientation) return
        layoutParams.screenOrientation = orientation
        if (orientation == Orientation.UNSPECIFIED) {
            if (isEnabled) {
                windowManager.removeView(view)
                isEnabled = false
            }
        } else {
            if (isEnabled) {
                windowManager.updateViewLayout(view, layoutParams)
            } else {
                windowManager.addView(view, layoutParams)
                isEnabled = true
            }
        }
    }

    fun stop() {
        if (isEnabled) {
            windowManager.removeViewImmediate(view)
        }
    }
}
