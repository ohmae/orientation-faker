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
import android.widget.CompoundButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getStringOrThrow
import kotlinx.android.synthetic.main.view_switch_menu.view.*
import net.mm2d.android.orientationfaker.R

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
    private val switchView: CompoundButton

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.view_switch_menu, this)
        switchView = menu_switch
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SwitchMenuView)
        menu_title.text = ta.getStringOrThrow(R.styleable.SwitchMenuView_title)
        descriptionOn = ta.getStringOrThrow(R.styleable.SwitchMenuView_descriptionOn)
        descriptionOff = ta.getStringOrThrow(R.styleable.SwitchMenuView_descriptionOff)
        val checked = ta.getBoolean(R.styleable.SwitchMenuView_checked, false)
        ta.recycle()
        switchView.isChecked = checked
        menu_description.text = if (checked) descriptionOn else descriptionOff
    }

    var isChecked: Boolean
        get() = switchView.isChecked
        set(value) {
            switchView.isChecked = value
            menu_description.text = if (value) descriptionOn else descriptionOff
        }
}
