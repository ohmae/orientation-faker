/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.FragmentCustomWidgetListBinding
import net.mm2d.android.orientationfaker.databinding.ItemCustomWidgetBinding
import net.mm2d.orientation.room.WidgetSettingEntity
import net.mm2d.orientation.util.autoCleared

@AndroidEntryPoint
class CustomWidgetListFragment: Fragment(R.layout.fragment_custom_widget_list) {
    private var binding: FragmentCustomWidgetListBinding by autoCleared()
    private val viewModel: CustomWidgetListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCustomWidgetListBinding.bind(view)
        val adapter = ItemAdapter(layoutInflater) {
            navigate(CustomWidgetListFragmentDirections.actionCustomWidgetListToCustomWidgetConfig(it))
        }
        binding.recyclerView.adapter = adapter
        viewModel.getAll().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun navigate(directions: NavDirections) {
        val controller = findNavController()
        if (controller.currentDestination?.id == R.id.CustomWidgetListFragment) {
            controller.navigate(directions)
        }
    }

    private class ItemViewHolder(
        val binding: ItemCustomWidgetBinding
    ): RecyclerView.ViewHolder(binding.root)

    private class ItemAdapter(
        private val inflater: LayoutInflater,
        private val onClickItem: (Int) -> Unit
    ): ListAdapter<WidgetSettingEntity, ItemViewHolder>(
        object : DiffUtil.ItemCallback<WidgetSettingEntity>() {
            override fun areItemsTheSame(oldItem: WidgetSettingEntity, newItem: WidgetSettingEntity): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: WidgetSettingEntity, newItem: WidgetSettingEntity): Boolean =
                oldItem == newItem
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
            ItemViewHolder(ItemCustomWidgetBinding.inflate(inflater, parent, false))

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val entity = getItem(position)
            holder.binding.title.text = "ID: ${entity.id}"
            holder.binding.remoteViewsShape1.setImageResource(entity.shape.iconId)
            holder.binding.remoteViewsShape2.setImageResource(entity.shape.iconId)
            holder.binding.remoteViewsShape1.imageTintList = ColorStateList.valueOf(entity.background)
            holder.binding.remoteViewsShape2.imageTintList = ColorStateList.valueOf(entity.backgroundSelected)
            holder.binding.remoteViewsIcon1.imageTintList = ColorStateList.valueOf(entity.foreground)
            holder.binding.remoteViewsIcon2.imageTintList = ColorStateList.valueOf(entity.foreground)
            holder.binding.sampleFrame.setBackgroundColor(entity.base)
            holder.binding.root.setOnClickListener {
                onClickItem(entity.id)
            }
        }
    }
}
