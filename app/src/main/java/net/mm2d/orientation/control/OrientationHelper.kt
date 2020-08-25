/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.core.content.getSystemService
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.PowerUtils
import net.mm2d.orientation.util.SystemSettings
import kotlin.math.abs
import kotlin.math.atan

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@SuppressLint("StaticFieldLeak")
object OrientationHelper {
    private lateinit var context: Context
    private lateinit var view: View
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: LayoutParams
    private var orientation: Int = Orientation.INVALID
    private var sensorManager: SensorManager? = null
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (!isEnabled) return
            if (PowerUtils.isInteractive(context) && orientation.usesSensor()) {
                startSensor()
            } else {
                stopSensor()
            }
        }
    }
    private val listener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        override fun onSensorChanged(event: SensorEvent) {
            if (!isEnabled) return
            setSensorOrientation(event.values[0], event.values[1], event.values[2])
        }
    }

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
        windowManager = appContext.getSystemService()!!
        layoutParams = LayoutParams(
            0, 0, 0, 0,
            type,
            LayoutParams.FLAG_NOT_FOCUSABLE
                or LayoutParams.FLAG_NOT_TOUCHABLE
                or LayoutParams.FLAG_NOT_TOUCH_MODAL
                or LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.screenOrientation = Orientation.UNSPECIFIED
        appContext.registerReceiver(broadcastReceiver, IntentFilter().also {
            it.addAction(Intent.ACTION_SCREEN_ON)
            it.addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    fun update(orientation: Int) {
        this.orientation = orientation
        ReviewRequest.updateOrientation(orientation)
        notifySystemSettingsIfNeed(orientation)
        if (orientation.usesSensor()) {
            if (!isEnabled) {
                setOrientation(Orientation.UNSPECIFIED)
            }
            if (PowerUtils.isInteractive(context)) {
                startSensor()
            }
        } else {
            stopSensor()
            setOrientation(orientation)
        }
    }

    fun cancel() {
        if (isEnabled) {
            windowManager.removeViewImmediate(view)
        }
        stopSensor()
    }

    fun getOrientation(): Int =
        if (isEnabled) {
            orientation
        } else {
            Settings.get().orientation
        }

    private fun setOrientation(orientation: Int) {
        layoutParams.screenOrientation = orientation
        if (isEnabled) {
            windowManager.updateViewLayout(view, layoutParams)
        } else {
            windowManager.addView(view, layoutParams)
        }
    }

    private fun minimumCoercion(x: Float, y: Float): Boolean {
        if (orientation == Orientation.SENSOR_PORTRAIT) {
            if (!layoutParams.screenOrientation.isPortrait()) {
                setOrientation(if (y > 0) Orientation.PORTRAIT else Orientation.REVERSE_PORTRAIT)
                return true
            }
        } else if (orientation == Orientation.SENSOR_LANDSCAPE) {
            if (!layoutParams.screenOrientation.isLandscape()) {
                setOrientation(if (x > 0) Orientation.LANDSCAPE else Orientation.REVERSE_LANDSCAPE)
                return true
            }
        }
        return false
    }

    private fun setSensorOrientation(x: Float, y: Float, z: Float) {
        if (minimumCoercion(x, y)) return
        if (abs(z).let { it > abs(x) && it > abs(y) }) {
            return // Z成分が大きい場合は判断しない
        }
        val angle = calculateAngle(x, y)
        when (orientation) {
            Orientation.SENSOR_PORTRAIT,
            Orientation.SENSOR_LANDSCAPE ->
                sensorUpSideDown(angle)
            Orientation.SENSOR_LIE_LEFT ->
                sensorLieLeft(angle)
            Orientation.SENSOR_LIE_RIGHT ->
                sensorLieRight(angle)
            Orientation.SENSOR_HEADSTAND ->
                sensorHeadstand(angle)
        }
    }

    private fun sensorUpSideDown(angle: Double) {
        val targetOrientation = when {
            angle < 1 / 8.0 -> Orientation.PORTRAIT
            angle < 3 / 8.0 -> Orientation.LANDSCAPE
            angle < 5 / 8.0 -> Orientation.REVERSE_PORTRAIT
            angle < 7 / 8.0 -> Orientation.REVERSE_LANDSCAPE
            else -> Orientation.PORTRAIT
        }
        if (layoutParams.screenOrientation == targetOrientation) return
        if (orientation == Orientation.SENSOR_PORTRAIT) {
            if (targetOrientation.isPortrait()) {
                setOrientation(targetOrientation)
            }
        } else if (orientation == Orientation.SENSOR_LANDSCAPE) {
            if (targetOrientation.isLandscape()) {
                setOrientation(targetOrientation)
            }
        }
    }

    private fun sensorLieLeft(angle: Double) {
        val targetOrientation = when {
            angle < 1 / 8.0 -> Orientation.REVERSE_LANDSCAPE
            angle < 3 / 8.0 -> Orientation.PORTRAIT
            angle < 5 / 8.0 -> Orientation.LANDSCAPE
            angle < 7 / 8.0 -> Orientation.REVERSE_PORTRAIT
            else -> Orientation.REVERSE_LANDSCAPE
        }
        if (layoutParams.screenOrientation == targetOrientation) return
        setOrientation(targetOrientation)
    }

    private fun sensorLieRight(angle: Double) {
        val targetOrientation = when {
            angle < 1 / 8.0 -> Orientation.LANDSCAPE
            angle < 3 / 8.0 -> Orientation.REVERSE_PORTRAIT
            angle < 5 / 8.0 -> Orientation.REVERSE_LANDSCAPE
            angle < 7 / 8.0 -> Orientation.PORTRAIT
            else -> Orientation.LANDSCAPE
        }
        if (layoutParams.screenOrientation == targetOrientation) return
        setOrientation(targetOrientation)
    }

    private fun sensorHeadstand(angle: Double) {
        val targetOrientation = when {
            angle < 1 / 8.0 -> Orientation.REVERSE_PORTRAIT
            angle < 3 / 8.0 -> Orientation.REVERSE_LANDSCAPE
            angle < 5 / 8.0 -> Orientation.PORTRAIT
            angle < 7 / 8.0 -> Orientation.LANDSCAPE
            else -> Orientation.REVERSE_PORTRAIT
        }
        if (layoutParams.screenOrientation == targetOrientation) return
        setOrientation(targetOrientation)
    }

    // 角度を[0-1]で表現
    // arctan(x/y)/2πでy軸から時計回りに[0-0.25][-0.25-0][0-0.25][-0.25-0]という値が取られる。
    // これを時計回りに[0-1]になるように計算する。
    private fun calculateAngle(x: Float, y: Float): Double =
        (atan(x / y) / (2 * Math.PI)).let {
            if (y > 0) (if (it > 0) it else 1 + it) else 0.5 + it
        }

    private fun startSensor() {
        if (sensorManager != null) return
        val manager: SensorManager? = context.getSystemService()
        if (manager == null) {
            Toast.makeText(context, R.string.toast_fail_to_initialize_sensor, Toast.LENGTH_LONG).show()
            return
        }
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            Toast.makeText(context, R.string.toast_fail_to_initialize_sensor, Toast.LENGTH_LONG).show()
            return
        }
        sensorManager = manager
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stopSensor() {
        sensorManager?.unregisterListener(listener)
        sensorManager = null
    }

    private fun notifySystemSettingsIfNeed(orientation: Int) {
        if (!Orientation.requestSystemSettings.contains(orientation)) {
            return
        }
        if (!Settings.get().autoRotateWarning) {
            return
        }
        if (SystemSettings.rotationIsFixed(context)) {
            Toast.makeText(context, R.string.toast_system_settings, Toast.LENGTH_LONG).show()
        }
    }

    private fun Int.usesSensor(): Boolean =
        Orientation.sensor.contains(this)

    private fun Int.isPortrait(): Boolean =
        Orientation.portrait.contains(this)

    private fun Int.isLandscape(): Boolean =
        Orientation.landscape.contains(this)
}
