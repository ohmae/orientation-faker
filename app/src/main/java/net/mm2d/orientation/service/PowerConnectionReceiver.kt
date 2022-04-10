/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.orientation.settings.PreferenceRepository
import javax.inject.Inject

@AndroidEntryPoint
class PowerConnectionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                preferenceRepository.updatePowerPlugged(true)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                preferenceRepository.updatePowerPlugged(false)
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        private const val BATTERY_PLUGGED_ANY = BatteryManager.BATTERY_PLUGGED_AC or
            BatteryManager.BATTERY_PLUGGED_USB or
            BatteryManager.BATTERY_PLUGGED_WIRELESS

        private fun updateConnectedStatus(preferenceRepository: PreferenceRepository) {
            val pluggedState = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
                context.registerReceiver(null, it)
            }?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            val plugged = pluggedState and BATTERY_PLUGGED_ANY != 0
            preferenceRepository.updatePowerPlugged(plugged)
        }

        fun initialize(c: Context, preferenceRepository: PreferenceRepository) {
            context = c.applicationContext
            updateConnectedStatus(preferenceRepository)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return
            }
            val intentFilter = IntentFilter().also {
                it.addAction(Intent.ACTION_POWER_CONNECTED)
                it.addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            context.registerReceiver(PowerConnectionReceiver(), intentFilter)
        }
    }
}
