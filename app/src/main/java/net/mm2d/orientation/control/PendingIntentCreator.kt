/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import net.mm2d.orientation.control.FunctionButton.LauncherButton
import net.mm2d.orientation.control.FunctionButton.OrientationButton
import net.mm2d.orientation.view.MainActivity

object PendingIntentCreator {
    private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    fun orientation(context: Context, orientation: Orientation): PendingIntent {
        val intent = Intent(OrientationReceiver.ACTION_ORIENTATION).also {
            it.putExtra(OrientationReceiver.EXTRA_ORIENTATION, orientation.value)
            it.setClass(context, OrientationReceiver::class.java)
        }
        return PendingIntent.getBroadcast(context, orientation.value + 1000, intent, FLAGS)
    }

    fun function(context: Context, function: FunctionButton): PendingIntent =
        when (function) {
            is OrientationButton -> orientation(context, function.orientation)
            is LauncherButton -> activity(context)
        }

    fun activity(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return PendingIntent.getActivity(context, 100, intent, FLAGS)
    }
}
