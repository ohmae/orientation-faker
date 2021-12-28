/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.settings

import net.mm2d.android.orientationfaker.R

enum class IconShape(
    val iconId: Int,
    val textId: Int
) {
    CIRCLE(
        R.drawable.bg_circle,
        R.string.menu_description_icon_shape_circle
    ),
    SQUIRECLE(
        R.drawable.bg_squircle,
        R.string.menu_description_item_shape_squircle
    ),
    ROUNDED_SQUARE(
        R.drawable.bg_rounded_square,
        R.string.menu_description_icon_shape_rounded_square
    ),
    TEARDROP(
        R.drawable.bg_teardrop,
        R.string.menu_description_icon_shape_teardrop
    ),
    HEXAGON(
        R.drawable.bg_hexagon,
        R.string.menu_description_icon_shape_hexagon
    ),
    ;

    companion object {
        fun of(value: String?): IconShape =
            values().find { it.name == value } ?: CIRCLE
    }
}
