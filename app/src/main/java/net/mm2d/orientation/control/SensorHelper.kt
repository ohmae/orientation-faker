/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.core.content.getSystemService
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.util.Powers

class SensorHelper(
    private val context: Context,
    private val sensorRequestedSupplier: () -> Boolean,
    private val sensorResultListener: (x: Float, y: Float, z: Float) -> Unit
) {
    private var sensorManager: SensorManager? = null
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (Powers.isInteractive(context) && sensorRequestedSupplier()) {
                startSensor()
            } else {
                stopSensor()
            }
        }
    }
    private val listener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        override fun onSensorChanged(event: SensorEvent) {
            sensorResultListener(event.values[0], event.values[1], event.values[2])
        }
    }

    init {
        context.registerReceiver(broadcastReceiver, IntentFilter().also {
            it.addAction(Intent.ACTION_SCREEN_ON)
            it.addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    fun startSensor() {
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

    fun stopSensor() {
        sensorManager?.unregisterListener(listener)
        sensorManager = null
    }
}
