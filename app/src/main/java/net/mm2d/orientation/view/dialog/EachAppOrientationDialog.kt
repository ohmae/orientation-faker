package net.mm2d.orientation.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import net.mm2d.android.orientationfaker.R
import net.mm2d.android.orientationfaker.databinding.LayoutOrientationItemBinding
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.Orientation

class EachAppOrientationDialog : DialogFragment() {
    interface Callback {
        fun onChangeSettings(position: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val position = arguments?.getInt(KEY_POSITION)!!
        val packageName = arguments?.getString(KEY_PACKAGE)!!
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = HelpAdapter(context) {
            ForegroundPackageSettings.put(packageName, it)
            (activity as? Callback)?.onChangeSettings(position)
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
        private val onClickListener: (orientation: Int) -> Unit
    ) : Adapter<ViewHolder>() {
        private val layoutInflater = LayoutInflater.from(context)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutOrientationItemBinding.inflate(layoutInflater, parent, false))

        override fun getItemCount(): Int =
            Orientation.values.size + 1

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position == Orientation.values.size) {
                holder.bind(null)
                holder.itemView.setOnClickListener {
                    onClickListener(Orientation.INVALID)
                }
            } else {
                val entity = Orientation.values[position]
                holder.bind(entity)
                holder.itemView.setOnClickListener {
                    onClickListener(entity.orientation)
                }
            }
        }
    }

    class ViewHolder(private val binding: LayoutOrientationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: Orientation.Entity?) {
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

        fun show(activity: FragmentActivity, position: Int, packageName: String) {
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            EachAppOrientationDialog().also { dialog ->
                dialog.arguments = Bundle().also {
                    it.putInt(KEY_POSITION, position)
                    it.putString(KEY_PACKAGE, packageName)
                }
            }.show(manager, TAG)
        }
    }
}
