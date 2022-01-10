/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.mm2d.orientation.settings.PreferenceRepository
import javax.inject.Inject

@AndroidEntryPoint
class OrientationReceiver : BroadcastReceiver() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ORIENTATION) {
            return
        }
        val orientation = intent.getIntExtra(
            EXTRA_ORIENTATION,
            Orientation.UNSPECIFIED.value
        ).toOrientation()
        preferenceRepository.scope.launch {
            preferenceRepository.orientationPreferenceRepository
                .updateOrientationManually(orientation)
        }
    }

    companion object {
        const val ACTION_ORIENTATION = "net.mm2d.android.orientationfaker.ACTION_ORIENTATION"
        const val EXTRA_ORIENTATION = "EXTRA_ORIENTATION"
    }
}
