/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.control

import android.content.pm.ActivityInfo
import net.mm2d.android.orientationfaker.R

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object OrientationIdManager {
    data class OrientationId(
        val orientation: Int,
        val iconId: Int,
        val viewId: Int,
        val iconViewId: Int,
        val titleViewId: Int
    )

    val list: List<OrientationId> = listOf(
        OrientationId(
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
            R.drawable.ic_unspecified,
            R.id.button_unspecified,
            R.id.icon_unspecified,
            R.id.title_unspecified
        ),
        OrientationId(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
            R.drawable.ic_portrait,
            R.id.button_portrait,
            R.id.icon_portrait,
            R.id.title_portrait
        ),
        OrientationId(
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
            R.drawable.ic_landscape,
            R.id.button_landscape,
            R.id.icon_landscape,
            R.id.title_landscape
        ),
        OrientationId(
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
            R.drawable.ic_reverse_portrait,
            R.id.button_reverse_portrait,
            R.id.icon_reverse_portrait,
            R.id.title_reverse_portrait
        ),
        OrientationId(
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            R.drawable.ic_reverse_landscape,
            R.id.button_reverse_landscape,
            R.id.icon_reverse_landscape,
            R.id.title_reverse_landscape
        ),
        OrientationId( // 設定ボタン用Fakeパラメータ
            -2, // ActivityInfo.SCREEN_ORIENTATION_UNSET
            R.drawable.ic_settings,
            R.id.button_settings,
            R.id.icon_settings,
            R.id.title_settings
        )
    )

    fun getIconIdFromOrientation(orientation: Int): Int {
        return list.find { it.orientation == orientation }?.iconId ?: 0
    }
}
