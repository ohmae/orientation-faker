/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget

import net.mm2d.android.orientationfaker.R.id

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
object ViewIds {
    data class OrientationId(
        val viewId: Int,
        val iconViewId: Int,
        val titleViewId: Int
    )

    val list: List<OrientationId> = listOf(
        OrientationId(
            id.remote_views_button1,
            id.remote_views_icon1,
            id.remote_views_title1
        ),
        OrientationId(
            id.remote_views_button2,
            id.remote_views_icon2,
            id.remote_views_title2
        ),
        OrientationId(
            id.remote_views_button3,
            id.remote_views_icon3,
            id.remote_views_title3
        ),
        OrientationId(
            id.remote_views_button4,
            id.remote_views_icon4,
            id.remote_views_title4
        ),
        OrientationId(
            id.remote_views_button5,
            id.remote_views_icon5,
            id.remote_views_title5
        )
    )
}
