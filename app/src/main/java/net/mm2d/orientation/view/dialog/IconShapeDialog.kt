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
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.ItemIconShapeBinding
import net.mm2d.orientation.settings.IconShape

class IconShapeDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.menu_title_icon_shape)
            .setAdapter(IconShapeAdapter(requireContext())) { _, which ->
                val requestKey = requireArguments().getString(KEY_REQUEST_KEY, "")
                parentFragmentManager.setFragmentResult(
                    requestKey, bundleOf(RESULT_SHAPE to IconShape.values()[which])
                )
            }
            .create()

    class IconShapeAdapter(context: Context) : BaseAdapter() {
        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        private val values = IconShape.values()
        override fun getCount(): Int = values.size
        override fun getItem(position: Int): IconShape = values[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val binding = if (convertView == null) {
                ItemIconShapeBinding.inflate(layoutInflater, parent, false)
            } else {
                ItemIconShapeBinding.bind(convertView)
            }
            val shape = getItem(position)
            binding.icon.setImageResource(shape.iconId)
            binding.text.setText(shape.textId)
            return binding.root
        }
    }

    companion object {
        private const val TAG = "IconShapeDialog"
        private const val KEY_REQUEST_KEY = "KEY_REQUEST_KEY"
        private const val RESULT_SHAPE = "RESULT_SHAPE"

        fun registerListener(fragment: Fragment, requestKey: String, listener: (IconShape) -> Unit) {
            fragment.childFragmentManager
                .setFragmentResultListener(requestKey, fragment) { _, result ->
                    listener(result.getSerializable(RESULT_SHAPE) as IconShape)
                }
        }

        fun show(fragment: Fragment, requestKey: String) {
            val manager = fragment.childFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            IconShapeDialog().also { dialog ->
                dialog.arguments = bundleOf(
                    KEY_REQUEST_KEY to requestKey
                )
            }.show(manager, TAG)
        }
    }
}
