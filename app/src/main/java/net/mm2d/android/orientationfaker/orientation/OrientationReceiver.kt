/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker.orientation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.text.TextUtils

import net.mm2d.android.orientationfaker.MainService
import net.mm2d.android.orientationfaker.settings.Settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class OrientationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!TextUtils.equals(intent.action, ACTION_ORIENTATION)) {
            return
        }
        val orientation = intent.getIntExtra(EXTRA_ORIENTATION,
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        Settings.get().orientation = orientation
        MainService.start(context)
    }

    companion object {
        const val ACTION_ORIENTATION = "net.mm2d.android.orientationfaker.ACTION_ORIENTATION"
        const val EXTRA_ORIENTATION = "EXTRA_ORIENTATION"
    }
}
