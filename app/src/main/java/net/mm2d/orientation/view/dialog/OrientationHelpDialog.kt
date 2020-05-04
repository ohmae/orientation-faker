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
import net.mm2d.orientation.control.Orientation
import net.mm2d.orientation.util.isActive

class OrientationHelpDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = HelpAdapter(context)
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        recyclerView.isVerticalFadingEdgeEnabled = true
        return AlertDialog.Builder(context)
            .setView(recyclerView)
            .setPositiveButton(R.string.ok, null)
            .create()
    }

    class HelpAdapter(context: Context) : Adapter<ViewHolder>() {
        private val layoutInflater = LayoutInflater.from(context)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(layoutInflater.inflate(R.layout.layout_orientation_item, parent, false))

        override fun getItemCount(): Int =
            Orientation.values.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(Orientation.values[position])
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(entity: Orientation.Entity) {
            itemView.icon.setImageResource(entity.icon)
            itemView.name.setText(entity.label)
            itemView.description.setText(entity.description)
        }
    }

    companion object {
        private const val TAG = "OrientationHelpDialog"

        fun show(activity: FragmentActivity) {
            if (!activity.isActive()) {
                return
            }
            activity.supportFragmentManager.also {
                if (it.findFragmentByTag(TAG) == null) {
                    OrientationHelpDialog().show(it, TAG)
                }
            }
        }
    }
}
