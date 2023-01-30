/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.view.widget.config

import androidx.recyclerview.widget.RecyclerView
import net.mm2d.orientation.control.Functions

interface DragHelper {
    fun setAdapter(adapter: DragItemAdapter)
    fun onAttachedToRecyclerView(recyclerView: RecyclerView)
    fun onDetachedFromRecyclerView(recyclerView: RecyclerView)
    fun itemAlpha(data: Functions.Entity): Float
    fun dragStart(holder: RecyclerView.ViewHolder, data: Functions.Entity)
}
