/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget

import net.mm2d.android.orientationfaker.R.id

object ViewIds {
    data class OrientationId(
        val buttonId: Int,
        val iconId: Int,
        val titleId: Int,
        val backgroundId: Int,
    )

    val list: List<OrientationId> = listOf(
        OrientationId(
            id.remote_views_button1,
            id.remote_views_icon1,
            id.remote_views_title1,
            id.remote_views_background1,
        ),
        OrientationId(
            id.remote_views_button2,
            id.remote_views_icon2,
            id.remote_views_title2,
            id.remote_views_background2,
        ),
        OrientationId(
            id.remote_views_button3,
            id.remote_views_icon3,
            id.remote_views_title3,
            id.remote_views_background3,
        ),
        OrientationId(
            id.remote_views_button4,
            id.remote_views_icon4,
            id.remote_views_title4,
            id.remote_views_background4,
        ),
        OrientationId(
            id.remote_views_button5,
            id.remote_views_icon5,
            id.remote_views_title5,
            id.remote_views_background5,
        )
    )
}
