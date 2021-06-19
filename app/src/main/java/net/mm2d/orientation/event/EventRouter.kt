/*
 * Copyright (c) 2019 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.orientation.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

object EventRouter {
    private val updateFlow: MutableSharedFlow<Unit> = MutableSharedFlow(1)

    fun notifyUpdate() {
        GlobalScope.launch {
            updateFlow.emit(Unit)
        }
    }

    fun observeUpdate(owner: LifecycleOwner, callback: () -> Unit) {
        owner.lifecycleScope.launch {
            updateFlow.collect { callback() }
        }
    }
}
