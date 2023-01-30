/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget.config

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.ItemSortItemBinding
import net.mm2d.orientation.control.Functions
import net.mm2d.orientation.room.WidgetSettingEntity
import net.mm2d.orientation.util.Toaster

class DragItemAdapter(
    private val activity: Activity,
    private var widgetSetting: WidgetSettingEntity,
    private val dragHelper: DragHelper,
    private val minimum: Int = 1,
) : ListAdapter<Functions.Entity, DragItemViewHolder>(
    object : ItemCallback<Functions.Entity>() {
        override fun areItemsTheSame(oldItem: Functions.Entity, newItem: Functions.Entity): Boolean =
            oldItem.function == newItem.function

        override fun areContentsTheSame(oldItem: Functions.Entity, newItem: Functions.Entity): Boolean =
            oldItem.function == newItem.function
    }
) {
    private val inflater = activity.layoutInflater

    init {
        dragHelper.setAdapter(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateWidgetSetting(widgetSetting: WidgetSettingEntity) {
        this.widgetSetting = widgetSetting
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        dragHelper.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        dragHelper.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DragItemViewHolder =
        DragItemViewHolder(ItemSortItemBinding.inflate(inflater, parent, false))

    override fun onBindViewHolder(holder: DragItemViewHolder, position: Int) {
        val data = getItem(position)
        holder.binding.let { binding ->
            binding.name.setText(data.label)
            binding.icon.setImageResource(data.icon)
            binding.shape.setImageResource(widgetSetting.shape.iconId)
            binding.icon.imageTintList = ColorStateList.valueOf(widgetSetting.foreground)
            binding.shape.imageTintList = ColorStateList.valueOf(widgetSetting.background)
            binding.root.setOnLongClickListener {
                if (itemCount <= minimum) {
                    Toaster.showLong(activity, R.string.toast_select_item_min)
                } else {
                    dragHelper.dragStart(holder, data)
                }
                true
            }
            val alpha = dragHelper.itemAlpha(data)
            binding.root.children.forEach { it.alpha = alpha }
        }
    }
}
