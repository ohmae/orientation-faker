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
import android.os.PowerManager
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.settings.Settings
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
    private val settings: Settings by lazy { Settings.get() }
    private var sensorManager: SensorManager? = null
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (!isEnabled) return
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            if (pm.isInteractive && settings.orientation.usesSensor()) {
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

    private fun Int.usesSensor(): Boolean =
        Orientation.sensor.contains(this)

    private fun Int.isPortrait(): Boolean =
        Orientation.portrait.contains(this)

    private fun Int.isLandscape(): Boolean =
        Orientation.landscape.contains(this)

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
        layoutParams.screenOrientation = Orientation.UNSPECIFIED
        appContext.registerReceiver(broadcastReceiver, IntentFilter().also {
            it.addAction(Intent.ACTION_SCREEN_ON)
            it.addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    fun updateOrientation() {
        ReviewRequest.initializeIfNeed()
        val orientation = settings.orientation
        notifySystemSettingsIfNeed(orientation)
        if (orientation != Orientation.UNSPECIFIED &&
            orientation != layoutParams.screenOrientation
        ) {
            settings.orientationChangeCount++
        }
        if (orientation.usesSensor()) {
            if (!isEnabled) {
                setOrientation(Orientation.UNSPECIFIED)
            }
            startSensor()
        } else {
            stopSensor()
            setOrientation(orientation)
        }
    }

    private fun setOrientation(orientation: Int) {
        layoutParams.screenOrientation = orientation
        if (isEnabled) {
            windowManager.updateViewLayout(view, layoutParams)
        } else {
            windowManager.addView(view, layoutParams)
        }
    }

    private fun setSensorOrientation(x: Float, y: Float, z: Float) {
        if (settings.orientation == Orientation.SENSOR_PORTRAIT) {
            if (!layoutParams.screenOrientation.isPortrait()) {
                if (y > 0) {
                    setOrientation(Orientation.PORTRAIT)
                } else {
                    setOrientation(Orientation.REVERSE_PORTRAIT)
                }
                return
            }
        } else if (settings.orientation == Orientation.SENSOR_LANDSCAPE) {
            if (!layoutParams.screenOrientation.isLandscape()) {
                if (x > 0) {
                    setOrientation(Orientation.LANDSCAPE)
                } else {
                    setOrientation(Orientation.REVERSE_LANDSCAPE)
                }
                return
            }
        }
        if (abs(z).let { it > abs(x) && it > abs(y) }) {
            return
        }
        val orientation = calculateOrientation(x, y)
        if (layoutParams.screenOrientation == orientation) return
        if (settings.orientation == Orientation.SENSOR_PORTRAIT) {
            if (orientation.isPortrait()) {
                setOrientation(orientation)
            }
        } else if (settings.orientation == Orientation.SENSOR_LANDSCAPE) {
            if (orientation.isLandscape()) {
                setOrientation(orientation)
            }
        }
    }

    private fun calculateOrientation(x: Float, y: Float): Int =
        (atan(x / y) / (2 * Math.PI)).let {
            if (y > 0) if (it > 0) it else 1 + it else 0.5 + it
        }.let {
            when {
                it < 0.125 -> Orientation.PORTRAIT
                it < 0.375 -> Orientation.LANDSCAPE
                it < 0.625 -> Orientation.REVERSE_PORTRAIT
                it < 0.875 -> Orientation.REVERSE_LANDSCAPE
                else -> Orientation.PORTRAIT
            }
        }

    private fun startSensor() {
        if (sensorManager != null) return
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
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

    fun cancel() {
        if (isEnabled) {
            windowManager.removeViewImmediate(view)
        }
        stopSensor()
    }
}
