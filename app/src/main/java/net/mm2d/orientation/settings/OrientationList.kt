package net.mm2d.orientation.settings

object OrientationList {
    const val MAX = 5
    const val MIN = 1
    private const val DELIMITER = ","

    fun toString(list: List<Int>): String =
        list.joinToString(DELIMITER)

    fun toList(string: String): List<Int> =
        string.split(DELIMITER).mapNotNull { it.toIntOrNull() }
}
