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

    fun function(context: Context, function: FunctionButton): PendingIntent =
        when (function) {
            is OrientationButton -> orientation(context, function.orientation)
            is LauncherButton -> activity(context)
        }

    private fun orientation(context: Context, orientation: Orientation): PendingIntent {
        val intent = orientationIntent(context, orientation)
        return PendingIntent.getBroadcast(context, orientation.value + 1000, intent, FLAGS)
    }

    private fun activity(context: Context): PendingIntent {
        val intent = activityIntent(context)
        return PendingIntent.getActivity(context, 100, intent, FLAGS)
    }

    private fun orientationIntent(context: Context, orientation: Orientation): Intent =
        Intent(OrientationReceiver.ACTION_ORIENTATION).also {
            it.putExtra(OrientationReceiver.EXTRA_ORIENTATION, orientation.value)
            it.setClass(context, OrientationReceiver::class.java)
        }

    private fun activityIntent(context: Context): Intent =
        Intent(context, MainActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
}
