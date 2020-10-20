/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getStringOrThrow
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.ViewSwitchMenuBinding

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class SwitchMenuView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val descriptionOn: String
    private val descriptionOff: String
    private val binding: ViewSwitchMenuBinding =
        ViewSwitchMenuBinding.inflate(LayoutInflater.from(context), this)

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SwitchMenuView)
        binding.menuTitle.text = ta.getStringOrThrow(R.styleable.SwitchMenuView_title)
        descriptionOn = ta.getStringOrThrow(R.styleable.SwitchMenuView_descriptionOn)
        descriptionOff = ta.getStringOrThrow(R.styleable.SwitchMenuView_descriptionOff)
        val checked = ta.getBoolean(R.styleable.SwitchMenuView_checked, false)
        ta.recycle()
        binding.menuSwitch.isChecked = checked
        binding.menuDescription.text = if (checked) descriptionOn else descriptionOff
    }

    var isChecked: Boolean
        get() = binding.menuSwitch.isChecked
        set(value) {
            binding.menuSwitch.isChecked = value
            binding.menuDescription.text = if (value) descriptionOn else descriptionOff
        }
}
