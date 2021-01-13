/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.util

import android.graphics.Color
import kotlin.math.pow

fun Int.shouldUseWhiteForeground(): Boolean =
    calculateContrast(this, Color.WHITE) >= 3

// https://www.w3.org/TR/WCAG20/#contrast-ratiodef
private fun calculateContrast(color1: Int, color2: Int): Float {
    val l1 = color1.calculateRelativeLuminance()
    val l2 = color2.calculateRelativeLuminance()
    return if (l1 > l2) {
        (l1 + 0.05f) / (l2 + 0.05f)
    } else {
        (l2 + 0.05f) / (l1 + 0.05f)
    }
}

// https://www.w3.org/TR/WCAG20/#relativeluminancedef
private fun Int.calculateRelativeLuminance(): Float {
    val r = red().normalize()
    val g = green().normalize()
    val b = blue().normalize()
    return r * 0.2126f + g * 0.7152f + b * 0.0722f
}

private fun Int.red(): Float = Color.red(this) / 255f
private fun Int.green(): Float = Color.green(this) / 255f
private fun Int.blue(): Float = Color.blue(this) / 255f
private fun Float.normalize(): Float =
    if (this < 0.03928f) this / 12.92f else ((this + 0.055f) / 1.055f).pow(2.4f)

fun Int.alpha(): Int =
    this.ushr(24) and 0xFF

fun Int.opaque(): Int =
    this or 0xFF.shl(24)
