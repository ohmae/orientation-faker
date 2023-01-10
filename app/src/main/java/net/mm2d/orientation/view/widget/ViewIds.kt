/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget

import net.mm2d.android.orientationfaker.R

object ViewIds {
    data class ViewId(
        val buttonId: Int,
        val iconId: Int,
        val shapeId: Int,
    )

    val notification: List<ViewId> = listOf(
        ViewId(
            R.id.remote_views_button1,
            R.id.remote_views_icon1,
            R.id.remote_views_shape1,
        ),
        ViewId(
            R.id.remote_views_button2,
            R.id.remote_views_icon2,
            R.id.remote_views_shape2,
        ),
        ViewId(
            R.id.remote_views_button3,
            R.id.remote_views_icon3,
            R.id.remote_views_shape3,
        ),
        ViewId(
            R.id.remote_views_button4,
            R.id.remote_views_icon4,
            R.id.remote_views_shape4,
        ),
        ViewId(
            R.id.remote_views_button5,
            R.id.remote_views_icon5,
            R.id.remote_views_shape5,
        ),
        ViewId(
            R.id.remote_views_button6,
            R.id.remote_views_icon6,
            R.id.remote_views_shape6,
        ),
    )

    val widget: List<ViewId> = listOf(
        ViewId(
            R.id.remote_views_button1,
            R.id.remote_views_icon1,
            R.id.remote_views_shape1,
        ),
        ViewId(
            R.id.remote_views_button2,
            R.id.remote_views_icon2,
            R.id.remote_views_shape2,
        ),
        ViewId(
            R.id.remote_views_button3,
            R.id.remote_views_icon3,
            R.id.remote_views_shape3,
        ),
        ViewId(
            R.id.remote_views_button4,
            R.id.remote_views_icon4,
            R.id.remote_views_shape4,
        ),
        ViewId(
            R.id.remote_views_button5,
            R.id.remote_views_icon5,
            R.id.remote_views_shape5,
        ),
        ViewId(
            R.id.remote_views_button6,
            R.id.remote_views_icon6,
            R.id.remote_views_shape6,
        ),
        ViewId(
            R.id.remote_views_button7,
            R.id.remote_views_icon7,
            R.id.remote_views_shape7,
        ),
        ViewId(
            R.id.remote_views_button8,
            R.id.remote_views_icon8,
            R.id.remote_views_shape8,
        ),
        ViewId(
            R.id.remote_views_button9,
            R.id.remote_views_icon9,
            R.id.remote_views_shape9,
        ),
        ViewId(
            R.id.remote_views_button10,
            R.id.remote_views_icon10,
            R.id.remote_views_shape10,
        ),
    )
}
