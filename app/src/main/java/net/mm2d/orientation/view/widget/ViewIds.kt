/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget

import net.mm2d.android.orientationfaker.R.id

object ViewIds {
    data class ViewId(
        val buttonId: Int,
        val iconId: Int,
        val labelId: Int,
        val shapeId: Int,
    )

    val list: List<ViewId> = listOf(
        ViewId(
            id.remote_views_button1,
            id.remote_views_icon1,
            id.remote_views_label1,
            id.remote_views_shape1,
        ),
        ViewId(
            id.remote_views_button2,
            id.remote_views_icon2,
            id.remote_views_label2,
            id.remote_views_shape2,
        ),
        ViewId(
            id.remote_views_button3,
            id.remote_views_icon3,
            id.remote_views_label3,
            id.remote_views_shape3,
        ),
        ViewId(
            id.remote_views_button4,
            id.remote_views_icon4,
            id.remote_views_label4,
            id.remote_views_shape4,
        ),
        ViewId(
            id.remote_views_button5,
            id.remote_views_icon5,
            id.remote_views_label5,
            id.remote_views_shape5,
        ),
        ViewId(
            id.remote_views_button6,
            id.remote_views_icon6,
            id.remote_views_label6,
            id.remote_views_shape6,
        ),
    )
}
