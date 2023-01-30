/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget.config

import android.content.ClipData
import android.os.Handler
import android.os.Looper
import android.view.DragEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.View.OnDragListener
import androidx.recyclerview.widget.RecyclerView
import net.mm2d.orientation.control.Functions

class InvisibleDragHelper : DragHelper, OnDragListener {
    private val handler = Handler(Looper.getMainLooper())
    private var recyclerView: RecyclerView? = null
    private var submitting = false
    private var dragData: Functions.Entity? = null
    private var dragState: State = State.NONE
    private var dragStartPosition: Int = -1
    private lateinit var adapter: DragItemAdapter

    override fun setAdapter(adapter: DragItemAdapter) {
        this.adapter = adapter
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.setOnDragListener(this)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
        recyclerView.setOnDragListener(null)
    }

    override fun itemAlpha(data: Functions.Entity): Float =
        if (data.function == dragData?.function) if (dragState == State.EXIT) 0f else 0.5f else 1f

    override fun dragStart(holder: RecyclerView.ViewHolder, data: Functions.Entity) {
        dragData = data
        dragState = State.MOVING
        dragStartPosition = holder.adapterPosition
        val view = holder.itemView
        view.startDragAndDrop(EMPTY_CLIP_DATA, DragShadowBuilder(view), data, 0)
        adapter.notifyItemChanged(dragStartPosition)
    }

    override fun onDrag(v: View, event: DragEvent): Boolean {
        val data = event.localState as? Functions.Entity ?: return false
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> Unit
            DragEvent.ACTION_DRAG_ENTERED -> dragEnter(data)
            DragEvent.ACTION_DRAG_LOCATION -> Unit
            DragEvent.ACTION_DRAG_EXITED -> dragExit(data)
            DragEvent.ACTION_DROP -> drop(data)
            DragEvent.ACTION_DRAG_ENDED -> dragEnd(data, event.result)
            else -> Unit
        }
        return true
    }

    private fun dragEnter(data: Functions.Entity) {
        if (dragState == State.NONE) {
            dragState = State.ENTER
            dragData = data
            val index = adapter.currentList.indexOfFirst { it.function.index > data.function.index }
            if (index < 0) {
                submitList { it.add(data) }
            } else {
                submitList { it.add(index, data) }
            }
            return
        }
        if (dragState == State.EXIT) {
            dragState = State.MOVING
            adapter.notifyItemChanged(adapter.currentList.indexOf(data))
        }
    }

    private fun dragExit(data: Functions.Entity) {
        val position = adapter.currentList.indexOf(data)
        if (dragState == State.MOVING) {
            dragState = State.EXIT
            if (position < 0) {
                submitList { it.add(dragStartPosition, data) }
                return
            }
            adapter.notifyItemChanged(position)
            swap(position, dragStartPosition)
            return
        }
        dragState = State.NONE
        dragData = null
        if (position < 0) return
        submitList { it.removeAt(position) }
    }

    private fun drop(data: Functions.Entity) {
        dragState = State.NONE
        dragData = null
        val position = adapter.currentList.indexOf(data)
        if (position < 0) {
            submitList { it.add(data) }
        } else {
            adapter.notifyItemChanged(position)
        }
    }

    private fun dragEnd(data: Functions.Entity, succeed: Boolean) {
        val position = adapter.currentList.indexOf(data)
        if (position < 0) return
        if (succeed && dragState == State.EXIT) {
            submitList { it.removeAt(position) }
        } else {
            adapter.notifyItemChanged(position)
        }
        dragState = State.NONE
        dragData = null
        dragStartPosition = -1
    }

    private fun submitList(list: List<Functions.Entity>?) {
        submitting = true
        adapter.submitList(list) {
            handler.post { submitting = false }
        }
    }

    private fun submitList(block: (MutableList<Functions.Entity>) -> Unit) {
        adapter.currentList.toMutableList().also(block).let { submitList(it) }
    }

    private fun swap(from: Int, to: Int) {
        if (from == to) return
        submitList { it.add(to, it.removeAt(from)) }
    }

    private enum class State {
        NONE,
        MOVING,
        EXIT,
        ENTER,
    }

    companion object {
        private val EMPTY_CLIP_DATA = ClipData.newPlainText("", "")
    }
}
