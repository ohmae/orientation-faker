/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker.orientation

import android.content.pm.ActivityInfo
import net.mm2d.android.orientationfaker.R
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object OrientationIdManager {
    data class OrientationId(val orientation: Int, val viewId: Int, val iconId: Int)

    val list: List<OrientationId> = Collections.unmodifiableList(
        ArrayList(
            Arrays.asList(
                OrientationId(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
                    R.id.button_unspecified,
                    R.drawable.ic_unspecified
                ),
                OrientationId(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                    R.id.button_portrait,
                    R.drawable.ic_portrait
                ),
                OrientationId(
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                    R.id.button_landscape,
                    R.drawable.ic_landscape
                ),
                OrientationId(
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                    R.id.button_reverse_portrait,
                    R.drawable.ic_reverse_portrait
                ),
                OrientationId(
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                    R.id.button_reverse_landscape,
                    R.drawable.ic_reverse_landscape
                )
            )
        )
    )

    fun getIconIdFromOrientation(orientation: Int): Int {
        return list.find { it.orientation == orientation }?.iconId ?: 0
    }
}
