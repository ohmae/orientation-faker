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
import kotlinx.android.synthetic.main.layout_orientation_item.view.*
import net.mm2d.android.orientationfaker.R
import net.mm2d.orientation.control.ForegroundPackageSettings
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.util.isResumed

class EachAppOrientationDialog : DialogFragment() {
    interface Callback {
        fun onChangeSettings(position: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context!!
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
            ViewHolder(layoutInflater.inflate(R.layout.layout_orientation_item, parent, false))

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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(entity: Orientation.Entity?) {
            if (entity != null) {
                itemView.icon.setImageResource(entity.icon)
                itemView.name.setText(entity.label)
                itemView.description.setText(entity.description)
            } else {
                itemView.icon.setImageResource(R.drawable.ic_remove)
                itemView.name.setText(R.string.clear)
                itemView.description.setText(R.string.description_clear)
            }
        }
    }

    companion object {
        private const val TAG = "EachAppOrientationDialog"
        private const val KEY_POSITION = "KEY_POSITION"
        private const val KEY_PACKAGE = "KEY_PACKAGE"

        fun show(activity: FragmentActivity, position: Int, packageName: String) {
            if (activity.isFinishing || !activity.isResumed()) {
                return
            }
            val arguments = Bundle().also {
                it.putInt(KEY_POSITION, position)
                it.putString(KEY_PACKAGE, packageName)
            }
            activity.supportFragmentManager.also {
                if (it.findFragmentByTag(TAG) == null) {
                    EachAppOrientationDialog().also { dialog ->
                        dialog.arguments = arguments
                    }.show(it, TAG)
                }
            }
        }
    }
}
