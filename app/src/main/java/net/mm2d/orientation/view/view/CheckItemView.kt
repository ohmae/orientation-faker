/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import net.mm2d.android.orientationfaker.databinding.ViewCheckItemBinding
import net.mm2d.orientation.control.Orientation

class CheckItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding: ViewCheckItemBinding =
        ViewCheckItemBinding.inflate(LayoutInflater.from(context), this)

    var orientation: Orientation = Orientation.UNSPECIFIED

    fun setText(id: Int) {
        binding.name.setText(id)
    }

    fun setIcon(id: Int) {
        binding.icon.setImageResource(id)
    }

    var isChecked: Boolean
        get() = binding.checkbox.isChecked
        set(value) {
            binding.checkbox.isChecked = value
        }
}
