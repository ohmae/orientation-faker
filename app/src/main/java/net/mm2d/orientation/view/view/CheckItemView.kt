package net.mm2d.orientation.view.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.view_check_item.view.*
import net.mm2d.android.orientationfaker.R

class CheckItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context)
            .inflate(R.layout.view_check_item, this)
    }

    var orientation: Int = 0

    fun setText(id: Int) {
        description.setText(id)
    }

    fun setIcon(id: Int) {
        icon.setImageResource(id)
    }

    var isChecked: Boolean
        get() = checkbox.isChecked
        set(value) {
            checkbox.isChecked = value
        }
}
