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
    ROUND_SQUARE(
        R.drawable.bg_round_square,
        R.string.menu_description_icon_shape_round_square
    ),
    HEXAGON(
        R.drawable.bg_hexagon,
        R.string.menu_description_icon_shape_hexagon
    ),
    ;

    companion object {
        fun of(value: String): IconShape =
            values().find { it.name == value } ?: CIRCLE
    }
}
