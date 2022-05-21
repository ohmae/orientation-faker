/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.review.ReviewRequest
import net.mm2d.orientation.settings.PreferenceRepository
import net.mm2d.orientation.util.SystemSettings
import net.mm2d.orientation.util.Toaster
import javax.inject.Inject

@AndroidEntryPoint
class ControlStatusReceiver : BroadcastReceiver() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onReceive(context: Context, intent: Intent) {
        val orientation = Orientation.of(intent.getIntExtra(EXTRA_ORIENTATION, Orientation.INVALID.value))
        if (orientation == Orientation.INVALID) return
        preferenceRepository.scope.launch {
            ReviewRequest.updateOrientation(orientation, preferenceRepository)
        }
        preferenceRepository
        notifySystemSettingsIfNeed(context, orientation)
    }

    private fun notifySystemSettingsIfNeed(context: Context, orientation: Orientation) {
        if (!orientation.requestsSystemSettings()) {
            return
        }
        if (preferenceRepository.menuPreferenceFlow.replayCache.getOrNull(0)?.warnSystemRotate != false) {
            return
        }
        if (SystemSettings.rotationIsFixed(context)) {
            Toaster.showLong(context, R.string.toast_system_settings)
        }
    }

    companion object {
        private const val ACTION_CONTROL_STATUS = "ACTION_CONTROL_STATUS"
        private const val EXTRA_ORIENTATION = "EXTRA_ORIENTATION"

        fun register(application: Application) {
            application.registerReceiver(ControlStatusReceiver(), IntentFilter(ACTION_CONTROL_STATUS))
        }

        fun updateOrientation(context: Context, orientation: Orientation) {
            context.sendBroadcast(Intent(ACTION_CONTROL_STATUS).also {
                it.putExtra(EXTRA_ORIENTATION, orientation.value)
            })
        }
    }
}
