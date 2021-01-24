/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.settings.Settings
import net.mm2d.orientation.util.Powers
import net.mm2d.orientation.util.SystemSettings
import kotlin.math.abs
import kotlin.math.atan

@SuppressLint("StaticFieldLeak")
object OrientationHelper {
    private lateinit var context: Context
    private lateinit var controller: OrientationController
    private lateinit var sensorHelper: SensorHelper
    private var requestedOrientation: Int = Orientation.INVALID

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        this.context = appContext
        controller = OrientationController(appContext)
        sensorHelper = SensorHelper(context, controller, { requestedOrientation }, this::setSensorOrientation)
    }

    fun update(orientation: Int) {
        this.requestedOrientation = orientation
        ReviewRequest.updateOrientation(orientation)
        notifySystemSettingsIfNeed(orientation)
        if (orientation.usesSensor()) {
            if (!controller.isEnabled) {
                controller.setOrientation(Orientation.UNSPECIFIED)
            }
            if (Powers.isInteractive(context)) {
                sensorHelper.startSensor()
            }
        } else {
            sensorHelper.stopSensor()
            controller.setOrientation(orientation)
        }
    }

    fun cancel() {
        controller.stop()
        sensorHelper.stopSensor()
    }

    fun getOrientation(): Int =
        if (controller.isEnabled) {
            requestedOrientation
        } else {
            Settings.get().orientation
        }

    private fun minimumCoercion(x: Float, y: Float): Boolean {
        if (requestedOrientation == Orientation.SENSOR_PORTRAIT) {
            if (!controller.orientation.isPortrait()) {
                controller.setOrientation(if (y > 0) Orientation.PORTRAIT else Orientation.REVERSE_PORTRAIT)
                return true
            }
        } else if (requestedOrientation == Orientation.SENSOR_LANDSCAPE) {
            if (!controller.orientation.isLandscape()) {
                controller.setOrientation(if (x > 0) Orientation.LANDSCAPE else Orientation.REVERSE_LANDSCAPE)
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
        when (requestedOrientation) {
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
        if (controller.orientation == targetOrientation) return
        if (requestedOrientation == Orientation.SENSOR_PORTRAIT) {
            if (targetOrientation.isPortrait()) {
                controller.setOrientation(targetOrientation)
            }
        } else if (requestedOrientation == Orientation.SENSOR_LANDSCAPE) {
            if (targetOrientation.isLandscape()) {
                controller.setOrientation(targetOrientation)
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
        if (controller.orientation == targetOrientation) return
        controller.setOrientation(targetOrientation)
    }

    private fun sensorLieRight(angle: Double) {
        val targetOrientation = when {
            angle < 1 / 8.0 -> Orientation.LANDSCAPE
            angle < 3 / 8.0 -> Orientation.REVERSE_PORTRAIT
            angle < 5 / 8.0 -> Orientation.REVERSE_LANDSCAPE
            angle < 7 / 8.0 -> Orientation.PORTRAIT
            else -> Orientation.LANDSCAPE
        }
        if (controller.orientation == targetOrientation) return
        controller.setOrientation(targetOrientation)
    }

    private fun sensorHeadstand(angle: Double) {
        val targetOrientation = when {
            angle < 1 / 8.0 -> Orientation.REVERSE_PORTRAIT
            angle < 3 / 8.0 -> Orientation.REVERSE_LANDSCAPE
            angle < 5 / 8.0 -> Orientation.PORTRAIT
            angle < 7 / 8.0 -> Orientation.LANDSCAPE
            else -> Orientation.REVERSE_PORTRAIT
        }
        if (controller.orientation == targetOrientation) return
        controller.setOrientation(targetOrientation)
    }

    // 角度を[0-1]で表現
    // arctan(x/y)/2πでy軸から時計回りに[0-0.25][-0.25-0][0-0.25][-0.25-0]という値が取られる。
    // これを時計回りに[0-1]になるように計算する。
    private fun calculateAngle(x: Float, y: Float): Double =
        (atan(x / y) / (2 * Math.PI)).let {
            if (y > 0) (if (it > 0) it else 1 + it) else 0.5 + it
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
}
