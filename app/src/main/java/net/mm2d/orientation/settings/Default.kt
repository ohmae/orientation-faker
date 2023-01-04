/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.FunctionButton
import net.mm2d.orientation.control.FunctionButton.OrientationButton
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Default @Inject constructor(
    @ApplicationContext context: Context
) {
    val color: Color = Color(
        ContextCompat.getColor(context, R.color.fg_notification),
        ContextCompat.getColor(context, R.color.bg_notification),
        ContextCompat.getColor(context, R.color.fg_notification),
        ContextCompat.getColor(context, R.color.bg_notification_selected),
        ContextCompat.getColor(context, R.color.bg_notification_base),
    )

    class Color(
        val foreground: Int,
        val background: Int,
        val foregroundSelected: Int,
        val backgroundSelected: Int,
        val base: Int,
    )

    companion object {
        const val nightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

        val functions: List<FunctionButton> = listOf(
            OrientationButton.UNSPECIFIED,
            OrientationButton.PORTRAIT,
            OrientationButton.LANDSCAPE,
            OrientationButton.REVERSE_PORTRAIT,
            OrientationButton.REVERSE_LANDSCAPE,
        )
    }
}
