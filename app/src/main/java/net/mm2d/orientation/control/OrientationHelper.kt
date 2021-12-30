/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.annotation.SuppressLint
import android.content.Context
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.OrientationHelper.Rotation.ROTATION_0
import net.mm2d.orientation.control.OrientationHelper.Rotation.ROTATION_180
import net.mm2d.orientation.control.OrientationHelper.Rotation.ROTATION_270
import net.mm2d.orientation.control.OrientationHelper.Rotation.ROTATION_90
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.util.Powers
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.Toaster
import kotlin.math.abs
import kotlin.math.atan

@SuppressLint("StaticFieldLeak")
object OrientationHelper {
    private lateinit var context: Context
    private lateinit var controller: OrientationController
    private lateinit var sensorHelper: SensorHelper
    private var requestedOrientation: Orientation = Orientation.INVALID
    private var isLandscapeDevice: Boolean = false
    private var warnSystemRotate: Boolean = false

    fun initialize(c: Context) {
        context = c.applicationContext
        controller = OrientationController(context)
        sensorHelper = SensorHelper(
            context,
            { controller.isEnabled && requestedOrientation.usesSensor() },
            this::sensorResult
        )
    }

    suspend fun update(orientation: Orientation, landscapeDevice: Boolean) {
        if (requestedOrientation == orientation &&
            controller.isEnabled &&
            isLandscapeDevice == landscapeDevice
        ) {
            return
        }
        requestedOrientation = orientation
        ReviewRequest.updateOrientation(orientation)
        notifySystemSettingsIfNeed(orientation)
        isLandscapeDevice = landscapeDevice
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

    fun setWarnSystemRotate(warn: Boolean) {
        warnSystemRotate = warn
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

    private fun sensorResult(x: Float, y: Float, z: Float) {
        if (!controller.isEnabled) return
        if (isLandscapeDevice) {
            setOrientationBySensor(y, -x, z)
        } else {
            setOrientationBySensor(x, y, z)
        }
    }

    private fun setOrientationBySensor(x: Float, y: Float, z: Float) {
        if (minimumCoercion(x, y)) return
        if (abs(z).let { it > abs(x) && it > abs(y) }) {
            return // Z成分が大きい場合は判断しない
        }
        val rotation = calculateAngle(x, y).toRotation()
        when (requestedOrientation) {
            Orientation.SENSOR_PORTRAIT,
            Orientation.SENSOR_LANDSCAPE ->
                sensorUpSideDown(rotation)
            Orientation.SENSOR_LIE_LEFT ->
                sensorLieLeft(rotation)
            Orientation.SENSOR_LIE_RIGHT ->
                sensorLieRight(rotation)
            Orientation.SENSOR_HEADSTAND ->
                sensorHeadstand(rotation)
            else -> Unit
        }
    }

    private fun sensorUpSideDown(rotation: Rotation) {
        val targetOrientation = when (rotation) {
            ROTATION_0 -> Orientation.PORTRAIT
            ROTATION_90 -> Orientation.LANDSCAPE
            ROTATION_180 -> Orientation.REVERSE_PORTRAIT
            ROTATION_270 -> Orientation.REVERSE_LANDSCAPE
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

    private fun sensorLieLeft(rotation: Rotation) {
        val targetOrientation = when (rotation) {
            ROTATION_0 -> Orientation.REVERSE_LANDSCAPE
            ROTATION_90 -> Orientation.PORTRAIT
            ROTATION_180 -> Orientation.LANDSCAPE
            ROTATION_270 -> Orientation.REVERSE_PORTRAIT
        }
        if (controller.orientation == targetOrientation) return
        controller.setOrientation(targetOrientation)
    }

    private fun sensorLieRight(rotation: Rotation) {
        val targetOrientation = when (rotation) {
            ROTATION_0 -> Orientation.LANDSCAPE
            ROTATION_90 -> Orientation.REVERSE_PORTRAIT
            ROTATION_180 -> Orientation.REVERSE_LANDSCAPE
            ROTATION_270 -> Orientation.PORTRAIT
        }
        if (controller.orientation == targetOrientation) return
        controller.setOrientation(targetOrientation)
    }

    private fun sensorHeadstand(rotation: Rotation) {
        val targetOrientation = when (rotation) {
            ROTATION_0 -> Orientation.REVERSE_PORTRAIT
            ROTATION_90 -> Orientation.REVERSE_LANDSCAPE
            ROTATION_180 -> Orientation.PORTRAIT
            ROTATION_270 -> Orientation.LANDSCAPE
        }
        if (controller.orientation == targetOrientation) return
        controller.setOrientation(targetOrientation)
    }

    private enum class Rotation {
        ROTATION_0,
        ROTATION_90,
        ROTATION_180,
        ROTATION_270,
    }

    private fun Double.toRotation(): Rotation =
        when {
            this < 1 / 8.0 -> ROTATION_0
            this < 3 / 8.0 -> ROTATION_90
            this < 5 / 8.0 -> ROTATION_180
            this < 7 / 8.0 -> ROTATION_270
            else -> ROTATION_0
        }

    // 角度を[0-1]で表現
    // arctan(x/y)/2πでy軸から時計回りに[0-0.25][-0.25-0][0-0.25][-0.25-0]という値が取られる。
    // これを時計回りに[0-1]になるように計算する。
    private fun calculateAngle(x: Float, y: Float): Double =
        (atan(x / y) / (2 * Math.PI)).let {
            if (y > 0) (if (it > 0) it else 1 + it) else 0.5 + it
        }

    private fun notifySystemSettingsIfNeed(orientation: Orientation) {
        if (!orientation.requestsSystemSettings()) {
            return
        }
        if (!warnSystemRotate) {
            return
        }
        if (SystemSettings.rotationIsFixed(context)) {
            Toaster.showLong(context, R.string.toast_system_settings)
        }
    }
}
