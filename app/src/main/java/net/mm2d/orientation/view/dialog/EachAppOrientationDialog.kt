/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.LayoutOrientationItemBinding
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.control.Orientations
import net.mm2d.orientation.util.parentViewModels

class EachAppOrientationDialog : DialogFragment() {
    private val viewModel: EachAppOrientationDialogViewModel by parentViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val position = arguments?.getInt(KEY_POSITION)!!
        val packageName = arguments?.getString(KEY_PACKAGE)!!
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = HelpAdapter(context) {
            ForegroundPackageSettings.put(packageName, it)
            viewModel.postChangedPosition(position)
            dialog?.cancel()
        }
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        recyclerView.isVerticalFadingEdgeEnabled = true
        return AlertDialog.Builder(context)
            .setView(recyclerView)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    class HelpAdapter(
        context: Context,
        private val onClickListener: (orientation: Orientation) -> Unit
    ) : Adapter<ViewHolder>() {
        private val layoutInflater = LayoutInflater.from(context)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutOrientationItemBinding.inflate(layoutInflater, parent, false))

        override fun getItemCount(): Int =
            Orientations.entries.size + 1

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position == Orientations.entries.size) {
                holder.bind(null)
                holder.itemView.setOnClickListener {
                    onClickListener(Orientation.INVALID)
                }
            } else {
                val entity = Orientations.entries[position]
                holder.bind(entity)
                holder.itemView.setOnClickListener {
                    onClickListener(entity.orientation)
                }
            }
        }
    }

    class ViewHolder(private val binding: LayoutOrientationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: Orientations.Entity?) {
            if (entity != null) {
                binding.icon.setImageResource(entity.icon)
                binding.name.setText(entity.label)
                binding.description.setText(entity.description)
            } else {
                binding.icon.setImageResource(R.drawable.ic_remove)
                binding.name.setText(R.string.label_clear)
                binding.description.setText(R.string.description_clear)
            }
        }
    }

    companion object {
        private const val TAG = "EachAppOrientationDialog"
        private const val KEY_POSITION = "KEY_POSITION"
        private const val KEY_PACKAGE = "KEY_PACKAGE"

        fun show(fragment: Fragment, position: Int, packageName: String) {
            val manager = fragment.childFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            EachAppOrientationDialog().also { dialog ->
                dialog.arguments = bundleOf(
                    KEY_POSITION to position,
                    KEY_PACKAGE to packageName,
                )
            }.show(manager, TAG)
        }
    }
}
